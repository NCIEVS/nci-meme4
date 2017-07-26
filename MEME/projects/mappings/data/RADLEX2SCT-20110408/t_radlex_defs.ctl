options (direct=true)
unrecoverable
load data
infile 't_radlex_defs' "str X'7c0a'"
badfile 't_radlex_defs.bad'
discardfile 't_radlex_defs.dsc'
truncate
into table t_radlex_defs
fields terminated by '|'
trailing nullcols
(
cui                                     CHAR,
code                                    CHAR,
name                                    CHAR(3000),
cui_pt                                  CHAR(3000),
attribute_value                         CHAR(3000),
source                                  CHAR
)
