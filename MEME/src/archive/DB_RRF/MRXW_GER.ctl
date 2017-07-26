options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRXW_GER.RRF' @LINE_TERM@
badfile 'MRXW_GER.bad'
discardfile 'MRXW_GER.dsc'
truncate
into table MRXW_GER
fields terminated by '|'
trailing nullcols
(LAT	char(3),
WD	char(100),
CUI	char(8),
LUI	char(8),
SUI	char(8)
)