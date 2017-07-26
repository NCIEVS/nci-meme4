#!/bin/tcsh -f
#
# File          verifyDefaultPreferredAtoms.csh
# Written by    Brian Carlsen (4/2012)
#
# This script verifies that default preferred atom mappings are correct.
# Call with -help for more info
#
# 04/27/2012 (4.1.0): First version.
#
set release=4
set version=1.0
set authority="BAC"
set date="04/27/2012"

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

if ($?ORACLE_HOME != 1) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

#
# Parse arguments
#
set usage = "Usage: $0 <database>"
if ($#argv == 0) then
    echo "Error: Bad argument"
    echo "$usage"
    exit 1
endif

set i=1
while ($i <= $#argv)
    switch ($argv[$i])
        case '-*help':
            cat <<EOF
 This script has the following usage:
   $usage

   This script double-checks the atom_id mappings in the relationships,
   attributes, and context_relationships tables.  It reports any atom
   id not properly mapped for ROOT_SOURCE_CUI, ROOT_SOURCE_DUI, and
   CODE_ROOT_SOURCE cases.  It only checks releasable data.

   This script should be run after each insertion and as part of pre-production.

EOF
            exit 0
        case '-version':
            echo "Release ${release}: version $version, $date ($authority)"
            exit 0
        case '-v':
            echo "$version"
            exit 0
        default :
            set arg_count=1
            set all_args=`expr $i + $arg_count - 1`
            if ($all_args != $#argv) then
                echo "Error: Incorrect number of arguments: $all_args, $#argv"
                echo "$usage"
                exit 1
            endif
            set db=$argv[$i]
            set i=`expr $i + 1`
    endsw
    set i=`expr $i + 1`
end

if ($?db != 1) then
    echo "$usage"
    exit 1
endif

set mu=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

echo "--------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------------"
echo "database:       $db"
echo ""

#
# Tables to check (and id suffixes)
#
set x = (relationships _1 relationships _2 attributes "" context_relationships _1 context_relationships _2)
set i = 1
while (1)
    # bail when finished
    if ($i >= $#x) break;
    @ i1 = $i + 1

    # SG_TYPEs to check
    foreach stype (ROOT_SOURCE_CUI ROOT_SOURCE_DUI CODE_ROOT_SOURCE)

    echo "PROCESSING $x[$i] $x[$i1] $stype ...`/bin/date`"
    $ORACLE_HOME/bin/sqlplus $mu@$db << EOF >&! /tmp/t.$$.log
        WHENEVER SQLERROR EXIT -2
declare
  tn varchar2(100);
  ct number;
begin
  meme_utility.drop_it('table','t1_$$');
  execute immediate 'create table t1_$$ as select atom_id$x[$i1], sg_id$x[$i1], sg_type$x[$i1], sg_qualifier$x[$i1] from $x[$i]
       where tobereleased in (''Y'',''y'') and sg_type$x[$i1] = ''$stype''';
  tn := meme_source_processing.create_rank_table('$stype','t1_$$','$x[$i1]');
  ct := meme_utility.exec_select('select count(*) from (select atom_id, sg_id, sg_qualifier from ' || tn ||
        ' a where exists (select 1 from $x[$i] b where sg_type$x[$i1]=''$stype'' and a.atom_id != b.atom_id$x[$i1] and a.sg_id=b.sg_id$x[$i1] and a.sg_qualifier=b.sg_qualifier$x[$i1] and b.tobereleased in (''Y'',''y'') ) )' );
  if ct > 0 then
        meme_utility.drop_it('table','t1_$$');
        execute immediate 'create table t1_$$ as select atom_id, sg_id, sg_qualifier from ' || tn ||
         ' a where exists (select 1 from $x[$i] b where sg_type$x[$i1]=''$stype'' and a.atom_id != b.atom_id$x[$i1] and a.sg_id=b.sg_id$x[$i1] and a.sg_qualifier=b.sg_qualifier$x[$i1] and b.tobereleased in (''Y'',''y'') )' ;
        meme_utility.drop_it('table',tn);
        RAISE_APPLICATION_ERROR(-1,'error');
  END if;
  meme_utility.drop_it('table','t1_$$');
  meme_utility.drop_it('table',tn);
END;
/
EOF
    if ($status != 0) then
        #cat /tmp/t.$$.log
        echo "ERROR in mapping for $x[$i] atom_id$x[$i1]";
        $MEME_HOME/bin/dump_mid.pl -t t1_$$ $db . >> /dev/null
        cat t1_$$.dat | sed 's/^/      /' | head
        /bin/rm -f t1_$$.*
    endif
end

    @ i = $i + 2
end

echo "--------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "--------------------------------------------------------------"
