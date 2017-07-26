options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRATX.dat' @LINE_TERM@
badfile 'MRATX.bad'
discardfile 'MRATX.dsc'
truncate
into table MRATX
fields terminated by '|'
trailing nullcols
(CUI	char(8),
SAB	char(40),
REL	char(3),
ATX	char(300)
)