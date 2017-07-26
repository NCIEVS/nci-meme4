#!/bin/csh -f
#
# This script initializes or updates the data in coc_headings/coc_subheadings
#
# File:    process_medline_data.csh
# Author:  Tun Tun Naing
#
# Usage:
#     process_medline_data.csh [-mrd] [ -i | -u ] <database>
#
# Options:
#     <database>: Required
#     -u        : Update
#     -i        : Initialize
#     [-mrd]    : Process on MRD
#     -v version: Version information
#     -h help:    On-line help
#
# Version Info
# 02/02/2006: TTN (1-76SUZ): bug fix for update
#             truncate source_coc_headings_todelete if data file is empty
# 06/17/2004: Removed bogus /* APPEND */ hints, better sqlldr params
#             Performance improvement to "source_coc_headings_todelete"
#             handling
# 12/13/2004: no more report table change
# 11/19/2004: Released
# 10/08/2002 Oracle 8.1.6 has an error loading an empty coc_subheadings.dat
#            file, so we check to see if it is empty before loading. Same
#            with coc_headings.
# 10/16/2001 First version created
set release="4"
set version="3.1"
set version_authority="BAC"
set version_date="06/17/2004"

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

#
# Check required environment variables
#
if ($?ORACLE_HOME == 0) then
    echo "\$ORACLE_HOME must be set"
    exit 1
endif

#
# Set variables
#

