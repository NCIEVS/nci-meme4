options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRXW_POR.RRF' @LINE_TERM@
badfile 'MRXW_POR.bad'
discardfile 'MRXW_POR.dsc'
truncate
into table MRXW_POR
fields terminated by '|'
trailing nullcols
(LAT	char(3),
WD	char(100),
CUI	char(8),
LUI	char(8),
SUI	char(8)
)