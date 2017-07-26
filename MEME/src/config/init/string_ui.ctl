options (direct=true)
unrecoverable
load data
infile 'string_ui' "str X'7c0a'"
badfile 'string_ui.bad'
discardfile 'string_ui.dsc'
truncate
into table string_ui
fields terminated by '|'
trailing nullcols
(
lui                                     CHAR,
sui                                     CHAR,
string_pre                              CHAR,
norm_string_pre                         CHAR,
language                                CHAR,
base_string                             CHAR,
string                                  CHAR(3000),
norm_string                             CHAR(3000),
isui                                    CHAR,
lowercase_string_pre                    CHAR
)
