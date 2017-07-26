#!@PATH_TO_PERL@

# Oracle table help via CGI
# suresh@nlm.nih.gov 7/99

#$umlsOracleDir="/site/umls/oracle";
##$umlsOracleDir="/export/home/suresh/umls/oracle";

#$utilsDir="$umlsOracleDir/utils";

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use DBI;
use DBD::Oracle;
use open ":utf8";
use Midsvcs;

use OracleIF;
use GeneralUtils;
require "utils.pl";

use File::Basename;

use CGI;

# CGI params:
# db=<SID> (default is current-browse-tns)
# table=<tablename> help on this table

$query = new CGI;

$statedir="/tmp";
$tmpdir = (-e $statedir ? $statedir : "/tmp");

# ORACLE vars
#&oracleIF::init_oracle;
$db = $query->param('db') || Midsvcs->get($opt_s || 'editing-db');
$oracleuser = $EMSCONFIG{ORACLE_USER};
$oraclepassword = GeneralUtils->getOraclePassword($oracleuser,$db);
eval { $dbh = new OracleIF("db=$db&user=$oracleuser&password=$oraclepassword"); };
&printhtml({db=>$db, body=>"Database: $db is unavailable", printandexit=>1}) if ($@ || !$dbh);
$dbh{user}=$oracleuser;
$dbh{password}=$oraclepassword;
#$oracleUSER = "meow";
#$oraclePWD = &oracleIF::get_passwd("/etc/umls/oracle.passwd", $oracleUSER);
#$oracleAUTH = "$oracleUSER/$oraclePWD";
#$oracleTNS = $query->param('db') || &midsvcs::get_mid_service("current-editing-tns");
#$oracleDBH = undef; # DBD handle
$defaultTABLESPACE = "MID";

$progname=basename($0);

$cgi = $ENV{'SCRIPT_NAME'} || "/cgi-oracle_meowusers/oracle-table-help.pl";
$cgi_NLM = "/cgi-oracle-nlmlti/$progname";

$remotehost = $ENV{'REMOTE_HOST'};

#$now = &utils::UNIX_date;
$now = GeneralUtils->date;

$table = $query->param('table');
$owner = $query->param('owner');
$droptable = $query->param('droptable');
$size = "-1";

#$oracleDBH = &oracleIF::connectDBD($oracleAUTH, $oracleTNS);

# Is Oracle up?
#if (!defined($oracleDBH) || $oracleIF::err) {
#    &print_cgi_header;
#    &print_header("Error: Oracle Not Available for TNS: $oracleTNS");
#    print <<"EOD";
#<H1>Error: Oracle Not Available for TNS: $oracleTNS</H1>
#Error: $oracleIF::errstr
#EOD
#    &print_trailer;
#    exit 0;
#}

if ($table) {
    &print_cgi_header;
    &table_help($table);
} elsif ($droptable) {
    &drop_table($droptable);
    dbh->disconnect;
    exit 0;
} elsif ($owner) {
    &print_cgi_header;
    &table_list;
} else {
    &print_cgi_header;
    &query_page;
}
&print_trailer;

$dbh->disconnect;
exit 0;

# returns the query page
sub query_page {
    my(@owners);
    my($SQL);

    $SQL = <<"EOD";
SELECT DISTINCT(owner) FROM all_tables
EOD
    @owners = $dbh->selectAllAsArray($SQL);
    $ownerHTML = "<SELECT NAME=\"owner\">" .
	join("\n", map { "<OPTION " . ($_ eq "WTH" ? "SELECTED>" : ">") . "$_</OPTION>" } @owners) .
	    "</SELECT>";

    print <<"EOD";
<HTML>
<HEAD>
<TITLE>Tables in DB: $db</TITLE>
</HEAD>
<BODY>

<H1>Tables in DB: $db</H1>
<P>
<TABLE WIDTH="600">
<TR>
<TD ALIGN=left><FONT SIZE=$size COLOR="red">Current Database (TNS): $db</FONT></TD>
<TD ALIGN=right><FONT SIZE=$size><B>$now</B></FONT></TD>
</TR>
</TABLE>

<HR NOSHADE WIDTH="600" ALIGN=left>
<P>

<FORM METHOD=POST>
Show all tables owned by $ownerHTML with names like <INPUT TYPE=INPUT NAME="like" SIZE=10> (optional)
<INPUT TYPE=hidden NAME=\"db\" VALUE=\"$db\">
<P>
<INPUT TYPE=submit>
</FORM>
EOD
    return;
}

