options (direct=true)
unrecoverable
load data
infile 'ic_override' "str X'7c0a'"
badfile 'ic_override.bad'
discardfile 'ic_override.dsc'
truncate
into table ic_override
fields terminated by '|'
trailing nullcols
(
ic_level                                DECIMAL EXTERNAL,
override_vector                         CHAR(2000)
)
