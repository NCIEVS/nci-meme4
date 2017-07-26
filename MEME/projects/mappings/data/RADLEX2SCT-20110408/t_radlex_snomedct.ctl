options (direct=true)
unrecoverable
load data
infile 't_radlex_snomedct' "str X'7c0a'"
badfile 't_radlex_snomedct.bad'
discardfile 't_radlex_snomedct.dsc'
truncate
into table t_radlex_snomedct
fields terminated by '|'
trailing nullcols
(
cui                                     CHAR,
radlex_code                             CHAR,
radlex_name                             CHAR(3000),
cui_pt                                  CHAR(3000),
snomedct_name                           CHAR(3000),
snomedct_code                           CHAR
)
