options (direct=true)
unrecoverable
load data
infile 't_10193_out_best_mrmap' "str X'7c0a'"
badfile 't_10193_out_best_mrmap.bad'
discardfile 't_10193_out_best_mrmap.dsc'
truncate
into table t_10193_out_best_mrmap
fields terminated by '|'
trailing nullcols
(
mapsetcui                               CHAR,
mapsab                                  CHAR,
mapsubsetid                             DECIMAL EXTERNAL,
maprank                                 DECIMAL EXTERNAL,
mapid                                   DECIMAL EXTERNAL,
mapsid                                  CHAR,
fromid                                  CHAR,
fromsid                                 CHAR,
fromexpr                                CHAR,
fromtype                                CHAR,
fromrule                                CHAR,
fromres                                 CHAR,
rel                                     CHAR,
rela                                    CHAR,
toid                                    CHAR,
tosid                                   CHAR,
toexpr                                  CHAR,
totype                                  CHAR,
torule                                  CHAR,
tores                                   CHAR,
maprule                                 CHAR,
mapres                                  CHAR,
maptype                                 CHAR,
atn                                     CHAR,
atv                                     CHAR
)
