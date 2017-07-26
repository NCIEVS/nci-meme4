#!@PATH_TO_PERL@
#
#

unshift(@INC,".");
use lib "$ENV{INV_HOME}/lib";
use lib "$ENV{INV_HOME}/bin";
use lib ".";
use strict 'vars';
use strict 'subs';

use NLMConfig;
use Logger;
use SrcStats;
use QaStats;
use SrcDiffs;
use SrcQa;
use MakeDoc;


use Tk;
use Tk::NoteBook;
use Tk::Balloon;
use Tk::Dialog;
use Tk::ProgressBar;

our ($stats, $qstats, $diffs, $qa, $mrdoc);

our ($w1, $w2, $w3, $w4, $w5, $w6, $w7, $w8, $b1, $b2);

our ($Log, $Cfg);


# variables
our $cfgFile = '                               ';
our $cfgSelected = 0;

our $stat_inDir = "../src";
our $stat_outFile = "../etc/qa/srcStats";
our $stat_mode = 0;

our $qstat_inDir = "../src";
our $qstat_outFile = "../src/qaStats.src";

our $mrdoc_inDir = "../src";

our $dif_cfgFile = '';
our $dif_ADir = '';
our $dif_BDir = '';
our $dif_ODir = '';
our $dif_which = 31;
our $dif_hierTTYs = '';
our $dif_inclAtoms = '';
our $dif_exclAtoms = '';
our $dif_inclAttrs = '';
our $dif_exclAttrs = '';

our $diff_atomP = 1;
our $diff_attrP = 2;
our $diff_mrgP = 4;
our $diff_relP = 8;
our $diff_cxtP = 16;

our $qa_cfgFile = '';
our $qa_inDir = '';
our $qa_repFile = '';
our $qa_which = 31;

our $qa_atomP = 1;
our $qa_attrP = 2;
our $qa_mrgP = 4;
our $qa_relP = 8;
our $qa_cxtP = 16;



# log windows
our ($mw, $t_statLog, $t_qstatLog, $t_mrdocLog, $t_diffLog, $t_qaLog, $pbar);
our $percent_done = 100;

sub update_pbar {
  $percent_done = shift;
  $mw->update;
}

