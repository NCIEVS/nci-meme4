options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRXW_SPA.dat' @LINE_TERM@
badfile 'MRXW_SPA.bad'
discardfile 'MRXW_SPA.dsc'
truncate
into table MRXW_SPA
fields terminated by '|'
trailing nullcols
(LAT	char(3),
WD	char(100),
CUI	char(8),
LUI	char(8),
SUI	char(8)
)