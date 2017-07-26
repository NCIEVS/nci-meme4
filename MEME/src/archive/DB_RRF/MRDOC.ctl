options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRDOC.RRF' @LINE_TERM@
badfile 'MRDOC.bad'
discardfile 'MRDOC.dsc'
truncate
into table MRDOC
fields terminated by '|'
trailing nullcols
(DOCKEY	char(50),
VALUE	char(200),
TYPE	char(50),
EXPL	char(1000)
)