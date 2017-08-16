package graphql.sql.engine.sql;

import com.google.common.collect.Sets;
import com.healthmarketscience.sqlbuilder.InCondition;
import com.healthmarketscience.sqlbuilder.QueryPreparer;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import graphql.execution.ExecutionContext;
import graphql.language.Argument;
import graphql.sql.core.HsqldbArrayPlaceholder;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.QueryLink;
import graphql.sql.core.config.TypeExecutor;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.domain.ScalarType;
import graphql.sql.engine.sql.extractor.ArrayKey;
import graphql.sql.engine.sql.extractor.FragmentExtractor;
import graphql.sql.engine.sql.extractor.NodeExtractor;
import graphql.sql.engine.sql.extractor.ResultNode;
import graphql.sql.engine.sql.extractor.ScalarExtractor;
import graphql.sql.engine.sql.sqlquery.JoinWithSqlQueryNode;
import graphql.sql.engine.sql.sqlquery.SqlQueryNode;
import graphql.sql.engine.sql.sqlquery.SqlQueryRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SqlTypeExecutor implements TypeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlTypeExecutor.class);

    @Nonnull
    private final String query;

    @Nonnull
    private final NodeExtractor nodeExtractor;

    @Nonnull
    private final Collection<PlaceHolder> placeHolders;

    @Nonnull
    private final DataSource dataSource;

    public SqlTypeExecutor(@Nonnull TableNode inputQueryNode,
                           @Nonnull ExecutionContext executionContext,
                           @Nonnull DataSource dataSource) {
        this.dataSource = dataSource;

        nodeExtractor = new NodeExtractor();
        placeHolders = new ArrayList<>();
        HashMap<TableNode, RejoinTable> tableCache = new HashMap<>();
        SqlQueryRoot queryRoot = new SqlQueryRoot(getRejoinTable(inputQueryNode, tableCache));

        List<DbConstraint> constraints = inputQueryNode.getType().getDbTable().getConstraints();

        for (DbConstraint constraint : constraints) {
            if (constraint.getType() == Constraint.Type.UNIQUE || constraint.getType() == Constraint.Type.PRIMARY_KEY) {
                for (DbColumn dbColumn : constraint.getColumns()) {
                    int position = queryRoot.addColumn(queryRoot.getTable().findColumn(dbColumn));
                    nodeExtractor.addKeyExtractor(new ScalarExtractor<>(position, dbColumn.getColumnNameSQL(), ScalarType.getByColumn(dbColumn).getTypeUtil()));
                }

                break;
            }
        }

        QueryPreparer preparer = new QueryPreparer();

        Set<TableNode> processed = new HashSet<>();

        init(inputQueryNode, queryRoot, queryRoot, nodeExtractor, placeHolders, tableCache, preparer, processed);
        SelectQuery selectQuery = queryRoot.buildSelectQuery();
        query = selectQuery.toString();
        LOGGER.info("Created query {}", query);
    }

    private static void init(TableNode tableNode,
                             SqlQueryRoot root,
                             SqlQueryNode current,
                             FragmentExtractor nodeExtractor,
                             Collection<PlaceHolder> placeHolders,
                             HashMap<TableNode, RejoinTable> tableCache,
                             QueryPreparer preparer,
                             Set<TableNode> processed) {

        if (processed.contains(tableNode)) {
            return;
        }
        processed.add(tableNode);

        List<DbConstraint> constraints = tableNode.getType().getDbTable().getConstraints();

        for (QueryLink link : tableNode.getParents()) {
            QueryNode parentNode = link.getTarget();
            if (parentNode instanceof TableNode) {
                Sets.SetView<String> commonFields =
                        Sets.intersection(tableNode.getType().getFields().keySet(), parentNode.getType().getFields().keySet());
                RejoinTable parentTable = getRejoinTable((TableNode) parentNode, tableCache);
                List<RejoinTable.RejoinColumn> fromColumns = getRejoinColumns(tableNode.getType().getFields(), commonFields, current.getTable());
                List<RejoinTable.RejoinColumn> toColumns = getRejoinColumns(parentNode.getType().getFields(), commonFields, parentTable);
                SqlQueryNode with = new SqlQueryNode(parentTable);
                current.addParent(new JoinWithSqlQueryNode(with, fromColumns, toColumns));
                init((TableNode) parentNode, root, with, nodeExtractor, placeHolders, tableCache, preparer, processed);
            } else {
                throw new IllegalStateException("Not implemented yet");
            }
        }

        for (QueryLink link : tableNode.getChildren()) {
            QueryNode childNode = link.getTarget();
            if (childNode instanceof TableNode) {
                Sets.SetView<String> commonFields =
                        Sets.intersection(tableNode.getType().getFields().keySet(), childNode.getType().getFields().keySet());
                RejoinTable childTable = getRejoinTable((TableNode) childNode, tableCache);
                List<RejoinTable.RejoinColumn> fromColumns = getRejoinColumns(tableNode.getType().getFields(), commonFields, current.getTable());
                List<RejoinTable.RejoinColumn> toColumns = getRejoinColumns(childNode.getType().getFields(), commonFields, childTable);
                SqlQueryNode with = new SqlQueryNode(childTable);
                current.addChild(new JoinWithSqlQueryNode(with, fromColumns, toColumns));
                int[] keyPositions = null;

                for (DbConstraint constraint : ((TableNode) childNode).getType().getDbTable().getConstraints()) {
                    if (constraint.getType() == Constraint.Type.PRIMARY_KEY) {
                        keyPositions = new int[constraint.getColumns().size()];
                        List<DbColumn> columns = constraint.getColumns();
                        for (int i = 0; i < columns.size(); i++) {
                            RejoinTable.RejoinColumn column = with.getTable().findColumn(columns.get(i));
                            keyPositions[i] = nodeExtractor.addKeyExtractor(new ScalarExtractor<>(root.addColumn(column),
                                    column.getColumnNameSQL(), ScalarType.getByColumn(columns.get(i)).getTypeUtil()));
                        }

                        break;
                    }
                }

                if (keyPositions == null) {
                    throw new IllegalStateException("Unique constraint not found on table " + ((TableNode) childNode).getType().getDbTable().getTableNameSQL());
                }
                FragmentExtractor fragmentExtractor = new FragmentExtractor(nodeExtractor, keyPositions);
                nodeExtractor.addFragment(fragmentExtractor);
                init((TableNode) childNode, root, with, fragmentExtractor, placeHolders, tableCache, preparer, processed);
            } else {
                throw new IllegalStateException("Not implemented yet");
            }
        }

        for (Map.Entry<String, Field> fieldEntry : tableNode.getFieldsToQuery().entrySet()) {
            Field field = fieldEntry.getValue();
            if (field instanceof SqlField) {
                DbColumn dbColumn = ((SqlField) field).getDbColumn();
                int position = root.addColumn(current.getTable().findColumn(dbColumn));
                nodeExtractor.addScalarField(fieldEntry.getKey(),
                        new ScalarExtractor<>(position, dbColumn.getColumnNameSQL(), ScalarType.getByColumn(dbColumn).getTypeUtil()));
            } else if (field instanceof CompositeSqlField) {
                throw new IllegalStateException("Shouldn't happen!");
                //CompositeSqlField compositeSqlField = (CompositeSqlField) field;
/*
                root.addNestedNode(new JoinWithSqlQueryNode(new SqlQueryNode(getRejoinTable(null, tableCache)), ))
                nodeExtractor.addCompositeFieldExtractor(compositeSqlField.getName(), new NodeExtractor());
*/
            }
        }

        for (Argument argument : tableNode.getArguments()) {
            AbstractTableCompositeType compositeType = tableNode.getType();

            Field field = compositeType.getField(argument.getName());

            if (!(field instanceof SqlField)) {
                throw new IllegalStateException("Shouldn't happen");
            }

            SqlField sqlField = (SqlField) field;

            //tableNode.get
            RejoinTable.RejoinColumn column = current.getTable().findColumn(sqlField.getDbColumn());
            HsqldbArrayPlaceholder placeHolder = new HsqldbArrayPlaceholder(preparer, ScalarType.getByColumn(sqlField.getDbColumn()), column.getTypeNameSQL(), argument);
            placeHolders.add(placeHolder);
            root.addCondition(new InCondition(column, placeHolder));
        }
    }

    private static List<RejoinTable.RejoinColumn> getRejoinColumns(Map<String, Field> allFields,
                                                                   Sets.SetView<String> commonFields,
                                                                   RejoinTable targetTable) {
        return allFields.entrySet().stream()
                .filter(e -> commonFields.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .filter(SqlField.class::isInstance)
                .map(SqlField.class::cast)
                .map(c -> targetTable.findColumn(c.getDbColumn()))
                .collect(Collectors.toList());
    }

    private static RejoinTable getRejoinTable(TableNode node, HashMap<TableNode, RejoinTable> tableCache) {
        return tableCache.computeIfAbsent(node, n -> {
            DbTable table = n.getType().getDbTable();
            return table.rejoin(String.valueOf(Character.toLowerCase(table.getName().charAt(0))));
        });
    }

    @Nonnull
    public String getQuery() {
        return query;
    }

    @Nonnull
    public NodeExtractor getNodeExtractor() {
        return nodeExtractor;
    }

    @Nonnull
    public Collection<PlaceHolder> getPlaceHolders() {
        return placeHolders;
    }

    ResultSet setParametersAndExecute(PreparedStatement ps, Map<String, Object> variables)
            throws SQLException {

        for (PlaceHolder placeHolder : placeHolders) {
            placeHolder.setValue(ps, variables);
        }

        return ps.executeQuery();
    }

    @Override
    @Nonnull
    public Object execute(Map<String, Object> variables) {
        Map<ArrayKey, ResultNode> response = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(getQuery());
             ResultSet rs = setParametersAndExecute(ps, variables)) {
            NodeExtractor extractor = getNodeExtractor();
            while (rs.next()) {
                ArrayKey key = extractor.getKey(rs);
                extractor.extractTo(rs, response, key);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return response.values();
    }
}
