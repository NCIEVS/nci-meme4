options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRSAT.RRF' @LINE_TERM@
badfile 'MRSAT.bad'
discardfile 'MRSAT.dsc'
truncate
into table MRSAT
fields terminated by '|'
trailing nullcols
(CUI	char(8),
LUI	char(8),
SUI	char(8),
METAUI	char(50),
STYPE	char(50),
CODE	char(100),
ATUI	char(10),
SATUI	char(50),
ATN	char(100),
SAB	char(40),
ATV	char(4000),
SUPPRESS	char(1),
CVF	integer external
)