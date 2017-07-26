options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRSTY.dat' @LINE_TERM@
badfile 'MRSTY.bad'
discardfile 'MRSTY.dsc'
truncate
into table MRSTY
fields terminated by '|'
trailing nullcols
(CUI	char(8),
TUI	char(4),
STY	char(50)
)