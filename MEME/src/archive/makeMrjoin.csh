#!/bin/csh -f
#
# makes a MRJOIN file from MRCON & MRSO
#
# input: <ORF dir>
# output: MRJOIN file
#
if ($#argv != 1) then
    echo "Usage: $0 <dir>"
    exit 1
endif

set dir=$1
if (! (-e $dir/MRCON)) then
    echo "$dir/MRCON must exist"
    exit 1
endif
if (! (-e $dir/MRSO)) then
    echo "$dir/MRSO must exist"
    exit 1
endif

echo "------------------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "------------------------------------------------------------------------"
echo "dir: $dir"

echo "    Sort MRSO ... `/bin/date`"
/bin/awk -F\| '{print $1$2$3"|"$0}' $dir/MRSO  | /bin/sort -T . -t\| -k 1,1 >! $dir/mrso.tmp
if ($status != 0) then
    echo "Error sorting MRSO"
    exit 1
endif
echo "    Sort MRCON... `/bin/date`"
/bin/awk -F\| '{print $1$4$6"|"$0}' $dir/MRCON | /bin/sort -T . -t\| -k 1,1 >! $dir/mrcon.tmp
if ($status != 0) then
    echo "Error sorting MRCON"
    exit 1
endif

echo "    Join into MRJOIN ... `/bin/date`"
# need fields:
# 1     2       3       4       5       6       7       8       9  10  11
# C     C       C       S       C       S       S       C       C  C   C
# CUI|LUI|SUI|SAB|TTY|SCD|SRL|LAT|TS|STT|STR|
/usr/bin/join -t\| -o 2.2 2.3 2.4 2.5 2.6 2.7 2.8 1.3 1.4 1.6 1.8 $dir/mrcon.tmp $dir/mrso.tmp |\
 sed 's/$/\|/' >! $dir/MRJOIN
if ($status != 0) then
    echo "Error joining for MRJOIN"
    exit 1
endif

echo "    Cleanup"
/bin/rm -f $dir/mrso.tmp $dir/mrcon.tmp

echo "------------------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "------------------------------------------------------------------------"
