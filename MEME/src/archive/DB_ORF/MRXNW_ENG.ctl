options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRXNW_ENG.dat' @LINE_TERM@
badfile 'MRXNW_ENG.bad'
discardfile 'MRXNW_ENG.dsc'
truncate
into table MRXNW_ENG
fields terminated by '|'
trailing nullcols
(LAT	char(3),
NWD	char(100),
CUI	char(8),
LUI	char(8),
SUI	char(8)
)