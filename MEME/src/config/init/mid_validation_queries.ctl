options (direct=true)
unrecoverable
load data
infile 'mid_validation_queries' "str X'7c0a'"
badfile 'mid_validation_queries.bad'
discardfile 'mid_validation_queries.dsc'
truncate
into table mid_validation_queries
fields terminated by '`'
trailing nullcols
(
check_type                              CHAR,
query                                   CHAR(2000),
check_name                              CHAR,
make_checklist                          CHAR,
description                             CHAR(4000),
adjustment                              DECIMAL EXTERNAL,
adjustment_dsc                          CHAR(1000),
auto_fix                                CHAR(4000)
)
