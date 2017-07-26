#!@PATH_TO_PERL@

# suresh@nlm.nih.gov - 8/97
# Originally implemented - suresh@nlm.nih.gov 5/2003
# EMS3 - 1/2006

# refresh the source stats cache for one or more sources

# Options
# -d database
# -s (one or more sources - VSAB or RSAB OK)
# -n (regenerate for the 'n' most stale sources or sources without data in the cache)

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";

use OracleIF;
use EMSUtils;
use GeneralUtils;
use ZipUtils;
use Midsvcs;
use EMSNames;
use EMSTables;

push @INC, "$ENV{EMS_HOME}/bin";
require "utils.pl";

use File::Basename;
use Getopt::Std;
use DBI qw(:sql_types);
use CGI;

getopts("d:s:n:");

$query = new CGI;

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$now = GeneralUtils->date;
$currentmonth = GeneralUtils->date("+%m");
$currentyear = GeneralUtils->date("+%Y");

$logdir = $ENV{EMS_HOME} . "/log";
$logfile = "$logdir/sourcestats.$currentyear.log";
system "/bin/touch $logfile" unless -e $logfile;

$db = $opt_d || Midsvcs->get('editing-db');
$oracleuser = $EMSCONFIG{ORACLE_USER};
$oraclepassword = GeneralUtils->getOraclePassword($oracleuser);
$dbh = new OracleIF("db=$db&user=$oracleuser&password=$oraclepassword");
die "ERROR: Database $db is unavailable\n" unless $dbh;

# restrict the environment
$ENV{PATH} = "/bin:$ENV{ORACLE_HOME}/bin";

$SOURCESTATSTABLE = $EMSNames::SOURCESTATSTABLE;
EMSTables->createTable($dbh, $SOURCESTATSTABLE);

# by default just do one source
$opt_n = 1 unless ($opt_s || $opt_n);

@allvsabs = MIDUtils->getSources($dbh, "versioned");
%knownsource = map { $_ => 1 } @allvsabs;

if ($opt_s) {

  foreach (map { MIDUtils->makeVersionedSAB($dbh, $_) } split /[\s,]+/, $opt_s) {
    unless ($knownsource{$_}) {
      &log_and_die("Unknown source: $_");
    } else {
      push @vsabs, $_;
    }
  }

} elsif ($opt_n) {

# all new sources first, then stale but current sources
  my($n) = 0;
  my(@cachedvsabs) = $dbh->selectAllAsArray("select vsab from $SOURCESTATSTABLE order by generation_date asc");

  foreach $s (@allvsabs) {
    next if grep { $_ eq $s } @cachedvsabs;
    push @vsabs, MIDUtils->makeVersionedSAB($dbh, $s);
    $n++;
    last if $n >= $opt_n;
  }
  foreach $s (@cachedvsabs) {
    next if grep { $_ eq $s } @allvsabs;
    last if $n >= $opt_n;
    push @vsabs, MIDUtils->makeVersionedSAB($dbh, $s);
    $n++;
  }
}

unless (@vsabs) {
  &log("No sources found for statistics generation");
  exit 0;
}

foreach $vsab (@vsabs) {
  &doit($vsab);
}

$dbh->disconnect;
exit 0;

