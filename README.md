# graphql-over-sql

This is a GraphQL Java implementation based on the [specification](https://github.com/facebook/graphql), 
reference implementation port [graphql-java](https://github.com/graphql-java/graphql-java) and 
[sqlbuilder](http://openhms.sourceforge.net/sqlbuilder/)

[![Build Status](https://travis-ci.org/zvorygin/graphql-over-sql.svg?branch=master)][1]
[![Coverage](https://sonarqube.com/api/badges/measure?key=com.graphql-over-sql:parent&metric=coverage)][2]
[![Technical debt ratio](https://sonarqube.com/api/badges/measure?key=com.graphql-over-sql:parent&metric=sqale_debt_ratio)][3]

Implementation of [GraphQL](http://graphql.org/) over relational database.

Aim of this project is to transparently convert query from GraphQL to SQL, execute it, and provide response in JSON format. 

[1]:https://travis-ci.org/zvorygin/graphql-over-sql
[2]:https://sonarqube.com/component_measures/domain/Coverage?id=com.graphql-over-sql\:parent
[3]:https://sonarqube.com/component_issues?id=com.graphql-over-sql\:parent