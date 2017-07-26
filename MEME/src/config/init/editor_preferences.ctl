options (direct=true)
unrecoverable
load data
infile 'editor_preferences' "str X'7c0a'"
badfile 'editor_preferences.bad'
discardfile 'editor_preferences.dsc'
truncate
into table editor_preferences
fields terminated by '|'
trailing nullcols
(
name                                    CHAR,
initials                                CHAR,
show_concept                            DECIMAL EXTERNAL,
show_classes                            DECIMAL EXTERNAL,
show_relationships                      DECIMAL EXTERNAL,
show_attributes                         DECIMAL EXTERNAL
)
