#!@PATH_TO_PERL@
# checkspell.pl
# Created by: Daniel  Date: Jan. 7, 1989
# Comments: : This is a poor-man's spellchecker of medical terms.  Essentially it checks
# to see if terms in some file (one per line) appear at all (case-insensitively)
# between white space in the string field of MRCON, i.e. in the Metathesaurus.
# It outputs those that do not appear there.  The tacit assumption here is that
# all (or most, at least) medical terms of importance appear correctly
# spelled in English in the Metathesaurus. 	 

# usage:  spellcheck.pl input_file  (> output_file, if you want the results in a file
#				instead of standard output, i.e., screen.)

$in = shift;						# pick up input file name

open IN, $in;
open MRCON, "/lti8f/M97MR/MRCON";

use integer;						# more efficient

while (<IN>) {chomp; $_ = lc $_; $hash{$_} = 1}		# create lower case hash key

while (<MRCON>)
	{
	($cui, $lang, $p, $lui, $pf, $sui, $string) = split /\|/;
	next unless $lang eq 'ENG';			# limit check to English
	$_ = lc $string;				# lower case
	@array = split;					# split on white space
	for $i (@array) {$hash{$i} = 2 if $hash{$i}}	# mark hits w/ a "2"
	}

for (keys %hash) {print "$_\n" if $hash{$_} == 1} 	# i.e., print only those terms
							# that did not appear at all
							# in Meta, else thay would
							# have a "2"

