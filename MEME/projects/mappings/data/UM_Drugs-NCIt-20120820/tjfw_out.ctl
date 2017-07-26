options (direct=true)
unrecoverable
load data
infile 'tjfw_out' "str X'7c0a'"
badfile 'tjfw_out.bad'
discardfile 'tjfw_out.dsc'
truncate
into table tjfw_out
fields terminated by '|'
trailing nullcols
(
original_name                           CHAR(3000),
code                                    CHAR,
atom_name                               CHAR(3000),
tty                                     CHAR,
rank                                    DECIMAL EXTERNAL
)
