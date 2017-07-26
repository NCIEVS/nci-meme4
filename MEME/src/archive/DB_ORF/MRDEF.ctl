options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRDEF.dat' @LINE_TERM@
badfile 'MRDEF.bad'
discardfile 'MRDEF.dsc'
truncate
into table MRDEF
fields terminated by '|'
trailing nullcols
(CUI	char(8),
SAB	char(40),
DEF	char(4000)
)