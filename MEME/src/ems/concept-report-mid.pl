#!@PATH_TO_PERL@

# Prints a concept report using the database
# suresh@nlm.nih.gov 6/97

# Modified to support MEME-II reports
# suresh@nlm.nih.gov 1/98

# Ported to Oracle and MEME-III
# suresh@nlm.nih.gov 3/2000

# Modified for EMS-3
# - self-contained in EMS_HOME
# - use ems.config data
# suresh@nlm.nih.gov 3/2005

# CGI parameters:
# action={searchform, search, randomatoms}
# subaction={concept_id, etc}
#
# config= (alternate EMS_CONFIG)
# db=<the database to search in - default is editing-db>
# service=<Use a different MID service for the TNS name>
# meme-server-hostport= where is the MEME server running?

# arg=<the argument value, e.g., concept_id or string to search>
# source=<array of sources to restrict searches to>
# pagenum=(which page of the result set to show)
# hitsperpage= (how many results to show per page)
#
# ignorerellimit=<Ignore rel limit and get report> rel limit is set to 250
# format=text|html
# emsenhanced= (show EMS data for matching concepts)

# These were added to support new options 5/99 suresh@nlm.nih.gov
# r={DEFAULT|ALL|XR}
# x={DEFAULT|ALL|XR}
# lat= list of languages to display in report (default is all)
# maxreviewedrels=(max number of reviewed relationships to show - default is all)

# $Id: concept-report-mid.pl,v 1.1.1.1 2006-05-25 16:14:10 aweinrich Exp $

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";
use lib "$ENV{EMS_HOME}/lib";
push @INC, "$ENV{EMS_HOME}/bin";

use Data::Dumper;

use WMSUtils;
use EMSUtils;
use MIDUtils;
use LVG;
use OracleIF;
use Midsvcs;
use GeneralUtils;

use CGI;

$now = GeneralUtils->date;

$fontsize="-1";
$na="n/a";
$defaulthitsperpage = 20;

# currently allowable actions
%allowableaction = (
		    searchform=>1,
		    search=>1,
		    randomatoms=>1,
);

# maps a string for human consumption to a subaction
@searchsubaction = (
		    [ "a concept_id", 'concept_id' ],
		    [ "an atom_id", 'atom_id' ],
		    [ "a CUI", 'cui' ],
		    [ "a CODE", 'code' ],
		    [ "a source_row_id", 'source_row_id' ],
		    [ "an exact string", 'exactstr' ],
		    [ "a string ignoring case", 'lowerstr' ],
		    [ "a normalized string", 'normstr' ],
		    [ "normalized words", 'normword' ],
		    [ "words (ignoring case)", 'lowerword' ],
		    [ "a LUI", 'lui' ],
		    [ "a SUI", 'sui' ],
		    [ "an AUI", 'aui' ],
#		    [ "a SAUI", 'saui' ],
#		    [ "a SCUI", 'scui' ],
#		    [ "a SDUI", 'sdui' ],
);

use CGI;

$query = new CGI;

# For backward compatibility, map old parameter names to new
%compat_actionmap =
  (
   searchbyconceptid=>{action=>'search', subaction=>'concept_id'},
   searchbyatomid=>{action=>'search', subaction=>'atom_id'},
   searchbycui=>{action=>'search', subaction=>'atom_id'},
   searchbycode=>{action=>'search', subaction=>'code'},
   searchbysourcerowid=>{action=>'search', subaction=>'atom_id'},
   searchbynormstr=>{action=>'search', subaction=>'atom_id'},
   searchbynormwrd=>{action=>'search', subaction=>'atom_id'},
   cuisearchform=>{action=>'searchform'},
   normstrsearchform=>{action=>'searchform'},
   normwordsearchform=>{action=>'searchform'},
   meme_server=>'meme-server-hostport',
   rslimit=>'hitsperpage',
  );

foreach $old (keys %compat_actionmap) {
  next unless $query->param($old) || $query->param('action') eq $old;

  if (ref($compat_actionmap{$old})) {
    my($key, $ref);

    $ref = $compat_actionmap{$old};
    foreach $key (keys %$ref) {
      $query->param(-name=>$key, -value=>$ref->{$key});
    }
  } else {
    $query->param(-name=>$compat_actionmap{$old}, -value=>$query->param($old));
  }
  $query->param(-name=>$old, -value=>'');
}

#----------------------------------------
$ENV{EMS_CONFIG} = $query->param('config') if $query->param('config');
EMSUtils->loadConfig;

