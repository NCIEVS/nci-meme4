options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRHIST.RRF' @LINE_TERM@
badfile 'MRHIST.bad'
discardfile 'MRHIST.dsc'
truncate
into table MRHIST
fields terminated by '|'
trailing nullcols
(CUI	char(8),
SOURCEUI	char(100),
SAB	char(40),
SVER	char(20),
CHANGETYPE	char(1000),
CHANGEKEY	char(1000),
CHANGEVAL	char(1000),
REASON	char(1000),
CVF	integer external
)