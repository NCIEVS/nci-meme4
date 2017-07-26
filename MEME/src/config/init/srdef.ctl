options (direct=true)
unrecoverable
load data
infile 'srdef' "str X'7c0a'"
badfile 'srdef.bad'
discardfile 'srdef.dsc'
truncate
into table srdef
fields terminated by '|'
trailing nullcols
(
rt                                      CHAR,
ui                                      CHAR,
sty_rl                                  CHAR,
stn_rtn                                 CHAR,
def                                     CHAR(1900),
ex                                      CHAR(700),
un                                      CHAR(700),
nh                                      CHAR,
abr                                     CHAR,
rin                                     CHAR
)
