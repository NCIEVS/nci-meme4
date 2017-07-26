options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRCXT.dat' @LINE_TERM@
badfile 'MRCXT.bad'
discardfile 'MRCXT.dsc'
truncate
into table MRCXT
fields terminated by '|'
trailing nullcols
(CUI	char(8),
SUI	char(8),
SAB	char(40),
CODE	char(100),
CXN	integer external,
CXL	char(3),
RNK	integer external,
CXS	char(3000),
CUI2	char(8),
HCD	char(100),
RELA	char(100),
XC	char(1)
)