# produces the table help along with some sample rows..
sub table_help {
    my($table) = @_;
    my($sqlplusFile) = GeneralUtils::tempname("", "sqlplusin", "sql");
    my($outFile) = GeneralUtils::tempname("", "sqlplusout", "out");
    print STDERR "OUTPUT FILE IS $outFile\n";

    open(SQLPLUS, ">$sqlplusFile") || die "Cannot open $sqlplusFile\n";

    print SQLPLUS <<"EOD";
COLUMN column_name FORMAT A30 HEADING 'Column Name'
COLUMN data_type FORMAT A17 HEADING 'Data Type'
COLUMN not_null FORMAT A9 HEADING 'Nullable?'
SET PAGESIZE 1000
SELECT column_name,
       DECODE (data_type,
         'VARCHAR2','VARCHAR2 (' || TO_CHAR(data_length) || ')',
         'NVARCHAR2','NVARCHAR2 (' || TO_CHAR(data_length) || ')',
         'CHAR','CHAR (' || TO_CHAR(data_length) || ')',
         'NCHAR','NCHAR (' || TO_CHAR(data_length) || ')',
         'NUMBER',
            DECODE (data_precision,
              NULL, 'NUMBER',
              'NUMBER (' || TO_CHAR(data_precision) 
                         || ',' || TO_CHAR(data_scale) || ')'),
         'FLOAT',
            DECODE (data_precision,
              NULL, 'FLOAT',
              'FLOAT (' || TO_CHAR(data_precision) || ')'),
         'DATE','DATE',
         'LONG','LONG',
         'LONG RAW','LONG RAW',
         'RAW','RAW (' || TO_CHAR(data_length) || ')',
         'MLSLABEL','MLSLABEL',
         'ROWID','ROWID',
         'CLOB','CLOB',
         'NCLOB','NCLOB',
         'BLOB','BLOB',
         'BFILE','BFILE',
         data_type || ' ???') data_type,
         DECODE (nullable, 'N','NOT NULL') not_null
  FROM all_tab_columns
  WHERE table_name = \'$table\';
EXIT
EOD
    close(SQLPLUS);
     system("$ENV{EMS_HOME}/bin/get_table_info.csh",$db, $sqlplusFile, $outFile);
#    $dbh->sqlplus_integer_output($sqlplusFile, $outFile);
    $html = &file2str($outFile);
    print STDERR "RETURN HTML IS $html\n";
    unlink $sqlplusFile;
    unlink $outFile;

    print <<"EOD";
<HTML>
<HEAD>
<TITLE>Table Help for table: $table, DB: $db</TITLE>
</HEAD>
<BODY>

<H1>Table Help for table: $table, DB: $db</H1>
<P>

<TABLE WIDTH="600">
<TR>
<TD ALIGN=left><FONT SIZE=$size COLOR="red">Current Database (TNS): $db</FONT></TD>
<TD ALIGN=right><FONT SIZE=$size><B>$now</B></FONT></TD>
</TR>
</TABLE>

<HR NOSHADE WIDTH="600" ALIGN=left>
<P>

<PRE>
$html
</PRE>
EOD
    return;
}
sub file2str {
    my($file) = @_;
    my($fd);
    my($str);

    if (ref $file eq "GLOB") {
	$fd = $file;
    } else {
	$fd = gensym;
	open($fd, $file) || return "";
    }
    @_ = <$fd>;
    $str = join("", @_);
    close($fd) if ref $file ne "GLOB";
    return $str;
}


