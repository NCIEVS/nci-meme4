#!/bin/csh -f

# WAK
# 1/17/2001
# modified for MEME3

# report conflicting BT/NT relationships.  
# sort -u removes duplicate NT or BT rels

grep NT relationships.src | awk -F\| '{if ($3<$6) {print  $2"|"$3"|"$6} else if ($6<$3){print $2"|"$6"|"$3}}'  | sort -u > duprels

grep BT relationships.src | awk -F\| '{if ($3<$6) {print  $2"|"$6"|"$3} else if ($6<$3){print $2"|"$3"|"$6}}'  | sort -u >> duprels

uniq -d duprels

rm -f duprels
