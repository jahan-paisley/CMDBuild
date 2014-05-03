grammar CQL;

options {
	output = AST;
	backtrack = true;
	k = 3;
}

tokens {

	INPUTVAL;
	LITSTR;
	LITNUM;
	LITDATE; //YY;MM;DD;
	LITTIMESTAMP; //HH;MIN;SS;
	LITBOOL; TRUE;FALSE;
	NATIVE;
	
	ATTRIBUTEID;
//	COMPLEX;
	LOOKUP;
	LOOKUPPARENT;
	
	NOT;
	BTW; IN; NOTBTW; NOTIN;
	LTEQ; GTEQ; LT; GT;
	
	CONT; BGN; END; EQ; ISNULL;
	NOTCONT; NOTBGN; NOTEND; NOTEQ; ISNOTNULL;
	
	GROUP; NOTGROUP;
	
	FIELD; FIELDID; FIELDOPERATOR; FIELDVALUE;
	
	AND; OR;
	
	DOMTYPE; INVERSE; DEFAULT;
	DOM; DOMREV; NOTDOM; NOTDOMREV; DOMREF; NOTDOMREF;
	DOMID; DOMNAME; DOMVALUE; DOMCARDS;
	
	EXPR; FROM; WHERE;
	
	CLASSREF; CLASS; CLASSID; CLASSALIAS; ATTRIBUTES;
	DOMOBJS; DOMMETA;
	
	SELECT; ALL; CLASSDOMREF; ATTRIBUTE; ATTRIBUTENAME; ATTRIBUTEAS; FUNCTION;
	OFFSET; LIMIT; ORDERBY; GROUPBY;
	HISTORY;
	
	ORDERELM; ASC; DESC;
}

@header {package org.cmdbuild.cql;}
@lexer::header {package org.cmdbuild.cql;}

@members {
	//return true if every parameter is null
	boolean isnull(Object... objs){
		if(objs == null){return true;}
		for(Object o : objs){if(o!=null){return false;}}
		return true;
	}
}

/*
	[SELECT select+]
	FROM fromref+
	[WHERE fields]
	[GROUP BY name,name...]
	[ORDER BY name, name ASC, name DESC,...]
	[LIMIT number|{varname}]
	[OFFSET number|{varname}]
*/
expr:	LROUND?
		(SELECTOP select (COMMA select)*)? 
		FROMOP history=HISTORYOP? fromref (COMMA fromref)* 
		(WHEREOP fields)? 
		(GROUPBYOP groupby=selattrs)? 
		(ORDEROP order (COMMA order)*)?
		(LMTOP  litlmt=NUMBER|(LGRAPH inlmt=NAME RGRAPH))? 
		(OFFSOP litoff=NUMBER|(LGRAPH inoff=NAME RGRAPH))?
		RROUND?
		->	^(EXPR ^(HISTORY $history)? 
			^(FROM fromref+) 
			^(SELECT select+)? 
			^(WHERE fields)? 
			^(GROUPBY $groupby)?
			^(ORDERBY order+)?
			^(LIMIT ^(LITNUM $litlmt)? ^(INPUTVAL $inlmt)? )?
			^(OFFSET ^(LITNUM $litoff)? ^(INPUTVAL $inoff)? )?
			)
	;

/*
A, A ASC, B DESC, B.C ASC...
*/
order
	:	(cdomscope=NAME DOT)? attr=NAME (asc='ASC'|desc='DESC')?	->	^(ORDERELM $attr ^(ASC $asc)? ^(DESC $desc)? ^(CLASSDOMREF $cdomscope)?)
	;

/*
	*,
	A::meta( selattrs ) objects( selattrs )
	A::(selattrs)
*/
select
	:	ALLOP	-> ^(ALL)
	|	alias=NAME COLON COLON LROUND selattr (COMMA selattr)* RROUND 
		->	^(CLASSREF $alias ^(ATTRIBUTES selattr+)) 
	|	alias=NAME COLON COLON (META LROUND meta=selattrs RROUND)? (OBJECTS LROUND objs=selattrs RROUND)? 
		->	^(DOMREF $alias ^(DOMMETA $meta)? ^(DOMOBJS $objs)?)
	|	selattr
	;
