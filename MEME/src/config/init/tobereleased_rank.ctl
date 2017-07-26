options (direct=true)
unrecoverable
load data
infile 'tobereleased_rank' "str X'7c0a'"
badfile 'tobereleased_rank.bad'
discardfile 'tobereleased_rank.dsc'
truncate
into table tobereleased_rank
fields terminated by '|'
trailing nullcols
(
tobereleased                            CHAR,
rank                                    DECIMAL EXTERNAL
)
