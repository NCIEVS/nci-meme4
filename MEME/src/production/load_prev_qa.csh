#!/bin/csh -f

set db = memedev
# set mu = pwd/user (may already be set)
set prevVersion = $old_release
set curVersion = $release
set mu = `$MIDSVCS_HOME/bin/get-oracle-pwd.pl`
set mrDir = "/meme_work/mr"

cd $mrDir/$prevVersion/QA

echo "Create template table"
sqlplus $mu@$db <<EOF >>& /dev/null
 drop table qa_mrdoc_$prevVersion;
 create table  qa_mrdoc_$prevVersion (test_name varchar2(100) not null,
   test_value varchar2(3000), test_count number(12));
EOF

foreach f (`ls *$prevVersion`)
    echo "Process $f"
    set ucf = `echo $f | perl -ne 's/^qa_//; s/_.*$//; print uc;' | sed 's/METAMORPHOSYS/MetaMorphoSys/'`
    if ($f != "qa_mrdoc_$prevVersion") then
        sqlplus -s $mu@$db  <<EOF >>& /dev/null
    drop table $f;
    create table $f as select * from qa_mrdoc_$prevVersion where 1=0;
EOF
    endif
    $MEME_HOME/bin/dump_mid.pl -t $f $db . >>& /dev/null
    sed "s/fields terminated by '|'/fields terminated by '~'/; s/7c0a/0a/;" $f.ctl >! $f.ctl2
    /bin/mv -f $f.ctl2 $f.ctl
    /bin/cp -f $f $f.dat
    sqlldr $mu@$db control=$f.ctl >>& /dev/null
    if ($status != 0) then
        echo "Problem loading $f"
        cat $f.log
        head $f.bad
    else
        /bin/rm $f.dat $f.ctl $f.bad $f.log
    endif
    set date = `/bin/date`
    /bin/rm -f prev_$f.log
    cat >! $mrDir/$curVersion/QA/prev_$ucf.log <<EOF
----------------------------------------------
Starting /local/content/MEME/MRD/bin/qa_previous.csh ... $date
----------------------------------------------
db:                     $db
previous minor dir:     $mrDir/$prevVersion/META
previous minor release: $prevVersion
previous major dir:     $mrDir/$prevVersion/META
previous major release: $prevVersion
target:                 $ucf

    *** THIS FILE IS FAKED, DATA WAS LOADED FROM PREVIOUS VERSION
    Generating counts for $prevVersion $ucf ... $date

----------------------------------------------
Finished /local/content/MEME/MRD/bin/qa_previous.csh ... $date
----------------------------------------------
EOF
end
