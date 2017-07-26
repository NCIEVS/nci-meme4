# 38.  Normal form merges: Concepts containing normal form atoms with 
#      different case-insensitive strings (ISUI).

SELECT concept_id FROM classes
WHERE source = (SELECT current_name FROM source_version WHERE source='RXNORM')
  AND tty in ('SCD','SBD','SCDC','SBDC','SCDF','SBDF')
  AND tobereleased in ('Y','y')
GROUP BY concept_id HAVING count(distinct tty||isui)>1;


