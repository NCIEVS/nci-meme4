options (direct=true)
unrecoverable
load data
infile 'code_map' "str X'7c0a'"
badfile 'code_map.bad'
discardfile 'code_map.dsc'
truncate
into table code_map
fields terminated by '|'
trailing nullcols
(
code                                    CHAR,
type                                    CHAR,
value                                   CHAR(4000)
)
