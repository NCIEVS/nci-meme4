options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRREL.dat' @LINE_TERM@
badfile 'MRREL.bad'
discardfile 'MRREL.dsc'
truncate
into table MRREL
fields terminated by '|'
trailing nullcols
(CUI1	char(8),
REL	char(3),
CUI2	char(8),
RELA	char(100),
SAB	char(40),
SL	char(40),
MG	char(1)
)