sub doit {
  my($vsab) = @_;
  my($sql);
  my($text, $html);
  my(@rows);
  my($vsabq) = $dbh->quote($vsab);
  my($limit) = 50;
  my($starttime) = time;
  my($tableprop) = {-border=>1, width=>'80%', -cellspacing=>0, -cellpadding=>5};
  my($rightalign) = {-align=>'right'};
  my($width) = {-width=>'50%'};
  my($tmptable) = $dbh->tempTable($EMSNames::PREFIX . "_");
  my($tmpindex) = "x_" . $dbh->tempTable($EMSNames::PREFIX . "_");

  &log("\n" . "-" x 10 . $now . " ($vsab) " . "-" x 10 . "\n");
  &log("EMS_HOME: " . $ENV{EMS_HOME});
  &log("Database: " . $dbh->getDB());
  &log_and_die("Database: $db is unavailable") unless $dbh;

# Are batch EMS scripts allowed?
  my($batchcutoff) = EMSMaxtab->get($dbh, $EMSNames::EMSBATCHCUTOFFKEY);
  if ($batchcutoff && lc($batchcutoff->{valuechar}) ne "no") {
    &log("Batch processes are currently disallowed in the EMS.");
    return;
  }

  $t = time;

  $language = $dbh->selectFirstAsScalar("select language from sims_info where source=$vsabq");
  $classes = "classes";

  push @rows, ["Source name:", $vsab];
  push @rows, ["Language:", $language];
  push @rows, ["RSAB:", MIDUtils->makeVersionlessSAB($dbh, $vsab)];
  push @rows, ["Offical name:", MIDUtils->officialSABName($dbh, $vsab)];
  push @rows, ["Insertion date:", MIDUtils->sourceInsertionDate($dbh, $vsab)];

  $sql = "select /* +PARALLEL(cr) */ count(*) from classes where source=$vsabq";
  $numAtoms = $dbh->selectFirstAsScalar($sql);
  push @rows, ["Atom Count (classes):", $numAtoms];

  $sql = "select /* +PARALLEL(cr) */ count(distinct concept_id) from classes where source=$vsabq";
  $numConcepts = $dbh->selectFirstAsScalar($sql);
  push @rows, ["In concepts:", $numConcepts];

  $sql = <<"EOD";
select /* +parallel(cr) */ termgroup, count(*) as c from $classes
where  source=$vsabq
group  by termgroup
order  by count(*) desc
EOD

  @x = ();
  foreach $r ($dbh->selectAllAsRef($sql)) {
    ($_,$tty) = split /\//, $r->[0];
    push @x, [$tty, [$rightalign, $r->[1]]];
    $tty{$tty} = $r->[1];
  }
  push @rows, ["Atom count by TTY:", &toHTMLtable($query, $tableprop, \@x)] if @x;

  $html .= $query->h1("Overall Counts");
  if (@rows) {
    $html .= &toHTMLtable($query, $tableprop, \@rows);
  } else {
    $html .= "None.";
  }
  $html .= $query->p . "(Time taken: " .  GeneralUtils->sec2hms(time-$t) . ")";
  &log("Time taken for overall counts: " .  GeneralUtils->sec2hms(time-$t));
  $t = time;

#----------------------------------------
  @rows = ();

  if ($numConcepts > 0) {

    $sql = <<"EOD";
select attribute_value, count(distinct concept_id) as c, count(distinct concept_id)/$numConcepts*100.0 as w from attributes
where  attribute_name || ''='SEMANTIC_TYPE'
and    concept_id in (select distinct concept_id from $classes where source=$vsabq)
group  by attribute_value
order  by count(*) desc
EOD
    my($n)=0;
    foreach $r ($dbh->selectAllAsRef($sql)) {
      $x = sprintf("%.2f\%", $r->[2]);
      $n++;
      push @rows, [[$rightalign, $n], $r->[0], [$rightalign, $r->[1]], [$rightalign, $x]];
      last if @rows >= $limit;
    }

    $html .= $query->h1("Semantic Type distribution");
    if (@rows) {
      $html .= <<"EOD";
Top $limit semantic types (STYs) for concepts containing atoms from $vsab.
Percentages are relative to the number of concepts containing $vsab atoms.
EOD
      $html .= $query->p;
      $html .= &toHTMLtable($query, $tableprop, \@rows);
    } else {
      $html .= "None.";
    }
    $html .= $query->p . "(Time taken: " .  GeneralUtils->sec2hms(time-$t) . ")";
    &log("Time taken for STY: " .  GeneralUtils->sec2hms(time-$t));
    $t = time;
  }

#----------------------------------------
  @rows = ();

  $dbh->dropTable($tmptable);
  $dbh->executeStmt("create table $tmptable as select distinct concept_id from classes where source=$vsabq");
  $dbh->createIndex($tmptable, "concept_id", $tmpindex);

  $sql = <<"EOD";
select relationship_name, count(*) from (
  select distinct relationship_name, concept_id_1, concept_id_2 from relationships
  where  concept_id_1 in (select concept_id from $tmptable)
     or  concept_id_2 in (select concept_id from $tmptable)
  )
group by relationship_name
order by count(*) desc
EOD

  $sql = <<"EOD";
select relationship_name, count(*) from (
  select distinct relationship_name, concept_id_1, concept_id_2 from relationships a, $tmptable b
  where (a.concept_id_1=b.concept_id or a.concept_id_2=b.concept_id)
  )
group by relationship_name
order by count(*) desc
EOD

  foreach $r ($dbh->selectAllAsRef($sql)) {
    $rel = $r->[0];
    $freq = $r->[1];
    push @rows, [$rel, [$rightalign, $freq]];
  }

  $html .= $query->h1("Relationships from/to concepts for this source");
  if (@rows) {
    $html .= <<"EOD";
These relationship counts are from or to concepts that have
atoms from this source.  Note that these may include relationships
from other sources or those created by NLM.
EOD
    $html .= &toHTMLtable($query, $tableprop, \@rows);
  } else {
    $html .= "None.";
  }

  $dbh->dropTable($tmptable);

  @rows = ();
  $sql = <<"EOD";
select /* +PARALLEL(cr) */ relationship_name, count(*) as c from relationships
where  source=$vsabq
and    relationship_level='S'
group  by relationship_name
order  by count(*) desc
EOD

  %relfreq = ();
  foreach $r ($dbh->selectAllAsRef($sql)) {
    $rel = $r->[0];
    $freq = $r->[1];
    $relfreq{$rel} = $freq;
    push @rows, [$rel, [$rightalign, $freq]];
  }

  $html .= $query->h1("Source-Asserted Relationships");
  if (@rows) {
    $html .= "Counts by relationship type for S level relationships attributed to $vsab" . $query->p;
    $html .= &toHTMLtable($query, $tableprop, \@rows);
  } else {
    $html .= "None.";
  }

  $html .= $query->p . "(Time taken: " .  GeneralUtils->sec2hms(time-$t) . ")";
  &log("Time taken for rel: " .  GeneralUtils->sec2hms(time-$t));
  $t = time;

# relationships by RELA
  @rows = ();
  foreach $rel (sort keys %relfreq) {
    $relfreq = $relfreq{$rel};
    @x = ();
    $relq = $dbh->quote($rel);

    $sql = <<"EOD";
select /* +PARALLEL(cr) */ relationship_attribute, count(*) as c, count(*)/$relfreq*100.0 as w from relationships
where  source=$vsabq
and    relationship_name=$relq
and    relationship_level='S'
group  by relationship_attribute
order  by count(*) desc
EOD

    foreach $r ($dbh->selectAllAsRef($sql)) {
      $rela = $r->[0];
      $freq = $r->[1];
      $frac = sprintf(" (%6.2f\%)", $r->[2]);
      next unless $rela;
      push @x, [$rela, [$rightalign, $freq . $frac]];
      last if @x >= $limit;
    }
    push @rows, [$rel, &toHTMLtable($query, $tableprop, \@x)] if @x;
  }

  $html .= $query->h1("Relationships by RELA");
  if (@rows) {
    $html .= "Counts of 'S' level relationships for $vsab grouped by RELA." . $query->p;
    $html .= &toHTMLtable($query, $tableprop, \@rows);
  } else {
    $html .= "None.";
  }
  $html .= $query->p . "(Time taken: " .  GeneralUtils->sec2hms(time-$t) . ")";
  &log("Time taken for rela: " .  GeneralUtils->sec2hms(time-$t));
  $t = time;

#----------------------------------------
  @rows = ();
  $sql = <<"EOD";
select /* +PARALLEL(cr) */ relationship_name, count(*) as c from context_relationships
where  source=$vsabq
group  by relationship_name
order  by count(*) desc
EOD

  foreach $r ($dbh->selectAllAsRef($sql)) {
    $rel = $r->[0];
    $freq = $r->[1];
    push @rows, [$rel, [$rightalign, $freq]];
  }

  $html .= $query->h1("Context Relationships");
  if (@rows) {
    $html .= "Counts of relationships from the context_relationships table." . $query->p;
    $html .= &toHTMLtable($query, $tableprop, \@rows);
  } else {
    $html .= "None.";
  }
  $html .= $query->p . "(Time taken: " .  GeneralUtils->sec2hms(time-$t) . ")";
  &log("Time taken for context rels: " .  GeneralUtils->sec2hms(time-$t));
  $t = time;

#----------------------------------------
  @rows = ();
  $sql = <<"EOD";
select /* +PARALLEL(cr) */ attribute_name, count(*) as c from attributes
where  source=$vsabq
group  by attribute_name
order  by count(*) desc
EOD

  foreach $r ($dbh->selectAllAsRef($sql)) {
    $rel = $r->[0];
    $freq = $r->[1];
    push @rows, [$rel, [$rightalign, $freq]];
  }

  $html .= $query->h1("Source-Asserted Attributes");
  if (@rows) {
    $html .= "Counts of attributes where the attribute authority is $vsab." . $query->p;
    $html .= &toHTMLtable($query, $tableprop, \@rows);
  } else {
    $html .= "None.";
  }
  $html .= $query->p . "(Time taken: " .  GeneralUtils->sec2hms(time-$t) . ")";
  &log("Time taken for attributes: " .  GeneralUtils->sec2hms(time-$t));
  $t = time;

#----------------------------------------
  @rows = ();
  if ($numConcepts > 0) {

    $sql = <<"EOD";
select source, count(concept_id) as c, count(concept_id)/$numConcepts*100.0 as w from (
  select /* +PARALLEL(cr) */ distinct concept_id, source from classes
  where  concept_id in (select concept_id from $classes where source=$vsabq)
  and    source!=$vsabq
  and    tobereleased in ('y', 'Y')
  and    source != 'MTH'
)
group  by source
order  by count(concept_id) desc
EOD
    my($n)=0;
    foreach $r ($dbh->selectAllAsRef($sql)) {
      $freq = $r->[1];
      $frac = sprintf(" (%6.2f\%)", $r->[2]);
      $n++;
      push @rows, [[$rightalign, $n], $r->[0], [{align=>'right'}, $freq . $frac]];
    }

    $html .= $query->h1("Overlap with other non-MTH Sources");
    if (@rows) {
      $html .= <<"EOD";
Number of concepts with $vsab atoms that also had atoms from other sources
(except MTH).  Percentages are relative to the number of concepts with $vsab atoms.
EOD
      $html .= $query->p;
      $html .= &toHTMLtable($query, $tableprop, \@rows);
    } else {
      $html .= "None.";
    }
    $html .= $query->p . "(Time taken: " .  GeneralUtils->sec2hms(time-$t) . ")";
    &log("Time taken for overlap: " .  GeneralUtils->sec2hms(time-$t));
    $t = time;
  }

#----------------------------------------
  @rows = ();

  $dbh->dropTable($tmptable);
  $sql = <<"EOD";
create table $tmptable as (
  select distinct concept_id from classes where source = $vsabq
  minus
  select concept_id from classes where source != $vsabq and source != 'MTH'
)
EOD
  $dbh->executeStmt($sql);
  $nooverlap = $dbh->selectFirstAsScalar("select count(distinct concept_id) from $tmptable") || 0;
  $frac = ($numConcepts > 0 ? sprintf(" (%.2f\%)", $nooverlap*100.0/$numConcepts) : "");
  push @rows, ["Total concepts:", [$rightalign, $nooverlap . $frac]];

  $sql = <<"EOD";
select termgroup, count(*) from (
  select concept_id, termgroup from classes
  where  source != 'MTH' and concept_id in (select concept_id from $tmptable)
)
group by termgroup
order by count(*) desc
EOD

  @x = ();
  foreach $r ($dbh->selectAllAsRef($sql)) {
    $frac = ($numConcepts > 0 ? sprintf(" (%.2f\%)", $r->[1]*100.0/$numConcepts) : "");
    push @x, [$r->[0], [$rightalign, $r->[1] . $frac]];
  }
  push @rows, [[$width, "Count by TTY:"], &toHTMLtable($query, $tableprop, \@x)];

  $sql = <<"EOD";
delete from $tmptable where concept_id in (select concept_id_1 from relationships)
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
delete from $tmptable where concept_id in (select concept_id_2 from relationships)
EOD
  $dbh->executeStmt($sql);

  $norels = $dbh->selectFirstAsScalar("select count(distinct concept_id) from $tmptable") || 0;
  $frac = ($numConcepts > 0 ? sprintf(" (%.2f\%)", $norels*100.0/$numConcepts) : "");
  push @rows, [[$width, "Of the $numConcepts, concepts with no associative relationships to other concepts: "], [$rightalign, $norels . $frac]];

  $sql = <<"EOD";
delete from $tmptable where concept_id in (select concept_id_1 from context_relationships)
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
delete from $tmptable where concept_id in (select concept_id_2 from context_relationships)
EOD
  $dbh->executeStmt($sql);

  $norels = $dbh->selectFirstAsScalar("select count(distinct concept_id) from $tmptable") || 0;
  $frac = ($numConcepts > 0 ? sprintf(" (%.2f\%)", $norels*100.0/$numConcepts) : "");
  push @rows, [[$width, "Of the remaining, these concepts did not even have a contextual relationship. These are considered \"true orphans\""], [$rightalign, $norels . $frac]];

  $html .= $query->h1("No Overlap with any Source");
  if ($nooverlap > 0) {
    $html .= "$nooverlap concepts had one or more $vsab atoms but none from any other non-MTH source." . $query->p;
  }
  if (@rows) {
    $html .= &toHTMLtable($query, $tableprop, \@rows);
  } else {
    $html .= "None.";
  }
  $html .= $query->p . "(Time taken: " .  GeneralUtils->sec2hms(time-$t) . ")";
  &log("Time taken for orphan: " .  GeneralUtils->sec2hms(time-$t));
  $t = time;

# String properties
  @rows = ();
  $sql = <<"EOD";
select /* +PARALLEL(cr) */ count(distinct a.sui) from string_ui a, classes b
where  a.sui=b.sui
and    a.language='ENG'
and    b.source=$vsabq
EOD
  $suis = $dbh->selectFirstAsScalar($sql) || 0;
  push @rows, ["Count of English language SUIs contributed to by this source", [$rightalign, $suis], [""]];

  $sql = <<"EOD";
select count(distinct SUI) from (
  select /* +PARALLEL(cr) */ a.SUI, b.termgroup from string_ui a, classes b
  where  a.SUI=b.SUI
  and    a.language='ENG'
  and    b.source=$vsabq
  and    a.SUI not in (select SUI from classes where source != $vsabq)
)
EOD
  $uniquesuis = $dbh->selectFirstAsScalar($sql) || 0;
  push @rows, ["Count of these SUIs with no other source contributing", [$rightalign, $uniquesuis], [$rightalign, ($suis > 0 ? sprintf("%.2f%", $uniquesuis/$suis*100.0) : "n/a")]];

  $sql = <<"EOD";
select /* +PARALLEL(cr) */ count(distinct a.isui) from string_ui a, classes b
where  a.isui=b.isui
and    a.language='ENG'
and    b.source=$vsabq
EOD
  $isuis = $dbh->selectFirstAsScalar($sql) || 0;
  push @rows, ["Count of English language ISUIs (case-insensitive SUIs) contributed to by this source", [$rightalign, $isuis], ""];

  $sql = <<"EOD";
select count(distinct isui) from (
  select /* +PARALLEL(cr) */ a.ISUI from string_ui a, classes b
  where  a.ISUI=b.ISUI
  and    a.language='ENG'
  and    b.source=$vsabq
  and    a.ISUI not in (select ISUI from classes where source != $vsabq)
)
EOD
  $uniqueisuis = $dbh->selectFirstAsScalar($sql) || 0;
  push @rows, ["Count of these ISUIs with no other source contributing", [$rightalign, $uniqueisuis], [$rightalign, ($isuis > 0 ? sprintf("%.2f%", $uniqueisuis/$isuis*100.0) : "n/a")]];

  $sql = <<"EOD";
select /* +PARALLEL(cr) */ count(distinct a.lui) from string_ui a, classes b
where  a.lui=b.lui
and    a.language='ENG'
and    b.source=$vsabq
EOD
  $luis = $dbh->selectFirstAsScalar($sql) || 0;
  push @rows, ["Count of English language LUIs contributed to by this source", [$rightalign, $luis], ""];

  $sql = <<"EOD";
select count(distinct LUI) from (
  select /* +PARALLEL(cr) */ a.LUI from string_ui a, classes b
  where  a.LUI=b.LUI
  and    a.language='ENG'
  and    b.source=$vsabq
  and    a.LUI not in (select LUI from classes where source != $vsabq)
)
EOD
  $uniqueluis = $dbh->selectFirstAsScalar($sql) || 0;

  push @rows, ["Count of these LUIs with no other source contributing", [$rightalign, $uniqueluis], [$rightalign, ($luis > 0 ? sprintf("%.2f%", $uniqueluis/$luis*100.0) : "n/a")]];

  $html .= $query->h1("String and lexical counts");
  if (@rows) {
    $html .= &toHTMLtable($query, $tableprop, \@rows);
  } else {
    $html .= "None.";
  }
  $html .= $query->p . "(Time taken: " .  GeneralUtils->sec2hms(time-$t) . ")";
  &log("Time taken for string counts: " .  GeneralUtils->sec2hms(time-$t));
  $t = time;

  $t = time-$starttime;
  $html = join('',
	       $dbh->quote($query->h1("Statistics for $vsab")),
	       "Data generated on $now.  Time to generate: " . GeneralUtils->sec2hms(time-$starttime) . " (hh:mm:ss)",
	       $query->p,
	       "Note: some foreign sources may have content in the foreign_* core tables.",
	       $query->p,
	       $html);

  $sql = "delete from $SOURCESTATSTABLE where vsab=$vsabq";
  $dbh->executeStmt($sql);

  $dbh->setAttributes({LongReadLen=>1024*1024, LongTruncOk=>0});
  $sql = <<"EOD";
insert into $SOURCESTATSTABLE (vsab, generation_date, generation_time, htmldata)
values ($vsabq, SYSDATE, $t, $htmlq)
EOD

# use bind params since this is a CLOB
  eval {
    $sql = <<"EOD";
insert into $SOURCESTATSTABLE (vsab, generation_date, generation_time, htmldata)
values ($vsabq, SYSDATE, $t, ?)
EOD
    $sth = $dbh->{dbh}->prepare($sql);
    $sth->bind_param(1, $html, SQL_CLOB);
    $sth->execute();
  };

  $dbh->dropTable($tmptable);
  &log_and_die("$@") if $@;
  &log_and_die($DBI::errstr) if $DBI::errstr;

  &log("\nDone generating data for $vsab in " . GeneralUtils->sec2hms(time-$starttime));
  return;
}

sub log {
  my($msg) = @_;
  open(L, ">>$logfile") || return;
  print L $msg, "\n";
  close(L);
}

sub log_and_die {
  my($msg) = @_;
  &log($msg);
  die $msg;
}
