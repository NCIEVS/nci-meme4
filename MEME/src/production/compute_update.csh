#!/bin/csh -f
#
# Author: Tun Tun Naing, Brian Carlsen
# Check required environment variables
#

#
# Parse arguments
#
if ($#argv == 0) then
    echo "Error: Bad argument"
    echo "Usage: $0 <old> <new> <filename> <key list> <dir>"
    exit 1
endif

if ($#argv == 5 ) then
    set old=$1
    set new=$2
    set file=$3
    set keys=$4
    set dir=$5
else
    echo "Error: Bad argument"
    echo "Usage: $0 <old> <new> <filename> <key list> <dir>"
    exit 1
endif

#
# Begin program logic
#
echo "----------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "----------------------------------------------"
echo "File $file"
echo "Keys $keys"

if (! -e $new/MRFILES.RRF) then
    echo "ERROR: required file $new/MRFILES.RRF cannot be found"
    exit 1
endif

set list = `grep $file $new/MRFILES.RRF | cut -d\| -f3 | sed 's/,/ /g'`
set fields
foreach key (`echo $keys | sed 's/,/ /g'`)
    set i = 1;
    foreach field ($list)
        if($field == $key) then
          if($fields == "" ) then
            set fields = '$'"$i";
          else
            set fields = "$fields"'$'"$i";
          endif
        endif
        @ i++;
    end
end

join -v 1 -v 2 -t '\n' $old/$file $new/$file | gawk -F\| '{print '$fields'"|"}' | sort -u >! $dir/$file.diff

gawk -F\| '{print '$fields`echo $fields | sed 's/\$/"|"$/g'`'}' $old/$file | sort -u >! $dir/$file.tmp

join -t\| $dir/$file.diff $dir/$file.tmp | cut -d\| -f3- >! $dir/$file.del

gawk -F\| '{print '$fields'"|"$0}' $new/$file  | sort -t\| +0 -1 >! $dir/$file.tmp

join -t\| $dir/$file.diff $dir/$file.tmp | cut -d\| -f3- >! $dir/$file.add

rm -f $dir/$file.{tmp,diff}

echo `ls -l $dir/$file.add`
echo `ls -l $dir/$file.del`



echo "----------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "----------------------------------------------"