sub getDir {
  my $path = shift;
  my @inp = split(/\//, $path);
  pop(@inp);
  my $dir = join('/', @inp);
  return $dir;
}

sub stop {
  exit;
}

sub start {
  if (my $file = $mw->getOpenFile()) {
    print "Got the CFG file $file\n";
    $cfgFile = $file;
    my $dir = getDir($file);

    $Cfg = new NLMConfig($cfgFile);

    my $tdir = $Cfg->getEle('TEMPDIR', '../tmp');
    my $vsab = $Cfg->getEle('VSAB', 'NONE');
    my $lmode = $Cfg->getEle('LogMode', 'Append');
    $Log = new Logger("$tdir/Qa_${vsab}.log", $lmode, 'INFO');

    SrcDiffs->init(\$Log, \$Cfg);
    SrcQa->init(\$Log, \$Cfg);

    $stats = new SrcStats();
    $qstats = new QaStats();
    $mrdoc = new MakeDoc();
    $diffs = new SrcDiffs();
    $qa = new SrcQa();

    # set variables here
    # src stats variable
    $stat_inDir = $Cfg->getEle('BDir', "../src");
    $dir = $Cfg->getEle('QaDir', "../tmp");
    $stat_outFile = "$dir/srcStats";
    $stat_mode = $Cfg->getEle('Stats.Mode', 0);

    # qa stats variables.
    $qstat_inDir = $Cfg->getEle('BDir', "../src");
    $qstat_outFile = "$qstat_inDir/qaStats.src";

    # diffs variables.
    $dif_ADir = $diffs->getADir;
    $dif_BDir = $diffs->getBDir;
    $dif_ODir = $diffs->getODir;
    $dif_which = $diffs->getWhich;

    if ($dif_which & 1) { $diff_atomP = 1; } else { $diff_atomP = 0; }
    if ($dif_which & 2) { $diff_attrP = 2; } else { $diff_attrP = 0; }
    if ($dif_which & 4) { $diff_mrgP = 4;  } else { $diff_mrgP = 0;  }
    if ($dif_which & 8) { $diff_relP = 8;  } else { $diff_relP = 0;  }
    if ($dif_which & 16) { $diff_cxtP = 16;  } else { $diff_relP = 0;  }

    my @temp = $diffs->getHierTTYs;
    $dif_hierTTYs = "<@temp>";
    @temp = $diffs->getInclAtoms;
    $dif_inclAtoms = "<@temp>";
    @temp = $diffs->getExclAtoms;
    $dif_exclAtoms = "<@temp>";
    @temp = $diffs->getInclAttrs;
    $dif_inclAttrs = "<@temp>";
    @temp = $diffs->getExclAttrs;
    $dif_exclAttrs = "<@temp>";

    $qa_inDir = $qa->getInDir;
    $qa_repFile = $qa->getReportFile;

    $qa_which = $qa->getWhich;
    if ($qa_which & 1) { $qa_atomP = 1; } else { $qa_atomP = 0; }
    if ($qa_which & 2) { $qa_attrP = 2; } else { $qa_attrP = 0; }
    if ($qa_which & 4) { $qa_mrgP = 4;  } else { $qa_mrgP = 0;  }
    if ($qa_which & 8) { $qa_relP = 8;  } else { $qa_relP = 0;  }
    if ($qa_which & 16) { $qa_cxtP = 16;} else { $qa_cxtP = 0;  }

    $stats->setLogwin($t_statLog);
    $stats->setPbar(\&update_pbar);

    $qstats->setLogwin($t_qstatLog);
    $qstats->setPbar(\&update_pbar);

    $mrdoc->setLogwin($t_mrdocLog);
    $mrdoc->setPbar(\&update_pbar);

    $diffs->setLogwin($t_diffLog);
    $diffs->setPbar(\&update_pbar);

    $qa->setLogwin($t_qaLog);
    $qa->setPbar(\&update_pbar);

    $cfgSelected = 1;
  }
}

#------------
# main window
#------------
$mw = MainWindow->new(-title => 'Source Information');
our $mwtf = $mw->Frame;
$w1 = $mwtf->Button(-text => 'Config File', -command => \&start,
		  -justify => 'left');
$w2 = $mwtf->Label(-textvariable => \$cfgFile, -justify => 'left');

$w3 = $mwtf->Button(-text => 'Exit', -command => \&stop);

$pbar = $mw->ProgressBar(-width => 10, -from => 0, -to => 100, -blocks => 100,
			 -colors => [0, 'green', 50, 'yellow', 80, 'red'],
			 -variable =>\$percent_done)
  ->pack(-fill => 'x', -expand => 1);
# geometry
Tk::grid($w1, $w2, $w3, -sticky => 'w');
$mwtf->pack();

our $nb = $mw->NoteBook()->pack(-expand => 1,
				-fill => 'both');
our $msgArea = $mw->Label(-borderwidth => 2, -relief => 'groove')->pack();

# help messages
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'select configuration file',
	  -statusmsg => 'press to select a configuration file');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'Selected Configuration File');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w3, -balloonmsg => 'Exit the program',
	  -statusmsg => 'press to exit the application.');


# notebook pages
our $pg_stats = $nb->add('SrcStatsPage', -label => 'SrcStatistics')->pack();
our $pg_qstats = $nb->add('QaStatsPage', -label => 'QaStatistics')->pack();
our $pg_mrdoc = $nb->add('MrDocPage', -label => 'MrDoc.RRF')->pack();
our $pg_diffs = $nb->add('DiffsPage', -label => 'Differences')->pack();
our $pg_qa = $nb->add('QaPage', -label => 'QaReport')->pack();



