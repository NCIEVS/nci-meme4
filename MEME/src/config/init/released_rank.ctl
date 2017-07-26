options (direct=true)
unrecoverable
load data
infile 'released_rank' "str X'7c0a'"
badfile 'released_rank.bad'
discardfile 'released_rank.dsc'
truncate
into table released_rank
fields terminated by '|'
trailing nullcols
(
released                                CHAR,
rank                                    DECIMAL EXTERNAL
)
