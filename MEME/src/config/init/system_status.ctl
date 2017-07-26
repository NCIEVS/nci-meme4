options (direct=true)
unrecoverable
load data
infile 'system_status' "str X'7c0a'"
badfile 'system_status.bad'
discardfile 'system_status.dsc'
truncate
into table system_status
fields terminated by '|'
trailing nullcols
(
system                                  CHAR(1000),
status                                  CHAR
)
