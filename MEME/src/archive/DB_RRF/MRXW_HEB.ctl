options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRXW_HEB.RRF' @LINE_TERM@
badfile 'MRXW_HEB.bad'
discardfile 'MRXW_HEB.dsc'
truncate
into table MRXW_HEB
fields terminated by '|'
trailing nullcols
(LAT	char(3),
WD	char(100),
CUI	char(8),
LUI	char(8),
SUI	char(8)
)