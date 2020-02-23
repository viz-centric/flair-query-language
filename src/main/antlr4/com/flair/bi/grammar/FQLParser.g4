parser grammar FQLParser;

options { tokenVocab=FQLLexer; }


parse:
    ( statement_list | error) * EOF;

error
 : UNEXPECTED_CHAR
   {
     throw new RuntimeException("UNEXPECTED_CHAR=" + $UNEXPECTED_CHAR.text);
   }
 ;
statement_list:
     ';'* statement ( ';'+ statement )* ';'*
     ;

statement:
     simple_select_stmt
 |   select_stmt
 |   describe_stmt
 ;

simple_select_stmt
 : with_rec_table_expr?
   select_core order_expr?
   limit_expr?
 ;

describe_stmt_like
 : K_LIKE expr
 ;

describe_stmt_limit
 : K_LIMIT expr
 ;

describe_stmt
 : K_SHOW K_TABLES describe_stmt_like? describe_stmt_limit?
 ;

select_stmt
 : with_rec_table_expr?
   select_core ( compound_operator select_core )*
   order_expr?
   limit_expr?
 ;

//select_or_values
// : K_SELECT ( K_DISTINCT | K_ALL )? result_column ( ',' result_column )*
//   ( K_FROM ( table_or_subquery ( ',' table_or_subquery )* | join_clause ) )?
//   ( K_WHERE expr )?
//    group_expr?
// |  values_expr
// ;

select_core
  : K_SELECT ( K_DISTINCT | K_ALL )? result_column ( ',' result_column )*
    ( K_FROM ( table_or_subquery ( ',' table_or_subquery )* | join_clause ) )?
    ( K_WHERE expr )?
    ( K_GROUP K_BY comma_sep_expr (K_HAVING expr)? )?
  | values_expr
  ;

values_expr:
    K_VALUES  value_expr( ',' value_expr )*
    ;

value_expr:
       '(' comma_sep_expr ')'
       ;

//group_expr:
// K_GROUP K_BY comma_sep_expr having_expr?
// ;

//having_expr:
//     K_HAVING expr;
limit_expr:
     K_LIMIT expr ( ( K_OFFSET | ',' ) expr )?
    ;
order_expr:
     K_ORDER K_BY ordering_term ( ',' ordering_term )*
    ;
with_rec_table_expr:
     K_WITH K_RECURSIVE? common_table_expression ( ',' common_table_expression )*
    ;
common_table_expression
 : table_name ( '(' column_name ( ',' column_name )* ')' )? K_AS '(' select_stmt ')'
 ;

ordering_term
  : expr ( K_COLLATE collation_name )? ( K_ASC | K_DESC )?
  ;
result_column
 : '*'
 | table_name '.' '*'
 | expr ( K_AS? column_alias )?
 ;

table_or_subquery
 : ( database_name '.' )? table_name ( K_AS? table_alias )?
//   ( K_INDEXED K_BY index_name
//   | K_NOT K_INDEXED )?
 | '(' ( table_or_subquery ( ',' table_or_subquery )*
       | join_clause )
   ')' ( K_AS? table_alias )?
 | '(' select_stmt ')' ( K_AS? table_alias )?
 ;

join_clause
 : table_or_subquery ( join_operator table_or_subquery join_constraint )*
 ;

compound_operator
 : K_UNION
 | K_UNION K_ALL
 | K_INTERSECT
 | K_EXCEPT
 ;

literal:
 NUMERIC_LITERAL
 | STRING_LITERAL
 | BLOB_LITERAL
 | K_NULL
 | K_CURRENT_TIME
 | K_CURRENT_DATE
 | K_CURRENT_TIMESTAMP;

join_constraint
 : ( K_ON expr
   | K_USING '(' column_name ( ',' column_name )* ')' )?
 ;

join_operator
 : ','
 | K_NATURAL? ( K_LEFT K_OUTER? | K_INNER | K_CROSS )? K_JOIN
 ;

any_name:
  IDENTIFIER
 | keyword
 | STRING_LITERAL
 | '(' any_name ')'
 ;

keyword:
   K_NULL
   | K_CURRENT_DATE
   | K_CURRENT_TIME
   | K_CURRENT_TIMESTAMP;

expr
 : literal
 | BIND_PARAMETER
 | unary_operator expr
 | expr binary_operator expr
 | db_tbl_col_expr
// | expr ( '*' | '/' | '%' ) expr
// | expr ( '+' | '-' ) expr
// | expr ( '<<' | '>>' | '&' | '|' ) expr
// | expr ( '<' | '<=' | '>' | '>=' ) expr
// | expr ( '=' | '==' | '!=' | '<>' | K_IS | K_IS K_NOT | K_IN | K_LIKE | K_GLOB | K_MATCH | K_REGEXP ) expr
// | expr K_AND expr
// | expr K_OR expr
 | func_call_expr
 | expr_in_brackets
// | K_CAST '(' expr K_AS type_name ')'
// | expr K_COLLATE collation_name
 | expr K_NOT? str_match_expr expr ( K_ESCAPE expr )?
 | expr null_check_op
// | expr K_IS K_NOT? expr
 | expr K_NOT? K_BETWEEN expr K_AND expr
 | expr K_NOT? K_IN ( '(' ( select_stmt
                            | comma_sep_expr
                            )?
                        ')'
                      | ( database_name '.' )? table_name )
 | ( ( K_NOT )? K_EXISTS )? '(' select_stmt ')'
// | K_CASE expr? ( K_WHEN expr K_THEN expr )+ ( K_ELSE expr )? K_END
// | raise_function
 ;

expr_in_brackets:
   '(' expr ')'
   ;

comma_sep_expr:
    expr ( ',' expr )*;
str_match_expr:
    K_LIKE | K_GLOB | K_REGEXP | K_MATCH;

null_check_op:
    K_ISNULL | K_NOTNULL | K_NOT K_NULL;
db_tbl_col_expr:
    ( ( database_name '.' )? table_name '.' )? column_name;

func_call_expr:
    function_name '(' ( K_DISTINCT? comma_sep_expr | '*' )? ')';


//type_name
// : name+ ( '(' signed_number ')'
//         | '(' signed_number ',' signed_number ')' )?
// ;
unary_operator
  : '-'
  | '+'
  | '~'
  | K_NOT
  ;

binary_operator
 : '||'
 | '*'
 | '/'
 | '%'
 | '+'
 | '-'
 | '<'
 | '<='
 | '>'
 | '>='
 | '='
 | '=='
 | '!='
 | '<>'
 | K_IS
 | K_IS K_NOT
 | K_IN
 | K_LIKE
 | K_GLOB
 | K_MATCH
 | K_REGEXP
 | K_AND
 | K_OR
 ;

//signed_number
// : ( '+' | '-' )? NUMERIC_LITERAL
// ;

column_alias
 : IDENTIFIER
 | STRING_LITERAL
 ;

name
 : any_name
 ;
table_alias
 : any_name
 ;
index_name
 : any_name
 ;
function_name
 : any_name
 ;
table_name
 : any_name
 ;
database_name
 : any_name
 ;
column_name
  : any_name
  ;
collation_name
 : any_name
 ;
