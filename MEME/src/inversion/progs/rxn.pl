#!@PATH_TO_PERL@

####NOTE: for the next release, make sure all ROOT_SOURCE_AUI and ROOT_SOURCE_CUI are changed to
####      SOURCE_AUI and SOURCE_CUI.

use lib "/umls_dev/NLM/inv/bin";

use lib "/umls_dev/webapp_root/meme/lib";
use lib "/umls_dev/webapp_root/meme/bin";

use OracleIF;
use Midsvcs;

use strict 'vars';
use strict 'subs';


use Getopt::Std;

my %options=();
getopts("gh", \%options);

if (defined $options{h}) {
  print "Usage: rxn.pl -hg\n";
  print " This file inverts RxNorm and produces the standard files.\n";
  print "\t-h prints this message.\n";
  print "\t-g generate inital files so actual inversion can be done.\n\n";
  print "Step 1) run rxn.pl -g - to produce\n";
  print "\t1) find out which sources are updated and which ones are not\n";
  print "\t2) generate sources.src and termgroups.src files.\n";
  print "\t3) get UMLS Auis for non-updated source atoms.\n\n";
  print "Step 2) run rxn.pl - without any arguments.\n";
  print "\tThis generates all the standard output files.[~ 30 min]\n";
  print "\nNote: RxNorm doesn't have context files.\n";
  exit;
}

use NLMInv;
use Atom;
use Attribute;
use Relation;
use Merge;
use Context;
use IdGen;

our $glERR;
our $glInv;

our $mainGenId;


# variables 

our ($gAtom, $gAttr, $acAttr, $cxtAttr, $styAttr, $gMerge);
our ($gRel, $gUpRel, $gCxt);

our $rsab;
our $Vsab;
our $Vsab18;
our $styVsab;

our %upVsab2Ssab = ();      # upVsab2Ssab - updated src: Vsab 2 StrpSab
our %nuVsab2Ssab= ();       # nuVsab2Ssab - non-updated src: Vsab 2 StrpSab
our %upSsab2Vsab = ();      # upSsab2Vsab - updated src: StrpSab 2 Vsab
our %nuSsab2Vsab = ();      # nuSsab2Vsab - non-updated src: StrpSab 2 Vsab

our %sup2tbr=();           # sup2tbr - suppressible 2 tobereleased
our %tty2status=();         # tty2status - tty2 2 status
our $sctUpdated = 0;
our $sctVsab = '';      # Snomed vsab name

our %mthAuis = ();         # mthAuis - Auis for non-updated srcs (from database)
our %rxaui2Aui = ();       # rxaui2Aui - 
our %sctSaid = ();         # sctSaid
our %rxaui2sctSaid = ();    # rxaui2sctSaid

our %rxaui2said_Rx = ();    # rxaui2said_Rx
our %rxaui2said_Up = ();    # rxaui2said_Up
our %rxaui2said_Nu = ();    # rxaui2said_Nu
our %rxaui2said_Sct = ();   # rxaui2said_Sct

our %rxCuis = ();         # rxCuis
our %upCuis = ();         # upCuis
our %nuCuis = ();         # nuCuis
our %sctCuis = ();        # sctCuis


sub trim {
  my$str = shift;
  $str =~ s/ {2,}/ /g;		# more than 1 blanks to 1 blank
  $str =~ s/^[ ]*//;		# strip leading blanks 
  $str =~ s/[ ]*$//;		# strip trailing blanks
  return $str;
}

# =======================================================================
sub doOnce {
  &prepareSourceStatus;
  &makeSrcSourcesNTermgroups;
  &formRxauiAssociations;
}

