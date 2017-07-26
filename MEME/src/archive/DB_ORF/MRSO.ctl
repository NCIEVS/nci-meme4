options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRSO.dat' @LINE_TERM@
badfile 'MRSO.bad'
discardfile 'MRSO.dsc'
truncate
into table MRSO
fields terminated by '|'
trailing nullcols
(CUI	char(8),
LUI	char(8),
SUI	char(8),
SAB	char(40),
TTY	char(20),
CODE	char(100),
SRL	integer external
)