options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRCUI.dat' @LINE_TERM@
badfile 'MRCUI.bad'
discardfile 'MRCUI.dsc'
truncate
into table MRCUI
fields terminated by '|'
trailing nullcols
(CUI1	char(8),
VER	char(10),
CREL	char(4),
CUI2	char(8),
MAPIN	char(1)
)