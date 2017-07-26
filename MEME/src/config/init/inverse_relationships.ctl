options (direct=true)
unrecoverable
load data
infile 'inverse_relationships' "str X'7c0a'"
badfile 'inverse_relationships.bad'
discardfile 'inverse_relationships.dsc'
truncate
into table inverse_relationships
fields terminated by '|'
trailing nullcols
(
relationship_name                       CHAR,
inverse_name                            CHAR,
weak_flag                               CHAR,
long_name                               CHAR,
release_name                            CHAR,
rank                                    DECIMAL EXTERNAL
)
