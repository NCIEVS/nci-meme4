options (direct=true)
load data
characterset UTF8 length semantics char
infile 'AMBIG_LUI.dat' @LINE_TERM@
badfile 'AMBIG_LUI.bad'
discardfile 'AMBIG_LUI.dsc'
truncate
into table AMBIG_LUI
fields terminated by '|'
trailing nullcols
(LUI	char(8),
CUI	char(8)
)