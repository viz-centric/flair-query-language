lexer grammar FQLLexer;

SCOL : ';';
DOT : '.';
OPEN_PAR : '(';
CLOSE_PAR : ')';
COMMA : ',';
ASSIGN : '=';
STAR : '*';
PLUS : '+';
MINUS : '-';
TILDE : '~';
PIPE2 : '||';
DIV : '/';
MOD : '%';
LT2 : '<<';
GT2 : '>>';
AMP : '&';
PIPE : '|';
LT : '<';
LT_EQ : '<=';
GT : '>';
GT_EQ : '>=';
EQ : '==';
NOT_EQ1 : '!=';
NOT_EQ2 : '<>';


K_SHOW : S H O W;
K_TABLES: T A B L E S;
K_SELECT : S E L E C T;
K_FROM : F R O M;
K_WHERE: W H E R E;
K_GROUP: G R O U P;
K_AND: A N D;
K_OR: O R;
K_WITH: W I T H;
K_RECURSIVE : R E C U R S I V E;
K_ORDER : O R D E R;
K_BY : B Y;
K_LIMIT: L I M I T;
K_OFFSET: O F F S E T;
K_DISTINCT: D I S T I N C T;
K_ALL: A L L;
K_HAVING: H A V I N G;
K_VALUES: V A L U E S;
K_AS: A S;
K_UNION: U N I O N;
K_INTERSECT: I N T E R S E C T;
K_EXCEPT: E X C E P T;
K_NATURAL : N A T U R A L;
K_IS: I S;
K_IN: I N;
K_LIKE: L I K E;
K_GLOB: G L O B;
K_MATCH: M A T C H;
K_REGEXP: R E G E X P;
K_CAST: C A S T;
K_ISNULL: I S N U L L;
K_ESCAPE : E S C A P E;
K_BETWEEN: B E T W E E N;
K_NOTNULL: N O T N U L L;
K_NOT: N O T;
K_EXISTS: E X I S T S;
K_CASE: C A S E;
K_WHEN: W H E N;
K_THEN: T H E N;
K_ELSE: E L S E;
K_END: E N D;
K_ON: O N;
K_USING: U S I N G;
K_LEFT: L E F T;
K_OUTER: O U T E R;
K_INNER: I N N E R;
K_CROSS: C R O S S;
K_JOIN: J O I N;
K_COLLATE: C O L L A T E;
K_ASC: A S C;
K_DESC: D E S C;


IDENTIFIER
 : '"' (~'"' | '""')* '"'
 | '`' (~'`' | '``')* '`'
 | '[' ~']'* ']'
 | [a-zA-Z_] [a-zA-Z_0-9]* // TODO check: needs more chars in set
 ;

NUMERIC_LITERAL
 : DIGIT+ ( '.' DIGIT* )? ( E [-+]? DIGIT+ )?
 | '.' DIGIT+ ( E [-+]? DIGIT+ )?
 ;

STRING_LITERAL
 : '\'' ( ~'\'' | '\'\'' )* '\''
 ;

BLOB_LITERAL:
X STRING_LITERAL;

BIND_PARAMETER
 : '?' DIGIT*
 | [:@$] IDENTIFIER
 ;


SINGLE_LINE_COMMENT
 : '--' ~[\r\n]* -> channel(HIDDEN)
 ;

MULTILINE_COMMENT
 : '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN)
 ;

SPACES
 : [ \u000B\t\r\n] -> channel(HIDDEN)
 ;

 UNEXPECTED_CHAR : .;

K_NULL : N U L L;
K_COUNT : C O U N T;
K_DISTINCT_COUNT : D I S T I N C T '_' C O U N T;
K_MAX : M A X;
K_MIN : M I N;
K_CURRENT_DATE : C U R R E N T '_' D A T E;
K_CURRENT_TIME : C U R R E N T '_' T I M E;
K_CURRENT_TIMESTAMP : C U R R E N T '_' T I M E S T A M P;

fragment DIGIT : [0-9];

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];