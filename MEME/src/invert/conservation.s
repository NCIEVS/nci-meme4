#!/bin/csh 

touch conservation.out

echo "termgroup count:" >> conservation.out
echo "" >> conservation.out
tallyfield.pl '$3' ../src/classes_atoms.src >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out

echo "termtype + suppressibility count:" >> conservation.out
echo "" >> conservation.out
tallyfield.pl '$3$9' ../src/classes_atoms.src >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out


echo "termtype + status count:" >> conservation.out
echo "" >> conservation.out
tallyfield.pl '$3$5' ../src/classes_atoms.src >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out

echo "termtype + status + tobereleased + released + suppressibility count" >> conservation.out
echo "" >> conservation.out
tallyfield.pl '$3$5$6$7$9' ../src/classes_atoms.src >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out

echo "count of non-blank SAUI fields" >> conservation.out
awk -F\| '$10!=""' ../src/classes_atoms.src | wc -l >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out

echo "count of non-blank SCUI fields" >> conservation.out
awk -F\| '$11!=""' ../src/classes_atoms.src | wc -l >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out

echo "count of non-blank SDUI fields" >> conservation.out
awk -F\| '$12!=""' ../src/classes_atoms.src | wc -l >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out

echo "mergeset count:" >> conservation.out
tallyfield.pl '$8' ../src/mergefacts.src >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out


echo "merge level + source + merge_set + id_type_1 + id_qual_1 + id_type_2 + id_qual_2 count:" >> conservation.out
tallyfield.pl '$2$4$8$9$10$11$12' ../src/mergefacts.src >> conservation.out 
echo "" >> conservation.out
echo "" >> conservation.out


echo "rel + rela count:" >> conservation.out
echo "" >> conservation.out
tallyfield.pl '$4$5' ../src/relationships.src >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out


echo "level + rel + rela + source of rel + source of rela count:" >> conservation.out
echo "" >> conservation.out
tallyfield.pl '$2$4$5$7$8' ../src/relationships.src >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out


echo "level + rel + rela + source of rel + source of rela + status + tbr + rel + supp + id_type_1 + id_qual_1 + id_type_2 + id_qual_2 count:" >> conservation.out
echo "" >> conservation.out
tallyfield.pl '$2$4$5$7$8$9$10$11$12$13$14$15$16' ../src/relationships.src >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out

echo "ATN count:" >> conservation.out
echo "" >> conservation.out
tallyfield.pl '$4' ../src/attributes.src >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out

echo "level + atn + source + status + tbr + released + suppressible + id_type + id_qual count" >> conservation.out
echo "" >> conservation.out
tallyfield.pl '$3$4$6$7$8$9$10$11$12' ../src/attributes.src >> conservation.out
echo "" >> conservation.out
echo "" >> conservation.out


