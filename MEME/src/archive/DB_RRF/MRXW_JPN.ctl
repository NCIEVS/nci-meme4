options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRXW_JPN.RRF' @LINE_TERM@
badfile 'MRXW_JPN.bad'
discardfile 'MRXW_JPN.dsc'
truncate
into table MRXW_JPN
fields terminated by '|'
trailing nullcols
(LAT	char(3),
WD	char(100),
CUI	char(8),
LUI	char(8),
SUI	char(8)
)