selattrs
	:	selattr (COMMA selattr)* -> selattr+;

/*
	function( selattrs ) [@alias],
	[scope.] name [@alias]
*/
selattr
	:	fname=NAME LROUND selattrs RROUND (AT as=NAME)?	->	^(FUNCTION $fname ^(ATTRIBUTES selattrs) ^(ATTRIBUTEAS $as)?)
	|	(cname=NAME DOT)? NAME (AT as=NAME)?			->	^(ATTRIBUTE ^(ATTRIBUTENAME NAME) ^(ATTRIBUTEAS $as)? ^(CLASSDOMREF $cname)?)
	;

/*
	(class/domain name/id) [@alias]
*/
fromref
	:	(name=NAME|NUMBER) (AT alias=NAME)? 	-> ^(CLASSREF ^(CLASS $name)? ^(CLASSID NUMBER)? ^(CLASSALIAS $alias)?)
	|	domaindecl
	;
/*
	[class scope] [.domain/.domainrev [ ~[) domain name/id () ]) [@alias] [subdomain]
*/
domaindecl
	:	cscope=NAME?
		((DOT? (DOMOP|rev=DOMREVOP) LROUND)| t=TILDE? LSQUARE) (NUMBER|domname=NAME) (RROUND|RSQUARE) 
		(AT domref=NAME)? (domaindecl)?
		->	{isnull($rev,$t)}?	^(DOM ^(CLASSDOMREF $cscope)? ^(DOMTYPE DEFAULT) 
								^(DOMNAME $domname)? ^(DOMID NUMBER)? 
								^(DOMREF $domref)? ^(DOMCARDS domaindecl)?)
							->	^(DOM ^(CLASSDOMREF $cscope)? ^(DOMTYPE INVERSE) 
								^(DOMNAME $domname)? ^(DOMID NUMBER)? 
								^(DOMREF $domref)? ^(DOMCARDS domaindecl)?)
	;

fields 
	: fieldgrpdom ((and|or)*)
	;

and	:
	ANDOP fieldgrpdom -> ^(AND fieldgrpdom);
or	:
	OROP fieldgrpdom -> ^(OR fieldgrpdom);

fragment fieldgrpdom
	:	field
	|	group
	|	domain
	;

/*
	[!] ( fields )
*/
group
	:	n=NEG? LROUND fields RROUND 	
		-> {$n!=null}? 	^(NOTGROUP fields)
		->				^(GROUP fields)
	;

/*
	referenced domain in "from":
	[!] domainrefname [.meta( fields )] [.objects( fields )]
	
	newly specified domain:
	[!] [class scope] (.domain .domainref [ ~[) domnain name/id () ]) [.meta( fields )] [.objects( fields )] <subdomain>
*/
domain
	:	n=NEG? 
		cscope=NAME? 
		((DOT? (DOMOP|rev=DOMREVOP) LROUND)| t=TILDE? LSQUARE) (NAME|NUMBER) (RROUND|RSQUARE) 
		(DOT META LROUND meta=fields RROUND)? ((DOT OBJECTS LROUND cards=fields)|subdom=domain)?
		->	{isnull($rev,$t)}?
			^(DOM 
			^(CLASSDOMREF $cscope)? 
			^(DOMTYPE DEFAULT ^(NOT $n)?) 
			^(DOMNAME NAME)? ^(DOMID NUMBER)? ^(DOMVALUE $meta)? ^(DOMCARDS $cards)? ^(DOMCARDS $subdom)?)
		->	^(DOM 
			^(CLASSDOMREF $cscope)? 
			^(DOMTYPE INVERSE ^(NOT $n)?) 
			^(DOMNAME NAME)? ^(DOMID NUMBER)? ^(DOMVALUE $meta)? ^(DOMCARDS $cards)? ^(DOMCARDS $subdom)?)
		
	|	n=NEG? NAME (DOT META LROUND meta=fields RROUND)? (DOT OBJECTS LROUND cards=fields RROUND)?
		->	^(DOMREF 
			^(DOMTYPE ^(NOT $n)?) 
			^(DOMNAME NAME) ^(DOMVALUE $meta)? ^(DOMCARDS $cards)?)
	;

