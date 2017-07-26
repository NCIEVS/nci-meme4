# Merged SNOMED hierarchies - excluding known permitted combinations
# carlsen@apelon.com

select distinct c1.concept_id, rownum as cluster_id
from classes c1, classes c2, atoms a1, atoms a2 where
c1.atom_id = a1.atom_id and
c2.atom_id = a2.atom_id and
c1.source= (select current_name from source_version where source='SNOMEDCT') and
c1.tty='FN' and
c1.tobereleased in ('Y', 'y') and
c2.source= (select current_name from source_version where source='SNOMEDCT') and
c2.tty='FN' and
c2.tobereleased in ('Y', 'y') and

c1.CONCEPT_ID = c2.CONCEPT_ID and

substr(a1.atom_name, 1, instr(a1.atom_name, '(', -1)-1) <> substr(a2.atom_name, 1, instr(a2.atom_name, '(', -1)-1) and

substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) <>
substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 

and not substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	in ('attribute','qualifier value','context-dependent category')
and not substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	in ('attribute','qualifier value','context-dependent category')

and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='product') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'substance') )
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='substance') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'product') )
	
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='disorder') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'morphologic abnormality') )
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='morphologic abnormality') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'disorder') )
	
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='disorder') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'finding') )
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='finding') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'disorder') )
	
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='finding') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'observable entity') )
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='observable entity') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'finding') )
	
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='finding') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'morphologic abnormality') )
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='morphologic abnormality') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'finding') )
	
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='regime/therapy') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'procedure') )
and not( (substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) 
	='procedure') and (substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) 
	= 'regime/therapy') )
