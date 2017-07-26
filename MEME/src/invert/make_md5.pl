#!@PATH_TO_PERL@ 
# make_md5.pl
#
# Thu Jun 19 14:19:37 PDT 2003 - WAK 
# usage: $0 -n infilename > outfile
# takes a field number
# 
use integer;
use Digest::MD5 qw(md5_hex);
$index = shift(@ARGV);
$file = shift(@ARGV);

$index--;

open(FILE, "$file") or die "Can't open ATTRS, $!";
while(<FILE>){
	chomp;
	@A = split(/\|/);
	$md5 = md5_hex($A[$index]);
	print "$_|$md5|\n";
}

close ATTRS;


