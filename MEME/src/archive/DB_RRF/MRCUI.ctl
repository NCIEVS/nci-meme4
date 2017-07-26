options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRCUI.RRF' @LINE_TERM@
badfile 'MRCUI.bad'
discardfile 'MRCUI.dsc'
truncate
into table MRCUI
fields terminated by '|'
trailing nullcols
(CUI1	char(8),
VER	char(10),
REL	char(4),
RELA	char(100),
MAPREASON	char(4000),
CUI2	char(8),
MAPIN	char(1)
)