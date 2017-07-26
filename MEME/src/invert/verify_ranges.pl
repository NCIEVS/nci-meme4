#!@PATH_TO_PERL@ -w

# run this script to verify that the termids file has no overlapping
# ranges.
# Based on code that is used in QA3.pl that looks at SAIDs found
# .src files and verifies that they are found in a valid Source

#use strict;

my($i)=0;
my($termids,$stat,$low,$high,$str,$src);
my(@tid, $prev_high,$cnt);
my($j)=0;
my($err_cnt)=0;
$prev_high = 0;

$src = lc($ARGV[0]);
if($src){
        print "Using $src\n";
        if($src eq 'umls'){
	    	$termids = "$ENV{INV_HOME}/etc/source_atom_ids";
        }
        elsif($src eq 'nci'){
	    	$termids = "$ENV{INV_HOME}/etc/source_atom_ids";
        }
        else{
                die "usage: $0 [nci|umls]\n";
        }

}
else{
        die "usage: $0 [nci|umls]\n";
}

open (TID, "$termids") or die "Can't open $termids, $!";
print "\n\n";
while(<TID>){
        #next if(/^#|\*|x/);
        next if(/^\#/);
        next if(/^\*/);
        next if(/^x/);
        next unless(/\w/);      # skip blank lines
        #print;
        ($stat,$low,$high,$str)=split(/\|/);
        $low =~ s/^ *//;
        $high =~ s/^ *//;
        ($raw_src) = split(/-/,$str);
        ($src) = split(/{/,$raw_src);
        # print "$low - $high | $src\n";
        $tid[$i] = ([$low,$high,$src]);
        if($low <= $prev_high){
                print "ERROR: Overlapping ranges\n";
                print "\t$src: Low $low  Prev. High $prev_high\n\n";
                $err_cnt++;
         }
        else{
                if(($low-$prev_high)>1){
                        $gap = $low - $prev_high - 1;
                        print "     $prev_high - $low : Gap in range of $gap\n";
                  }
                $cnt = $high - $low + 1;
                printf "%9d - %9d %10d %s\n", $low,$high,$cnt,$src;
         }
        $prev_high = $high;
        $i++;
}
close TID;
$cnt = scalar(@tid);
print "Found $cnt records\n";
print "Highest value is $tid[$#tid][2]: $tid[$#tid][1]\n";
print "$err_cnt Errors\n";
print "\n\n";
exit;
