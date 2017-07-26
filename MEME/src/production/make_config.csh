#!/bin/csh -f
#
# This script is used to build the configuration
# file used by MMS.  It takes three parameters
# which should match the "release.dat" file for
# this Metathesaurus and an MRD database name.
#
# Changes:
# 03/23/2006 BAC (1-AR2I4): support "build_indexes" property for output streams
# 01/09/2006 BAC (1-73ET5): rel_types.dat now built properly
# 12/22/2005 BAC (1-719SM): use binmode ":utf8" when opening file handles in perl calls
#
# Version Information
#   06/30/2005 3.2 - Remove code to build CUI|AUI list
#		     Adds IMETA field to sources list
#   09/09/2004 3.1 - Builds content view information
#   11/26/2003 3.0 - DefaultSubset info & UTF-8 flag added
#   09/26/2002 2.0 - The script was prepared for use
#                    in generating 2002AD and beyond style
#                    configuration files.
#   01/22/2002 1.0 - Ported to MRD
#
set version = "3.2"
set version_date = "06/30/2005"
set version_auth = "BAC"
set usage="Usage: $0 <release date> <release dsc> <release ver> <db> <meta_dir> <mmsys_dir>"

source $ENV_HOME/bin/env.csh

if ($#argv == 1) then
    if ($argv[1] == "-v") then
        echo "$version"
        exit 0
    else if ($argv[1] == "-version") then
        echo "Version $version, $version_date ($version_auth)"
        exit 0
    else if ($argv[1] == "-help" || $argv[1] == "--help" || $argv[1] == "-h") then
	cat <<EOF
 $usage

 This script is used to build the configuration file used
 by MetamorphoSys.  It requires that the \$META_RELEASE property
 is set to a directory containing validated MRSAB.RRF and MRRANK.RRF
 files and that the mrd_source_rank table contains current and
 correct information about the sources. The parameters
 passed in should match the data found in the release.dat file
 created for this release.

 Typically, this script is run AFTER all of the files have
 been built.  It produces three default metamorphosys properties
 files:  mmsys.a.prop, mmsys.b.prop, mmsys.c.prop.  These files
 should be copied to the config directory of the MetamorphoSys
 distribution.  The mmsys.a.prop file should also be copied to
 the config directory as mmsys.prop.sav.

EOF
        exit 0

    else
        echo "$usage"
	exit 1
    endif
endif


if ($#argv == 6) then
    set rdate="$1"
    set rdes="$2"
    set rver="$3"
    set db=$4
    set META_RELEASE=$5
    set MMSYS_DIR=$6
    set mu=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl`
else if ($#argv == 4) then
    if ($?META_RELEASE == 0) then
        echo 'ERROR: $META_RELEASE must be set'
	exit 1
    endif
    if ($?MMSYS_DIR == 0) then
        echo 'ERROR: $MMSYS_DIR must be set'
	exit 1
    endif
    set rdate="$1"
    set rdes="$2"
    set rver="$3"
    set db=$4
    set META_RELEASE=$5
    set mu=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl`
else if ($#argv == 5) then
    if ($?MMSYS_DIR == 0) then
        echo 'ERROR: $MMSYS_DIR must be set'
	exit 1
    endif
    set rdate="$1"
    set rdes="$2"
    set rver="$3"
    set db=$4
    set mu=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl`
else
    echo "ERROR: Wrong number of parameters"
    echo "ERROR: $usage"
    exit 1
endif

if (!(-e $META_RELEASE/MRSAB.RRF)) then
    echo "ERROR: $META_RELEASE/MRSAB.RRF does not exist."
    exit 1
endif

if (!(-e $META_RELEASE/MRRANK.RRF)) then
    echo "$META_RELEASE/MRRANK.RRF does not exist."
    exit 1
endif

setenv PATH "/bin:/usr/bin:/usr/local/bin"
set join=join
set sort="sort -T ."
set sed=sed
set awk=awk

echo "-----------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "-----------------------------------------------------"
echo "release date:          $1"
echo "release desc:          $2"
echo "release version:       $3"
echo "META_RELEASE:          $META_RELEASE"
echo "database:              $4"

echo ""
echo "    Note: This script assumes that the restriction level "
echo "          and suppressibility flags for the sources and "
echo "          termgroups have already been verified.  Additionally"
echo "          it assumes that the source list in MRSAB.RRF is correct"
echo "          and that MRSAB.RRF conforms to the spec laid out for 2002AD"
echo ""

echo "    Get data for sources and termgroups  ... `/bin/date`"

#
# Get language data for sources property
# Fields:
#   vsab|language|rsab|cfr|imeta
#
$PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[19]|$_[3]|$_[15]|$_[9]\n" if $_[22] eq "Y";' \
  $META_RELEASE/MRSAB.RRF | $sort -t\| -k 1,1 -u >! mrsab.dat

#
# Get data for sources property from mrd_source_rank
# Fields:
#   normalized_source|official_name|sab|source family|restriction level|root_source
#
$MEME_HOME/bin/dump_table.pl -u $mu -d $db -q \
  "select normalized_source,source_official_name,source,source_family,restriction_level,root_source from mrd_source_rank where expiration_date is null and is_current='Y'" | $sort -t\| -k 3,3 -u >! sr.dat

#
# Join the sr.dat with the mrsab.dat to provide the sources
# property with the following fields:
#   root_source|official_name|sab|source family|restriction level|language|cfr|imeta
#
$join -t\|  -1 3 -2 1 -o 2.3 1.2 1.3 1.4 1.5 2.2 2.4 2.5 sr.dat mrsab.dat >! source_info.dat 

#
# If there are any sources in sr.dat not in MRSAB.RRF
# then we have a problem
#
cut -d\| -f 1 mrsab.dat | $sort -u >! sabs
cut -d\| -f 3 source_info.dat | $sort -u >! sabs2
set ct=(`diff sabs sabs2 | wc`)
if ($ct[1] != 0) then
    echo "ERROR: MRSAB.RRF and mrd_source_rank have mismatched sources."
    diff sabs sabs2 | sed 's/^/ERROR: /'
    exit 1
endif
/bin/rm -f sabs2
/bin/rm -f sabs


#
# Get data for precedence and suppressed_termgroups properties
# rank|sab|tty|suppres
#
$sort -t\| -k 1,1 -o source_info.dat{,}
$sed 's/^.//' $META_RELEASE/MRRANK.RRF | $sed 's/\|$//' |\
  $sort -t\| -k 2,2 |\
  $join -t\| -j1 2 -j2 1 -o 1.1 2.1 1.3 1.4 - source_info.dat >! termgroup_info.dat

#
# Sort source/termgroup data
#
$sort -t\| -k 1,1 -o source_info.dat{,}
$sort -t\| -k 2,2 -o termgroup_info.dat{,}

#
# Join sources/termgroups to obtain the following fields:
#  official_name|root_source|tty|source|rank|suppressible
#  sab|tty|rank|ssab|suppressible
#
$join -t\| -1 1 -2 2 -o  1.3 2.3 2.1 1.1 2.4 \
  source_info.dat termgroup_info.dat >! join1.dat 

#
# Get data for termgroups property by stripping out suppressible
# Sort by rank (in reverse order) for precedence property
#
$awk -F\| '{print $1 "|" $2 "|" $3}' join1.dat >! termgroups.dat
$sort -t\| +2r -o join1.dat join1.dat

#
# Get rid of extra fields for precedence property
#
$awk -F\| '{print $4 "|" $2}' join1.dat >! precedence.dat

#
# Get suppressed termgroups by 
# keeping rows with suppressible='Y'
#
/bin/cat join1.dat | \
  $awk -F\| '{if($5=="Y") {print $4 "|" $2}}' >! suppr_tg.dat

#
# Get cui,sui for the suppressed terms, reject MTH atoms
#

# This query gets where all atoms in the SUI are suppressible
$MEME_HOME/bin/dump_table.pl -u $mu -d $db -q \
  "select distinct cui,sui from mrd_classes a, mrd_termgroup_rank b, \
   mrd_source_rank c where a.root_source=c.root_source and \
   b.normalized_termgroup=c.source || '/' || a.tty and a.suppressible='E' \
   and b.suppressible='N' and a.root_source != 'MTH' \
   and a.expiration_date is null and b.expiration_date is null \
   and c.expiration_date is null and is_current = 'Y' \
   minus select cui,sui from mrd_classes b where suppressible='N'" \
  >! suppr_sui.dat

echo "    Put it all together ... `/bin/date`"

#
# Get sources to remove list by finding
# rows that do not have a restriction level of 0
#
$PATH_TO_PERL -ne 'chop; split /\|/; print "$_[0]|$_[3]\n" if $_[4] ne "0";' source_info.dat >! sources_to_remove.dat 

#
# Get data for languages property from language table
# Fields: lat
#
$MEME_HOME/bin/dump_table.pl -u $mu -d $db -q \
  "select lat, language from language" | $sort >! language.dat

/bin/cat language.dat | $awk -F\| '{print $1 }' >! lat.dat 

#
# Get data for sty property from sty table
# Fields: lat
#
$MEME_HOME/bin/dump_table.pl -u $mu -d $db -q \
  "select ui, sty, stn from srsty" | $sort >! stys.dat

#
# Get data for rel property from rel table
# Fields: sab|rel_type 
#
$MEME_HOME/bin/dump_table.pl -u $mu -d $db -q \
  "select /*+ parallel(r) */ \
       distinct root_source, decode(relationship_name,'RB', \
       'RB/RN','RN','RB/RN','AQ','AQ/QB','QB', \
       'AQ/QB', 'PAR', 'PAR/CHD', 'CHD', 'PAR/CHD', \
       relationship_name) as rel \
   from mrd_relationships r where relationship_level='S' \
    and expiration_date is null \
   union select 'MSH', 'AQ/QB' from dual \
   union select 'MTH','RB/RN' from dual \
   union select 'MTH','RO' from dual \
   union select distinct root_source,'PAR/CHD' \
         from mrd_source_rank \
         where expiration_date is null and is_current='Y' \
	 and context_type is not null \
   union select distinct root_source,'SIB' \
         from mrd_source_rank where is_current='Y' \
         and expiration_date is null and context_type is not null \
         and context_type not like '%NOSIB%'" | $sort >! rel_types.dat

#
# Get data for attributes property from attributes table
# Fields: sab|rel_type 
#
$MEME_HOME/bin/dump_table.pl -u $mu -d $db -q \
"select /*+ parallel(a) */  \
 distinct root_source, attribute_name \
 from mrd_attributes a \
 where expiration_date is null \
   and attribute_name not in \
    ('DEFINITION','ATX_REL', 'SEMANTIC_TYPE','MRLO', \
     'COC','HPC','HDA','NON_HUMAN','LEXICAL_TAG') \
 union select 'MSH','LT' from dual \
 union select 'MTH','LT' from dual \
 union select 'MTH','NH' from dual \
 union select 'MTH','DA' from dual \
 union select 'MTH','MR' from dual \
 union select 'MTH','AM' from dual \
 union select 'MTH','ST' from dual \
 union select /*+ parallel(c) */ \
       distinct 'NLM-MED','MED'||to_char(publication_date,'yyyy') \
       from coc_headings c \
       where source = 'NLM' " >! att_types.dat 

#
# Build RELA to SNOMEDCT RELATIONSHIPTYPE map
#
perl -ne 'chop; split /\|/; print "$_[3]|$_[1]\n" if $_[2] eq "snomedct_rela_mapping"' $META_RELEASE/MRDOC.RRF >! snomed_rela_map.dat 

#
# Get data for mrcolsfiles.dat
#
$MEME_HOME/bin/dump_table.pl -u $mu -d $db -q \
  "select * from mrd_properties where expiration_date is null and key_qualifier in ('MRCOLS', 'MRFILES')"  >! mrpluscolsfiles.dat

#
# Write all prop files: mmsys.{a,b,c}.prop
#
foreach f (a b) 

    #
    # Write all data to configuration file
    #
    echo "    Write configuration file ... `/bin/date`"
    cat <<EOF >! mmsys.$f.prop
# Configuration Properties File
# `date`
#
# Directories
# 
source_paths=../
subsetname=../
current_config_file=config/mmsys.$f.prop

# 
# List of sources:root_sab|official_name|sab|source_family|restriction_level|cfr|imeta
# 
EOF

    $PATH_TO_PERL -e ' \
        open(PREC, "source_info.dat"); \
        binmode(PREC,":utf8"); \
        $source_line = "sources="; \
        while ($line = <PREC>) { \
          chop($line); ($ssab,$son,$sab) = split /\|/, $line; \
        print STDERR "ERROR: $sab has null official name in mrd_source_rank\n" unless $son; \
        $source_line .= "$line;"; } \
        chop($source_line); \
        print "$source_line\n"; ' >> mmsys.$f.prop

    cat <<EOF >> mmsys.$f.prop

# 
# List of sources to remove
# fields are: root_sab|source_family
# 
EOF

    $PATH_TO_PERL -e '\
	open(SAB, "sources_to_remove.dat"); \
	binmode(SAB,":utf8"); \
	$remove_line = "gov.nih.nlm.mms.filters.SourcesToRemoveFilter.selected_sources="; \
	while ($line = <SAB>) { \
	  if ($ARGV[0] eq "b" && ($line =~ /SNOMEDCT/ || $line =~ /MTHSCT/)) { next; } \
	  chop($line); $remove_line .= "$line;"; } \
	$remove_line =~ s/;$//; \
	print "$remove_line\n";' $f >> mmsys.$f.prop

cat <<EOF >> mmsys.$f.prop

# 
# Termgroups 
# fields are: official_name|root_sab|tty|sab|rank
# 
EOF

    $PATH_TO_PERL -e '\
	open(PREC, "termgroups.dat"); \
	binmode(PREC,":utf8"); \
	$prec_line = "termgroups="; \
	while ($line = <PREC>) { \
	    chop($line); $prec_line .= "$line;"; } \
	$prec_line =~ s/;$//; \
	print "$prec_line\n";' >> mmsys.$f.prop

    cat <<EOF >> mmsys.$f.prop

# 
# Precedence 
# fields are: root_sab|tty
# 
EOF

    $PATH_TO_PERL -e '\
	open(PREC, "precedence.dat"); \
	binmode(PREC,":utf8"); \
	$prec_line = "gov.nih.nlm.mms.filters.PrecedenceFilter.precedence="; \
	while ($line = <PREC>) { \
	    chop($line); $prec_line .= "$line;"; } \
	$prec_line =~ s/;$//; \
	print "$prec_line\n";' >> mmsys.$f.prop

    cat <<EOF >> mmsys.$f.prop

# 
# Termgroups that are considered suppressible
# fields are: root_sab|tty
# 
EOF

    $PATH_TO_PERL -e ' \
	open(PREC, "suppr_tg.dat"); \
	binmode(PREC,":utf8"); \
	$suptgs_line = "gov.nih.nlm.mms.filters.SuppressibleTermgroupsFilter.suppressed_termgroups="; \
	while ($line = <PREC>) { \
	    chop($line); $suptgs_line .= "$line;"; } \
	$suptgs_line =~ s/;$//; \
	print "$suptgs_line\n";' >> mmsys.$f.prop


    $PATH_TO_PERL -e '\
	open(PREC, "suppr_sui.dat"); \
	binmode(PREC,":utf8"); \
	$supsui_line = "suppressed_suis="; \
	while ($line = <PREC>) { \
	    chop($line); $supsui_line .= "$line;"; } \
	$supsui_line =~ s/;$//; \
	print "$supsui_line\n";' >> mmsys.$f.prop

    cat <<EOF >> mmsys.$f.prop

# 
# Languages
# fields are: lat|language
# 
EOF

    $PATH_TO_PERL -e '\
	open(PREC, "language.dat"); \
	binmode(PREC,":utf8"); \
	$lats = "languages="; \
	while ($line = <PREC>) { \
	    chop($line); $lats .= "$line;"; } \
	$lats =~ s/;$//; \
	print "$lats\n";' >> mmsys.$f.prop

	$PATH_TO_PERL -e '\
	open(PREC, "lat.dat"); \
	binmode(PREC,":utf8"); \
	$lprop="gov.nih.nlm.mms.filters.LanguagesToRemoveFilter.selected_languages=";  \
	while ($line = <PREC>) { \
	    chop($line); $lprop .= "$line;" unless $line eq "ENG";  } \
	$lprop =~ s/;$//; \
	print "$lprop\n";' >> mmsys.$f.prop

    cat <<EOF >> mmsys.$f.prop

#
# Suppressible values
#
suppress=SUPPRESS|Y|expanded_form|Suppressible due to SAB,TTY|;SUPPRESS|N|expanded_form|Not suppressible|;SUPPRESS|O|expanded_form|Obsolete, SAB,TTY may be independently suppressible|;SUPPRESS|E|expanded_form|Suppressible due to editor decision|

# 
# Semantic Types
# fields are: TUI|STY|STN
# 
EOF

    $PATH_TO_PERL -e '\
	open(PREC, "stys.dat"); \
	binmode(PREC,":utf8"); \
	$stys="gov.nih.nlm.mms.filters.SemanticTypesToRemoveFilter.selected_semantic_types="; \
	$lprop = "semantic_types="; \
	while ($line = <PREC>) { \
	    chop($line);  \
	$lprop .= "$line;"  } \
	$stys =~ s/;$//; $lprop =~ s/;$//; \
	print "$stys\n$lprop\n";' >> mmsys.$f.prop

    cat <<EOF >> mmsys.$f.prop

# 
# Relationship Types
# fields are: SAB|TYPE
#
gov.nih.nlm.mms.filters.RelationshipTypesToRemoveFilter.selected_relationship_types=
gov.nih.nlm.mms.filters.RelationshipTypesToRemoveFilter.remove_selected_rels=true
EOF

#$PATH_TO_PERL -e '\
#  open(PREC, "rels.dat"); \
#  binmode(PREC,":utf8"); \
#  $lprop="gov.nih.nlm.mms.filters.RelationshipTypesToRemoveFilter.relationship_types_to_remove="; \
#  $rels = "gov.nih.nlm.mms.filters.RelationshipTypesToRemoveFilter.relationship_types="; \
#  while ($line = <PREC>) { \
#    chop($line); $rels .= "$line;"; } \
#  $rels =~ s/;$//; $lprop =~ s/;$//; \
#  print "$rels\n$lprop\n";' >> mmsys.$f.prop

    cat <<EOF >> mmsys.$f.prop

# 
# Attributes Types
# fields are: SAB|ATN
# 
gov.nih.nlm.mms.filters.AttributeTypesToRemoveFilter.selected_attribute_types=
gov.nih.nlm.mms.filters.AttributeTypesToRemoveFilter.remove_selected_attributes=true
EOF

#$PATH_TO_PERL -e '\
#  open(PREC, "atts.dat"); \
#  binmode(PREC,":utf8"); \
#
#  $atts = "gov.nih.nlm.mms.filters.AttributeTypesToRemoveFilter.attribute_types="; \
#  while ($line = <PREC>) { \
#    chop($line); $atts .= "$line;"; } \
#  $atts =~ s/;$//; $lprop =~ s/;$//; \
#  print "$atts\n$lprop\n";' >> mmsys.$f.prop


    cat <<EOF >> mmsys.$f.prop

#
# Default Subset Information
#
default_subset=$f
default_subsets=a;b
default_subset_name_a=Level 0
default_subset_name_b=Level 0 + SNOMEDCT
default_subset_description_a=Exclude all non-level 0 sources 
default_subset_description_b=Exclude all non-level 0 sources except SNOMEDCT

#
# Content View Information
#
gov.nih.nlm.mms.filters.ContentViewFilter.selected_views=
EOF

foreach id (`grep CVF_ID $META_RELEASE/MRDOC.RRF | /bin/cut -d\| -f 2`)

    set name=`grep CVF_NAME $META_RELEASE/MRDOC.RRF | grep $id | /bin/cut -d\| -f 2`
    set description=`grep CVF_DESCRIPTION $META_RELEASE/MRDOC.RRF | grep $id | /bin/cut -d\| -f 2`
    set code=`grep CVF_CODE $META_RELEASE/MRDOC.RRF | grep $id | /bin/cut -d\| -f 2`
    
    cat <<EOF >> mmsys.$f.prop
gov.nih.nlm.mms.filters.ContentViewFilter.name_$id=$name
gov.nih.nlm.mms.filters.ContentViewFilter.description_$id=$description
gov.nih.nlm.mms.filters.ContentViewFilter.code_$id=$code

EOF
end


    cat <<EOF >> mmsys.$f.prop

# 
# Release Information
# 
release_date=$rdate
release_description=$rdes
release_version=$rver

#
# Input Stream Configuration
#
mmsys_input_stream=gov.nih.nlm.mms.NLMFileMetamorphoSysInputStream
gov.nih.nlm.mms.RichMRMetamorphoSysInputStream.enable_efficient=true
gov.nih.nlm.mms.NLMFileMetamorphoSysInputStream.enable_efficient=true

#
# Output Steam Configuration
#
mmsys_output_stream=gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream

gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream.write_oracle=false
gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream.write_mysql=false
gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream.versioned_output=false
gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream.max_field_length=4000
gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream.truncate=false
gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream.remove_utf8=false
gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream.remove_mth_only=false
gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream.calculate_md5s=false
gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream.add_unicode_bom=false
gov.nih.nlm.mms.RichMRMetamorphoSysOutputStream.build_indexes=true

gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.write_oracle=false
gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.write_mysql=false
gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.versioned_output=false
gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.max_field_length=4000
gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.truncate=false
gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.remove_utf8=false
gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.remove_mth_only=false
gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.calculate_md5s=false
gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.add_unicode_bom=false
gov.nih.nlm.mms.OriginalMRMetamorphoSysOutputStream.build_indexes=true

# 
# Advanced Options Properties
# 
gov.nih.nlm.mms.filters.SourcesToRemoveFilter.enforce_family_selection=true
gov.nih.nlm.mms.filters.SourcesToRemoveFilter.enforce_dep_source_selection=true
gov.nih.nlm.mms.filters.SourcesToRemoveFilter.remove_selected_sources=true
automatic_selection=false
#gov.nih.nlm.mms.filters.SuppressibleTermgroupsFilter.suppress_preferred_atoms=false
gov.nih.nlm.mms.filters.SuppressibleTermgroupsFilter.remove_suppressible_data=false
gov.nih.nlm.mms.filters.SourcesToRemoveFilter.ip_associations=

gov.nih.nlm.mms.filters.PrecedenceFilter.cut_mode=false
gov.nih.nlm.mms.filters.SemanticTypesToRemoveFilter.any_sty=true
gov.nih.nlm.mms.filters.SemanticTypesToRemoveFilter.remove_selected_stys=true
gov.nih.nlm.mms.filters.RemoveByTermgroupFilter.remove_selected_termgroups=true


#
# Valid/Active filters
#
valid_filters=gov.nih.nlm.mms.filters.SourcesToRemoveFilter;gov.nih.nlm.mms.filters.PrecedenceFilter;gov.nih.nlm.mms.filters.SuppressibleTermgroupsFilter;gov.nih.nlm.mms.filters.AttributeTypesToRemoveFilter;gov.nih.nlm.mms.filters.LanguagesToRemoveFilter;gov.nih.nlm.mms.filters.RelationshipTypesToRemoveFilter;gov.nih.nlm.mms.filters.SemanticTypesToRemoveFilter;gov.nih.nlm.mms.filters.RxNormFilter;gov.nih.nlm.mms.filters.ContentViewFilter;gov.nih.nlm.mms.filters.RemoveByTermgroupFilter

EOF

cat << EOF >> mmsys.$f.prop

active_filters=gov.nih.nlm.mms.filters.SourcesToRemoveFilter;gov.nih.nlm.mms.filters.PrecedenceFilter;gov.nih.nlm.mms.filters.SuppressibleTermgroupsFilter
EOF

end
echo "    Copying mmsys.*.prop files .... `/bin/date`"
/bin/cp -f mmsys.a.prop mmsys.prop.sav
/bin/mv mmsys.*.prop $MMSYS_DIR/config/
/bin/mv mmsys.prop.sav $MMSYS_DIR/config/
#echo "    Copying mrcolsfiles files .... `/bin/date`"
#/bin/mv mrcolsfiles.dat $MMSYS_DIR/config/
echo "    Copying mrpluscolsfiles .... `/bin/date`"
/bin/mv mrpluscolsfiles.dat $MMSYS_DIR/config
echo "    Copuing types ... `/bin/date`"
/bin/mv att_types.dat $MMSYS_DIR/config
/bin/mv rel_types.dat  $MMSYS_DIR/config
/bin/mv snomed_rela_map.dat $MMSYS_DIR/config
echo "    Cleanup ... `/bin/date`"
/bin/cp -f mmsys.a.prop mmsys.prop.sav
/bin/rm -f termgroups.dat precedence.dat join1.dat termgroup_info.dat sr.dat
/bin/rm -f mrsab.dat source_info.dat stys.dat suppr_tg.dat 
/bin/rm -f join1.dat sources_to_remove.dat language.dat lat.dat
/bin/rm -f suppr_sui.dat

echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
