options (direct=true)
load data
characterset UTF8 length semantics char
infile 'MRMAP.RRF' @LINE_TERM@
badfile 'MRMAP.bad'
discardfile 'MRMAP.dsc'
truncate
into table MRMAP
fields terminated by '|'
trailing nullcols
(MAPSETCUI	char(8),
MAPSETSAB	char(40),
MAPSUBSETID	char(10),
MAPRANK	integer external,
MAPID	char(50),
MAPSID	char(50),
FROMID	char(50),
FROMSID	char(50),
FROMEXPR	char(4000),
FROMTYPE	char(50),
FROMRULE	char(4000),
FROMRES	char(4000),
REL	char(4),
RELA	char(100),
TOID	char(50),
TOSID	char(50),
TOEXPR	char(4000),
TOTYPE	char(50),
TORULE	char(4000),
TORES	char(4000),
MAPRULE	char(4000),
MAPRES	char(4000),
MAPTYPE	char(50),
MAPATN	char(100),
MAPATV	char(4000),
CVF	integer external
)