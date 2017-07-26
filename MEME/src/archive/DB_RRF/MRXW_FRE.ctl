options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRXW_FRE.RRF' @LINE_TERM@
badfile 'MRXW_FRE.bad'
discardfile 'MRXW_FRE.dsc'
truncate
into table MRXW_FRE
fields terminated by '|'
trailing nullcols
(LAT	char(3),
WD	char(100),
CUI	char(8),
LUI	char(8),
SUI	char(8)
)