options (direct=true)
unrecoverable
load data
infile 'inverse_rel_attributes' "str X'7c0a'"
badfile 'inverse_rel_attributes.bad'
discardfile 'inverse_rel_attributes.dsc'
truncate
into table inverse_rel_attributes
fields terminated by '|'
trailing nullcols
(
relationship_attribute                  CHAR,
inverse_rel_attribute                   CHAR,
rank                                    DECIMAL EXTERNAL
)
