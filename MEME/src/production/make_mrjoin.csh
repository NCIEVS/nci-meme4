#!/bin/csh -f
#
# makes a MRJOIN file from MRCON & MRSO
# 
# only need to run these once
/bin/awk -F\| '{print $1$2$3"|"$0}' MRSO  | /bin/sort -T . -t\| -k 1,1 >! mrso.tmp
/bin/awk -F\| '{print $1$4$6"|"$0}' MRCON | /bin/sort -T . -t\| -k 1,1 >! mrcon.tmp
 
# need fields:
# 1	2	3	4	5	6	7	8	9  10  11
# C	C	C	S	C	S	S	C	C  C   C
# CUI|LUI|SUI|SAB|TTY|SCD|SRL|LAT|TS|STT|STR|
/usr/bin/join -t\| -o 2.2 2.3 2.4 2.5 2.6 2.7 2.8 1.3 1.4 1.6 1.8 mrcon.tmp mrso.tmp |\
 sed 's/$/\|/' >! MRJOIN
/bin/rm -f mrso.tmp mrcon.tmp
