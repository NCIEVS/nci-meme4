options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRSAT.dat' @LINE_TERM@
badfile 'MRSAT.bad'
discardfile 'MRSAT.dsc'
truncate
into table MRSAT
fields terminated by '|'
trailing nullcols
(CUI	char(8),
LUI	char(8),
SUI	char(8),
CODE	char(100),
ATN	char(100),
SAB	char(40),
ATV	char(4000)
)