#!/bin/csh -f

# usage:  spellcheck.s <inputfile> 
# where format of inputfile is 2 fields:  <anything>|term to spellcheck

sed -e 's/.*|//; s/\\/ /g' $1 | spell | spell +/net/bmp/b7/womp/MESH88/lexicon.medical |\
spell +$INV_HOME/etc/Healtheon/entries |\
spell +$INV_HOME/etc/Moby/spell-list |\
spell +/net/lti10/d0/MR.STED99/STED99.words |\
nawk '{if ($0 ~ /^[A-Z][a-z0-9-][a-z0-9-]*/) print tolower(substr($0,1,1)) substr($0,2); else print $0}' |\
spell +/net/bmp/b7/womp/MESH88/lexicon.medical |\
spell +/d1/u/sherertz/Healtheon/entries |\
spell +/d1/u/sherertz/Moby/spell-list |\
spell +/net/lti10/d0/MR.STED99/STED99.words |\
/net/lti10/lti10f/ACP/AMA/counts.pl $1 | sort -t\| +1n -2n +0n -1n > nospell.rpt
