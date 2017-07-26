#!@PATH_TO_PERL@

# maketreepos.pl
# usage: maketreepos.pl InputFile > Output
# written by Bill King 5/2000

# Notes:
# This script will take a list of Parent|Child rels
# in no particular order and create a TREEPOS style file
# Tree hierarchy goes from ancestor -> parent -> child
# A-00200.A-00220.A-00225
#
# if a Concepts file exists, you can use this script to generate
# an indented hierarchy for the terms for easy review.
# Physical agent, activity AND/OR force
#     Physical agent
#         Device
#             Instrument of aggression
#                 Nuclear weapon
#                     Thermonuclear weapon
#
# Physical agent, activity AND/OR force
#     Physical agent
#         Device
#             Transport vehicle
#                 Air AND/OR spacecraft
#                     Spacecraft
#                         Interplanetary craft
#
# Caveats:
# Assumes that all the relationships are valid, that there are no
# missing ancestors.
#
# Pseudocode 
# make a hash of child=>parent, each child and its parent
# make a hash of each parent, each child
# look at all the children
# if the child is found in the parent hash, ignore, it's not a node
# recursively find the parents using the child=>parent hash
# write the hierarchy string

use integer;
$numeric = 1;	# 1 - numeric codes  0 - prints term hierarchy
$errorcnt = 0;
$relstr = "part-of";
$relstr = "ISA";

$file = shift(@ARGV);
open(IN, $file) or die "Can't open $file, $!";
$file = "SRT_CONCEPTS0403.txt";
open(CON, $file) or die "Can't open $file, $!";
$errfile = $file.".errors";
open (ERR, ">$errfile") or die "Can't open $errfile";

print STDERR "\nRunning $0 on $file\n";
print STDERR "\nCreating Hierarchy for \"$relstr\"\n";

print STDERR "Making Relationship Hash\n";
&make_relhash();
print STDERR "Making Concept Hash\n";
&make_conhash();
print STDERR "Building Contexts\n\n";
&doit;


if($errorcnt){
	print STDERR "\nSee $file.errors for error messages\n";
}
sub doit{
foreach $x (sort keys %chd){
	next if($par{$x});	# if it appears in the parent hash it's not a node
	$str = &getpar($x);
	# print TREEPOS string or Hierarchy
	unless(&bad_treetop($str)){
		if($numeric){ print "$str\n" }
		else{ &print_hier($str)}
	}
	$str = '';
}
}

sub bad_treetop{
	my $x = shift;
	my $y;
	$y = split(/\./,$x);
	if($chd{$y}){
		unless($errcnt){ print ERR "These Codes are not Top Level Codes\n";
		print ERR "$y : $termhash{$y}\n"; 
		$errcnt++;
		return 1;
	}
	else{
		return 0;
	}
}
}
sub getpar{
	my $x = shift;
	my $y;
	if($parent{$x}){		# if x has a parent go get it
		$y = &getpar($parent{$x});
		$x = "$y.$x";		# add the parent's code to the string
	}
	return $x;
}


sub make_relhash{
# RELS file has format 'B ISA A', B is child of A
while(<IN>){
	chomp;
	($child,$rel,$par)=split(/\|/);
	next unless($rel eq $relstr);
	$parent{$child}=$par;
	$par{$par}++;	# all the parents
	$chd{$child}++;	# all the children
}
close IN;
}

sub make_conhash{
while(<CON>){
	chomp;
	($code,$stat,$term)=split(/\|/);
	$termhash{$code}=$term;
}
}

sub print_hier{
	my $str = shift;
	my @x;
	my $y;
	@x=split(/\./,$str);
	foreach $y (@x){
		print "$pad $termhash{$y}\n";
		$pad = "$pad    ";
	}
	$pad = '';
	print "\n";
}
