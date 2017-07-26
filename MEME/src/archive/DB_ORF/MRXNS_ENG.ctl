options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRXNS_ENG.dat' @LINE_TERM@
badfile 'MRXNS_ENG.bad'
discardfile 'MRXNS_ENG.dsc'
truncate
into table MRXNS_ENG
fields terminated by '|'
trailing nullcols
(LAT	char(3),
NSTR	char(3000),
CUI	char(8),
LUI	char(8),
SUI	char(8)
)