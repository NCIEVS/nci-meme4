options (direct=true)
unrecoverable
load data
infile 'src_qa_queries' "str X'7c0a'"
badfile 'src_qa_queries.bad'
discardfile 'src_qa_queries.dsc'
truncate
into table src_qa_queries
fields terminated by '|'
trailing nullcols
(
name                                    CHAR,
query                                   CHAR(1000)
)
