options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRCON.dat' @LINE_TERM@
badfile 'MRCON.bad'
discardfile 'MRCON.dsc'
truncate
into table MRCON
fields terminated by '|'
trailing nullcols
(CUI	char(8),
LAT	char(3),
TS	char(1),
LUI	char(8),
STT	char(3),
SUI	char(8),
STR	char(3000),
LRL	integer external
)