options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRCXT.RRF' @LINE_TERM@
badfile 'MRCXT.bad'
discardfile 'MRCXT.dsc'
truncate
into table MRCXT
fields terminated by '|'
trailing nullcols
(CUI	char(8),
SUI	char(8),
AUI	char(9),
SAB	char(40),
CODE	char(100),
CXN	integer external,
CXL	char(3),
RANK	integer external,
CXS	char(3000),
CUI2	char(8),
AUI2	char(9),
HCD	char(100),
RELA	char(100),
XC	char(1),
CVF	integer external
)