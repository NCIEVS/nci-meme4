options (direct=true)
unrecoverable
load data
infile 'source_rank' "str X'7c0a'"
badfile 'source_rank.bad'
discardfile 'source_rank.dsc'
truncate
into table source_rank
fields terminated by '|'
trailing nullcols
(
source                                  CHAR,
rank                                    DECIMAL EXTERNAL,
restriction_level                       DECIMAL EXTERNAL,
normalized_source                       CHAR,
stripped_source                         CHAR,
source_family                           CHAR,
version                                 CHAR,
notes                                   CHAR(1000)
)
