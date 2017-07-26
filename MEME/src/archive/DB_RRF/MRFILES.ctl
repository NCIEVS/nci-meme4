options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRFILES.RRF' @LINE_TERM@
badfile 'MRFILES.bad'
discardfile 'MRFILES.dsc'
truncate
into table MRFILES
fields terminated by '|'
trailing nullcols
(FIL	char(50),
DES	char(200),
FMT	char(300),
CLS	integer external,
RWS	integer external,
BTS	integer external
)