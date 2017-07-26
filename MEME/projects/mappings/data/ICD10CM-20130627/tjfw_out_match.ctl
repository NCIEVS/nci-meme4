options (direct=true)
unrecoverable
load data
infile 'tjfw_out_match' "str X'7c0a'"
badfile 'tjfw_out_match.bad'
discardfile 'tjfw_out_match.dsc'
truncate
into table tjfw_out_match
fields terminated by '|'
trailing nullcols
(
drug_name                               CHAR(3000),
drug_code                               CHAR,
icd10_code                              CHAR,
icd10_name                              CHAR(3000),
atv                                     CHAR(3000)
)
