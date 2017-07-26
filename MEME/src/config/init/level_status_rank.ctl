options (direct=true)
unrecoverable
load data
infile 'level_status_rank' "str X'7c0a'"
badfile 'level_status_rank.bad'
discardfile 'level_status_rank.dsc'
truncate
into table level_status_rank
fields terminated by '|'
trailing nullcols
(
level_value                             CHAR,
status                                  CHAR,
table_name                              CHAR,
rank                                    DECIMAL EXTERNAL
)