# one of the preprocessing steps (1)
# prepares src/sources.src and src/termgroups.src
sub makeSrcSourcesNTermgroups {
  $glInv->prTime("BEGIN makeSrcSourcesNTermgroups");
  my $db = Midsvcs->get('editing-db');
  my $oracleuser = 'meow';
  my $oraclepassword = GeneralUtils->getOraclePassword($oracleuser,$db);
  my $dbh = new OracleIF("db=$db&user=$oracleuser&password=$oraclepassword");

  my @ans = $dbh->selectAllAsRef 
    ("SELECT source, current_name 
        FROM source_version");

  # save in a file or to a hash.
  my %lastVsab = ();   #lastVsab
  my ($val);
  foreach $val (@ans) {
    $lastVsab{"$val->[0]"} = $val->[1];
  }
  $dbh->disconnect;

  # prepare src/sources.src file.
  open(IN, "<:utf8", $glInv->getEle('File.sab')) 
    or die "Could not open File.sab\n";
  open (OUT, ">:utf8",  $glInv->getEle('File.Sources'))
    or die "Could not open File.sources.\n";
  my ($vcui,$rcui,$vsab,$son,$sf,$sver,$vstart,$vend,$imeta,$rmeta);
  my ($slc,$scc,$srl,$tfr,$cfr,$cxty,$ttyl,$atnl,$lat,$cenc,$curver);
  my ($sabin,$ssn,$scit, $prevVsab, $ele);

  my %updatedSabs= ();           # updatedSabs;
  my %nonupdatedSabs= ();         # nonupdatedSabs;
  while(<IN>) {
    chomp;
    ($vcui, $rcui, $vsab, $rsab, $son, $sf, $sver, $vstart, $vend, $imeta,
     $rmeta, $slc, $scc, $srl, $tfr, $cfr, $cxty, $ttyl, $atnl, $lat, $cenc,
     $curver, $sabin, $ssn, $scit) = split(/\|/, $_);

    $prevVsab = $lastVsab{"$rsab"};
    if ($vsab eq $prevVsab) {
      $nonupdatedSabs{"$rsab"} = $vsab;
    } else {

      $updatedSabs{"$rsab"} = $vsab;

      # don't create a record for SNOMEDCT_US_US even when it's updated
      next if ($rsab eq 'SNOMEDCT_US_US');


      # fix extra-date bug in NDDF's SON
      if ($rsab eq 'NDDF') { $son =~ s/, 20\d\d_\d\d_\d\d$//; }

      # fix bad format in RXNORM's SVER
      if ($rsab eq 'RXNORM') { $sver =~ s/FUL_20(06)_(03)_(14)/$1$2$3F/; }

      # if srl is empty use 0 as default.
      if ($srl eq '') { $srl = 0; };

      # this source is being updated, so print a line for it
      print OUT "$vsab|$prevVsab|$srl|$vsab|$rsab|$sver|$sf|$son|";
      print OUT "wth\@nlm.nih.gov||$scc|$slc|olson\@apelon.com|";
      print OUT "$cxty||$lat|$scit||$cenc||\n";
    }
  }
  close(IN);
  close(OUT);

  print ERR "Following are the updated sources:\n";
  print "Following are the updated sources:\n";
  foreach $ele (keys (%updatedSabs)) {
    print "\t$ele => $updatedSabs{$ele}\n";
    print ERR "\t$ele => $updatedSabs{$ele}\n";
  }
  print "\n\nFollwoing are non updated sources:\n";
  foreach $ele (keys (%nonupdatedSabs)) {
    print "\t$ele => $nonupdatedSabs{$ele}\n";
    print ERR "\t$ele => $nonupdatedSabs{$ele}\n";
  }
  print "\nModify configuration file accordingly and run the appl.\n\n";
  print ERR "\nModify configuration file accordingly and run the appl.\n\n";

  ## now prepare src/termgroups.src file.
  # find obsolete ttys from mrdoc
  my %obsolete = ();     # suppressible
  open (IN, "<:utf8", $glInv->getEle('File.doc'))
    or die "Could not open File.doc\n";
  # get suppressible info
  my ($docKey, $val, $type, $expl);
  while (<IN>) {
    chomp;
    ($docKey, $val, $type, $expl) = split(/\|/, $_);
    if ($docKey eq 'TTY' && $type eq 'tty_class' && $expl eq 'obsolete') {
      $obsolete{"$val"} = 1;
    }
  }
  close(IN);

  # now write termgroups file.
  open (CONSO, "<:utf8", $glInv->getReqEle('File.conso'))
    or die "Could not open input conso file.\n";
  open (OUT, ">:utf8", $glInv->getEle('File.Termgroups')) 
    or die "Could not open File.Termgroups\n";

  my ($ign, $cui, $lang, $ts, $lui, $stt, $sui, $isp, $aui, $saui, $scui);
  my ($sdui, $sab, $tty, $code, $name, $srl, $supp);
  my %seen=();
  while(<CONSO>) {
    chomp;
    ($cui, $lang, $ts, $lui, $stt, $sui, $isp, $aui, $saui, $scui, $sdui, $sab,
     $tty, $code, $name, $srl, $supp) = split(/\|/, $_);

    unless(defined($seen{"$sab|$tty"})) {
      $vsab = $updatedSabs{"$sab"};
      if ($vsab ne '') {
	$prevVsab = $lastVsab{"$sab"};
	$supp = (defined($obsolete{"$tty"})) ? 'Y' : 'N';
	print OUT "$vsab/$tty|$prevVsab/$tty|$supp|N|N|$tty|\n";
	$seen{"$sab|$tty"} = 1;
      }
    }
  }
  close(CONSO);
  close(OUT);

  # foreach non-updated sabs get auis from db.
  # if SNOMEDCT_US_US is getting updated, do not get SNOMEDCT_US_US.

  my @strpSrcs = ();
  foreach $ele (keys (%nonupdatedSabs)) {
    if ($sctUpdated == 1) {
      push (@strpSrcs, $ele) if ($ele ne 'SNOMEDCT_US_US');
    } else {
      push (@strpSrcs, $ele);
    }
  }

  &getAidsFromDb(@strpSrcs);
  $glInv->prTime("END makeSrcSourcesNTermgroups");
}




# one of the preprocessing steps (2)
# get this for only non-updated sources.
sub formRxauiAssociations {
  $glInv->prTime("BEGIN formRxauiAssociations");
  &getSctSaids;       # get saids of the latest SNOMEDCT_US_US atoms.

  open (IN, "<:utf8", $glInv->getEle('File.conso'))
    or die "Could not open File.conso\n";
  my ($ig, $rxaui, $sab, $tty, $code, $str, $key, $ele);
  while (<IN>) {
    chomp;
    ($ig, $ig, $ig, $ig, $ig, $ig, $ig, $rxaui, $ig, $ig, $ig, 
     $sab, $tty, $code, $str) = split(/\|/, $_);
    $key = "$sab|$tty|$code|$str";
    if (defined($mthAuis{"$key"})) {
      $rxaui2Aui{"$rxaui"} = $mthAuis{"$key"};
    }
    if (defined($sctSaid{"$key"})) {
      $rxaui2sctSaid{"$rxaui"} = $sctSaid{"$key"};
    }
  }
  close(IN);
  # release memory for mthAuis and sctSaids
  %mthAuis = ();
  %sctSaid = ();
  # record them into files.
  # save rxaui2Aui
  open (OUT, ">:utf8", $glInv->getEle('File.rxaui2Aui', '../etc/rxaui2Aui'))
    or die "Could not open file File.rxaui2Aui\n";
  foreach $ele (sort keys (%rxaui2Aui)) { print OUT "$ele|$rxaui2Aui{$ele}\n"; }
  close(OUT);

  # save rxaui2sctSaid
  open (OUT, ">:utf8", $glInv->getEle('File.rxaui2sctSaid', 
				 '../etc/rxaui2sctSaid'))
    or die "Could not open file File.rxaui2sctSaid\n";
  foreach $ele (sort keys (%rxaui2sctSaid)) { print OUT "$ele|$rxaui2sctSaid{$ele}\n";}
  close(OUT);
  $glInv->prTime("END formRxauiAssociations");
}

sub getAidsFromDb {
  $glInv->prTime("\tBEGIN getAidsFromDb");
  my $saveP = 1;
  my @strpSrcsToGet = @_;
  if (@_ <= 0) {
    print "There are no non-udpated sources to get AUIs for.\n";
    print ERR "There are no non-udpated sources to get AUIs for.\n";
    return;
  }

  my $db = Midsvcs->get('editing-db');
  my $oracleuser = 'meow';
  my $oraclepassword = GeneralUtils->getOraclePassword($oracleuser,$db);
  my $dbh = new OracleIF("db=$db&user=$oracleuser&password=$oraclepassword");
  my ($ele, $val);

  # we should get these values only for the non-updated sources.
  # which we can get from the config file.
  my $query = "Select c.stripped_source, a.tty, a.code, b.string, a.aui 
         From classes a, string_ui b, source_rank c, source_rank d 
         Where a.source = d.source 
           AND c.source = d.normalized_source 
           AND a.sui = b.sui 
           AND c.stripped_source in ";

  my $nuSrc = join (',', (map {"\'$_\'"} (@strpSrcsToGet) ));
  $query = "$query ( $nuSrc )";

  my @ans = $dbh->selectAllAsRef($query);

  # save in a file or to a hash.
  open (OUT, ">:utf8",  $glInv->getEle('File.midAuis',
				       '../tmp/atomsWithAuis')) 
    or die "Could not open File.midAuis.\n" if ($saveP == 1);

  foreach $val (@ans) {
    print OUT "$val->[0]|$val->[1]|$val->[2]|$val->[3]|$val->[4]\n"
      if ($saveP == 1);
    # strpSrc|tty|code|str => AUI
    $mthAuis{"$val->[0]|$val->[1]|$val->[2]|$val->[3]"} = $val->[4];
  }
  close(OUT) if ($saveP == 1);
  $dbh->disconnect;
  $glInv->prTime("\tEND getAidsFromDb");
}

sub getSctSaids {
  $glInv->prTime("\tBEGIN getSctSaids");
  %sctSaid = ();
  return if ($sctUpdated == 0);

  my $saveP = 1;

  open (IN, "<:utf8", $glInv->getEle('File.SNOMEDClasses'))
    or die "Could not open File.SNOMEDClasses\n";

  open (OUT, ">:utf8", $glInv->getEle('File.sctSaids', '../tmp/sctSaids'))
    or die "Could not open File.sctSaids\n" if ($saveP == 1);

  my ($ign, $said, $tg, $tty, $code, $str);
  while(<IN>) {
    chomp;
    ($said, $ign, $tg, $code, $ign, $ign, $ign, $str) = split(/\|/, $_);
    next if ($tg !~/^SNOMEDCT_US/);
    ($ign, $tty) = split(/\//, $tg);
    $sctSaid{"SNOMEDCT_US|$tty|$code|$str"} = $said;
    print OUT "SNOMEDCT_US|$tty|$code|$str|$said\n" if ($saveP == 1);
  }
  close(IN);
  $glInv->prTime("\tEND getSctSaids");
}
# =======================================================================

sub checkNeededConfigInfo {
  my $inv = shift;
  $$inv->getReqEle('SaidStart');
  $$inv->getReqEle('File.Atoms');
  $$inv->getReqEle('File.Attributes');
  $$inv->getReqEle('File.Merges');
  $$inv->getReqEle('File.Relations');
  $$inv->getReqEle('File.Sources');
  $$inv->getReqEle('File.Termgroups');
  $$inv->getReqEle('File.Contexts');
  $$inv->getReqEle('File.conso');
  $$inv->getReqEle('File.doc');
  $$inv->getReqEle('File.rel');
  $$inv->getReqEle('File.sab');
  $$inv->getReqEle('File.sat');
  $$inv->getReqEle('File.sty');
  #$$inv->getReqEle('File.rxaui2Aui');
  #$$inv->getReqEle('File.rxaui2sctSaid');
  $$inv->getReqEle('File.SNOMEDClasses');
  #$$inv->getReqEle('');
}



sub init {
  &prepareSourceStatus;

  # create a main generator with proper seeds.
  $mainGenId = new IdGen();

  # fill suppressible2tobereleased and tty2status from config file.
  %sup2tbr = $glInv->getHash('Sup2Tbr');
  %tty2status = $glInv->getHash('Tty2Status');

  # create templates.
  # ATOM templates.
  $gAtom = new Atom;

  # ATTRIBUTE templates.
  $gAttr = new Attribute('Attribute.GEN');
  $acAttr = new Attribute('Attribute.AuiCui');
  $cxtAttr = new Attribute('Attribute.CXT');
  $styAttr = new Attribute('Attribute.STY');

  # MERGE templates
  $gMerge = new Merge();

  # RELATION templates
  $gRel = new Relation();
  # for updates sources where sab and sol are different
  $gUpRel = new Relation();

  # CONTEXT templates
  $gCxt = new Context();
}

sub prepareSourceStatus {
  my ($ele, $temp, $strpSrc);
  #$rsab = $glInv->getReqEle('RSAB');
  #$Vsab = $glInv->getReqEle('VSAB');
  #$Vsab18 = substr($Vsab,0,18);

  # from cfg file, populate updpated, non-updated sources

  foreach $ele ($glInv->getList('SRC.updated')) {
    ($strpSrc) = split(/\_/, $ele);
    if ($strpSrc eq 'RXNORM') {
      $rsab = 'RXNORM';
      $Vsab = $ele;
      $Vsab18 = substr($Vsab, 0, 18);
      $styVsab = "E-$Vsab18";
    }
    if ($strpSrc eq 'SNOMEDCT_US') {
      # do not add this to updated list. 
      # always treat snomed as non updated src, but remember 
      # for special processing.
      $sctUpdated = 1;
      $sctVsab = $ele;
      $nuVsab2Ssab{"$ele"} = $strpSrc;
      $nuSsab2Vsab{"$strpSrc"} = $ele
    } else {
      $upVsab2Ssab{"$ele"} = $strpSrc;
      $upSsab2Vsab{"$strpSrc"} = $ele;
    }
  }
  foreach $ele ($glInv->getList('SRC.nonupdated')) {
    ($strpSrc) = split(/\_/, $ele);
    $nuVsab2Ssab{"$ele"} = $strpSrc;
    $nuSsab2Vsab{"$strpSrc"} = $ele
  }
}

sub process {

  # dump source meta data.
  $glInv->processSrc('RXNORM');

  # process conso file.
  &processConso;
  &processSat;
  &processRel;
  &processMerge;
  &processSty;
}

sub processConso {
  $glInv->prTime("\tBegin Conso.");

  my ($ign, $cui, $lang, $ts, $lui, $stt, $sui, $isp, $rxaui, $saui, $scui);
  my ($sdui, $sab, $tty, $code, $name, $srl, $supp, $thisId);
  my ($Aui, $sctSaid);

  # process conso file.
  open (CONSO, "<:utf8", $glInv->getReqEle('File.conso'))
    or die "Could not open input File.conso file.\n";

  # we ignore snomedct atoms here.(whether updated or not)
  while(<CONSO>) {
    chomp;
    ($cui, $lang, $ts, $lui, $stt, $sui, $isp, $rxaui, $saui, $scui, $sdui,
     $sab, $tty, $code, $name, $srl, $supp) = split(/\|/, $_);
    $supp = 'N' if ($supp eq '');
    if ($sab eq 'RXNORM') {
      # dump all rxnorm atoms
      $gAtom->dumpAtom({sab => $Vsab, tty => $tty, code => $code,
			status => $tty2status{"$tty"}, tbr => $sup2tbr{"$supp"},
			rlsd => 'N',name => &trim($name), supp => $supp,
			saui => $saui, scui => $scui, sdui => $sdui});
      # no need to add lrc => $rxaui
      $thisId = $gAtom->getLastId();
      $rxaui2said_Rx{"$rxaui"} = $thisId;
      push(@{$rxCuis{"$cui"}}, $rxaui);

      # A1: create RXAUI and RXCUI attributes - rxnorm
      $acAttr->dumpAttr({id => $rxaui, aname => 'RXAUI', aval => $rxaui,
			 idt => 'SOURCE_AUI', idq => $Vsab});
      $acAttr->dumpAttr({id => $rxaui, aname => 'RXCUI', aval => $cui,
			 idt => 'SOURCE_AUI', idq => $Vsab});

      # M1: merge between rxaui to rxcui of RxNorm
      $gMerge->dumpMerge({id1 => $rxaui, id2 => $cui, mset => 'RXNORM-CID',
			 idt1 => 'SOURCE_AUI', idq1 => $Vsab,
			 idt2 => 'SOURCE_CUI', idq2 => $Vsab});

    } elsif (defined($upSsab2Vsab{"$sab"})) {
      # dump updated sources. (all except snomedct)
      $gAtom->dumpAtom({sab => $upSsab2Vsab{"$sab"}, tty => $tty, code => $code,
			status => 'N', tbr => $sup2tbr{"$supp"},
			rlsd => 'N', name => &trim($name), supp => $supp,
			saui => $saui, scui => $scui, sdui => $sdui});
      # no need to add lrc => $rxaui
      $thisId = $gAtom->getLastId();
      $rxaui2said_Up{"$rxaui"} = $thisId;
      push(@{$upCuis{"$cui"}}, $rxaui);

      # A2: cretae RXAUI and RXCUI attributes - updated sources
      $acAttr->dumpAttr({id => $thisId, aname => 'RXAUI', aval => $rxaui,
			 idt => 'SRC_ATOM_ID', idq => ''});
      $acAttr->dumpAttr({id => $thisId, aname => 'RXCUI', aval => $cui,
			 idt => 'SRC_ATOM_ID', idq => ''});


    } elsif ($sab eq 'SNOMEDCT_US' && $sctUpdated == 1) {
      # updated scnomedct case. This is same as the non-updated sources case
      # except that here we use saids as opposed to AUIs in non-updated case.
      $rxaui2said_Sct{"$rxaui"} = 0;
      push(@{$sctCuis{"$cui"}}, $rxaui);
      $sctSaid = $rxaui2sctSaid{"$rxaui"};

      # A3: cretae RXAUI and RXCUI attributes - SNOMED
      $acAttr->dumpAttr({id => $sctSaid, aname => 'RXAUI', aval => $rxaui,
			 idt => 'SRC_ATOM_ID', idq => ''});
      $acAttr->dumpAttr({id => $sctSaid, aname => 'RXCUI', aval => $cui,
			 idt => 'SRC_ATOM_ID', idq => ''});
    } else {
      # other non updated sources (may include snomed if it is not updated)
      # check that an Aui is available.
      if (!defined($rxaui2Aui{"$rxaui"})) {
	print ERRS "ERROR: rxaui: $rxaui in non updated sources has no ";
	print ERRS "corresponding umls aui. Skipping.\n";
      } else {
	$rxaui2said_Nu{"$rxaui"} = 0;
	push(@{$nuCuis{"$cui"}}, $rxaui);
	$Aui = $rxaui2Aui{"$rxaui"};

	# A4: cretae RXAUI and RXCUI attributes - non updated sources
	$acAttr->dumpAttr({id => $Aui, aname => 'RXAUI', aval => $rxaui,
			   idt => 'AUI', idq => ''});
	$acAttr->dumpAttr({id => $Aui, aname => 'RXCUI', aval => $cui,
			   idt => 'AUI', idq => ''});
      }
    }
  }

  close(CONSO);

  open (OUT1, "> ../tmp/rsaids") or die "could not open rsaids\n";
  open (OUT2, "> ../tmp/rcuis") or die "could not open rcuis\n";
  open (OUT3, "> ../tmp/usaids") or die "could not open usaids\n";
  open (OUT4, "> ../tmp/ucuis") or die "could not open ucuiss\n";
  open (OUT5, "> ../tmp/nauis") or die "could not open nauis\n";
  open (OUT6, "> ../tmp/ncuis") or die "could not open ncuiss\n";
  open (OUT7, "> ../tmp/ssaids") or die "could not open ssaids\n";
  open (OUT8, "> ../tmp/scuis") or die "could not open scuis\n";

  foreach $rxaui (keys (%rxaui2said_Rx)) {
    print OUT1 "$rxaui|$rxaui2said_Rx{$rxaui}\n"; }

  foreach $rxaui (keys (%rxCuis)) { 
    print OUT2 "$rxaui|@{$rxCuis{$rxaui}}\n"; }

  foreach $rxaui (keys (%rxaui2said_Up)) {
    print OUT3 "$rxaui|$rxaui2said_Up{$rxaui}\n"; }

  foreach $rxaui (keys (%upCuis)) { 
    print OUT4 "$rxaui|@{$upCuis{$rxaui}}\n"; }

  foreach $rxaui (keys (%rxaui2Aui)) {
    print OUT5 "$rxaui|$rxaui2Aui{$rxaui}\n"; }

  foreach $rxaui (keys (%nuCuis)) { 
    print OUT6 "$rxaui|@{$upCuis{$rxaui}}\n"; }

  foreach $rxaui (keys (%rxaui2said_Sct)) {
    print OUT7 "$rxaui|$rxaui2said_Sct{$rxaui}\n"; }

  foreach $rxaui (keys (%sctCuis)) { 
    print OUT8 "$rxaui|@{$sctCuis{$rxaui}}\n"; }



  close(OUT1);
  close(OUT2);
  close(OUT3);
  close(OUT4);
  close(OUT5);
  close(OUT6);
  close(OUT7);
  close(OUT8);

  $glInv->prTime("\tEnd Conso.");
}


sub processSat {
  $glInv->prTime("\tBegin Sat.");
  # process attr file.
  open (SAT, "<:utf8", $glInv->getReqEle('File.sat'))
    or die "Could not open input File.sat file.\n";
  my ($cui, $lui, $sui, $rxaui, $type, $code, $atui, $satui, $atn, $sab);
  my ($atval, $supp, $ign);
  while (<SAT>) {
    chomp;
    ($cui, $lui, $sui, $rxaui, $type, $code, $atui, $satui, $atn, $sab,
     $atval, $supp) = split(/\|/, $_);

    # skip UMLSAUI and UMLSCUI attributes.
    next if ($atn eq 'UMLSAUI' or $atn eq 'UMLSCUI');

    if ($sab eq 'RXNORM') {
      # RxNorm attributes.
      if (!defined($rxaui2said_Rx{"$rxaui"})) {
	print ERR "ERROR: Attribute $atn encountered for undefined ";
	print ERR "rxaui: $rxaui. Skipping.\n";
	next;
      }
      # A5: dump rxnorm attribute.
      $gAttr->dumpAttr({id => $rxaui, alvl => 'S', aname => $atn,
			aval => &trim($atval), sab => $Vsab, stat => 'R',
			tbr => 'Y', rlsd => 'N', supp => $supp,
			idt => 'SOURCE_AUI', idq => $Vsab});

    } elsif (defined($upSsab2Vsab{"$sab"})) {
      # A6: dump other updated sources attributes.
      $gAttr->dumpAttr({id => $rxaui2said_Up{"$rxaui"}, alvl => 'S',
			aname => $atn, aval => &trim($atval), 
			sab => $upSsab2Vsab{"$sab"}, stat => 'R',tbr => 'Y',
			rlsd => 'N', supp => $supp,
			idt => 'SRC_ATOM_ID', idq => ''});
    } elsif ($sab eq 'SNOMEDCT_US' && $sctUpdated == 1) {
      # updated snomed attributes. ignore
      next;
    } else {
      # attributes for non updated sources. These (must) have Auis.
      # ignore these.
      next;
    }
  }
  close(SAT);
  $glInv->prTime("\tEnd Sat.");
}

sub processRel {
  $glInv->prTime("\tBegin Rel.");
  # convert RO => RT, RB => BT, RN => NT RQ => RT?.
  # ignore CHD, PAR and SIB relationships.
  open (iREL, "<:utf8", $glInv->getReqEle('File.rel'))
    or die "Could not open input File.rel file.\n";
  my ($cui1, $aui1, $ty1, $rel, $cui2, $aui2, $ty2, $rela, $rui, $srui);
  my ($sab, $sl, $rg, $dir, $supp);
  my ($said, $Aui, $ign, %ignoreRel, %transformRel, $said1, $said2);
  my $upVsab;

  # find from config file what rels to ignore nad which ones to transform.
  foreach $ign ($glInv->getList('Rel.Ignore')) { 
    print "Ignoring rel: $ign\n";
    $ignoreRel{"$ign"} = 0; 
  }
  %transformRel = $glInv->getHash('Raw2Rel');
  foreach my $ele (keys (%transformRel)) {
    print "Transforming $ele to $transformRel{$ele}\n";
  }

  while(<iREL>) {
    chomp;
    ($cui1, $aui1, $ty1, $rel, $cui2, $aui2, $ty2, $rela, $rui, $srui,
     $sab, $sl, $rg, $dir, $supp) = split(/\|/, $_);

    # ignore if in ignore rels list.
    next if (defined($ignoreRel{"$rel"}));

    # transform the rel, if in trans list.
    $rel = $transformRel{"$rel"} if (defined($transformRel{"$rel"}));
    # if $supp is empty, use 'N'
    $supp = 'N' if ($supp eq '');


    if ($sab eq 'RXNORM') {
      # R1: Do the main RXNORM RXCUI1 <-> RXNORM Cui2 rels.
      if ($ty1 eq 'CUI' && $cui1 le $cui2) {
	if (!defined($rxCuis{"$cui1"})) {
	  print ERR "cui1 = $cui1 defined in rel file is not a valid rxcui\n";
	} elsif (!defined($rxCuis{"$cui2"})) {
	  print ERR "cui2 = $cui2 defined in rel file is not a valid rxcui\n";
	} else {
	  $gRel->dumpRel({id1 => $cui1, rname => $rel, rela => $rela,
			  id2 => $cui2, supp =>$supp,
			  idt1 => 'SOURCE_CUI', idq1 => $Vsab,
			  idt2 => 'SOURCE_CUI', idq2 => $Vsab,
			  srui => $srui});
	}
      } elsif ($ty1 eq 'AUI') {
	# first make sure that aui1 is rxnorm
	if (!defined($rxaui2said_Rx{"$aui1"})) {
	  print ERR "aui1 = $aui1 is not a valid rxnorm aui. Skipping\n";
	  next;
	}

	# ignore these. (since source is providing bidirectional rels.
	if (!($aui1 le $aui2 or $rel eq 'SY')) { next; }


	if ((defined($rxaui2said_Rx{"$aui2"}))
	    or (defined($rxaui2said_Up{"$aui2"}))) {
	  # Case1: 2nd atom is in rxnorm or in updated source.
	  # get aui2s said
	  $said = 0;
	  $said = $rxaui2said_Rx{"$aui2"} if (defined($rxaui2said_Rx{"$aui2"}));
	  if ($said == 0) {
	    $said = $rxaui2said_Up{"$aui2"} if (defined($rxaui2said_Up{"$aui2"}));
	  }
	  # R2: Do RXNORM RXAUI1<-> RXNORM-or-other-updated_source AUI2 rels.
	  $gRel->dumpRel({id1 => $aui1, rname => $rel, rela => $rela,
			  id2 => $said, supp =>$supp,
			  idt1 => 'SOURCE_AUI', idq1 => $Vsab,
			  idt2 => 'SRC_ATOM_ID', idq2 => '', srui => $srui});
	} elsif (defined ($rxaui2sctSaid{"$aui2"}) && $rel eq 'SY') {
	  # case2: 2nd atom is in updated snomed. This is essentially the
	  # same as case3, except here we use snomed atom's said as opposed
	  # to a non-updated souce atom's atui.
	  # R3: 2nd atom is updated SNOMEDCT_US
	  $said2 = $rxaui2sctSaid{"$aui2"};
	  $gRel->dumpRel({id1 => $aui1, rname => $rel, rela => $rela,
			  id2 => $said2, supp => 'N',
			  idt1 => 'SOURCE_AUI', idq1 => $Vsab,
			  idt2 => 'SRC_ATOM_ID', idq2 => '',
			  srui => $srui});
	} else {
	  # case3: 2nd atom is in non-updated sources.
	  if (defined($rxaui2Aui{"$aui2"})) {
	    # R4: other non updated case (but not SNOMED)
	    $Aui = $rxaui2Aui{"$aui2"};
	    $gRel->dumpRel({id1 => $aui1, rname => $rel, rela => $rela,
			    id2 => $Aui, supp => 'N',
			    idt1 => 'SOURCE_AUI', idq1 => $Vsab,
			    idt2 => 'AUI', idq2 => '', srui => $srui});
	  } else {
	    print ERR "non-updated aui2: $aui2 from sab: $sab is not ";
	    print ERR "present in AUIs collected from db. Skipping.\n";
	    next;
	  }
	}
      } else {
	print ERR "Given type1 $ty1 is neither CUI nor AUI. Skipping.\n";
	next;
      }

      # still inside RXNORM loop. create RXNORM-SYR merges here.
      ## M8-12: merge between Rxnorm's rxaui1 to .... if rel = SY
      if ($rel eq 'SY'
	  && $ty1 eq 'AUI'
	  && defined($rxaui2said_Rx{"$aui1"})) {

	if ($aui2 =~ /^A/) {
	  # M8: merge between rxaui1 to rxaui2 as AUI
	  $gMerge->dumpMerge({id1 => $aui1, id2 => $aui2, mset => 'RXNORM-SYR',
			      idt1 => 'SOURCE_AUI', idq1 => $Vsab,
			      idt2 => 'AUI', idq2 => ''});
	} else {
	  # aui2 is not a meta (UMLS) aui.
	  if (defined($rxaui2said_Rx{"$aui2"})) {
	    # M9: aui2 belongs to rxnorm's, merge between rxnorm aui1 and aui2.
	    $gMerge->dumpMerge({id1 => $aui1, id2 => $aui2,
				mset => 'RXNORM-SYR',
				idt1 => 'SOURCE_AUI', idq1 => $Vsab,
				idt2 => 'SOURCE_AUI', idq2 => $Vsab});

	  } else {
	    if (defined($rxaui2said_Up{"$aui2"})) {
	      # M10: rel between rxnorm aui and updated said
	      $gMerge->dumpMerge({id1 => $aui1, id2 => $rxaui2said_Up{"$aui2"},
				  mset => 'RXNORM-SYR',
				  idt1 => 'SOURCE_AUI', idq1 => $Vsab,
				  idt2 => 'SRC_ATOM_ID', idq2 => ''});

	    } elsif (defined($rxaui2sctSaid{"$aui2"})) {
	      # M11: take care of snomed fist. rest is non-updated.
	      $gMerge->dumpMerge({id1 => $aui1, id2 => $rxaui2sctSaid{"$aui2"},
				  mset => 'RXNORM-SYR',
				  idt1 => 'SOURCE_AUI', idq1 => $Vsab,
				  idt2 => 'SRC_ATOM_ID', idq2 => ''});

	    } elsif (defined($rxaui2Aui{"$aui2"})) {
	      # M12: non-updated case.
	      $gMerge->dumpMerge({id1 => $aui1, id2 => $rxaui2Aui{"$aui2"},
				  mset => 'RXNORM-SYR',
				  idt1 => 'SOURCE_AUI', idq1 => $Vsab,
				  idt2 => 'AUI', idq2 => ''});
	    }
	  }
	}
      }

    } elsif (defined($upSsab2Vsab{"$sab"})) {
      # R5: Do rels between two other-updated-source AUIs
      # do not include SNOMEDCT_US. do only if aui1 < aui2
      if ($aui1 le $aui2
	  && (defined($rxaui2said_Up{"$aui1"}))
	  && (defined($rxaui2said_Up{"$aui2"}))) {

	# get said1 & said2  from updated list
	$said1 = $rxaui2said_Up{"$aui1"};
	$said2 = $rxaui2said_Up{"$aui2"};
	$upVsab = $upSsab2Vsab{"$sab"};
	$gUpRel->dumpRel({id1 => $said1, rname => $rel, rela => $rela,
			  id2 => $said2, supp => $supp,
			  idt1 => 'SRC_ATOM_ID', idq1 => '',
			  idt2 => 'SRC_ATOM_ID', idq2 => '', srui => $srui,
			  sab => $upVsab, sol => $upVsab});
      }
    }
  }

  close(iREL);
  $glInv->prTime("\tEnd Rel.");
}

sub processMerge {
  $glInv->prTime("\tBegin Merge.");
  ## M2: merge between updated said to rxnorm's cui
  my ($uCui, $uRxaui);
  foreach $uCui (keys (%upCuis)) {
    if (defined($rxCuis{"$uCui"})) {
      foreach $uRxaui (@{$upCuis{"$uCui"}}) {
	$gMerge->dumpMerge({id1 => $rxaui2said_Up{"$uRxaui"},
			    id2 => $uCui, mset => 'RXNORM-CID',
			    idt1 => 'SRC_ATOM_ID', idq1 => '',
			    idt2 => 'SOURCE_CUI', idq2 => $Vsab});
      }
    }
  }

  ## M3: merge between non updated Aui to rxnorm's cui 
  my ($nCui, $nRxaui);
  foreach $nCui (keys (%nuCuis)) {
    if (defined($rxCuis{"$nCui"})) {
      foreach $nRxaui (@{$nuCuis{"$nCui"}}) {
	$gMerge->dumpMerge({id1 => $rxaui2Aui{"$nRxaui"},
			    id2 => $nCui, mset => 'RXNORM-CID',
			    idt1 => 'AUI', idq1 => '',
			    idt2 => 'SOURCE_CUI', idq2 => $Vsab});
      }
    }
  }

  ## M4: merge between snomed (if updated) said to rxnorm's cui
  my ($sCui, $sRxaui);
  foreach $sCui (keys (%sctCuis)) {
    if (defined($rxCuis{"$sCui"})) {
      foreach $sRxaui (@{$sctCuis{"$sCui"}}) {
	$gMerge->dumpMerge({id1 => $rxaui2sctSaid{"$sRxaui"},
			    id2 => $nCui, mset => 'RXNORM-CID',
			    idt1 => 'SRC_ATOM_ID', idq1 => '',
			    idt2 => 'SOURCE_CUI', idq2 => $Vsab});
      }
    }
  }

  ## M5: merge with in updated sources, merge atoms with the same cui
  # fore each cui, merge the first said with each of the rest of the saids
  # in the same cui.
  my (@rxauis, $firstRxaui, $said1, $said2);
  foreach $uCui (keys (%upCuis)) {
    @rxauis = sort @{$upCuis{"$uCui"}};
    $firstRxaui = shift(@rxauis);
    $said1 = $rxaui2said_Up{"$firstRxaui"};
    foreach $uRxaui (@rxauis) {
      $gMerge->dumpMerge({id1 => $said1, id2 => $rxaui2said_Up{"$uRxaui"},
			  mset => 'RXNORM-CID',
			  idt1 => 'SRC_ATOM_ID', idq1 => '',
			  idt2 => 'SRC_ATOM_ID', idq2 => ''});
    }
  }

  ## M6: for non-updated Auis to updated said with the same cui.
  # this is one to many - each updated said to nonupdated auis.
  my ($Aui, $nRxaui, $said);
  foreach $uCui (keys (%upCuis)) {
    # take the first rxaui from the updated ones in this cui.
    ($uRxaui) =  sort (@{$upCuis{"$uCui"}});
    $said = $rxaui2said_Up{"$uRxaui"};
      foreach $nRxaui (@{$nuCuis{"$uCui"}}) {
	if (!defined ($rxaui2Aui{"$nRxaui"})) {
	  print ERR "rxaui = $nRxaui for non-updated src doesnot have AUI\n";
	} else {
	  $gMerge->dumpMerge({id1 => $rxaui2Aui{"$nRxaui"},
			      id2 => $said,
			      mset => 'RXNORM-CID',
			      idt1 => 'AUI', idq1 => '',
			      idt2 => 'SRC_ATOM_ID', idq2 => ''});
	}
      }
  }

  ## M7: merge between snomed (if updated) said to one of the saids for each
  # updated cuis. sct said to updated src atom's said
  foreach $sCui (keys (%sctCuis)) {
    if (defined($upCuis{"$sCui"})) {
      ($uRxaui) = sort (@{$upCuis{"$sCui"}});
      $said2 = $rxaui2said_Up{"$uRxaui"};
      foreach $sRxaui (@{$sctCuis{"$sCui"}}) {
	$said1 = $rxaui2sctSaid{"$sRxaui"};
	$gMerge->dumpMerge({id1 => $said1, id2 => $said2,
			    mset => 'RXNORM-CID',
			    idt1 => 'SRC_ATOM_ID', idq1 => '',
			    idt2 => 'SRC_ATOM_ID', idq2 => ''});
      }
    }
  }




  $glInv->prTime("\tEnd Merge.");
}

sub processSty {
  $glInv->prTime("\tBegin Sty.");
  # process semantic types.
  open (STY, "<:utf8", $glInv->getReqEle('File.sty'))
    or die "Could not open input File.sty file.\n";
  my ($ign, $cui, $tui, $stn, $sty, $rxaui, $said);
  my ($Aui);

  while(<STY>) {
    chomp;
    ($cui, $ign, $ign, $sty) = split(/\|/, $_);

    # A8: sty attribute for each RXNORM cui.
    if (defined($rxCuis{"$cui"})) {
      $styAttr->dumpAttr({id => $cui, aval => &trim($sty),
			  idt => 'SOURCE_CUI', idq => $Vsab,
			  sab => "$styVsab"});
    }

    # A9: dump sty attribute for each updated atom.
    if (defined($upCuis{"$cui"})) {
      foreach $rxaui (@{$upCuis{"$cui"}}) {
	$styAttr->dumpAttr({id => $rxaui2said_Up{"$rxaui"}, aval => &trim($sty),
			    idt => 'SRC_ATOM_ID', idq => '',
			    sab => "$styVsab"});
      }
    }

    ## for non-updated atoms. SNOMED is always in this case.
    # A10: snomed case.
    if ($sctUpdated == 1 && (defined($sctCuis{"$cui"}))) {
      foreach $rxaui (@{$sctCuis{"$cui"}}) {
	$styAttr->dumpAttr({id => $rxaui2sctSaid{"$rxaui"}, aval => &trim($sty),
			    idt => 'SRC_ATOM_ID', idq => '',
			    sab => "$styVsab"});
      }
    }

    # A11: other non-updated sources.
    if (defined($nuCuis{"$cui"})) {
      foreach $rxaui (@{$nuCuis{"$cui"}}) {
	if (defined ($rxaui2Aui{"$rxaui"})) {
	  $styAttr->dumpAttr({id => $rxaui2Aui{"$rxaui"}, aval => &trim($sty),
			      idt => 'AUI', idq => '',
			      sab => "$styVsab"});
	}
      }
    }
  }
  close(OUT);
  close(STY);
  $glInv->prTime("\tEnd Sty.");
}

our $cfgFile = "../etc/rxnorm.cfg";
our $ofErrors = "errors.rxnorm";
sub main {
  open (ERRS, ">:utf8", $ofErrors) or die "Couldn't open $ofErrors file.\n";

  $glInv = new NLMInv($cfgFile, *ERRS);
  $glInv->prTime("Begin");
  &checkNeededConfigInfo (\$glInv);

  $sctUpdated = $glInv->getEle('SNOMEDCT_US.Updated', 0);

  if (defined $options{g}) {
    &doOnce;
  } else {

    ## read rxaui2Aui and rxaui2sctSaid associations from files.
    my ($a, $b);
    # read rxaui2Aui
    open (IN, "<:utf8", $glInv->getEle('File.rxaui2Aui', '../etc/rxaui2Aui'))
      or die "Coudl not open file File.rxaui2Aui\n";
    %rxaui2Aui = ();
    while (<IN>) {
      chomp;
      ($a, $b) = split(/\|/, $_);
      $rxaui2Aui{"$a"} = $b;
    }
    close(IN);

    if ($sctUpdated == 1) {
      # read rxaui2sctSaid
      open (IN, "<:utf8", $glInv->getEle('File.rxaui2sctSaid', 
					 '../etc/rxaui2sctSaid'))
	or die "Could not open file File.rxaui2sctSaid\n";
      %rxaui2sctSaid = ();
      while(<IN>) {
	chomp;
	($a, $b) = split(/\|/, $_);
	$rxaui2sctSaid{"$a"} = $b;
      }
      close(IN);
    }

    ## initialize inv object to open files etc..
    $glInv->invBegin;

    ## initalize;
    &init;

    &process;
  }

  $glInv->prTime("End");
  $glInv->invEnd;
  close(ERRS);

}
&main;
