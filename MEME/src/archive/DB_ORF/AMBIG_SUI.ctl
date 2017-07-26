options (direct=true)
load data
characterset UTF8 length semantics char
infile 'AMBIG_SUI.dat' @LINE_TERM@
badfile 'AMBIG_SUI.bad'
discardfile 'AMBIG_SUI.dsc'
truncate
into table AMBIG_SUI
fields terminated by '|'
trailing nullcols
(SUI	char(8),
CUI	char(8)
)