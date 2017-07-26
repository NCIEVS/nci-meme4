options (direct=true)
unrecoverable
load data
infile 'editors' "str X'7c0a'"
badfile 'editors.bad'
discardfile 'editors.dsc'
truncate
into table editors
fields terminated by '|'
trailing nullcols
(
name                                    CHAR,
editor_level                            DECIMAL EXTERNAL,
initials                                CHAR,
grp                                     CHAR,
cur                                     CHAR
)