# produces a list of tables
sub table_list {
    my($SQL);
    my($html);
    my($owner) = $query->param('owner');
    my($like) = $query->param('like');
    my(@tableRefs, $ref);

    $like =~ tr/a-z/A-Z/;
    my($likeSQL) = ($like ? "AND table_name LIKE \'$like\'" : "");

    $SQL = <<"EOD";
select table_name, tablespace_name, owner, num_rows from ALL_TABLES where
    owner = \'$owner\'
    $likeSQL
ORDER BY table_name
EOD
    @tableRefs = $dbh->selectAllAsRef($SQL);
    unless (@tableRefs) {
	print <<"EOD";
<HTML>
<HEAD>
<TITLE>Tables owned by: $owner</TITLE>
</HEAD>
<BODY>

<H1>Tables owned by: $owner</H1>

<P>

<TABLE WIDTH="600">
<TR>
<TD ALIGN=left><FONT SIZE=$size COLOR="red">Current Database (TNS): $db</FONT></TD>
<TD ALIGN=right><FONT SIZE=$size><B>$now</B></FONT></TD>
</TR>
</TABLE>

<HR NOSHADE WIDTH="600" ALIGN=left>
<P>

There were no matching tables for owner: $owner and matching pattern: "$like".
EOD
        return;
    }

    $needdrop++ if ($owner eq "MEOW" && grep { $_ eq $remotehost } ("astra", "green"));

    foreach $ref (@tableRefs) {
	$table_name = $ref->[0];
	$tablespace_name = $ref->[1];
	$owner = $ref->[2];
	$num_rows = $ref->[3];

	$n++;

	if ($needdrop) {
	    $drophead = "<TH>Action</TH>";
	    if ($table_name !~ /^EMS_/i) {
		$drophtml = <<"EOD";
<TD>Drop <A HREF="$cgi_NLM?owner=$owner&droptable=$table_name">$table_name</A></TD>
EOD
            } else {
		$drophtml = "<TD><BR></TD>";
	    }
	}
	$html .= <<"EOD";
<TR>
<TD ALIGN=right>$n</TD>
<TD><A HREF="$cgi?table=$table_name&db=$db">$table_name</A></TD>
<TD>$tablespace_name<BR></TD>
<TD>$owner<BR></TD>
<TD>$num_rows<BR></TD>
$drophtml
</TR>
EOD
    }

    print <<"EOD";
<HTML>
<HEAD>
<TITLE>Tables in DB: $db</TITLE>
</HEAD>
<BODY>

<H1>Tables in DB: $db</H1>

<P>

<TABLE WIDTH="600">
<TR>
<TD ALIGN=left><FONT SIZE=$size COLOR="red">Current Database (TNS): $db</FONT></TD>
<TD ALIGN=right><FONT SIZE=$size><B>$now</B></FONT></TD>
</TR>
</TABLE>

<HR NOSHADE WIDTH="600" ALIGN=left>
<P>

<TABLE BORDER=1 CELLSPACING=1 CELLPADDING=5>
<TR>
<TH></TH>
<TH>Table</TH>
<TH>Tablespace</TH>
<TH>Owner</TH>
<TH>Rows</TH>
$drophead
</TR>
$html
</TABLE>
EOD
    return;
}

sub drop_table {
    my($table) = @_;

    OracleIF::dropTable($table);
# redirect back to the me_bins URL
    srand;
    $rand = int(rand 100000);
    print <<"EOD";
Location: $cgi?owner=$owner&db=$db&cachebust=$rand

EOD
}

sub print_cgi_header {
    print <<"EOD";
Content-type: text/html

EOD
}

sub print_header {
    my($title, $header) = @_;

    $title = "Editing Management System" unless $title;
    $header = $title unless $header;

    print <<"EOD";
<HTML>
<HEAD>
<TITLE>$title</TITLE>
</HEAD>
<BODY>

<H1>$header</H1>

EOD
    return;
}

sub print_trailer {
    print <<"EOD";
<P><HR>
<ADDRESS><A HREF="/index.html">Meta News Home</A></ADDRESS>
</BODY>
</HTML>
EOD
}