#---------------------------------------------------------------------
# srcStats page Begin
#---------------------------------------------------------------------
our $f_tpStat = $pg_stats->Frame;
our $f_btStat = $pg_stats->Frame;

# widgets sourceDir and outputFile
$w1 = $f_tpStat->Button(-text => 'Source Dir',
			-command => \&stat_getDirName);
$w2 = $f_tpStat->Label(-textvariable => \$stat_inDir);

$w3 = $f_tpStat->Button(-text => 'OutputFile',
			-command => \&stat_getOutFile);
$w4 = $f_tpStat->Label(-textvariable => \$stat_outFile);

# help messages
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Path to src files',
	  -statusmsg => 'press to select any src file to selct src directory');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w3, -balloonmsg => 'Output File',
	  -statusmsg => 'press to specify output file.');
# geometry
Tk::grid($w1, $w2, $w3, $w4, -sticky => 'w');


# widgets
$w1 = $f_tpStat->Radiobutton(-text => 'Normal', -variable => \$stat_mode,
			    -value => '0');
$w2 = $f_tpStat->Radiobutton(-text => 'Extensive',
			     -variable => \$stat_mode,
			     -value => '1');
$w3 = $f_tpStat->Button(-text => 'Process', -command => \&stat_process);

# help messages
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'for normal src file statistics',
	  -statusmsg => 'press to select normal statistics collection');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'for extentive src file statistics',
	  -statusmsg => 'press to select extensive statistics collection.');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w3, -balloonmsg => 'Start collecting src statistics',
	  -statusmsg => 'press to initiate collection of src statistics');
# geometry
Tk::grid($w1, $w2, $w3, -sticky => 'w');

# attch log to the object.
$t_statLog = $f_btStat->Text()
  ->pack(-side => 'bottom',
	 -anchor => 's',
	 -expand => 1,
	 -fill => 'both');

$f_tpStat->pack(-anchor => 'w');
$f_btStat->pack(-fill => 'both',
		-expand => 1, -anchor => 'w');


#---------------------------------------------------------------------
# qaStats page Begin
#---------------------------------------------------------------------
our $f_tpQStat = $pg_qstats->Frame;
our $f_btQStat = $pg_qstats->Frame;

# widgets sourceDir and outputFile
$w1 = $f_tpQStat->Button(-text => 'Source Dir',
			-command => \&qstat_getDirName);
$w2 = $f_tpQStat->Label(-textvariable => \$stat_inDir);

# help messages
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Path to src files',
	  -statusmsg => 'press to select any src file to selct src directory');
# geometry
Tk::grid($w1, $w2, -sticky => 'w');


# widgets
$w1 = $f_tpQStat->Button(-text => 'Process', -command => \&qstat_process);

# help messages
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Start collecting QA statistics',
	  -statusmsg => 'press to initiate collection of QA statistics');
# geometry
Tk::grid($w1, -sticky => 'w');

# attch log to the object.
$t_qstatLog = $f_btQStat->Text()
  ->pack(-side => 'bottom',
	 -anchor => 's',
	 -expand => 1,
	 -fill => 'both');

$f_tpQStat->pack(-anchor => 'w');
$f_btQStat->pack(-fill => 'both',
		-expand => 1, -anchor => 'w');


#---------------------------------------------------------------------
# mrDoc page Begin
#---------------------------------------------------------------------
our $f_tpMrDoc = $pg_mrdoc->Frame;
our $f_btMrDoc = $pg_mrdoc->Frame;

# widgets sourceDir and outputFile
$w1 = $f_tpMrDoc->Button(-text => 'Source Dir',
			-command => \&qstat_getDirName);
$w2 = $f_tpMrDoc->Label(-textvariable => \$mrdoc_inDir);

# help messages
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Path to src files',
	  -statusmsg => 'press to select any src file to selct src directory');
# geometry
Tk::grid($w1, $w2, -sticky => 'w');


# widgets
$w1 = $f_tpMrDoc->Button(-text => 'Process', -command => \&mrdoc_process);

