options (direct=true)
unrecoverable
load data
infile 'language' "str X'7c0a'"
badfile 'language.bad'
discardfile 'language.dsc'
truncate
into table language
fields terminated by '|'
trailing nullcols
(
language                                CHAR,
lat                                     CHAR,
iso_lat                                 CHAR
)
