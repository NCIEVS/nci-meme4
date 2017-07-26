options (direct=true)
unrecoverable
load data
infile 'integrity_constraints' "str X'7c0a'"
badfile 'integrity_constraints.bad'
discardfile 'integrity_constraints.dsc'
truncate
into table integrity_constraints
fields terminated by '|'
trailing nullcols
(
ic_name                                 CHAR,
v_actions                               CHAR,
c_actions                               CHAR,
ic_status                               CHAR,
ic_type                                 CHAR,
activation_date                         DATE "DD-mon-YYYY HH24:MI:SS",
deactivation_date                       DATE "DD-mon-YYYY HH24:MI:SS",
ic_short_dsc                            CHAR,
ic_long_dsc                             CHAR(1000)
)