# help messages
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Start creating MRDOC.RRF',
	  -statusmsg => 'press to initiate creation of MRDOC.RRF');
# geometry
Tk::grid($w1, -sticky => 'w');

# attch log to the object.
$t_mrdocLog = $f_btMrDoc->Text()
  ->pack(-side => 'bottom',
	 -anchor => 's',
	 -expand => 1,
	 -fill => 'both');

$f_tpMrDoc->pack(-anchor => 'w');
$f_btMrDoc->pack(-fill => 'both',
		 -expand => 1, -anchor => 'w');


#---------------------------------------------------------------------
# Diffs page Begin
#---------------------------------------------------------------------


our $f_tpDiff = $pg_diffs->Frame;
our $f_btDiff = $pg_diffs->Frame;

# widgets

# ADir
$w1 = $f_tpDiff->Button(-text => 'ADir', -command => \&diff_getDir1,
		       -justify => 'left');
$w2 = $f_tpDiff->Label(-textvariable => \$dif_ADir, -justify => 'left');

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Left(A) side directory to compare',
	  -statusmsg => 'press to change the Left directory path');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'Path to Left (A) Dir',
	  -statusmsg => 'Left (A) directory selected for comparison');
Tk::grid($w1, $w2, -sticky => 'w');

# BDir
$w1 = $f_tpDiff->Button(-text => 'BDir', -command => \&diff_getDir2,
			-justify => 'left');
$w2 = $f_tpDiff->Label(-textvariable => \$dif_BDir, -justify => 'left');

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Right (B) side directory to compare',
	  -statusmsg => 'press to change the Right directory path');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'Path to Right (B) Dir',
	  -statusmsg => 'Right (B) directory selected for comparison');
Tk::grid($w1, $w2, -sticky => 'w');

# ODir
$w1 = $f_tpDiff->Button(-text => 'OutDir', -command => \&diff_getDir3,
			-justify => 'left');
$w2 = $f_tpDiff->Label(-textvariable => \$dif_ODir, -justify => 'left');

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'select output directory',
	  -statusmsg => 'press to change output directory');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'output directory selected',
	  -statusmsg => 'selected output directory');
Tk::grid($w1, $w2,-sticky => 'w');


# which
$w1 = $f_tpDiff->Checkbutton(-text => 'Atoms', -variable => \$diff_atomP,
			     -onvalue => 1, -offvalue => 0);
$w2 = $f_tpDiff->Checkbutton(-text => 'Attributes', -variable => \$diff_attrP,
			     -onvalue => 2, -offvalue => 0);
$w3 = $f_tpDiff->Checkbutton(-text => 'Merges', -variable => \$diff_mrgP,
			     -onvalue => 4, -offvalue => 0);
$w4 = $f_tpDiff->Checkbutton(-text => 'Relations', -variable => \$diff_relP,
			     -onvalue => 8, -offvalue => 0);
$w5 = $f_tpDiff->Checkbutton(-text => 'Contexts', -variable => \$diff_cxtP,
			     -onvalue => 16, -offvalue => 0);

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Compare Atoms',
	  -statusmsg => 'select to compare Atoms');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'Compare Attributes',
	  -statusmsg => 'select to compare Attributes');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w3, -balloonmsg => 'Compare Merges',
	  -statusmsg => 'select to compare Merges');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w4, -balloonmsg => 'Compare Relationships',
	  -statusmsg => 'select to compare Relationships');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w5, -balloonmsg => 'Compare Contexts',
	  -statusmsg => 'select to compare Contexts');

Tk::grid($w1, $w2, $w3, $w4, $w5, -sticky => 'w');


# widgets: MTHTTYs
$w1 = $f_tpDiff->Label(-text => 'MTHTTYs', -justify => 'left');
$w2 = $f_tpDiff->Label(-textvariable => \$dif_hierTTYs, -justify => 'left');

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'MTH assigned TTYs',
	  -statusmsg => 'CODE field is not used while comparing these ttys');

