#!@PATH_TO_PERL@

# CGI interface to luinorm > lvg2002
# suresh@nlm.nih.gov 10/2001
#
# Changes
# 12/22/2005 BAC (1-719SM): use open ":utf8"  added

use CGI;
BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}
use open ":utf8";

$cnorm = "$ENV{LVGIF_HOME}/bin/cnorm.pl";
$cluinorm = "$ENV{LVGIF_HOME}/bin/cluinorm.pl";
$cwordind = "$ENV{LVGIF_HOME}/bin/cwordind.pl";
$cgi = $ENV{'SCRIPT_NAME'} || "/cgi-bin/lvgcgi.pl";

$query = new CGI;
$lvgversion = $query->param('version') || "lvg2005";
$version = $lvgversion;
$now = &UNIX_date;

$str = $query->param('str');
$action = $query->param('action') || "luinorm";

if ($action eq "luinorm") {
  $answer=&luinorm($str);
} elsif ($action eq "norm") {
  $answer = &norm($str);
} elsif ($action eq "wordind") {
  $answer = &wordind($str);
}

$qstr = $str;
$qstr =~ s/&/&amp;/g;
$qstr =~ s/</&lt;/g;
$qstr =~ s/>/&gt;/g;

$qanswer = $answer;
$qanswer =~ s/&/&amp;/g;
$qanswer =~ s/</&lt;/g;
$qanswer =~ s/>/&gt;/g;

if ($str) {
  $html = "Query: <B>$qstr</B><BR>\n";

  $html .= <<"EOD";
Action: <B>$action</B><BR>
Output: <B>$qanswer</B><BR>
EOD
}

$lhtml .= <<"EOD";
Show <SELECT NAME="action"><OPTION VALUE="luinorm">LuiNorm<OPTION VALUE="norm">Norm<OPTION VALUE="wordind">WordInd</SELECT><BR>
EOD

&print_cgi_header;
print <<"EOD";
<HTML>
<HEAD>
<TITLE>LVG Tools</TITLE>
</HEAD>
<BODY>
<H1>LVG Tools</H1>
Current time: $now<BR>
LVG version: $version
<P>
$html
<P>
<FORM ACTION="$cgi" METHOD="post">
$lhtml
Enter string: <INPUT TYPE="text" NAME="str">
<INPUT TYPE="hidden" NAME="version" VALUE="$lvgversion">
</FORM>
<P>
EOD
&print_trailer;
exit 0;

sub print_cgi_header {
    print <<"EOD";
Content-type: text/html

EOD
}

sub print_trailer {
    print <<"EOD";
<P><HR>
<ADDRESS><A HREF="/index.html">Meta News Home</A></ADDRESS>
</BODY>
</HTML>
EOD
}


# Interface to lvg
# suresh@nlm.nih.gov 4/98
# Updated for new norm 7/99
# Updated for the new Java LVG (v2002) 8/2001

$defaultversion = "current";

# clients

sub luinorm {
  my($str) = @_;
  my($x) = &normfamily($str, $cluinorm);
  $x =~ s/^[^\|]*\|//;
  return $x;
}

sub norm {
  my($str) = @_;
  return &normfamily($str, $cnorm);
}

sub wordind {
  my($str) = @_;
  return &normfamily($str, $cwordind);
}

# Always use TCP for single terms - quickest
sub normfamily {
    my($str, $cmd) = @_;
    return "" unless $str;

    if ($str =~ /[\'\"]/) {
      my($tmpfile) = "/tmp/norm.$$";
      open(TMPNORM,">$tmpfile") || return "";
      print TMPNORM $str, "\n";
      close(TMPNORM);

      $cmd = "$cmd < $tmpfile";
      open(CMD, "$cmd|") || die "Cannot invoke $cmd\n";
      $_ = <CMD>;
      chomp;
      close(CMD);
      unlink $tmpfile;

    } else {

      $cmd = "$cmd \'$str\'";
      open(CMD, "$cmd|") || die "Cannot invoke $cmd\n";
      $_ = <CMD>;
      chomp;
      close(CMD);
    }
    return $_;
}

sub luinormfile {
  return &normfamilyfile(@_, $cluinorm);
}

sub normfile {
  return &normfamilyfile(@_, $cnorm);
}

sub wordindfile {
  return &normfamilyfile(@_, $cwordind);
}

# for a file of input terms
sub normfamilyfile {
    my($fileIN, $fileOUT, $cmd) = @_;

    $cmd = "$cmd < $fileIN > $fileOUT";
    system $cmd;
    return $_;
}

# what version of norm
sub normversion {
    my($version) = @_;

    my($localnormpath) = join('/', $localnormdir, ($version || $defaultversion));

    if (-e $localnormpath) {
	$ENV{'LVG_HOME'} = $localnormpath;
	open(NORM, "$localnormpath/bin/norm -v|") || die "Cannot invoke norm in $localnormpath\n";
	$_ = <NORM>;
	close(NORM);
	chop;
	return $_;
    }
}


sub UNIX_date {
    my($fmt) = @_;
    my($cmd) = "/bin/date";

    $cmd .= " \'$fmt\'" if $fmt;
    $d = `$cmd`;
    chop($d);
    return($d);
}
