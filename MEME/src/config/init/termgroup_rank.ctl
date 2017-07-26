options (direct=true)
unrecoverable
load data
infile 'termgroup_rank' "str X'7c0a'"
badfile 'termgroup_rank.bad'
discardfile 'termgroup_rank.dsc'
truncate
into table termgroup_rank
fields terminated by '|'
trailing nullcols
(
termgroup                               CHAR,
rank                                    DECIMAL EXTERNAL,
notes                                   CHAR,
suppressible                            CHAR,
tty                                     CHAR,
release_rank                            DECIMAL EXTERNAL
)
