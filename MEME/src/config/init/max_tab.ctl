options (direct=true)
unrecoverable
load data
infile 'max_tab' "str X'7c0a'"
badfile 'max_tab.bad'
discardfile 'max_tab.dsc'
truncate
into table max_tab
fields terminated by '|'
trailing nullcols
(
table_name                              CHAR,
max_id                                  DECIMAL EXTERNAL
)
