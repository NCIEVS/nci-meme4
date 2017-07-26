options (direct=true)
unrecoverable
load data
infile 'ic_applications' "str X'7c0a'"
badfile 'ic_applications.bad'
discardfile 'ic_applications.dsc'
truncate
into table ic_applications
fields terminated by '|'
trailing nullcols
(
application                             CHAR,
integrity_vector                        CHAR(2000)
)