#$ENV{MIDSVCS_HOME} = $EMSCONFIG{MIDSVCS_HOME};
#$ENV{LVGIF_HOME} = $EMSCONFIG{LVGIF_HOME};
#$ENV{DBPASSWORD_HOME} = $EMSCONFIG{DBPASSWORD_HOME};
#$ENV{ENV_HOME} = $EMSCONFIG{ENV_HOME};
#$ENV{ENV_FILE} = $EMSCONFIG{ENV_FILE};

$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user);

$db = $query->param('db');
$db = "" if $db eq $na;
$db = $db || Midsvcs->get($query->param('service') || 'editing-db');

$dbh = new OracleIF({user=>$user, password=>$password, db=>$db});
unless ($dbh) {
  &print_html("Sorry, the Oracle database: $db is not available at the current time.");
  exit 0;
}

$emsURL = $main::EMSCONFIG{LEVEL0EMSURL};
$wmsURL = $main::EMSCONFIG{LEVEL0WMSURL};

# fonts
$fontsize = $query->param('fontsize') || $fontsize;
# font-family : Verdana, Arial, Helvetica, Sans-serif;

$format = $query->param('format') || "html";

# from CERT
$OK_CHARS='a-zA-Z0-9_.'; # things that are allowed in type-ins

$midsvcs = Midsvcs->load;
$lvghost = $midsvcs->{'lvg-server-host'};
$lvgport = $midsvcs->{'lvg-server-port'};

$action = $query->param('action') || "searchform";
$action =~ s/\s+//g;
$action =~ tr/A-Z/a-z/;

$subaction = $query->param('subaction');
$subaction =~ s/\s+//g;
$subaction =~ tr/A-Z/a-z/;

# which parameters should be sticky
@stickyparams = (
		 'db',
		 'service',
		 'env-home',
		 'env-file',
		 'meme-server-hostport',
		 'format',
		 'r',
		 'x',
		 'lat');
foreach $p ($query->param()) {
  next unless grep { $_ eq $p } @stickyparams;
  $v = $query->param($p);
  next unless $v;
  push @cgiGET,  $p . "=" . $v;
  push @cgiPOST, $query->hidden(-name=>$p, -value=>$v, -override=>1);
}
$cgiGET  = join("&", @cgiGET);
$cgiPOST = join("\n", @cgiPOST);

if ($allowableaction{$action}) {
  eval { &$action };
} else {
  $@ = "Action: $action is not currently allowed.  Known actions are: " . join(', ', keys %allowableaction);
}
$error = ($@ || $DBI::errstr);
if ($error) {
  &print_html($error);
}
exit 0;

# Provides a form to search for concepts
sub searchform {
  my($html);
  my(@subactionlabels, @subactionvalues);

  my(@services);
  my(@databases);
  my(@meme_hostport);
  my(%default);
  my(%x);
  my(@rows);

  @databases = split /,/, $midsvcs->{databases};
  push @databases, $na;
  $default{databases} = $na;

  %x = ();
  map { $x{$_}++ if /-db$/ } keys %$midsvcs;
  @services = sort keys %x;
  $default{services} = 'editing-db';
  %servicelabels = map { $_ => $_ . " [" . $midsvcs->{$_} . "]" } @services;

  %x = ();
  map { $x{$_}++ } split /,/, $midsvcs->{'meme-server-hostport'};
  @meme_hostport = sort keys %x;
  $default{meme_hostport} = join(':', $midsvcs->{'meme-server-host'}, $midsvcs->{'meme-server-port'});

#  %x = ();
#  map { $x{$midsvcs->{$_}}++ if /meme-server-host/ } keys %$midsvcs;
#  @meme_hosts = sort keys %x;
#  $default{meme_hosts} = $midsvcs->{"meme-server-host"};

#  %x = ();
#  map { $x{$midsvcs->{$_}}++ if /meme-server-port/ } keys %$midsvcs;
#  @meme_ports = sort keys %x;
#  $default{meme_ports} = $midsvcs->{"meme-server-port"};

  $html .= $query->start_form(-method=>'POST');

  @rows = ();
  push @rows, $query->td([
			  "Using Data source:",
			  $query->popup_menu(-name=>'service', -values=>\@services, -labels=>\%servicelabels, -default=>$default{services}),
			  " or database: ",
			  $query->popup_menu(-name=>'db', -values=>\@databases, -default=>$default{databases})
			 ]);
  push @rows, $query->td([
			  "MEME server on:",
			  $query->popup_menu(-name=>'meme-server-hostport', -values=>\@meme_hostport, -default=>$default{meme_hostport})
			  ]);
  $html .= $query->table({-border=>0}, $query->Tr(\@rows));
  $html .= $query->hr . $query->p;

  $html .= "Look for " . $query->textfield(-name=>'arg', -size=>20) . " as ";

  %subactionlabels = map { $_->[1] => $_->[0] } @searchsubaction;
  @subactionvalues = map { $_->[1] } @searchsubaction;
  $html .= " " . $query->popup_menu(-name=>'subaction', -values=>\@subactionvalues, -labels=>\%subactionlabels);

  my(@sources) = MIDUtils->getClassesSources($dbh);
  $html .= $query->p;
  $html .= "Where applicable, restrict results to: ";
  $html .= $query->scrolling_list(-name=>'source',
				  -values=>\@sources,
				  -size=>4,
				  -multiple=>1,
				  -valign=>'center');

  $html .= $query->p;
  $html .= $query->submit(-name=>"action", -value=>"Search");

  $html .= $query->p . $query->hr . $query->p;
  $html .= "Or, return a random sample: " . $query->submit(-name=>'action', -label=>'Random Atoms');

  $html .= $query->end_form;
  $html .= $query->end_html;

  &print_html("Concept Report from the MID", $html);
  return;
}