Tk::grid($w1, $w2);


# widgets: InclAtoms, ExclAtoms
$w1 = $f_tpDiff->Label(-text => 'InclAtoms', -justify => 'left');
$w2 = $f_tpDiff->Label(-textvariable => \$dif_inclAtoms, -justify => 'left');
$w3 = $f_tpDiff->Label(-text => 'ExclAtoms', -justify => 'left');
$w4 = $f_tpDiff->Label(-textvariable => \$dif_exclAtoms, -justify => 'left');

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'Atoms to be included for comparison',
	  -statusmsg => 'null value selects all tty types');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w4, -balloonmsg => 'Atoms to be excluded for comparison');

Tk::grid($w1, $w2, $w3, $w4);

# widgets: InclAttrs, ExclAttrs
$w1 = $f_tpDiff->Label(-text => 'InclAttrs', -justify => 'left');
$w2 = $f_tpDiff->Label(-textvariable => \$dif_inclAttrs, -justify => 'left');
$w3 = $f_tpDiff->Label(-text => 'ExclAttrs', -justify => 'left');
$w4 = $f_tpDiff->Label(-textvariable => \$dif_exclAttrs, -justify => 'left');

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'Attributes to be included for comparison',
	  -statusmsg => 'null value includes all the attributes');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w4, -balloonmsg => 'Attributes to be excluded for comparison');

Tk::grid($w1, $w2, $w3, $w4);


# widgets: Process & Exit
$w1 = $f_tpDiff->Button(-text => 'Process', -command => \&diff_process,
			-justify => 'left');

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Compare files',
	  -statusmsg => 'press to compare files');

Tk::grid($w1);

# attch log to the object.
$t_diffLog = $f_btDiff->Text()
  ->pack(-side => 'bottom',
	 -anchor => 's',
	 -expand => 1,
	 -fill => 'both');

$f_tpDiff->pack(-anchor => 'w');
$f_btDiff->pack(-fill => 'both',
		-expand => 1, -anchor => 'w');


#---------------------------------------------------------------------
# qa page Begin
#---------------------------------------------------------------------
our $f_tpQa = $pg_qa->Frame;
our $f_btQa = $pg_qa->Frame;


# widgets: inpDirectory
# input directory
$w1 = $f_tpQa->Button(-text => 'InputDir', -command => \&qa_getInpDir,
			-justify => 'left');
$w2 = $f_tpQa->Label(-textvariable => \$qa_inDir, -justify => 'left');

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'source file directory',
	  -statusmsg => 'press to change sources directory');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'selected source file directory');
Tk::grid($w1, $w2, -sticky => 'w');

# widgets: reportFile
$w1 = $f_tpQa->Button(-text => 'Report File', -command => \&qa_getRepFile,
		       -justify => 'left');
$w2 = $f_tpQa->Label(-textvariable => \$qa_repFile, -justify => 'left');

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Output Report File',
	  -statusmsg => 'press to change the output report file');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'selected report file');

Tk::grid($w1, $w2, -sticky => 'w');


# widgets: whichP
$w1 = $f_tpQa->Checkbutton(-text => 'Atoms', -variable => \$qa_atomP,
			     -onvalue => 1, -offvalue => 0);
$w2 = $f_tpQa->Checkbutton(-text => 'Attributes', -variable => \$qa_attrP,
			     -onvalue => 2, -offvalue => 0);
$w3 = $f_tpQa->Checkbutton(-text => 'Merges', -variable => \$qa_mrgP,
			     -onvalue => 4, -offvalue => 0);
$w4 = $f_tpQa->Checkbutton(-text => 'Relations', -variable => \$qa_relP,
			     -onvalue => 8, -offvalue => 0);
