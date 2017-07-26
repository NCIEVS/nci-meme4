options (direct=true)
unrecoverable
load data
infile 't_asdf_out' "str X'7c0a'"
badfile 't_asdf_out.bad'
discardfile 't_asdf_out.dsc'
truncate
into table t_asdf_out
fields terminated by '|'
trailing nullcols
(
snomedct_code                           CHAR,
snomedct_name                           CHAR(3000),
relationship_name                       CHAR,
map_rank                                DECIMAL EXTERNAL,
icd10_code                              CHAR,
icd10_name                              CHAR(3000)
)
