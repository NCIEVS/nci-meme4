options (direct=true)
load data
characterset UTF8 length semantics char
infile 'AMBIGSUI.RRF' @LINE_TERM@
badfile 'AMBIGSUI.bad'
discardfile 'AMBIGSUI.dsc'
truncate
into table AMBIGSUI
fields terminated by '|'
trailing nullcols
(SUI	char(8),
CUI	char(8)
)