options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRCOC.RRF' @LINE_TERM@
badfile 'MRCOC.bad'
discardfile 'MRCOC.dsc'
truncate
into table MRCOC
fields terminated by '|'
trailing nullcols
(CUI1	char(8),
AUI1	char(9),
CUI2	char(8),
AUI2	char(9),
SAB	char(40),
COT	char(3),
COF	integer external,
COA	char(300),
CVF	integer external
)