set CAT=/bin/cat
set SORT=/bin/sort
#
# Parse arguments
#
if ($#argv == 0) then
    echo "Error: Bad argument"
    echo "Usage: $0 [-mrd] [ -i | -u ] <database>"
    exit 1
endif
set i=1
set db_mode=MID

while ($i <= $#argv)
    switch ($argv[$i])
        case '-*help':
            echo "Usage: $0 [-mrd] [ -i | -u ] <database>"
            echo "Version $version, $version_date ($version_authority)"
            exit 0
        case '-v':
            echo $version
            exit 0
        case '-version':
            echo "Version $version, $version_date ($version_authority)"
            exit 0
	case '-u':
	    set mode=update
	    set command=append
	    breaksw
	case '-i':
	    set mode=init
	    set command=truncate
	    breaksw
	case '-mrd':
	    set db_mode=MRD
	    breaksw
        default:
            breaksw
    endsw
    set i=`expr $i + 1`
end

#
# Check arguments
#
if($?mode == 0) then
    echo "Either the -u or -i option must be specified."
    exit 1
endif

if ($#argv == 2 && "$argv[1]" =~ -[iu]) then
    set db=$2
else if ($#argv == 3 && "$argv[1]" == "-mrd" &&  "$argv[2]" =~ -[iu] ) then
    set db=$3
else
    echo "Error: Bad argument"
    echo "Usage: $0 [-mrd] [ -i | -u ] <database>"
    exit 1
endif

#
# Begin program logic
#
echo "----------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "----------------------------------------------"
echo "ORACLE_HOME:    $ORACLE_HOME"
echo "DATABASE:       $db"
echo "mode:           $mode"
echo ""
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
if ($mode == "update") then

#
# Create control file
#
$CAT <<EOF >! coc_headings_todelete.ctl
options (rows=10000, bindsize=10000000, readsize=10000000, silent=feedback)
load data
infile 'coc_headings_todelete' "str X'7c0a'"
badfile 'coc_headings_todelete.bad'
discardfile 'coc_headings_todelete.dsc'
truncate
into table source_coc_headings_todelete
fields terminated by '|'
trailing nullcols
(
citation_set_id         INTEGER EXTERNAL
)
EOF

echo "    Loading coc_headings_todelete.ctl"
set ct=(`wc -l coc_headings_todelete.dat`)
if ($ct[1] != 0) then
    $ORACLE_HOME/bin/sqlldr $user@$db control="coc_headings_todelete.ctl"
    if ($status != 0 || `/bin/fgrep -c 'ORA-' coc_headings_todelete.log` > 0) then
	echo "    ORA- : Error loading coc_headings_todelete.ctl"
	exit 1
    endif
else
echo "    Truncating the source_coc_headings_todelete"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2;
    TRUNCATE TABLE SOURCE_COC_HEADINGS_TODELETE;
/
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "    ORA- : Error truncating the table source_coc_headings_todelete"
    exit 1
    endif
endif

echo "    Deleting rows from coc_headings"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2;

    DELETE FROM coc_headings WHERE citation_set_id IN
    (SELECT citation_set_id FROM source_coc_headings_todelete)
    AND source = 'NLM-MED';

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "    ORA- : Error deleting rows from coc_headings"
    exit 1
endif

echo "    Deleting rows from coc_subheadings"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2;

    DELETE FROM coc_subheadings WHERE citation_set_id IN
    (SELECT citation_set_id FROM source_coc_headings_todelete);

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "    ORA- : Error deleting rows from coc_subheadings"
    exit 1
endif

endif


#
# Create control file
#

$CAT <<EOF >! coc_headings.ctl
options (rows=10000, bindsize=10000000, readsize=10000000, silent=feedback)
load data
infile 'coc_headings'"str X'7c0a'"
badfile 'coc_headings.bad'
discardfile 'coc_headings.dsc'
truncate
into table source_coc_headings
fields terminated by '|'
trailing nullcols
(
citation_set_id         INTEGER EXTERNAL,
publication_date        DATE "DD-mon-YYYY",
heading_id              INTEGER EXTERNAL,
major_topic             CHAR,
subheading_set_id       INTEGER EXTERNAL,
source                  CHAR,
coc_type                CHAR
)
EOF

echo "    Loading coc_headings.ctl"
set ct=(`wc -l coc_headings.dat`)
if ($ct[1] != 0) then
    $ORACLE_HOME/bin/sqlldr $user@$db control="coc_headings.ctl"
    if ($status != 0 || `/bin/fgrep -c 'ORA-' coc_headings.log` > 0) then
	echo "    ORA- : Error loading coc_headings.ctl"
	exit 1
    endif
else
echo "    Truncating the source_coc_headings"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2;
    TRUNCATE TABLE SOURCE_COC_HEADINGS;
/
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "    ORA- : Error truncating the table source_coc_headings"
    exit 1
    endif
endif

echo "    Inserting rows into coc_headings"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2;



    BEGIN
    IF '$mode' = 'init' THEN
	INSERT INTO source_coc_headings
	    SELECT * FROM coc_headings WHERE source != 'NLM-MED';
	MEME_SYSTEM.truncate('coc_headings');
	MEME_SYSTEM.drop_indexes('coc_headings');
    END IF;
    END;
/

    INSERT INTO coc_headings
    SELECT * FROM source_coc_headings;

    BEGIN
	IF '$mode' = 'init' THEN
	    MEME_SYSTEM.reindex('coc_headings');
        MEME_SYSTEM.ANALYZE('coc_headings');
	END IF;
    END;
/
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "    ORA- : Error inserting rows into coc_headings"
    exit 1
endif


#
# Create control file
#
$CAT <<EOF >! coc_subheadings.ctl
options (rows=10000, bindsize=10000000, readsize=10000000, silent=feedback)
load data
infile 'coc_subheadings'"str X'7c0a'"
badfile 'coc_subheadings.bad'
discardfile 'coc_subheadings.dsc'
truncate
into table source_coc_subheadings
fields terminated by '|'
trailing nullcols
(
citation_set_id         INTEGER EXTERNAL,
subheading_set_id       INTEGER EXTERNAL,
subheading_qa           CHAR,
subheading_major_topic  CHAR
)
EOF

echo "    Loading coc_subheadings.ctl"
$SORT -u -o coc_subheadings.dat{,}
set ct=(`wc -l coc_subheadings.dat`)
if ($ct[1] != 0) then
    $ORACLE_HOME/bin/sqlldr $user@$db control="coc_subheadings.ctl"
    if ($status != 0 || `/bin/fgrep -c 'ORA-' coc_subheadings.log` > 0) then
	echo "    ORA- : Error loading coc_subheadings.ctl"
	exit 1
    endif
else
echo "    Truncating the source_coc_subheadings"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2;
    TRUNCATE TABLE SOURCE_COC_SUBHEADINGS;
/
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "    ORA- : Error truncating the table source_coc_hsubeadings"
    exit 1
    endif
endif

echo "    Inserting rows into coc_subheadings"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2;

    BEGIN
    IF '$mode' = 'init' THEN
	MEME_SYSTEM.truncate('coc_subheadings');
	MEME_SYSTEM.drop_indexes('coc_subheadings');
    END IF;
    END;
/
    INSERT INTO coc_subheadings
    SELECT * FROM source_coc_subheadings;

    BEGIN
	IF '$mode' = 'init' THEN
	    MEME_SYSTEM.reindex('coc_subheadings');
        MEME_SYSTEM.ANALYZE('coc_subheadings');
	END IF;
    END;
/
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "    ORA- : Error inserting rows into coc_subheadings"
    exit 1
endif

if ($db_mode == 'MRD' && $mode == "update") then
echo "    Generating states for update"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2;

    exec MRD_OPERATIONS.generate_auxiliary_data_states('source_coc_headings');
    exec MRD_OPERATIONS.generate_auxiliary_data_states('source_coc_subheadings');

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "    ORA- : Error generating states for update"
    exit 1
endif

else if ($db_mode == 'MRD') then

echo "    Generating states for insert"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2;
    exec MEME_SYSTEM.drop_indexes('mrd_coc_headings');
    exec MRD_OPERATIONS.generate_auxiliary_data_states('coc_headings');
    exec MEME_SYSTEM.reindex('mrd_coc_headings','N',' ');
    exec MEME_SYSTEM.drop_indexes('mrd_coc_subheadings');
    exec MRD_OPERATIONS.generate_auxiliary_data_states('coc_subheadings');
    exec MEME_SYSTEM.reindex('mrd_coc_subheadings','N',' ');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "    ORA- : Error generating states for insert"
    exit 1
endif

endif

echo "    Cleaning temp files..."
\rm -f {coc_headings_todelete,coc_subheadings,coc_headings}.ctl
\rm -f {coc_headings_todelete,coc_subheadings,coc_headings}.log
\rm -f /tmp/t.$$.log

echo ""
echo "----------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "----------------------------------------------"




