options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRDEF.RRF' @LINE_TERM@
badfile 'MRDEF.bad'
discardfile 'MRDEF.dsc'
truncate
into table MRDEF
fields terminated by '|'
trailing nullcols
(CUI	char(8),
AUI	char(9),
ATUI	char(10),
SATUI	char(50),
SAB	char(40),
DEF	char(4000),
SUPPRESS	char(1),
CVF	integer external
)