$w5 = $f_tpQa->Checkbutton(-text => 'Contexts', -variable => \$qa_cxtP,
			     -onvalue => 16, -offvalue => 0);

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'Qa Atoms',
	  -statusmsg => 'select to QA Atoms');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w2, -balloonmsg => 'Qa Attributes',
	  -statusmsg => 'select to QA Attributes');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w3, -balloonmsg => 'Qa Merges',
	  -statusmsg => 'select to QA Merges');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w4, -balloonmsg => 'Qa Relationships',
	  -statusmsg => 'select to QA Relationships');
$mw->Balloon(-statusbar => $msgArea)
  ->attach($w5, -balloonmsg => 'Qa Contexts',
	  -statusmsg => 'select to QA Contexts');

Tk::grid($w1, $w2, $w3, $w4, $w5, -sticky => 'w');


# widgets: Process & Exit
$w1 = $f_tpQa->Button(-text => 'Process', -command => \&qa_process,
			-justify => 'left');

$mw->Balloon(-statusbar => $msgArea)
  ->attach($w1, -balloonmsg => 'QA files',
	  -statusmsg => 'press to QA files');

Tk::grid($w1);

# attch log to the object.
$t_qaLog = $f_btQa->Text()
  ->pack(-side => 'bottom',
	 -anchor => 's',
	 -expand => 1,
	 -fill => 'both');


$f_tpQa->pack(-anchor => 'w');
$f_btQa->pack(-fill => 'both',
		-expand => 1, -anchor => 'w');

