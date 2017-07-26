options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRXW_DAN.dat' @LINE_TERM@
badfile 'MRXW_DAN.bad'
discardfile 'MRXW_DAN.dsc'
truncate
into table MRXW_DAN
fields terminated by '|'
trailing nullcols
(LAT	char(3),
WD	char(100),
CUI	char(8),
LUI	char(8),
SUI	char(8)
)