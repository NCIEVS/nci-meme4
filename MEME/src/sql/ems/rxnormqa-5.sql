# 5. RXNORM/IN,BN concepts.  If you can find a better predicate
#    for "New Ingredients" I can write a query for it.

SELECT distinct concept_id FROM classes WHERE source  like 'RXNORM%'
AND termgroup IN 
  (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' 
   AND tty in ('IN','BN')) AND tobereleased in ('Y','y');


