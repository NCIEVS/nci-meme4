options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRRANK.dat' @LINE_TERM@
badfile 'MRRANK.bad'
discardfile 'MRRANK.dsc'
truncate
into table MRRANK
fields terminated by '|'
trailing nullcols
(RANK	integer external,
SAB	char(40),
TTY	char(20),
SUPRES	char(1)
)