options (direct=true)
unrecoverable
load data
infile 'mid_qa_queries' "str X'7c0a'"
badfile 'mid_qa_queries.bad'
discardfile 'mid_qa_queries.dsc'
truncate
into table mid_qa_queries
fields terminated by '|'
trailing nullcols
(
name                                    CHAR,
query                                   CHAR(1000)
)
