options (direct=true)
unrecoverable
load data
infile 'meme_properties' "str X'7c0a'"
badfile 'meme_properties.bad'
discardfile 'meme_properties.dsc'
truncate
into table meme_properties
fields terminated by '|'
trailing nullcols
(
definition                              CHAR(4000),
example                                 CHAR(4000),
reference                               CHAR(1000),
key                                     CHAR,
key_qualifier                           CHAR,
value                                   CHAR(4000),
description                             CHAR(1000)
)
