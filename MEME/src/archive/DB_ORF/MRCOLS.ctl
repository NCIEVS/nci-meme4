options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRCOLS.dat' @LINE_TERM@
badfile 'MRCOLS.bad'
discardfile 'MRCOLS.dsc'
truncate
into table MRCOLS
fields terminated by '|'
trailing nullcols
(COL	char(10),
DES	char(100),
REF	char(20),
MIN	integer external,
AV	float external,
MAX	integer external,
FIL	char(50),
DTY	char(20)
)