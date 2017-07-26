options (direct=true)
unrecoverable
load data
infile 'stringtab' "str X'7c0a'"
badfile 'stringtab.bad'
discardfile 'stringtab.dsc'
truncate
into table stringtab
fields terminated by '|'
trailing nullcols
(
string_id                               DECIMAL EXTERNAL,
row_sequence                            DECIMAL EXTERNAL,
text_total                              DECIMAL EXTERNAL,
text_value                              CHAR(1786)
)
