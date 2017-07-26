options (direct=true)
unrecoverable
load data
infile 'meme_tables' "str X'7c0a'"
badfile 'meme_tables.bad'
discardfile 'meme_tables.dsc'
truncate
into table meme_tables
fields terminated by '|'
trailing nullcols
(
table_name                              CHAR
)
