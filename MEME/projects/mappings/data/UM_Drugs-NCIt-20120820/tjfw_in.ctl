options (direct=true)
unrecoverable
load data
infile 'tjfw_in' "str X'7c0a'"
badfile 'tjfw_in.bad'
discardfile 'tjfw_in.dsc'
truncate
into table tjfw_in
fields terminated by '|'
trailing nullcols
(
original_name                           CHAR(3000),
norm_name                               CHAR(3000)
)