#---------------------------------------------------------------------
# callbacks
#---------------------------------------------------------------------
sub stat_getDirName {
  my $file;
  if ($file = $mw->getOpenFile()) {
    my @inp = split(/\//, $file);
    pop(@inp);
    $stat_inDir = join('/',@inp);
    print "inDir <$file> to <$stat_inDir>\n";
  }
}

sub stat_getOutFile {
  my $file;
  if ($file = $mw->getSaveFile()) {
    print "got $file\n";
    $stat_outFile = $file;
  }
}

sub stat_process {
  my $ans = &checkCFGFile();
  if ($ans != 1) {
    print "Select a config file first.\n";
    return;
  }
  &update_pbar(0);
  $t_statLog->insert('end',"Process called.\n");
  $t_statLog->insert('end',"\tinDir : $stat_inDir.\n");
  $t_statLog->insert('end',"\tourFie : $stat_outFile.\n");
  $t_statLog->insert('end',"\tmode : $stat_mode.\n");

  $stats->setMode($stat_mode);
  $stats->process($stat_inDir, $stat_outFile);
  $t_statLog->insert('end',"Done Processing Statistics.\n");
}



#===========
sub qstat_getDirName {
  my $file;
  if ($file = $mw->getOpenFile()) {
    my @inp = split(/\//, $file);
    pop(@inp);
    $qstat_inDir = join('/',@inp);
    print "inDir <$file> to <$qstat_inDir>\n";
  }
}

sub qstat_process {
  my $ans = &checkCFGFile();
  if ($ans != 1) {
    print "Select a config file first.\n";
    return;
  }
  &update_pbar(0);
  $t_qstatLog->insert('end',"Process called.\n");
  $t_qstatLog->insert('end',"\tinDir : $stat_inDir.\n");

  $qstats->process($qstat_inDir);
  $t_qstatLog->insert('end',"Done Processing Statistics.\n");
}


#===========
sub mrdoc_getDirName {
  my $file;
  if ($file = $mw->getOpenFile()) {
    my @inp = split(/\//, $file);
    pop(@inp);
    $mrdoc_inDir = join('/',@inp);
    print "inDir <$file> to <$mrdoc_inDir>\n";
  }
}

sub mrdoc_process {
  my $ans = &checkCFGFile();
  if ($ans != 1) {
    print "Select a config file first.\n";
    return;
  }
  &update_pbar(0);
  $t_mrdocLog->insert('end',"Process called.\n");
  $t_mrdocLog->insert('end',"\tinDir : $stat_inDir.\n");

  $mrdoc->makeMrDoc($mrdoc_inDir);
  $t_mrdocLog->insert('end',"Done Processing Statistics.\n");
}


#=====================
sub diff_getDir {
  my $abo = shift;
  my $file;
  if ($file = $mw->getOpenFile()) {
    my @inp = split(/\//, $file);
    pop(@inp);
    my $dir = join('/',@inp);
    if ($abo == 1) { $dif_ADir = $dir; }
    elsif ($abo == 2) { $dif_BDir = $dir; }
    elsif ($abo == 3) { $dif_ODir = $dir; }
  }
}

sub diff_getDir1 {
  my $file;
  if ($file = $mw->getOpenFile()) {
    my @inp = split(/\//, $file);
    pop(@inp);
    my $dir = join('/',@inp);
    $dif_ADir = $dir;
  }
}

sub diff_getDir2 {
  my $file;
  if ($file = $mw->getOpenFile()) {
    my @inp = split(/\//, $file);
    pop(@inp);
    my $dir = join('/',@inp);
    $dif_BDir = $dir;
  }
}

sub diff_getDir3 {
  my $file;
  if ($file = $mw->getOpenFile()) {
    my @inp = split(/\//, $file);
    pop(@inp);
    my $dir = join('/',@inp);
    $dif_ODir = $dir;
  }
}

#===============
sub diff_process {
  my $ans = &checkCFGFile();
  if ($ans != 1) {
    print "Select a config file first.\n";
    return;
  }
  &update_pbar(0);
  # first update all the values
  $diffs->setADir($dif_ADir);
  $diffs->setBDir($dif_BDir);
  $diffs->setODir($dif_ODir);
  # now set whichP
  my $which = $diff_atomP + $diff_attrP + $diff_relP + $diff_mrgP + $diff_cxtP;
  $diffs->setWhich($which);
  print "Process called.\n";
  $t_diffLog->insert('end',"Process called.\n");
  $t_diffLog->insert('end',"\tADir : $dif_ADir.\n");
  $t_diffLog->insert('end',"\tBDir : $dif_BDir.\n");
  $t_diffLog->insert('end',"\tODir : $dif_ODir.\n");
  $t_diffLog->insert('end',"\twhich = $which.\n");
  $diffs->process;
  $t_diffLog->insert('end',"Done Processing Differences.\n");
}



sub qa_getInpDir {
  my $file; 
  if ($file = $mw->getOpenFile()) {
    my @inp = split(/\//, $file);
    pop(@inp);
    my $dir = join('/',@inp);
    $qa_inDir = $dir;
  }
}

sub qa_getRepFile {
  my $file;
  if ($file = $mw->getSaveFile()) {
    $qa_repFile = $file;
  }
}

sub qa_process {
  my $ans = &checkCFGFile();
  if ($ans != 1) {
    print "Select a config file first.\n";
    return;
  }
  &update_pbar(0);
  # first update all the values
  $qa->setInDir($qa_inDir);
  $qa->setReportFile($qa_repFile);
  # now set whichP
  my $which = $qa_atomP + $qa_attrP + $qa_relP + $qa_mrgP + $qa_cxtP;
  $qa->setWhich($which);
  $t_qaLog->insert('end', "Starting QA\n");
  $t_qaLog->insert('end', "\tConfig file: $qa_cfgFile\n");
  $t_qaLog->insert('end', "\tInput Dir :  $qa_inDir\n");
  $t_qaLog->insert('end', "\tReport File: $qa_repFile\n");
  $t_qaLog->insert('end', "\tWhich: $which\n");
  $qa->process;
  $t_qaLog->insert('end', "Done processing QA.\n");
}

sub checkCFGFile {
  if ($cfgSelected == 1) { return 1; }
  my $ans = $mw->Dialog(-title => 'Please choose',
			-text => 'Config file not selected yet.\nWould you like to select one now and proceed?',
			-default_button => 'yes', -buttons => ['yes', 'no'],
			-bitmap => 'question')->Show();
  if ($ans eq 'yes') {
    &start();
    if ($cfgSelected == 1) { return 1; }
  }
  return 0;
}

MainLoop;
