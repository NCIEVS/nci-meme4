options (direct=true)
unrecoverable
load data
infile 'source_version' "str X'7c0a'"
badfile 'source_version.bad'
discardfile 'source_version.dsc'
truncate
into table source_version
fields terminated by '|'
trailing nullcols
(
source                                  CHAR,
current_name                            CHAR,
previous_name                           CHAR
)
