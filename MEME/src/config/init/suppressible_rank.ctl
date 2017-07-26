options (direct=true)
unrecoverable
load data
infile 'suppressible_rank' "str X'7c0a'"
badfile 'suppressible_rank.bad'
discardfile 'suppressible_rank.dsc'
truncate
into table suppressible_rank
fields terminated by '|'
trailing nullcols
(
suppressible                            CHAR,
rank                                    DECIMAL EXTERNAL
)
