options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRSMAP.RRF' @LINE_TERM@
badfile 'MRSMAP.bad'
discardfile 'MRSMAP.dsc'
truncate
into table MRSMAP
fields terminated by '|'
trailing nullcols
(MAPSETCUI	char(8),
MAPSETSAB	char(40),
MAPID	char(50),
MAPSID	char(50),
FROMEXPR	char(4000),
FROMTYPE	char(50),
REL	char(4),
RELA	char(100),
TOEXPR	char(4000),
TOTYPE	char(50),
CVF	integer external
)