# Does the actual search
sub search {
  my($subaction) = $query->param('subaction');

  unless (grep { $subaction eq $_->[1] } @searchsubaction) {
    &print_html("Sorry, the subaction: $subaction for a search is not available at the current time.");
    exit 0;
  }
  $sub = join("_", $action, "by", $subaction);
  &$sub;
}

# Retrieves a report by Concept ID
sub search_by_concept_id {
  my($concept_id) = $query->param('arg');
  my($report);
  my($header) = "";

  $concept_id =~ s/^\s*//;
  $concept_id =~ s/\s*$//;
  unless ($concept_id && $concept_id =~ /^[0-9]*$/) {
    &print_html("Missing or Malformed concept_id", <<"EOD");
The concept_id you typed (\"$concept_id\") was malformed.  Legitimate concept_id\'s are integers.
Please refine the query and try again.
EOD
    return;
  }

  $currentconcept = $concept_id;
  $reporttime = time;
  $report = join("\n",
		 $query->h1("Concept Report for ID: $concept_id"),
		 &report($concept_id)
		);

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= join("\n",
		$query->start_form(-method=>'POST'),
		&leader,
		$query->a({-name=>"report"}, $query->p),
		$query->table({-border=>1, cellpadding=>20}, $query->Tr($query->td($report))),
		$query->p,
		"Time to generate report: $reporttime");

  &print_html($header, $html);
}

sub search_by_cui {
  my($cui) = MIDUtils->makeCUI($query->param('arg'));

  unless ($cui && $cui =~ /^C\d+$/) {
    my($usercui) = $query->param('cui');
    &print_html("Missing or Malformed CUI", <<"EOD");
The CUI you entered (\"$usercui\") was malformed.  Legitimate CUIs are integers,
or strings with leading "C" followed by 7 digits, for example C0010221.
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for CUI: $cui";
  $reporttime = time;

  my(@concept_ids) = MIDUtils->cui2concept_id($dbh, $cui);
  my($concept_id);

  if (@concept_ids) {
    if (@concept_ids>1) {
      my($a, @rows);

      $html .= "There are multiple concepts that match this CUI";
      foreach $concept_id (@concept_ids) {
	$a = $query->address($query->a({-href=>$query->script_name() . "?action=search&subaction=concept_id&arg=$concept_id&$cgiGET"}, $concept_id));
	push @rows, $query->td([$a, $cui, MIDUtils->conceptPreferredName($concept_id)]);
      }
      $html .= $query->table({-border=>1, -cellpadding=>5, -cellspacing=>0}, $query->Tr(\@rows));

    } else {

      $concept_id = $concept_ids[0];
      my($a) = $query->script_name() . "?action=search&subaction=concept_id&arg=$concept_id&$cgiGET";
      print $query->redirect($a);
    }
  } else {
    $html .= "Sorry, no matches were found for CUI: $cui";
  }

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");
  $html .= $query->p . "Time to generate report: $reporttime";
  &print_html($header, $html);
}

sub search_by_atom_id {
  my($atom_id) = $query->param('arg');

  unless ($atom_id && $atom_id =~ /^\d+$/) {
    &print_html("Missing or Malformed atom_id", <<"EOD");
The atom_id you entered (\"$atom_id\") was malformed.  Legitimate atom_ids are integers.
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for atom_id: $atom_id";
  $reporttime = time;

  my($info) = MIDUtils->atomInfo($dbh, $atom_id);
  my($concept_id) = $info->{concept_id};
  my(@atomrows);

  push @atomrows, $query->th(['name','source','tty','code','language']);
  push @atomrows, $query->td([$info->{atom_name}, map { $info->{$_} } qw(source tty code language)]);
  $html .= $query->table({border=>1, cellpadding=>5}, $query->Tr(\@atomrows));
  $html .= $query->p;

  $report = &report($concept_id);

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->table({-border=>1, cellpadding=>20}, $query->Tr($query->td($report))) .
    $query->p . "Time to generate report: $reporttime";
  &print_html($header, $html);
}

# searches by CODE
sub search_by_code {
  my($code) = $query->param('arg');
  my(@sources) = $query->param('source');

  unless ($code) {
    &print_html("Missing CODE", <<"EOD");
Please refine the query and try again.
EOD
    return;
  }
  if (uc($code) eq "NOCODE") {
    &print_html("NOCODE is not an allowed code", <<"EOD");
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for CODE: $code";
  $reporttime = time;

  my(@atom_ids) = MIDUtils->code2atom_id($dbh, $code, \@sources);

#    if (@atom_ids == 1) {
#      $atom_id=$atom_ids[0];
#      $a = $query->script_name() . "?action=search&subaction=atom_id&arg=$atom_id&$cgiGET";
#      print $query->redirect($a);
#      return;
#    }

  $html .= &matching_atoms($dbh, \@atom_ids);

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->p . "Time to generate report: $reporttime";

  &print_html($header, $html);
}

sub search_by_source_row_id {
  my($source_row_id) = $query->param('arg');

  unless ($source_row_id && $source_row_id =~ /^\d+$/) {
    &print_html("Missing or Malformed source_row_id", <<"EOD");
The source_row_id you entered (\"$source_row_id\") was malformed.  Legitimate source_row_ids are integers.
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for source_row_id: $source_row_id";
  $reporttime = time;

  my(@atom_ids) = MIDUtils->source_row_id2atom_id($dbh, $source_row_id);
  $html .= &matching_atoms($dbh, \@atom_ids);

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->p . "Time to generate report: $reporttime";

  &print_html($header, $html);
}

# searches by LUI
sub search_by_lui {
  my($lui) = MIDUtils->makeLUI($query->param('arg'));
  my(@sources) = $query->param('source');

  unless ($lui && $lui =~ /L\d+$/) {
    &print_html("Missing or malformed LUI", <<"EOD");
LUIs are either a sequence of digits or a "L" followed by 8 digits.
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for LUI: $lui";
  $reporttime = time;

  my(@atom_ids) = MIDUtils->lui2atom_id($dbh, $lui, \@sources);
  $html .= &matching_atoms($dbh, \@atom_ids);

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->p . "Time to generate report: $reporttime";

  &print_html($header, $html);
}

# searches by SUI
sub search_by_sui {
  my($lui) = MIDUtils->makeSUI($query->param('arg'));
  my(@sources) = $query->param('source');

  unless ($sui && $sui =~ /S\d+$/) {
    &print_html("Missing or malformed SUI", <<"EOD");
SUIs are either a sequence of digits or a "S" followed by 8 digits.
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for SUI: $sui";
  $reporttime = time;

  my(@atom_ids) = MIDUtils->sui2atom_id($dbh, $sui, \@sources);
  $html .= &matching_atoms($dbh, \@atom_ids);

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->p . "Time to generate report: $reporttime";

  &print_html($header, $html);
}

# searches by AUI
sub search_by_aui {
  my($aui) = MIDUtils->makeAUI($query->param('arg'));
  my(@sources) = $query->param('source');

  unless ($aui && $aui =~ /A\d+$/) {
    &print_html("Missing or malformed AUI", <<"EOD");
AUIs are either a sequence of digits or a "A" followed by 8 digits.
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for AUI: $aui";
  $reporttime = time;

  my(@atom_ids) = MIDUtils->aui2atom_id($dbh, $aui, \@sources);
  $html .= &matching_atoms($dbh, \@atom_ids);

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->p . "Time to generate report: $reporttime";

  &print_html($header, $html);
}

# search by exact string
sub search_by_exactstr {
  my($exactstr) = $query->param('arg');
  my(@sources) = $query->param('source');

  unless ($exactstr) {
    &print_html("Missing exact string", <<"EOD");
There was no query string entered.
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for exact string: $exactstr";
  $reporttime = time;

  my(@atom_ids) = MIDUtils->str2atom_id($dbh, $exactstr, \@sources);
  $html .= &matching_atoms($dbh, \@atom_ids);

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->p . "Time to generate report: $reporttime";

  &print_html($header, $html);
}

# search by string ignoring case
sub search_by_lowerstr {
  my($querystr) = $query->param('arg');
  my(@sources) = $query->param('source');

  unless ($querystr) {
    &print_html("Missing string", <<"EOD");
There was no query string entered.
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for string ignoring case: $querystr";
  $reporttime = time;

  my(@atom_ids) = MIDUtils->lowerstr2atom_id($dbh, $querystr, \@sources);
  $html .= &matching_atoms($dbh, \@atom_ids);

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->p . "Time to generate report: $reporttime";

  &print_html($header, $html);
}

# search for concepts by norm string
sub search_by_normstr {
  my($querystr) = $query->param('arg');
  my($luinormstr) = LVG->luinorm($querystr);
  my(@sources) = $query->param('source');
  my($normstr);

  unless ($luinormstr) {
    &print_html("Missing Query String", <<"EOD");
Your query norm\'ed to an empty string.
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for normalized string: $querystr";
  $reporttime = time;

  my(@atom_ids);
  my($atom_id, $concept_id);

  @atom_ids = MIDUtils->normstr2atom_id($dbh, $luinormstr, \@sources);
  $html .= &matching_atoms($dbh, \@atom_ids);

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->p . "Time to generate report: $reporttime";

  &print_html($header, $html);
}

# search for all words ignoring case
sub search_by_lowerword {
  my($querystr) = $query->param('arg');
  my(@sources) = $query->param('source');

  unless ($querystr) {
    &print_html("Missing Query String", <<"EOD");
Please refine the query and try again.
EOD
    return;
  }
  $header = "MID concept report for words ignoring case: $querystr";
  $reporttime = time;

  my($hitsperpage) = $query->param('hitsperpage') || $defaulthitsperpage;
  my($pagenum) = $query->param('pagenum') || 0;

  my(@words) = LVG->wordind($querystr);
  my($outputref) = MIDUtils->lowerword2atom_id($dbh, \@words, \@sources, {hitsperpage=>$hitsperpage, pagenum=>$pagenum, prefix=>$EMSNames::TMPTABLEPREFIX });

  @rows = ();
  push @rows, $query->td(['Query: ', $querystr]);
  push @rows, $query->td(['Tokens: ', join(', ', @words)]);
  if (@sources) {
    push @rows, $query->td(["Restricted to sources: ", join(", ", @sources)]);
  }
  $html .= $query->table({border=>0, cellpadding=>0}, $query->Tr(\@rows));
  $html .= $query->p;

  my($matches) = $outputref->{matches};
  my($from) = $pagenum*$hitsperpage+1;
  my($to) = $from+$hitsperpage-1;
  $to = $matches if $matches < $to;

  $nextpagenum = $pagenum + 1;
  $prevpagenum = $pagenum - 1;
  if ($pagenum > 0) {
    $prevurl = $query->a({-href=>$query->script_name() . "?action=$action&subaction=$subaction&arg=$querystr&hitsperpage=$hitsperpage&pagenum=$prevpagenum&$cgiGET"}, "Previous $hitsperpage");
  }
  if ($to < $matches) {
    $nexturl = $query->a({-href=>$query->script_name() . "?action=$action&subaction=$subaction&arg=$querystr&hitsperpage=$hitsperpage&pagenum=$nextpagenum&$cgiGET"}, "Next $hitsperpage");
  }
  $navtable = $query->table({border=>0,cellpadding=>10}, $query->Tr($query->td({ align=>'left' }, $prevurl), $query->td({ align=>'right' }, $nexturl)));

  if (@{$outputref->{atom_ids}} > 0) {
    $html .= &matching_atoms($dbh,
			     $outputref->{atom_ids},
			     "There were " . $outputref->{matches} . " matching atoms ($from to $to shown)." . $query->p,
			     $from);

    $html .= $query->p;
    $html .= $navtable;
  } else {
    $html .= "Sorry, no matches were found for your query.";
  }

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->p . "Time to generate report: $reporttime";
  &print_html($header, $html);
}

# search for all words using normalization
sub search_by_normword {
  my($querystr) = $query->param('arg');
  my(@sources) = $query->param('source');

  unless ($querystr) {
    &print_html("Missing Query String", <<"EOD");
Please refine the query and try again.
EOD
    return;
  }
  $header = "Matching atoms";
  $reporttime = time;

  my($hitsperpage) = $query->param('hitsperpage') || $defaulthitsperpage;
  my($pagenum) = $query->param('pagenum') || 0;

  my(@words) = LVG->cwordind($lvghost, $lvgport, $querystr);
  foreach $w (@words) {
    foreach (LVG->cnorm($lvghost, $lvgport, $w)) {
      push @normwords, $_;
    }
  }

  @rows = ();
  push @rows, $query->td(['Query: ', $querystr]);
  push @rows, $query->td(['Normalized: ', join(', ', @normwords)]);
  if (@sources) {
    push @rows, $query->td(["Restricted to sources: ", join(", ", @sources)]);
  }
  $html .= $query->table({border=>0, cellpadding=>0}, $query->Tr(\@rows));
  $html .= $query->p;

  my($outputref) = MIDUtils->normword2atom_id($dbh, \@normwords, \@sources, {hitsperpage=>$hitsperpage, pagenum=>$pagenum, prefix=>$EMSNames::TMPTABLEPREFIX });

  my($matches) = $outputref->{matches};
  my($from) = $pagenum*$hitsperpage+1;
  my($to) = $from+$hitsperpage-1;
  $to = $matches if $matches < $to;

  $nextpagenum = $pagenum + 1;
  $prevpagenum = $pagenum - 1;
  if ($pagenum > 0) {
    $prevurl = $query->a({-href=>$query->script_name() . "?action=$action&subaction=$subaction&arg=$querystr&hitsperpage=$hitsperpage&pagenum=$prevpagenum&$cgiGET"}, "Previous $hitsperpage");
  }
  if ($to < $matches) {
    $nexturl = $query->a({-href=>$query->script_name() . "?action=$action&subaction=$subaction&arg=$querystr&hitsperpage=$hitsperpage&pagenum=$nextpagenum&$cgiGET"}, "Next $hitsperpage");
  }
  $navtable = $query->table({border=>0,cellpadding=>10}, $query->Tr($query->td({ align=>'left' }, $prevurl), $query->td({ align=>'right' }, $nexturl)));

  if (@{ $outputref->{atom_ids} }) {
    $html .= &matching_atoms($dbh,
			     $outputref->{atom_ids},
			     "There were " . $outputref->{matches} . " matching atoms ($from to $to shown)." . $query->p,
			     $from);

    $html .= $query->p;
    $html .= $navtable;
  } else {
    $html .= "Sorry, no matches were found for your query.";
  }

  $reporttime = time - $reporttime;
  $reporttime .= ($reporttime == 1 ? " second" : " seconds");

  $html .= $query->p . "Time to generate report: $reporttime";
  &print_html($header, $html);
}

# helper function that displays matching atoms with links in a HTML table
sub matching_atoms {
  my($dbh, $atom_ids, $caption, $from) = @_;
  my($atom_id, $concept_id);
  my($html, $n);
  my($defaultcaption) = "The following atom(s) match your query.  Select links to see the concept report." . $query->p;

  if (@$atom_ids) {
    my($a, $b, @rows);
    my($info);

    $n = $from || 1;
    $html .= $caption || $defaultcaption;
    push @rows, $query->th(["", "atom_id", "concept_id", "source", "TTY", "Name"]);
    foreach $atom_id (@$atom_ids) {
      $info = MIDUtils->atomInfo($dbh, $atom_id);
      $concept_id=$info->{concept_id};
      $a = $query->a({-href=>$query->script_name() . "?action=search&subaction=atom_id&arg=$atom_id&$cgiGET"}, $atom_id);
      $b = $query->a({-href=>$query->script_name() . "?action=search&subaction=concept_id&arg=$concept_id&$cgiGET" . "#report"}, $concept_id);
      push @rows, $query->td([$n++, $a, $b, $info->{source}, $info->{tty}, $info->{atom_name}]);
    }
    $html .= $query->table({-border=>1, -cellpadding=>5, -cellspacing=>0}, $query->Tr(\@rows));

  } else {
    $html .= "Sorry, no matches were found for your query.";
  }
  return $html;
}

# random atoms
sub randomatoms {
  my($sql);
  my($limit) = $query->param('hitsperpage') || $defaulthitsperpage;
  my(@allsources) = MIDUtils->getClassesSources($dbh);
  my(@n) = (10, 20, 50, 100);
  my($html);
  my($reporttime) = time;
  my(@source) = $query->param('source');

  push @n, $default unless grep { $_ == $default } @n;

  $html .= $query->start_form(-method=>'POST');
  $html .= "Show: " . $query->popup_menu(-name=>'hitsperpage', -values=>\@n, -default=>$limit);
  $html .= " random atoms";
  $html .= $query->br;
  $html .= "Restrict them to one or more sources: ";
  $html .= $query->scrolling_list(-name=>'source',
				  -value=>\@allsources,
				  -size=>4,
				  -multiple=>1,
				  -valign=>'center');
  $html .= $query->p;
  $html .= $query->submit;
  $html .= $query->hidden(-name=>'doit', -value=>1, -force=>1);
  $html .= $query->hidden(-name=>'action', -value=>$action, -force=>1);
  $html .= $cgiPOST;
  $html .= $query->end_form;

  if ($query->param('doit')) {
    my(@atom_ids) = MIDUtils->random_atom_ids($dbh, $limit, \@source);

    $html .= $query->hr;
    $html .= $query->p;

    if (@source) {
      $html .= "Sample is restricted to atoms from these source(s): " . join(", ", @source);
      $html .= $query->p;
    }
    $html .= &matching_atoms($dbh, \@atom_ids);

    $reporttime = time - $reporttime;
    $reporttime .= ($reporttime == 1 ? " second" : " seconds");

    $html .= $query->p . "Time to generate report: $reporttime";
  }
  &print_html("Random sample of atoms", $html);
  return;
}

# generates a report given a concept id
sub report {
  my($concept_id) = @_;
  my(%optref);

  $optref{ids} = {idtype=>'concept_id', list=>[$concept_id]};
  $optref{db} = $db;
  $optref{env_home} = $ENV{ENV_HOME};
  $optref{env_file} = $ENV{ENV_FILE};
  $optref{meme_host} = (split /:/, $query->param('meme-server-hostport'))[0];
  $optref{meme_port} = (split /:/, $query->param('meme-server-hostport'))[1];
  $optref{outputformat} = $format;
  $optref{r} = $query->param('r') if ($query->param('r'));
  $optref{x} = $query->param('x') if ($query->param('x'));
  $optref{lat} = join(",", $query->param('lat')) if ($query->param('lat'));
  $optref{maxreviewedrels} = join(",", $query->param('maxreviewedrels')) if ($query->param('maxreviewedrels'));

  $optref{'-url_mid_for_concept_id'} = "'" . $query->escapeHTML($query->script_name() . "?" . join('&', "action=searchbyconceptid", $cgiGET, "arg=")) . "'";
  $optref{'-url_mid_for_code'} = "'" . $query->escapeHTML($query->script_name() . "?" . join('&', "action=searchbycode", $cgiGET, "arg=")) . "'";
  $optref{'-url_mid_for_cui'} = "'" . $query->escapeHTML($query->script_name() . "?" . join('&', "action=searchbycui", $cgiGET, "arg=")) . "'";
  $optref{'-url_release_for_cui'} = "";
  $optref{'-url_release_for_sty'} = "";

  return WMSUtils->xreports(\%optref);
}

sub print_trailer {
  my($html);

  $html .= $query->p;
  $html .= $query->hr({-width=>600, -align=>'left'});
  $html .= $query->address($query->a({-href=>$query->script_name()}, "Concept report from MID"));
  $html .= $query->address($query->a({-href=>"$main::EMSCONFIG{HOMEPAGEURL}"}, $main::EMSCONFIG{HOMEPAGENAME}));
  $html .= $query->end_html();
  print $html;
}

sub print_cgi_header {
  my($mime_type) = ($format eq "html" ? "text/html" : "text/plain");
  return if $cgi_header_printed++;
  print <<"EOD";
Content-type: $mime_type

EOD
  print <<"EOD" if $mime_type eq "text/html";
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
EOD
}

sub print_header {
  my($title, $header) = @_;

  $title = "Concept Report" unless $title;
  $header = $title unless $header;

  &print_cgi_header;
  print $query->start_html(-title=>$title);
  print $query->table({-border=>0, -width=>'100%'},
		      $query->Tr($query->td({-align=>'left'}, $query->font({-color=>"red"}, "Database: $db")) . $query->td({-align=>'right'}, $now)));
  print $query->hr({-width=>'100%', -noshade=>1, -align=>'left', -size=>6});
  print $query->h1($header);
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

# generates all the HTML for the top of the report
sub leader {
  my($html);
  my($h);

  foreach (qw(action subaction arg)) {
    $h .= $query->hidden(-name=>$_, -value=>$query->param($_), -override=>1) . "\n";
  }
  $html .= join("\n",
		$query->start_form(-method=>'POST'),
		&latmenu,
		&relmenu,
		&roptmenu,
		&xoptmenu,
		$query->p,
		$cgiPOST,
		$h,
		$query->submit({-value=>'Resubmit'}),
		$query->end_form,
		$query->p, $query->hr,
		&emsinfo,
		&helplink,
		);
  return $html;
}

# returns the menu for restriction by LAT
sub latmenu {
  my($html);
  my($sql) = "select distinct language, lat from language";

  my(@refs) = $dbh->selectAllAsRef($sql);
  my(@values) = map { $_->[1] } @refs;
  my(%labels) = map { $_->[1] => $_->[0] } @refs;
  my(@defaults) = $query->param('lat');

  $html .= "In the report only show content from these languages: " . $query->scrolling_list(-name=>'lat', -values=>\@values, -labels=>\%labels, -size=>4, -multiple=>1, -override=>1, -valign=>'center', -default=>\@defaults) . $query->p;
}

# returns the menu for restricting amount of rels (not used in interactive mode)
sub relmenu {
  my($html);
  return $html;
}

# returns the r option menu
sub roptmenu {
  my($html);
  my(@opts) = (
	       [ "DEFAULT", "Winning Rels" ],
	       [ "XR", "Winning + XR Rels" ],
	       [ "ALL", "All Rels" ],
	      );
  my($values, %labels);
  @values = map { $_->[0] } @opts;
  %labels = map { $_->[0] => $_->[1] } @opts;

  $html .= "Relationship view: " . $query->popup_menu(-name=>'r', -values=>\@values, -labels=>\%labels, -default=>'DEFAULT');
  return $html;
}

# returns the x option menu
sub xoptmenu {
  my($html);
  my(@opts) = (
	       [ "DEFAULT", "Par+Chd if no Cxt Rels" ],
	       [ "SIB", "Par+Chd+Sib if no Cxt Rels" ],
	       [ "ALL", "All Contexts" ],
	      );

  my($values, %labels);
  @values = map { $_->[0] } @opts;
  %labels = map { $_->[0] => $_->[1] } @opts;

  $html .= ",  context view: " . $query->popup_menu(-name=>'x', -values=>\@values, -labels=>\%labels, -default=>'DEFAULT');
  return $html;
}

sub emsinfo {
  my($html);
  my($sql);
  my(@rows, @x);
  my($concept_id) = $currentconcept;

  return unless $concept_id;
  $sql = "select distinct bin_name from " . $EMSNames::MEBINSTABLE . " where concept_id=$concept_id";
  push @rows, $query->td([
			  "Concept is on ME bins: ",
			  join(", ", $dbh->selectAllAsArray($sql)) || "[none]",
			  ]);

  $sql = "select distinct bin_name from " . $EMSNames::QABINSTABLE . " where concept_id=$concept_id";
  push @rows, $query->td([
			  "Concept is on QA bins: ",
			  join(", ", $dbh->selectAllAsArray($sql)) || "[none]",
			  ]);

  $sql = "select distinct bin_name from " . $EMSNames::AHBINSTABLE . " where concept_id=$concept_id";
  push @rows, $query->td([
			  "Concept is on AH bins: ",
			  join(", ", $dbh->selectAllAsArray($sql)) || "[none]",
			  ]);

  $sql = "select distinct worklist_name from " . $EMSNames::BEINGEDITEDTABLE . " where concept_id = $concept_id";
  push @rows, $query->td([
			  "Concept is on worklist\(s\): ",
			  join(', ', map{ $query->a({-href=>"$wmsURL?action=view&worklist=$_&db=$db"}, $_) } $dbh->selectAllAsArray($sql)) || "[none]",
			  ]);

  $html .= $query->table({-border=>0}, $query->Tr(\@rows));
  $html .= $query->br;
  $html .= $query->a({-href=>"$emsURL?action=whyisthisn&concept_id=$concept_id&db=$db"}, "Why is this concept in N status?");
  $html .= $query->p;

  return $html;
}

# link to a help page that describes how to read concept report
sub helplink {
  my($link) = $main::EMSCONFIG{SAMPLEREPORTURL};
  my($html);

  if ($link) {
    $html .= $query->a({-href=>$link, -target=>'sample_report'}, "Click here") . " for help on how to read a report (will open in a separate window)";
  }
  $html .= $query->p;
  return $html;
}

# converts CGI args to hidden fields
sub args2html {
    my($n, $v);
    foreach $n ($query->param) {
	$v = $query->param($n);
	$html .= "<INPUT TYPE=hidden NAME=\"$n\" VALUE=\"$v\">";
    }
    return $html;
}
