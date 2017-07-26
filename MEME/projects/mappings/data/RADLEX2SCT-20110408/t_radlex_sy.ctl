options (direct=true)
unrecoverable
load data
infile 't_radlex_sy' "str X'7c0a'"
badfile 't_radlex_sy.bad'
discardfile 't_radlex_sy.dsc'
truncate
into table t_radlex_sy
fields terminated by '|'
trailing nullcols
(
cui                                     CHAR,
code                                    CHAR,
pt_name                                 CHAR(3000),
cui_pt                                  CHAR(3000),
sy_name                                 CHAR(3000)
)
