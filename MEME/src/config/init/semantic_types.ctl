options (direct=true)
unrecoverable
load data
infile 'semantic_types' "str X'7c0a'"
badfile 'semantic_types.bad'
discardfile 'semantic_types.dsc'
truncate
into table semantic_types
fields terminated by '|'
trailing nullcols
(
semantic_type                           CHAR,
is_chem                                 CHAR,
chem_type                               CHAR,
editing_chem                            CHAR
)
