# HTML utility functions
# suresh@nlm.nih.gov 3/2005

package HTMLUtils;

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use Data::Dumper;
use GeneralUtils;

sub print_cgi_header {
  my($class, $mimetype) = @_;

  return if $CGIHEADERPRINTED++;
  $mimetype = "text/html" unless $mimetype;
  print <<"EOD";
Content-type: $mimetype

EOD
  print <<"EOD" if $mime eq "text/html";
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
EOD
}

sub print_header {
    my($class, $title, $header, $bodyargs) = @_;

    $title = "TITLE HERE" unless $title;
    $header = $title unless $header;

    $class->print_cgi_header;
    print <<"EOD";
<HTML>
<HEAD>
<TITLE>$title</TITLE>

<STYLE TYPE="text/css">
    BODY { FONT-SIZE: $fontsize }
    SELECT { FONT-SIZE: $fontsize }
    INPUT { FONT-SIZE: $fontsize }
    BUTTON { FONT-SIZE: $fontsize }
</STYLE>

</HEAD>
<BODY $bodyargs>

<TABLE BORDER="0" WIDTH="600" CELLSPACING="0" CELLPADDING="0">
<TR>
<TD ALIGN="left">
<FONT SIZE="-1" COLOR="red">Oracle Database: $oracleTNS</FONT>
</TD>
<TD ALIGN="right">
<FONT SIZE="-1">$now</FONT>
</TD>
</TR>
</TABLE>

<!-- Horizontal bar -->

<HR WIDTH=600 SIZE=6 NOSHADE ALIGN=left>

<H1>$header</H1>

EOD
    return;
}

sub print_html {
    my($title, $html) = @_;

    $title = "Concept Report: $action" unless $title;
    $html = $title unless $html;
    &print_header($title);
    print <<"EOD";

$html
EOD
    &print_trailer;
    return;
}

# -----------------------------
1;
