#!/bin/csh -f

# Script to create mrdoc.toedit, a template for adding MRDOC information
# for NEW .src TTYs, ATNs and RELAs

# usage:  get.mrdoclist.s
# after running this script, cp mrdoc.toedit MRDOC.RRF and edit the ###

# written by:  Stephanie Lipow
# last update:  May 23, 2005
# last update:  Aug 3, 2006 by RH, (see ### lines)
# last update:  Aug 9, 2006 by SSL to fix path errors
# 12/01/2006 TTN (1-CDMK9): get the MRDOC data from MID instead of RRF file

\rm ../tmp/mrdoc.toedit
touch ../tmp/mrdoc.toedit
###\rm mrdoc.toedit
###touch mrdoc.toedit

# get existing MRDOC data;  The MRDOC we make for a .src source insertion will
# have only new data

set db=`$MIDSVCS_HOME/bin/midsvcs.pl -s editing-db`

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

echo "DB is set to: $db"
echo ""
echo ""
echo "Reminder: termgroups.src must be correct for this script to work"
echo ""

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! mrdoc.umls
	SET ECHO OFF;
	SET HEADING OFF;
	SET PAGESIZE 0;
	SET FEEDBACK OFF;
	SELECT key_qualifier || '|' ||value FROM meme_properties WHERE key_qualifier IN ('TTY','RELA','ATN')
	UNION
	SELECT 'RELA|' || inverse_rel_attribute
	 FROM inverse_rel_attributes a
	 WHERE nvl(relationship_attribute,'null') IN
	  (SELECT NVL(value,'null') FROM meme_properties b
	   WHERE key = 'expanded_form'
	   AND key_qualifier = 'RELA');
EOF

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! mrdocfull.umls
	SET ECHO OFF;
	SET HEADING OFF;
	SET PAGESIZE 0;
	SET FEEDBACK OFF;
	SET LINESIZE 4000;
	
	SELECT key_qualifier || '_' ||value || '|' ||key|| '|' ||description|| '|' FROM meme_properties WHERE key_qualifier IN ('TTY','RELA','ATN')
	UNION
	 SELECT 'RELA_' || relationship_attribute|| '|rela_inverse|'
		|| inverse_rel_attribute || '|'
	 FROM inverse_rel_attributes a
	 WHERE nvl(relationship_attribute,'null') IN
	  (SELECT NVL(value,'null') FROM meme_properties b
	   WHERE key = 'expanded_form'
	   AND key_qualifier = 'RELA');
EOF

sort -u mrdoc.umls -o mrdoc.umls

# script to find elements that need MRDOC entries

# TTY - expanded form, tty_class

awk -F\| '{print("TTY|"$6)}' ../src/termgroups.src > mrdoc.tmp
###awk -F\| '{print("TTY|"$6)}' termgroups.src > mrdoc.tmp

# RELA - expanded_form, rela_inverse

awk -F\| '{print("RELA|"$5)}' ../src/relationships.src | sort -u >> mrdoc.tmp
###awk -F\| '{print("RELA|"$5)}' relationships.src | sort -u >> mrdoc.tmp

# ATN - expanded form

# these ATNs are in .src but not in RRF
#ATN|CONTEXT
#ATN|SEMANTIC_TYPE
#ATN|XMAP
#ATN|XMAPFROM
#ATN|XMAPTO
#ATN|DEFINITION
#ATN|LEXICAL_TAG
#ATN|COMPONENTHISTORY

nawk -F\| '($4!="CONTEXT" && $4!="SEMANTIC_TYPE" && $4!="XMAP" && $4!="XMAPFROM" && $4!="XMAPTO" && $4!="DEFINITION" && $4!="LEXICAL_TAG" && $4!="COMPONENTHISTORY") {print("ATN|"$4)}' ../src/attributes.src | sort -u >> mrdoc.tmp
###awk -F\| '($4!="CONTEXT" && $4!="SEMANTIC_TYPE" && $4!="XMAP" && $4!="XMAPFROM" && $4!="XMAPTO" && $4!="DEFINITION" && $4!="LEXICAL_TAG" && $4!="COMPONENTHISTORY") {print("ATN|"$4)}' attributes.src | sort -u >> mrdoc.tmp

sort -u mrdoc.tmp -o mrdoc.tmp

comm -23 mrdoc.tmp mrdoc.umls > mrdoc.tmp2


awk -F\| '($1=="TTY"){print($1"|"$2"|expanded_form|###|")}' mrdoc.tmp2 >> ../tmp/mrdoc.toedit
awk -F\| '($1=="TTY"){print($1"|"$2"|tty_class|###|")}' mrdoc.tmp2 >> ../tmp/mrdoc.toedit

awk -F\| '($1=="RELA"){print($1"|"$2"|expanded_form|###|")}' mrdoc.tmp2 >> ../tmp/mrdoc.toedit
awk -F\| '($1=="RELA"){print($1"|"$2"|rela_inverse|###|")}' mrdoc.tmp2 >> ../tmp/mrdoc.toedit
awk -F\| '($1=="RELA"){print($1"|###|expanded_form|###|")}' mrdoc.tmp2 >> ../tmp/mrdoc.toedit
awk -F\| '($1=="RELA"){print($1"|###|rela_inverse|"$2"|")}' mrdoc.tmp2 >> ../tmp/mrdoc.toedit

awk -F\| '($1=="ATN"){print($1"|"$2"|expanded_form|###|")}' mrdoc.tmp2 >> ../tmp/mrdoc.toedit

sort -t\| +0 -1 mrdocfull.umls -o mrdocfull.umls

awk -F\| '{print($1"_"$2)}' mrdoc.tmp | sort -t\| +0 -1 | join -t\| -j1 1 -j2 1 -o 2.1 2.2 2.3 2.4 - mrdocfull.umls | sed 's/_/|/' > mrdoc.toedit2

cat mrdoc.toedit2 >> ../tmp/mrdoc.toedit
###cat mrdoc.toedit2 >> mrdoc.toedit

echo ""
echo "cp mrdoc.toedit MRDOC.RRF and manually edit ###"
echo ""

\rm mrdoc.tmp mrdoc.tmp2 mrdoc.umls mrdocfull.umls mrdoc.toedit2