/*
	(id) [!]=,<,>,<=,>=,... val,
	(id) [!]BETWEEN ( val:val ) | (id)[!](val:val),
	(id) [!]IN ( val,val,... )  | (id)[!](val,val,...)
*/
field
	:	id operator val?	-> ^(FIELD ^(FIELDID id) ^(FIELDOPERATOR operator) ^(FIELDVALUE val)?)
	|	id n=NEG? (BTWOP|LROUND) val (BTWANDOP|COLON) val (RROUND)?
							-> {$n!=null}?	^(FIELD ^(FIELDID id) ^(FIELDOPERATOR NOTBTW) ^(FIELDVALUE val val))
							->				^(FIELD ^(FIELDID id) ^(FIELDOPERATOR BTW) ^(FIELDVALUE val val))
	|	id n=NEG? INOP? LROUND val (COMMA val)* RROUND
							-> {$n!=null}?	^(FIELD ^(FIELDID id) ^(FIELDOPERATOR NOTIN) ^(FIELDVALUE val+))
							->				^(FIELD ^(FIELDID id) ^(FIELDOPERATOR IN) ^(FIELDVALUE val+))
	;

operator
	:	LTEQOP				-> ^(LTEQ)
	|	GTEQOP				-> ^(GTEQ)
	|	LTOP				-> ^(LT)
	|	GTOP				-> ^(GT)
	|	n=NEG? CONTOP		-> {$n!=null}? ^(NOTCONT)		-> ^(CONT)
	|	n=NEG? BGNOP		-> {$n!=null}? ^(NOTBGN)		-> ^(BGN)
	|	n=NEG? ENDOP		-> {$n!=null}? ^(NOTEND)		-> ^(END)
	|	n=NEG? EQOP			-> {$n!=null}? ^(NOTEQ)			-> ^(EQ)
	|	n=NEG? NULLOP		-> {$n!=null}? ^(ISNOTNULL)		-> ^(ISNULL)
	;

/*
	TRUE,FALSE,
	YYYY/MM/DD | YY/MM/DD
	YYYY/MM/DDThh:mm:ss | YY/MM/DDThh:mm:ss
	{ varname }
	'literal' | "literal"
	10, 10.44
	( expr ) -> subquery
*/
val
	:	BOOLTRUE				->	^(LITBOOL TRUE)
	|	BOOLFALSE				->	^(LITBOOL FALSE)
	|	DATE					->	^(LITDATE DATE)
	|	TIMESTAMP				->	^(LITTIMESTAMP TIMESTAMP)
	|	LGRAPH NAME RGRAPH		->	^(INPUTVAL NAME)
	|	NATIVEELM				->	^(NATIVE NATIVEELM)
	|	LITERAL					->	^(LITSTR LITERAL)
	|	NUMBER					->	^(LITNUM NUMBER)
	|	expr
	;
/*
simple
	:	A
reference
	:	A.Code		follow reference field A, use Code field
	:	A.B.Code	follow ref. A, follow ref. B, use Code field
lookup
	:	A.parent()	
*/
id	:	NAME								-> ^(ATTRIBUTEID NAME)
	|	(cdref=NAME DOT)? attr=NAME lookupOp		-> ^(ATTRIBUTEID $attr ^(LOOKUP lookupOp) ^(CLASSDOMREF $cdref)?)
	|	NAME (DOT NAME)+ 					-> ^(ATTRIBUTEID NAME+)
	;
