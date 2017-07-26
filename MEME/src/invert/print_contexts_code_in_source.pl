#!@PATH_TO_PERL@
# print_contexts_code_in_source.pl
# 
# WAK 08/06/03
# this script takes a command line arg of the SAB
# in the form:
# print.contexts.pl ./path_to_file/whatever.all

# SSL 11/29/04
# cloned the script "print.contexts.pl" to make this
# to use CODE_IN_SOURCE rather than SRC_ATOM_ID
# to be used to create CONTEXT attributes for loading into NCI-MEME
# (may be useful for other purposes also).
# creates a file called attributes.contexts.src


$ENV{"LANG"} = "en_US.UTF-8";
$ENV{"LC_COLLATE"} = "C";

# used to make MD5 hashes
use Digest::MD5 qw(md5_hex);

#used to get the command line options
use Getopt::Std;

# valid options are 's & f'
getopts('hf:s:', \%opts);

$file=$opts{'f'};
$SAB=$opts{'s'};

use File::Copy;

if($opts{'h'}){ &print_help; exit}

unless($opts{'s'} && $opts{'f'}){
	&print_help;
	exit;
}

print "\n\t.all file: $file\n\t      SAB: $SAB\n\n";

$attrib = 'attributes.src';
#$attrib = 'attributes.test';

# does attributes.src exist?
if(-e $attrib){
	print "\tFound '$attrib'\n"; 
	# get last 'source_attribute_id' from attributes.src
	$lastline = `tail -1 attributes.src`;
	die "Attempt to read $attrib exited funny" if($?);
	($id) = split(/\|/,$lastline);
	if($id =~ /\D/ || $id !~ /\d/){
		print "Whoops, last id number [$id] is not a valid number in attributes.src\n";
		print "Are you sure you've got a correct MEME3 file?\n\n "; 
		$id=1;
		#exit;
	}
	print "\n\n\tCopying old '$attrib' to /var/tmp\n";
	copy("$attrib", "/var/tmp/$attrib") or die "copy failed $!";
	$id++;
}
else{
	print "\tNo '$attrib' file found\n";
	print "\tCreating a new '$attrib'\n";
	`touch $attrib`;
	die "Attempt to 'touch' $attrib exited funny" if($?);
	$id = 1;
}

print "\tStarting numbering at: $id\n";

# get code_in_source from source_atoms.dat

open (IN, "cxt/source_atoms.dat") || die "can't open atoms\n";

while (<IN>)
	{
	chomp;
	@fields=split(/\|/);
	$code{$fields[0]}=$fields[2];
	$srctg{$fields[0]}=$fields[1];
	}

close (IN);


open (IN,"<$file") or die "can't open $file, $!";
open (OUT, ">attributes.contexts.src") or die "can't open attribute file";


print "\tAppending data from $file \n\n";

while (<IN>){
	chomp;
	@fields=split(/\|/);
	$att_val = "$fields[1]\t$fields[2]\t$fields[3]";
	$md5 = md5_hex($att_val);
	if (!$code{$fields[0]}) {print "MISSING CODE: $_\n";}
	print OUT "$id|$code{$fields[0]}|S|CONTEXT|$att_val|$SAB|R|n|N|N|CODE_TERMGROUP|$srctg{$fields[0]}||$md5|\n";
	$id++;
}

close IN;
close OUT;

exit;


sub print_help{
print <<EOT;
   
  usage: $0 -s SAB -f path/to/.allfile
  
             *** MEME3 format ***

  Run this from the directory that holds attributes.src

  the .all file is appended to the attributes file
  a copy of which is saved in /var/tmp


EOT

}
