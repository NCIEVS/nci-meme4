options (direct=true)
load data
characterset UTF8 length semantics char
infile 'AMBIGLUI.RRF' @LINE_TERM@
badfile 'AMBIGLUI.bad'
discardfile 'AMBIGLUI.dsc'
truncate
into table AMBIGLUI
fields terminated by '|'
trailing nullcols
(LUI	char(8),
CUI	char(8)
)