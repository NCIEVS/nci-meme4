options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRCOC.dat' @LINE_TERM@
badfile 'MRCOC.bad'
discardfile 'MRCOC.dsc'
truncate
into table MRCOC
fields terminated by '|'
trailing nullcols
(CUI1	char(8),
CUI2	char(8),
SOC	char(40),
COT	char(3),
COF	integer external,
COA	char(300)
)