lookupOp
	:	DOT 'parent()' (lookupOp)? (DOT NAME)?		-> ^(LOOKUPPARENT ^(lookupOp)? ^(ATTRIBUTE NAME)?)
	;

// LEXER
//struct
SELECTOP
	:	'SELECT'|'select'
	;
FROMOP
	:	'FROM'|'from';
WHEREOP
	:	'WHERE'|'where';

HISTORYOP
	:	'HISTORY'|'history';
	
GROUPBYOP
	:	'GROUP BY'|'group by';
ORDEROP
	:	'ORDER BY'|'order by';
LMTOP
	:	'LIMIT'|'limit';
OFFSOP
	:	'OFFSET'|'offset';

//operators
BTWOP
	:	'BETWEEN'|'between';
BTWANDOP
	:	'AND'|'and';
INOP:	'IN'|'in';

LTEQOP
	:	'<=';
GTEQOP
	:	'>=';
LTOP:	'<';
GTOP:	'>';
CONTOP
	:	'CONTAINS'|'contains'|':=:';
BGNOP
	:	'BEGIN'|'begin'|'=:';
ENDOP
	:	'END'|'end'|':=';
EQOP:	'=';
NULLOP
	:	'NULL'|'null';

//doms
DOMOP:	'domain';
DOMREVOP
	:	'domainRev';
OBJECTS
	:	'objects'|'OBJECTS';
META
	:	'meta'|'META';

//andor
ANDOP	:	'AND' | 'and' | '&';
OROP	:	'OR' | 'or' | '|';

ALLOP	:	'*';

BOOLTRUE
	:	'TRUE' | 'true' | 't';
BOOLFALSE
	:	'FALSE' | 'false' | 'f';

fragment LETTER	:
	('a'..'z'|'A'..'Z');
fragment SIGN	:
	'+' | '-';
fragment DIGIT	:
	'0'..'9';
DATE:	(QDGT|DDGT) '/' DDGT '/' DDGT; //YYYY|YY/MM/DD
TIMESTAMP
	:	DATE 'T' DDGT COLON DDGT COLON DDGT //DATE'T'HH:mm:ss
	;
fragment QDGT
	:	'0'..'9' '0'..'9' '0'..'9' '0'..'9';
fragment DDGT
	:	'0'..'9' '0'..'9';
NUMBER	:
	INT | FLOAT;
fragment INT 	:
	SIGN? DIGIT+ ;
fragment FLOAT	:
	INT '.' '0'..'9'+;

//utils
WS	:
	(' '|'\t')+ { $channel=HIDDEN; } ;
LROUND
	:	'(';
RROUND
	:	')';
LSQUARE
	:	'[';
RSQUARE
	:	']';
LGRAPH
	:	'{';
RGRAPH
	:	'}';
TILDE
	:	'~';
NEG	:	'!';
DOT	:	'.';
SQUOTE
	:	'\'';
DQUOTE
	:	'"';
COLON
	:	':';
COMMA
	:	',';
AT	:	'@'|'AS'|'as';
SLASH
	:	'/';

NAME
	:	LETTER (DIGIT|LETTER|'_'|'#')*;
LITERAL
	:
//	DQUOTE ( ~(DQUOTE) )* DQUOTE | SQUOTE ( ~(SQUOTE) )* SQUOTE;//(~('"'))*;
		( '"' ( QUOTED_CHARACTER | '\'' )* '"' )
	|	( '\'' ( QUOTED_CHARACTER | '"' )* '\'' )
	;
QUOTED_CHARACTER
     : (~( '\'' | '"' | '\r'  | '\n' | '\\' ))
     | '\\' ( ( '\'' | '"' | 'n' |  'r'  | 't'  |  'b' |  'f' | '\\' )
//            | OCTAL_DIGIT
//	     (options {greedy=true;} : OCTAL_DIGIT)?
//              (options {greedy=true;} : OCTAL_DIGIT)?
	   )
     ;
NATIVEELM
	:	'/' ( ~('/') )* '/';
//protected OCTAL_DIGIT: '0'..'7';
