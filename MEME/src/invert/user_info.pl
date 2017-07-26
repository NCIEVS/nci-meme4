#!@PATH_TO_PERL@

use Cwd;

$login = getlogin || (getpwuid($<)) [0] || "Intruder!!";
@user = getpwnam($login);

# did we catch a usable user ID?
if($user[5] ne "umls"){
	print "$user[5] "
}
#	Otherwise, we just have the CWD
$dir = cwd();
@D = split(/\//,$dir);
$loc = $D[($#D-1)]."/".$D[$#D];
print "from $loc\n";

