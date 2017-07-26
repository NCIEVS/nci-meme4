options (direct=true)
unrecoverable
load data
infile 'nhsty' "str X'7c0a'"
badfile 'nhsty.bad'
discardfile 'nhsty.dsc'
truncate
into table nhsty
fields terminated by '|'
trailing nullcols
(
ui                                      CHAR,
sty                                     CHAR,
stn                                     CHAR,
def                                     CHAR(1900)
)
