grammar GraphqlSchema ;

schemaDocument
	: definition* schema definition* EOF;

definition
	: scalarDefinition
	| interfaceDefinition
	| typeDefinition
	| inputDefinition
	;

scalarDefinition : 'scalar' TYPE_NAME ;

interfaceDefinition : 'interface' TYPE_NAME LCURLY fieldDefinition+ RCURLY ;

typeDefinition : 'type' TYPE_NAME LCURLY fieldDefinition+ RCURLY ;

inputDefinition : 'input' TYPE_NAME LCURLY fieldDefinition+ RCURLY ;

query : 'query' COLON TYPE_NAME ;

mutation : 'mutation' COLON TYPE_NAME ;

fieldDefinition : FIELD_NAME fieldArguments? ':' fieldType ;

schema : SCHEMA LCURLY query? mutation? RCURLY ;

fieldArguments: LPAREN fieldArgument (',' fieldArgument)* RPAREN ;

fieldArgument: FIELD_NAME COLON TYPE_NAME defaultValue? ;

defaultValue: EQ value ;

value
	: INT_VALUE
	| STRING_VALUE
	| FLOAT_VALUE
	| BOOLEAN_VALUE
	;

fieldType
	: TYPE_NAME                   #fieldTypeNameReference
	| LBRACKET fieldType RBRACKET #fieldTypeList
	| fieldType EXCLM             #fieldTypeNotNull
	;

LCURLY : '{' ;
RCURLY : '}' ;
EQ: '=' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACKET : '[' ;
RBRACKET : ']' ;
COLON : ':' ;
EXCLM : '!';
SCHEMA : 'schema';

TYPE_NAME : [A-Z][0-9A-Za-z]* ;

FIELD_NAME : [a-z][0-9A-Za-z]* ;

STRING_VALUE : '"' ~["]* '"' ;

FLOAT_VALUE : '-'? INT '.' [0-9]+ EXP? | '-'? INT EXP | '-'? INT;

INT_VALUE : INT ;

fragment INT : '0' | ('-' ? [1-9] [0-9]*);

fragment EXP : [Ee] [+\-]? INT ;

BOOLEAN_VALUE : 'true' | 'false' ;

IGNORED
	: (WHITESPACE|LINE_TERMINATOR|COMMENT) -> skip ;

fragment COMMENT
	: '#' ~[\n\r\u2028\u2029]* ;

fragment LINE_TERMINATOR
	: [\n\r\u2028\u2029] ;

fragment WHITESPACE
	: [\t\u000b\f\u0020\u00a0] ;

