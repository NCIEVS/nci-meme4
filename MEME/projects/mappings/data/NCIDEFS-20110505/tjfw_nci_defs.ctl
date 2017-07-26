options (direct=true)
unrecoverable
load data
infile 'tjfw_nci_defs' "str X'7c0a'"
badfile 'tjfw_nci_defs.bad'
discardfile 'tjfw_nci_defs.dsc'
truncate
into table tjfw_nci_defs
fields terminated by '|'
trailing nullcols
(
cui                                     CHAR,
code                                    CHAR,
name                                    CHAR(3000),
attribute_value                         CHAR(3000),
source                                  CHAR
)
