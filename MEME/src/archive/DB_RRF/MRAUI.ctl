options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRAUI.RRF' @LINE_TERM@
badfile 'MRAUI.bad'
discardfile 'MRAUI.dsc'
truncate
into table MRAUI
fields terminated by '|'
trailing nullcols
(AUI1	char(9),
CUI1	char(8),
VER	char(10),
REL	char(4),
RELA	char(100),
MAPREASON	char(4000),
AUI2	char(9),
CUI2	char(8),
MAPIN	char(1)
)