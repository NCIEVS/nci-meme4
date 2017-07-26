#!/bin/csh -f
#
# file:    editing.csh
# author:  Brian Carlsen (2/27/98)
#
# takes a database and a number of days ago, generates report of editor
# activitiy from today - #days ago to today
# OR
# takes a database, start_date, end_date, and worklist and
# generates a report of activity on that worklist between
# the date range.  "on that worklist" means orig_concept_id 

#
# Version information
# 02/04/2000  (3.0.5)  ORACLE_HOME must be set.
# 2/7/2000    3.2.0    Released
# 3/29/2000   3.2.1    uses get_oracle_password does not take a user
# 8/09/2000   3.3.0    Released
#
set release="3"
set version="3.0"
set version_authority="BAC"
set version_date="08/09/2000"

source $ENV_HOME/bin/env.csh

if ($?ORACLE_HOME == 0) then
    echo "\$ORACLE_HOME must be set."
    exit 1
endif

if ($#argv > 0) then
    if ("-version" == $argv[1]) then
	echo "Release ${release}: version $version, $version_date ($version_authority)"
	exit 0
    else if ("$argv[1]" == "-v") then
	echo "$version"
	exit 0
    else if ("$argv[1]" == "--help" || "$argv[1]" == "-help") then
    cat <<EOF
 This script has the following usage:
   Usage: editing.csh database days_ago
     or   editing.csh database start_date end_date worklist_name

 If 2 arguments are passed the script produces a report of 
 editing activity since the specified number of days ago.  
 Following are the arguments:
     database:    database name
     days_ago:    A number of days ago to calculate from
 
 If 4 arguments are passed the script produces a report of 
 editing on a particular worklist from a start date which is 
 usually the worklist create date.  Following are the arguments:
     database:    database name
     start_date:  a date to start looking from (worklist create)
     end_date:    a date to stop looking from (stamp_date or 'now')
     worklist:    a worklist name.

EOF
    exit 0
    endif
endif

if ($#argv == 2) then
    set database=$1
    set days_ago=$2
    set table_source_clause=""
    set table_target_clause=""
    set create_table=""
    set date_clause="timestamp >= trunc(sysdate,'ddd') - $days_ago"
    set drop_worklists=""
    set table_name=""
else if ($#argv == 4) then
    set database=$1
    set start_date="$2"
    set end_date="$3"
    set table_name=$4
    set create_table="create table worklist_concepts_$$ as select distinct orig_concept_id as concept_id from $table_name;";
    set drop_worklists="execute meme_utility.drop_it('table','worklist_concepts_$$');"
    set table_source_clause="and source_id IN (select concept_id FROM worklist_concepts_$$)";
    set table_target_clause="and target_id IN (select concept_id FROM worklist_concepts_$$)";
    set date_clause="timestamp >= '$start_date'"
    if ("$end_date" != "now") then
        set date_clause="$date_clause and timestamp < '$end_date'"
    endif
else
    echo "Usage: editing.csh database days_ago"
    echo "  or   editing.csh database start_date end_date worklist_name"
    exit 1
endif

# get subset of actions in which we are interested
#cat <<EOF
set user=`${MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $database`;
$ORACLE_HOME/bin/sqlplus $user@$database <<EOF
set autocommit on
set pages 5000
set lines 5000
set colsep |

$create_table

/* Get a table of all molecule_ids (w/concept_id) since date */
execute meme_utility.drop_it('table','t_ma_$$'); 
create table t_ma_$$ as
select molecule_id, molecular_action, undone, 
source_id as concept_id, authority
from molecular_actions where $date_clause $table_source_clause
union
select molecule_id, molecular_action, undone, target_id, authority
from molecular_actions where $date_clause
and target_id != 0 $table_target_clause; 

/* For optimization reasons, we must select above, and then delete the undone='Y' cases */
delete from t_ma_$$ where undone='Y';

/* concept approvals */
execute meme_utility.drop_it('table','approve_count_$$'); 
create table approve_count_$$ (authority, num) as
select m.authority, count(distinct molecule_id) from  t_ma_$$ m 
where molecular_action = 'MOLECULAR_CONCEPT_APPROVAL'
group by m.authority;

/* Insert relationships */
execute meme_utility.drop_it('table','i_rels_count_$$'); 
create table i_rels_count_$$  (authority, num) as
select m.authority, count(distinct m.molecule_id) 
from  t_ma_$$ m, atomic_actions aa 
where m.molecule_id=aa.molecule_id 
  and molecular_action = 'MOLECULAR_INSERT' and action='I' 
  and table_name='R'
group by m.authority;

/* Insert semantic type */
execute meme_utility.drop_it('table','i_sty_count_$$'); 
create table i_sty_count_$$ (authority, num) as
select m.authority, count(distinct m.molecule_id) 
from t_ma_$$ m, atomic_actions aa, attributes a 
WHERE m.molecule_id=aa.molecule_id 
and aa.row_id = a.attribute_id and m.molecular_action like '%INSERT%' 
AND aa.action='I' and a.attribute_name='SEMANTIC_TYPE' AND aa.table_name='A'
group by m.authority;

/* Total counts */
execute meme_utility.drop_it('table','total_count_$$'); 
create table total_count_$$ (authority, num) as
select authority, count(distinct molecule_id) from t_ma_$$ 
group by authority;

/* Total counts of concepts touched */
execute meme_utility.drop_it('table','concept_count_$$'); 
create table concept_count_$$ (authority, num) as
select authority,  count (distinct concept_id) from t_ma_$$ group by authority;

/* splits */
execute meme_utility.drop_it('table','split_count_$$'); 
create table split_count_$$ (authority, num) as
select m.authority, count(distinct molecule_id) from t_ma_$$ m 
where molecular_action = 'MOLECULAR_SPLIT'
group by m.authority;

/* merges */
execute meme_utility.drop_it('table','merge_count_$$'); 
create table merge_count_$$ (authority, num) as
select m.authority, count(distinct molecule_id) from  t_ma_$$ m 
where molecular_action = 'MOLECULAR_MERGE'
group by m.authority;

/* Aggregate */
execute meme_utility.drop_it('table','aggregate_$$');
create table aggregate_$$ (authority,actions ,concepts ,splits , merges ,approves , rels,  stys ) as 
select distinct authority, 0, 0, 0, 0, 0, 0, 0 from t_ma_$$;

update aggregate_$$ a 
set approves = (select b.num from approve_count_$$ b
	        where a.authority = b.authority)
where exists (select 'X' from approve_count_$$ b 
where a.authority = b.authority);

update aggregate_$$ a 
    set rels = (select b.num from i_rels_count_$$ b
		where a.authority = b.authority)
where exists (select 'X' from i_rels_count_$$ b
where a.authority = b.authority);


update aggregate_$$ a 
    set stys = (select b.num from i_sty_count_$$ b 
		where a.authority = b.authority)
where exists (select 'X' from i_sty_count_$$ b 
where a.authority = b.authority);


update aggregate_$$ a 
    set actions = (select b.num from total_count_$$ b
		   where a.authority = b.authority)
where exists (select 'X' from total_count_$$ b
where a.authority = b.authority);


update aggregate_$$ a 
    set concepts = (select b.num from concept_count_$$ b
		    where a.authority = b.authority)
where exists (select 'X' from concept_count_$$ b
where a.authority = b.authority);


update aggregate_$$ a 
    set splits = (select b.num from split_count_$$ b
		  where a.authority = b.authority)
where exists (select 'X' from split_count_$$ b
where a.authority = b.authority);


update aggregate_$$ a 
    set merges = (select b.num from merge_count_$$ b
		  where a.authority = b.authority)
where exists (select 'X' from merge_count_$$ b
where a.authority = b.authority);

select * from aggregate_$$;

DROP TABLE aggregate_$$;
DROP TABLE total_count_$$;
DROP TABLE concept_count_$$;
DROP TABLE split_count_$$;
DROP TABLE merge_count_$$;
DROP TABLE approve_count_$$;
DROP TABLE i_rels_count_$$;
DROP TABLE i_sty_count_$$;
DROP TABLE t_ma_$$; 
$drop_worklists

EOF
