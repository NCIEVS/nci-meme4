options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRCONSO.RRF' @LINE_TERM@
badfile 'MRCONSO.bad'
discardfile 'MRCONSO.dsc'
truncate
into table MRCONSO
fields terminated by '|'
trailing nullcols
(CUI	char(8),
LAT	char(3),
TS	char(1),
LUI	char(8),
STT	char(3),
SUI	char(8),
ISPREF	char(1),
AUI	char(9),
SAUI	char(100),
SCUI	char(100),
SDUI	char(100),
SAB	char(40),
TTY	char(20),
CODE	char(100),
STR	char(3000),
SRL	integer external,
SUPPRESS	char(1),
CVF	integer external
)