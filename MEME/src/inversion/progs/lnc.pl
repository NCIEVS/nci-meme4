#!@PATH_TO_PERL@
#
# Update on lnc.pl
# RC -- August 7, 2007 -- added a hash called vsab2hash to keep track of  LN_atoms ID with its corresponding vsab

unshift(@INC,".");
unshift(@INC, "/umls_dev/NLM/inv/bin3");

use lib "/umls_s/webapp_root/meme/lib";
use lib "/umls_s/webapp_root/meme/bin";
use lib "/umls_dev/NLM/inv/bin3";

use OracleIF;
use Midsvcs;

use strict 'vars';
use strict 'subs';

use Getopt::Std;

my %options=();
getopts("gh", \%options);

if (defined $options{h}) {
  print "Usage: lnc.pl -hg\n";
  print " This file inverts LOINC and produces the standard files.\n";
  print "\t-h prints this message.\n";
  print "\t-g generate inital files so actual inversion can be done.\n\n";
  print "First) How to generate the necessary input files:\n";
  print "\t1) Manually prepare the etc/MG_Abbrevs.* files from the \n";
  print "\t   manual.\n";
  print "\t2) Edit the orig/LOINCDB.TXT and remove the header info.\n";
  print "\t   and save it as LOINC_nohdr.txt file.\n";
  print "\t3) Update the etc/lnc.cfg file\n";
  print "\t4) run lnc.pl -g to generate the necessary intermediate files.\n\n";
  print "Second) Update the generated etc/MG_Abbrevs.*.ordered files.\n";
  print "\t1) Update the Abbrevs.Property.ordereded to bring Ratios\n";
  print "\t   to the begining of the file and save.\n";
  print "Third) run lnc.pl - without any arguments.\n";
  print "\tThis generates all the standard output files.[~ 15 min]\n";
  print "\nNote: Any special notes will be put here.\n";
  exit;
}



use NLMInv;
use Atom;
use Attribute;
use Relation;
use Merge;
use Context;
use IdGen;

our $cfgFile = "../etc/lnc.cfg";
our $ofErrors = "errors.loinc";
our $glInv;

our $mainGenId;
our $code = 0;
our %part_ids=();
our %part_types=();
our %MeshRegId = ();
our %clsAb2Name=();
our %clsAb2Code=();
our %CN_sty_valids=();
our %LS_sty=();
our %prevMthIds=();
our %vsab2hash=(); #key=code value=vsab2


our $maxMthIdNum = 0;

#---------------------
# constant data
#--------------------
our $vsab = '';
our $rsab = '';
our $vsab2 = '';
our ($pvsab, $prsab, $mvsab, $mrsab, $ovsab, $orsab);

# these are based on a statistical analysis done on LNC213
our %parttype2sty = ();
our %class2rela = ();

# set hash tables with 62 elements
# actual field names in LOINCDB file
our @FieldNames = ();
our @FieldSyms = ();
our @FieldVals;
our @names_tty=();
our %part_types=();
our %partRela= ();
our %inpFiles = ();
our $totalDBFields = 0;

our %obsl = ();
our %obsl2new=();
our $LN2_ids=();
our %seen_LS = ();
our %seen_CN= ();
our %seen_CX= ();
our %seen_SN=();
our %seen_SX=();
our %seen_LPN=();

our ($srcAtom, $hsAtom, $hcAtom, $lnAtom, $loAtom, $lxAtom, $xmAtom);
our ($olxAtom, $osnAtom, $oosnAtom, $lsAtom);
our ($cnAtom, $cxAtom, $snAtom, $sxAtom, $lpnAtom, $lpdnAtom);
#our ($plnAtom, $plxAtom, $olnAtom, $olxAtom, $mlnAtom, $mlxAtom);


our ($sAttr, $sosTgAttr, $defAttr, $styAttr, $styTgAttr, $xmapAttr);
our ($srcMerge, $exMerge, $syMerge, $tgexMerge, $srcRel);
our ($sRel, $tgRel, $sCxt, $HC_name, $HC_id);
our ($bad_parts, $bad_types, %rej_type, $no_terms, %MULT);
our (%HS_cxtIds, %HS_cxtNames);
%rej_type=();
%MULT=();
%HS_cxtIds=();
%HS_cxtNames=();



#--------------------------------------------------------------------
# subs utilities.
#--------------------------------------------------------------------
# strip blanks (at begin, end and more than 1 blank) in the given line.
# input - a string.
# output - string with blanks stripped.
sub stripBlanks {
  my($str, $doQuotes) = @_;

  $str =~ s/ {2,}/ /g;		# more than 1 blanks to 1 blank
  $str =~ s/^[ ]*//;		# strip leading blanks 
  $str =~ s/[ ]*$//;		# strip trailing blanks

  if ($doQuotes == 1) {
    $str =~ s/^\"//;		# strip leading double quote
    $str =~ s/\"$//;		# strip trailing double quote
    $str =~ s/^[ ]*//;		# strip leading blanks 
    $str =~ s/[ ]*$//;		# strip trailing blanks
  }

  return $str;
}


# ----------------------------------------------------------------------
# soruce specific utilities
# ----------------------------------------------------------------------
# generate ABBREVS2 files which are expanded by length from the ABBREVS file.
# this reads ABBREVS files from etc directory and writes the sorted ones in
# etc2 directory.
sub genAbbrevFilesByLength {
  my ($abbr, $expnd);
  foreach my $fld ('Analyte', 'Challenge', 'ChallengeDelay',
		   'ChallengeRoute', 'ChallengeType', 'Method', 'Person',
		   'Property', 'Scale', 'Super', 'System', 'Timing',
		   'TimingAspect') {
    open (IN, "<:utf8", $inpFiles{"$fld"}) 
      or die "Couldn't open ABBREVS file for $fld\n";

    open (OUT, ">:utf8", "$inpFiles{$fld}.ordered")
      or die "Couldn't open Abbrevs.$fld.ordered\n";
    my %dict=();
    while (<IN>) {
      chomp;
      if ( /^#/ ) {
	print OUT "$_\n";
	next;}

      if(/^ *$/) { next; }
	
      ($abbr, $expnd) = split(/\|/);
      $dict{"$abbr"} = $expnd;
    }
    close(IN);
    foreach my $abbr (sort { length($b) cmp length($a) } (keys %dict)) {
      print OUT "$abbr|$dict{$abbr}\n";
    }
    close(OUT);
  }
}

# ------------------------------------------------------------
# there are a number of files ABBREVS.* in /etc directory containing 
# information about abbreviations and their corresponding expanded forms.
# we are trying to create a subroutine to do these expansions - 1 sub for
# each file. Each sub is like "expand_ANALYTE" where it expands a given string
# in the ABBREVS.ANALYTE file and returns the expanded form.
sub makeAbbrs_byInOrder {
  my ($abbr, $expnd, $expand, $strSub);
  open (OUT, ">:utf8", $glInv->getEle('File.OUT.ABBRS'))
    or die "Could not open File.OUT.ABBRS file.\n";

  foreach my $fld ('Analyte', 'Challenge', 'ChallengeDelay', 
		   'ChallengeRoute', 'ChallengeType', 'Method', 'Person',
		   'Property', 'Scale', 'Super', 'System', 'Timing',
		   'TimingAspect') {
    open (IN, "<:utf8", "$inpFiles{$fld}.ordered") 
      or die "Couldn't open ABBREVS file for $fld\n";

    $expand = '';
    $strSub = ' my($str) = @_;'."\n";
    while (<IN>) {
      chomp;
      next if ( /^#/ );
      next if(/^ *$/);
      next if(/^$/);
      ($abbr, $expnd) = split(/\|/);
      $strSub.= "   \$str =~ s#\\b\\Q$abbr\\E\\b#$expnd#g;\n";
    }
    close(IN);

    $strSub.= "    return \$str;\n";

    eval "sub expand_$fld {\n$strSub\n}";
    print OUT "sub expand_$fld {\n$strSub\n}\n";
  }
  &makeLittleMap_byLength(1, \*OUT);
  close(OUT);
}


# keep the longest ones at the top.
sub makeLittleMap_byLength {
  my $writeOut = shift;
  my $ofhOUT = shift;
  my %dict = ();
  my ($abbr, $expnd, $expand, $strSub);
  open (IN, "<:utf8", $inpFiles{'LittleMap'}) or
    die "Couldn't open Little.Map\n";
  while (<IN>) {
    chomp;
    next if ( /^#/ );
    next if(/^(\s)+$/);
    next if(/^$/);
    ($abbr, $expnd) = split(/\|/);
    $dict{"$expnd"} = $abbr;
  }
  close(IN);
  $expand = '';
  $strSub = ' my($str) = @_;'."\n";
  foreach $abbr (sort { length($b) cmp length($a) } (keys %dict)) {
    $strSub.= "   \$str =~ s#\\Q$abbr\\E#$dict{$abbr}#g;\n";
  }
  $strSub.= "    return \$str;\n";

  eval "sub shorten_LITTLE {\n$strSub\n}";
  if ($writeOut == 1) {
    print $ofhOUT "sub shorten_LITTLE {\n$strSub\n}";
  }
}

sub getLsCnCxSnSxIdsFromDb {
  my $db = Midsvcs->get('editing-db');
  my $oracleuser = 'meow';
  my $oraclepassword = GeneralUtils->getOraclePassword($oracleuser,$db);
  my $dbh = new OracleIF("db=$db&user=$oracleuser&password=$oraclepassword");

  # get the latest sab (LNC, LNC_MDS, LNC_OASIS, LNC_PHQ_9)
  my @ans = $dbh->selectAllAsRef
    ("SELECT cl.tty, cl.code, st.string
        FROM classes cl, string_ui st
        WHERE cl.source like 'LNC%'
          AND cl.tty in ('LS', 'CN', 'CX', 'SN', 'SX')
          AND cl.sui = st.sui");

  # save in a file
  open (OUT, ">:utf8", $glInv->getEle('File.INP.MthIds'))
    or die "Could not open File.INP.MthIds.\n";
  my $dt = `date`;
  print OUT "\#MthIds generated on $dt";
  print OUT "\#TTY|Code|String\n";
  my $val;
  foreach $val (@ans) {
    print OUT "$val->[0]|$val->[1]|$val->[2]\n";
  }
  close(OUT);
  print "Got MthIds from DB.\n";
  $dbh->disconnect;
}


sub makeLOINCIntFiles {

  ## first make INT.LOINC from INP.LOINCDB_nohdr.
  # in format is cp1252. out is utf8. Replace 
  #     x2013/2014 to '-'
  #     x2018/2019 to "'"
  #     X201C/201D to '"'
  # always replace x00A0(NBSP) to space.

  open (IN, "<:encoding(cp1252)", $inpFiles{"LOINCDB_nohdr"}) or
    die "Could not open INP.LOINCDB_nohdr(from cfg) file.\n";
  open (OUT, ">:utf8", $glInv->getEle('File.INT.LOINC')) or
    die "could not open INT.LOINC file.\n";

  my ($code, $pType);
  while (<IN>) {
    chomp;
    next if(/^\s*$/);
    s/\r//;                     # replace pc LB
    s/\x{00A0}/ /g;		# 0x00A0  - NO-BREAK SPAC (NBSP)
    # if the incoming file has any |s
    s/\|/&#124;/g;	        # any pipes need to converted to XML char. ent.

      # we want to replace degree symbol with nothing. The degree symbol is 
      # actually x00B0. Instead LOINC is using x00F8 (LATIN SMALL LETTER 0
      # WITH STROKE). so we replace that char with nothing.
      s/\xF8//;			# degree symbol deleted, see KM mail 10/13/03
    #s/\x{00B0}//;		# degree symbol deleted, see KM mail 10/13/03

    s/\x{2013}/-/g;	        # 0x2013  - EN DASH
    s/\x{2014}/-/g;	        # 0x2014  - EM DASH

    s/\x{2018}/\'/g;		# 0x2018  - LEFT SINGLE QUOTATION MARK
    s/\x{2019}/\'/g;		# 0x2019  - RIGHT SINGLE QUOTATION MARK

    s/\x{201C}/\"/g;	        # 0x201C  - LEFT DOUBLE QUOTATION MARK
    s/\x{201D}/\"/g;		# 0x201D  - RIGHT DOUBLE QUOTATION MARK

    s/\t/|/g;	                # all tabs convert to pipe

    s/^\"//;			# " at the beginning of the first record
    s/\"$//;			# " at the end of the last record 
    s/\"\|/|/g;			# all " at the end of a field
    s/\|\"/|/g;			# all " at the start of a field
    s/\"\|\"/\|/g;
    s/\"\"/\"/g;		# all 2dbl quotes to 1dbl quote (fake ")
    s/ {2,}/ /g;                # all multiple spaces to single space
    s/ \|/|/g;	                # all spaces at the end of a field
    s/\| /|/g;	                # all spaces at the begining of a field

    print OUT "$_\n";
  }

  close(IN);
  close(OUT);

  ## for loinc parts and links file, there aren't any substitutions. so
  # we can use NLMInv method ConvertRaw2Utf method. double quotes are
  # preserved in the file. Those needs to be dealt with in the prog.
  ## now make INT.LOINC_PARTS.
  $glInv->convertRaw2Utf($inpFiles{"LOINC_PARTS"}, 'cp1252',
			 $glInv->getEle('File.INT.LOINC_PARTS'));


  # convert links file to utf8 in the etc directory
  $glInv->convertRaw2Utf($inpFiles{"LOINC_LINKS"}, 'cp1252',
			 $glInv->getEle('File.INT.LOINC_LINKS'));


  # now get mthids from the db.
  &getLsCnCxSnSxIdsFromDb;
}


sub getMthId {
  my $ttyStr = shift;
  if (defined $prevMthIds{"$ttyStr"}) { return $prevMthIds{"$ttyStr"}; }
  $maxMthIdNum++;
  return sprintf("MTHU%06d", $maxMthIdNum);
}


# ----------------
# init
# ----------------
sub init0 {
  # get all input filenames
  %inpFiles = $glInv->getHash('File.INP');

  # get parttype to sty associations
  %parttype2sty = $glInv->getHash('PartType2STY');

  # get part to rela associations
  %partRela = $glInv->getHash('Part2RELA');

  # get class to rela associations
  %class2rela = $glInv->getHash('Class2RELA');


  # modify names here from SUPER_SYSTEM to 'SUPER SYSTEM'
  # and from TIME_MODIFIER to 'TIME MODIFIER' and
  # FRAGMENTS_FOR_SYNONYMS to 'FRAGMENTS FOR SYNONYMS'
  $partRela{'SUPER SYSTEM'} = $partRela{'SUPER_SYSTEM'};
  delete $partRela{'SUPER_SYSTEM'};

  $partRela{'TIME MODIFIER'} = $partRela{'TIME_MODIFIER'};
  delete $partRela{'TIME_MODIFIER'};

}


sub init {
  &init0;

  # create a main generator with proper seeds.
  $mainGenId = new IdGen();
  $mainGenId->startMthId(1000);

  $vsab = $glInv->getEle('VSAB');
  $rsab = $glInv->getEle('RSAB');

  $mvsab = $glInv->getEle('MDSVSAB');
  $mrsab = $glInv->getEle('MDSRSAB');
  $pvsab = $glInv->getEle('PHQVSAB');
  $prsab = $glInv->getEle('PHQRSAB');
  $ovsab = $glInv->getEle('OASISVSAB');
  $orsab = $glInv->getEle('OASISRSAB');

  $totalDBFields = $glInv->getEle('NumberOfDBFields');

  # read names and their corresponding symbols
  my ($ele, $name, $sym);
  foreach $ele ($glInv->getList('LNCFieldNameSym')) {
    ($name, $sym) = split(/\ /, $ele);
    push (@FieldNames, $name);
    push(@FieldSyms, $sym);
  }

  # create abbreviations.
  &makeAbbrs_byInOrder;

  # read already assigned mthids.
  $maxMthIdNum = 0;
  my $curNum = 0;
  my ($tty, $mthid, $str, $curNum);
  open(IN, "<:utf8", $glInv->getEle('File.INP.MthIds'))
    or die "Could not open File.INP.MthIds\n";
  while (<IN>) {
    s/#.*//;           # ignore comments by erasing them
    next if /^(\s)*$/; # ignore blank lines
    chomp;
    ($tty, $mthid, $str) = split(/\|/, $_);
    $prevMthIds{"$tty|$str"} = $mthid;

    # read the number followed by MTHU in the mthid read.
    $curNum = substr($mthid, 4,6);
    if ($curNum > $maxMthIdNum) {
      $maxMthIdNum = $curNum;
    }
  }
  close(IN);

  # make print templates
  # ATOM templates
  $hsAtom = new Atom('Atom.HS');
  $hcAtom = new Atom('Atom.HC');

  $lnAtom = new Atom('Atom.LN');
  $loAtom = new Atom('Atom.LO');
  $lxAtom = new Atom('Atom.LX');
  $olxAtom = new Atom('Atom.OLX');
  $osnAtom = new Atom('Atom.OSN');
  $oosnAtom = new Atom('Atom.OOSN');

  $lsAtom = new Atom('Atom.LS');
  $cnAtom = new Atom('Atom.CN');
  $cxAtom = new Atom('Atom.CX');
  $snAtom = new Atom('Atom.SN');
  $sxAtom = new Atom('Atom.SX');
  $lpnAtom = new Atom('Atom.LPN');
  $lpdnAtom = new Atom('Atom.LPDN');
  $xmAtom = new Atom('Atom.XM');


  #$plnAtom = new Atom('Atom.PDQLN');
  #$plxAtom = new Atom('Atom.PDQLX');
  #$olnAtom = new Atom('Atom.OASISLN');
  #$olxAtom = new Atom('Atom.OASISLX');
  #$mlnAtom = new Atom('Atom.MDSLN');
  #$mlxAtom = new Atom('Atom.MDSLX');

  # Attribute templates
  $sAttr = new Attribute();
  $defAttr = new Attribute('Attribute.DEF');
  $styAttr = new Attribute('Attribute.STY');
  $styTgAttr = new Attribute('Attribute.STYTG');
  $sosTgAttr = new Attribute('Attribute.SOSTG');
  $xmapAttr = new Attribute('Attribute.XMAP');


  # Merge templates
  $exMerge = new Merge('Merge.EX');
  $syMerge = new Merge('Merge.SY');
  $tgexMerge = new Merge('Merge.TGEX');

  # Relation templates
  $sRel = new Relation();
  $tgRel = new Relation('Relation.TG');

  # Context templates.
  $sCxt = new Context();
}




# create HC/HS atoms. These are from ABBREVS.CLASSTYPE (level 1
#  hierarchy) and ABBREVS.CLASS (level 2 hierarchy). All LO/LN atoms 
#  come as level 3 hierarchy under level 2.
our %codeorAb2Said=();
our %said2Name=();
our %said2Marker=();
sub mangledName2Said {
  my $nd = shift;
  ($nd) = split(/\^/, $nd);
  return $codeorAb2Said{"$nd"};
}

sub dumpCXTInfo {
  # fill here.
  # first call INV to prepare contexts.
  $glInv->prTime("Preparing context trees.");
  # debug begin
  my @roots = $glInv->getRoots;
  print "Root nodes are:\n";
  print ERRS "Root nodes are:\n";
  foreach my $nd (@roots) {
    print ERRS "\t$nd => $said2Marker{$nd} => $said2Name{$nd}\n";
  }
  print "\n";
  print ERRS "\n";

  # debug end
  $glInv->prepareCxts;
  $glInv->prTime("Done preparing context trees.");

  my $parRef = $glInv->getParentsRef;
  my $chldRef = $glInv->getChildrenRef;
  my $pathRef = $glInv->getParentPathsRef;
  my ($nd, $attrNum, $cxt, @tree, $par, $FScxt, $treeNames);
  my ($rlNd, $rlCxt, $rlPar);
  my ($ndSaid, $ndName);
  open (OUT, ">:utf8", "cxtNames.tmp") or die "Could not open cxtNames.tmp\n";
  # root node doesn't have parents. So process all nodes in the parRef.
  foreach $nd (sort keys %{$parRef}) {
    $ndSaid = &mangledName2Said($nd);
    $ndName = $said2Name{"$ndSaid"};

    $attrNum = 0;
    foreach $cxt (@{$$pathRef{"$nd"}}) {
      print OUT "$nd ==> $cxt\n";
      @tree = split(/\|/, $cxt);
      @tree = map { &mangledName2Said($_); } @tree;
      $par = @tree[$#tree];
      $cxt = join('.', @tree);

      # now dump context.
      $sCxt->dumpCxt({id1 => $ndSaid, id2 => $par, ptnm => $cxt,
		      sgid1 => $ndSaid, sgid2 => $par});
      # now dump context attribute
      if (++$attrNum < 11) {
	$FScxt = "$attrNum\t::~~~~~~\t";
	$treeNames = join('~', (map {$said2Name{"$_"}} @tree));
	$defAttr->dumpAttr({id => $ndSaid, aname => 'CONTEXT', sab => $vsab,
			    aval => "$FScxt$treeNames\t$ndName\t\t"});
      } elsif ($attrNum == 11) {
	# dump the special attribute statin that it has more then 10.
	$defAttr->dumpAttr
	  ({id => $ndSaid, aname => 'CONTEXT', sab => $vsab,
	    aval => "11\t::~~~~~~\tLOINC Atoms\tMore Atoms not shown\t\t"});

      }
    }
  }
  close(OUT);

}

sub makeClsTypesHier {
  # the root node of the LNC hierarchy is fixed at '1500295'
  $codeorAb2Said{'LOINCROOT'} = '1500295';
  $said2Marker{'1500295'} = 'LOINCROOT';
  $said2Name{'1500295'} = 'LOINC ROOT';

  # now create classtype nodes.
  open(CLSTYPE, "<:utf8", $inpFiles{'ClassType'})
    or die "Could not open INF.CLSTYPE file.\n";

  my ($HS_name, $HC_name, $HS_id, $HC_id, $mthid, $clty, $cls, $clsab);
  my ($sgid2);
  # prepare 1st level hierarchy under LOINCCLASSTYPES node  -- this is fixed 
  # for LOINC
  my ($cxtNames, $cxtIds, $FScxtName);
  while (<CLSTYPE>) {
    chomp;
    ($HS_name, $HC_name, $mthid) = split(/\|/, $_);
	
    $hsAtom->dumpAtom({name => $HS_name, code => $mthid, sab => $vsab});
    $HS_id = $hsAtom->getLastId();

    $hcAtom->dumpAtom({name => $HC_name,'code' => $mthid, sab => $vsab});
    $HC_id = $hcAtom->getLastId();

    # mergeset EX HS - HC
    $exMerge->dumpMerge ({id1 => $HS_id, id2 => $HC_id, sab => $vsab});

    # use abbreviation as the key to said
    $codeorAb2Said{"$HS_name"} = $HC_id;
    $said2Marker{"$HC_id"} = $HS_name;
    $said2Name{"$HC_id"} = $HC_name;
    $glInv->addParChild('LOINCROOT', $HS_name);
    print PRCH "1500295|$HC_id|LOINCROOT|$said2Marker{$HC_id}\n";
  }
  close(CLSTYPE);

  # before we can do the 2nd level hierarchy, we need to read the class abbrs
  open (CLS, "<utf8", $inpFiles{'Class'})
    or die "Could not open class abbrs.\n";
  %clsAb2Name=();
  %CN_sty_valids=();
  %clsAb2Code=();
  my $rest;
  while (<CLS>) {
    chomp;
    my ($clsab, $cls, $mthid, $rest) = split(/\|/, $_);
    $clsAb2Name{"$clsab"} = $cls;      # class abbreviations
    $clsAb2Code{"$clsab"} = $mthid;      # class abbreviations
    $CN_sty_valids{"$clsab"} = $rest;  # class stys
  }
  close(CLS);

  # now read LOINCDB.txt and form 2nd level cxts
  open (LOINC2, "<:utf8", $glInv->getEle('File.INT.LOINC'))
    or die "Could not open input file INT.LOINC(cfg)\n";
  while (<LOINC2>) {
    chomp;
    # parse record and assign values.
    &readDBRec ($_);
    $clty = $FieldVals[35];
    $HS_name = $FieldVals[9];

    # cretae cxt atom and context
    # remember that this is a strict hierarchy. So if we have seen this
    # before, skip it.
    next if (defined($codeorAb2Said{"$HS_name"}));


    # here we need to create 2 atoms HC and HS
    # create the HS atom
    if (defined($clsAb2Code{"$HS_name"})) {
      # obtain the mth code
      $mthid = $clsAb2Code{"$HS_name"};

      # create hs atom
      $hsAtom->dumpAtom({name => $HS_name, code => $mthid, sab => $vsab});
      $HS_id = $hsAtom->getLastId();

    } else {
      # otherwise, obtain a new mth code and create HS atom
      # generate a new mth id
      $mthid = $mainGenId->newMthId();

      # create the hs atom
      $hsAtom->dumpAtom({name => $HS_name, code => $mthid, sab => $vsab});
      $HS_id = $hsAtom->getLastId();
      print ERRS "class $HS_name has not mthid in ABBREVS.CLASS\n";
      print ERRS "\tCreating a new atom with code of $mthid.\n";
    }

    # now create the HC atom.
    if (defined($clsAb2Name{"$HS_name"})) {
      $HC_name = $clsAb2Name{"$HS_name"};
      # if it has expanded name - use it.
      $hcAtom->dumpAtom({name => $HC_name, code => $mthid, sab => $vsab});
      $HC_id = $hcAtom->getLastId();
    } else {
      # otherwise, create HC same as HS and record in ERRS
      $HC_name = $HS_name;
      $hcAtom->dumpAtom({name => $HC_name, code => $mthid, sab => $vsab});
      $HC_id = $hcAtom->getLastId();
      print ERRS "class $HS_name has no expansion in ABBREVS.CLASS\n";
      print ERRS " creating expansion atom with the same name\n";
    }

    # mergeset AB HS - HC
    # change mergeset from LNC-AB to LNC-EX based on si_proposal (2/1/2007)
    $exMerge->dumpMerge ({id1 => $HS_id, id2 => $HC_id, sab => $vsab});

    # now save data and connect the HC atom to its parent.
    # use abbreviation as the key to said
    $codeorAb2Said{"$HS_name"} = $HC_id;
    $said2Marker{"$HC_id"} = $HS_name;
    $said2Name{"$HC_id"} = $HC_name;

    #$glInv->addParChild($codeorAb2Said{"$clty"}, $HC_id);
    $glInv->addParChild("$clty", $HS_name);
    print PRCH "$codeorAb2Said{$clty}|$HC_id|$clty|$said2Marker{$HC_id}\n";
  }
  close(LOINC2);
}

sub resetFieldVals {
  my($x);
  $x = 0;
  while ($x <= $totalDBFields) {
    $FieldVals[$x] = "";
    $x++;
  }
}

sub readDBRec {
  &resetFieldVals;
  my ($inp) = @_;
  #my(@flds) = split(/\t/,$inp);
  my(@flds) = split(/\|/,$inp);

  my($x, $y) = (1, 0);
  while ($x <= $totalDBFields) {
    $FieldVals[$x] = &stripBlanks ($flds[$y]);
    $x++; $y++;
  }
}

our %LN2_ids=();
sub processLOINCFile {
  my ($code, $LX2_name, $LX2_id, $LN2_name, $LN2_id, $OSN2_name);
  my ($OSN2_id, $LS_name, $LS_id, $CN_name, $CN_id,  $CX_name);
  my ($CX_id, $SN_name, $SN_id, $SX_name, $SX_id);
  my ($newCode, $MULT_id);
  my ($fld2, $fld3, $fld4, $fld5, $fld6, $fld7, @pieces, $tmp, $rela);
  my ($ign, $temp, %seenVals, $FScxtName, $sgid2, $mthId);
  my %seen_SNinLN = ();
  my %CN2SNMerges=();           # remember CN2SNMerges.


  # intially set mthid to '' so a new one can be generated if it is blank.
  $mthId = '';

  open (LOINC, "<:utf8", $glInv->getEle('File.INT.LOINC'))
    or die "Could not open input file: File.INT.LOINC\n";
  while (defined ($_ = <LOINC>)) {
    chomp;

    # parse record and assign values.
    &readDBRec ($_);
    $vsab2 = $vsab;
    #$vsab2 = $pvsab if ($FieldVals[9] =~ /SURVEY.PHQ9$/);
    #$vsab2 = $mvsab if ($FieldVals[9] =~ /SURVEY.MDS$/);
    #$vsab2 = $ovsab if ($FieldVals[9] =~ /SURVEY.OASIS$/);
    $vsab2 = $pvsab if ($FieldVals[9] =~ /PHQ9$/);
    $vsab2 = $mvsab if ($FieldVals[9] =~ /MDS$/);
    $vsab2 = $ovsab if ($FieldVals[9] =~ /OASIS$/);

    $code = $FieldVals[1];
    #this is to keep track of the vsav2 value for each $code(id)
    $vsab2hash{$code} = $vsab2; 
    if ($FieldVals[19] =~ /DEL/) {
      @names_tty = qw(LO OLX OOSN);
      $obsl{"$code"} = 1;
    } else {
      @names_tty = qw(LN LX OSN);
      $obsl{"$code"} = 0;
    }

    #---------------------------------------------------
    # 1. create LN/LO atom - F1 as code
    $LN2_name = "$FieldVals[2]:$FieldVals[3]:$FieldVals[4]:$FieldVals[5]:$FieldVals[6]:$FieldVals[7]";
    $LN2_name =~ s/:$//;	# remove trialing ":" if no 6th ele

    #dump atom
    if ($obsl{"$code"} == 1) {
      $loAtom->dumpAtom({name => $LN2_name, code => $code, sab => $vsab2});
      # remember obsolete code to new code here
      $newCode = $FieldVals[20];
      if ($newCode ne "") {
	$obsl2new{"$code"} = $newCode;
      }
    } else {
      $lnAtom->dumpAtom({name => $LN2_name, code => $code, sab => $vsab2});
    }

    $LN2_id = $lnAtom->getLastId();
    $LN2_ids{"$code"} = $LN2_id;


    # create context attribute.
    if (0) {
    my $cxtNames = "$HS_cxtNames{$FieldVals[9]}\t$LN2_name\t\t";

    $FScxtName = "1\t::~$HS_cxtIds{LOINCROOT}~SRC_ATOM_ID~~$LN2_id~SRC_ATOM_ID~\t$cxtNames";

    $defAttr->dumpAttr({id => $LN2_id, aname => 'CONTEXT', aval => $cxtNames,
		       sab => $vsab});

    #create context
    ($sgid2) = reverse (split(/\./, $HS_cxtIds{"$FieldVals[9]"}));
    $sCxt->dumpCxt2({id1 => $LN2_id, ptnm => $HS_cxtIds{"$FieldVals[9]"},
		    sgid1 => $LN2_id, sgid2 => $sgid2});
  }
    $codeorAb2Said{"$code"} = $LN2_id;
    $said2Marker{"$LN2_id"} = $code;
    $said2Name{"$LN2_id"} = $LN2_name;
    #$glInv->addParChild($codeorAb2Said{"$FieldVals[9]"}, $LN2_id);
    $glInv->addParChild("$FieldVals[9]", $code);
    print PRCH "$codeorAb2Said{$FieldVals[9]}|$LN2_id|$FieldVals[9]|$said2Marker{$LN2_id}\n";


    my $fldVal;
    #dump attrs
    foreach my $ii (2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 16, 17,
		    18, 19, 20, 21, 22, 24, 26, 28, 29, 30, 31, 33, 34,
		    35, 36, 37, 38, 39, 40, 41, 42, 43,44, 45,  46, 47,
		    50,  51, 52, 53, 54, 55, 56, 57, 58, 60, 61, 62, 63) {
      my $aNam = $FieldSyms[$ii];
      my $aVal = $FieldVals[$ii];

      if ($ii == 45) {
	%seenVals = ();
	# create one for each ',' sep term as an LCA attr.
	foreach $fldVal (split (/\,/, $aVal)) {
	  $fldVal = &stripBlanks($fldVal);
	  if ($fldVal ne ""){
	    if (!defined ($seenVals{"$fldVal"})) {
	      $sAttr->dumpAttr({id => $LN2_id, aname => $aNam,
				aval =>$fldVal, sab => $vsab2});
	      $seenVals{"$fldVal"}++;
	    }
	  }
	}
      } elsif ($ii == 30) {
	%seenVals = ();
	# create one for each ';' sep term as an LGC attr.
	foreach $fldVal (split (/\;/, $aVal)) {
	  $fldVal = &stripBlanks($fldVal);
	  if ($fldVal ne "") {
	    if (!defined ($seenVals{"$fldVal"})) {
	      $sAttr->dumpAttr({id => $LN2_id, aname => $aNam,
				aval => $fldVal, sab => $vsab2});
	      $seenVals{"$fldVal"}++;
	    }
	  }
	}
      } elsif ($aVal ne "") {
	$sAttr->dumpAttr({id => $LN2_id, aname => $aNam,
			  aval => $aVal, sab => $vsab2});
      }
    }

    # create a semant_type of 'Clinical Attribute' for all LN2 atoms.
    # shoudl this be $vsab?????????????????????
    $styAttr->dumpAttr({id => $LN2_id, aval => 'Clinical Attribute',
			sab => "E-$vsab2"});


    #----------------------------------------------
    # create LX2_name - F1 as code
    # also create CX
    # CN name is the first part of Field 2 with out any processing.
    # CX name is the expanded form of First part of Field 2.
    $fld2 = $FieldVals[2];
    @pieces = split(/\^/, $fld2);
    if ($pieces[0] =~ /^[,*.?:;]$/) { $pieces[0] = ''; }
    $CN_name = &stripBlanks($pieces[0]);

    $pieces[0] = &shorten_LITTLE($pieces[0]);
    $pieces[0] = &expand_Analyte($pieces[0]);
    $pieces[0] = &stripBlanks($pieces[0]);
    $CX_name = $pieces[0];
    #$CX_name =~ s/\^//g;


    $pieces[1] = &expand_Challenge($pieces[1]);
    #$pieces[1] = &expand_ChallengeDelay($pieces[1]);
    #$pieces[1] = &expand_ChallengeRoute($pieces[1]);
    #$pieces[1] = &expand_ChallengeType($pieces[1]);
    $pieces[1] = &stripBlanks($pieces[1]);
    $fld2 = join('^', @pieces);
    $fld2 =~ s/(\^)*$//;	# removing trailing ^
    #$fld2 =~ s/\~/\^/g;
    $fld2 = &stripBlanks($fld2);



    $fld3 = &expand_Property($FieldVals[3]);
    $fld3 = &stripBlanks($fld3);



    # Some times this field has multiple * in it. Each one expands to 
    # "LIFE OF THE UNIT". So we need to remove multiple starts.
    $fld4 = $FieldVals[4];
    $fld4 =~ s/^\*+/\*/;
    @pieces = split(/\^/, $fld4);
    $pieces[0] = &expand_Timing($pieces[0]);

    # SPECIAL PROCESSING - BEGIN
    # if timing has "*", replace it with "LIFE OF THE UNIT"
    # since this is not seperated by word block, the one in the abbrs
    #  is not going to work. Do it explicitly here.
    $pieces[0] = &stripBlanks($pieces[0]);
    $pieces[0] =~ s/^\*/LIFE OF THE UNIT/;
    # SPECIAL PROCESSING - END

    $pieces[1] = &expand_TimingAspect($pieces[1]);
    $pieces[1] = &stripBlanks($pieces[1]);
    $fld4 = join('^', @pieces);
    $fld4 = &stripBlanks($fld4);
    $fld4 =~ s/\^^$//;		# removing trailing ^
    $fld4 =~ s/\^$//;		# removing trailing ^



    @pieces = split(/\^/, $FieldVals[5]);
    $pieces[0] = &shorten_LITTLE($pieces[0]);
    $pieces[0] = &expand_System($pieces[0]);
    $pieces[0] = &stripBlanks($pieces[0]);

    # if LS name is only the first part of field 5 - expanded ???BK???
    $LS_name = $pieces[0];
    $LS_name =~ s#\+# and #g;
    $LS_name = &stripBlanks($LS_name);
    if ($LS_name =~ /^[,*.?:;]$/) { $LS_name = ''; }


    $pieces[1] = &expand_Super($pieces[1]);
    $pieces[1] = &stripBlanks($pieces[1]);
    $fld5 = join('^', @pieces);
    $fld5 = &stripBlanks($fld5);
    $fld5 =~ s/\^$//;		# removing trailing ^
    $fld5 = &stripBlanks($fld5);


    $fld6 = &expand_Scale($FieldVals[6]);
    $fld6 = &stripBlanks($fld6);

    $fld7 = &expand_Method($FieldVals[7]);
    $fld7 = &stripBlanks($fld7);

    $LX2_name = "$fld2:$fld3:$fld4:$fld5:$fld6:$fld7";
    $LX2_name =~ s/:$//;	# remove trailing : if no 6th element.

    $LX2_id = 0;
    if ($LX2_name ne $LN2_name) {
      $LX2_name =~ s/\~/\^/g;
      if ($obsl{"$code"} == 1) {
	$olxAtom->dumpAtom({name => $LX2_name, code => $code, sab => $vsab2});
      } else {
	$lxAtom->dumpAtom({name => $LX2_name, code => $code, sab => $vsab2});
      }
      $LX2_id = $lxAtom->getLastId();;
		
      #mergeset SY LN2-LX2
      $syMerge->dumpMerge({id1 => $LN2_id, id2 => $LX2_id, sab => $vsab2});

      #rel LN2-LX2 - SFO/LFO - expanded_form_of
      $sRel->dumpRel({id1 => $LN2_id, id2 => $LX2_id, rname => 'SFO/LFO',
		      rela => 'expanded_form_of',
		      sab => $vsab2, sol => $vsab2});

    }


    #----------------------------------------------
    # create OSN names F1 as code
    $OSN2_name = $FieldVals[59];
    if ($OSN2_name =~ /^[,*.?:;]$/) { $OSN2_name = ''; }
    $OSN2_id = 0;
    if ($OSN2_name ne "") {
      #$OSN2_name =~ s/\~/\^/g;
      if ($obsl{"$code"} == 1) {
	$oosnAtom->dumpAtom({name => $OSN2_name, code => $code, sab => $vsab});
      } else {
	$osnAtom->dumpAtom({name => $OSN2_name, code => $code, sab => $vsab});
      }
      $OSN2_id = $osnAtom->getLastId();

      #mergeset  AB LN2 - OSN2
      #mergset should change to LNC-EX per si_proposal (2/1/2007)
      $exMerge->dumpMerge({id1 => $LN2_id, id2 => $OSN2_id});

      #rel LN2 - OSN2 - SFO/LFO - has_expanded_form
      $sRel->dumpRel({id1 => $LN2_id, id2 => $OSN2_id, rname => 'SFO/LFO',
		      rela => 'has_expanded_form',
		      sab => $vsab2, sol => $vsab2});
    }

    #----------------------------------------------
    # make LS F5:1
    #$LS_name = $FieldVals[5];	# NOCODE

    # LS_name is already done in LX
    $LS_id = 0;
    if ($LS_name ne "") {
      $LS_name =~ s/\~/\^/g;
      if (defined($seen_LS{"$LS_name"})) {
	$LS_id = $seen_LS{"$LS_name"};
      } else {
	$mthId = &getMthId("LS|$LS_name");
	$lsAtom->dumpAtom({name => $LS_name, code => $mthId, sab => $vsab});
	$LS_id = $lsAtom->getLastId();
	$seen_LS{"$LS_name"} = $LS_id;
      }

      # remember its semantic type. if its class type is as follows
      # if classtype = 1 => sty = 'Body Substance'
      # if classtype = 2 => sty = 'Body Part, Organ, or Organ Component'
      my $clType = $FieldVals[35];
      if (($clType == 1) or ($clType == 2)) {
	$LS_sty{"$LS_id|$clType"}++;
      }

      # rel LX2 - LS - RT - Analyzed_by
      # should be LN -LS  RT-Analyzed by(2/1/2007)
      if ($obsl{"$code"} == 1) {
	$tmp = 'LO';
      } else {
	$tmp = 'LN';
      }
      $sRel->dumpRel({id1 => $LN2_id, id2 => $LS_id, rname => 'RT',
		      rela => 'analyzed_by',
		     sab => $vsab2, sol => $vsab2});
    }

    #----------------------------------------------
    # make CN F2 & CX - CX created above while doing LX2.
    #$CN_name = $FieldVals[2];	# NOCODE

    # CN name is already done in LX
    $CN_id = 0;
    my $newbie_CN = 0;
    if ($CN_name ne "") {
      if (defined($seen_CN{"$CN_name"})) {
	$CN_id = $seen_CN{"$CN_name"};
      } else {
	$mthId = &getMthId("CN|$CN_name");
	$cnAtom->dumpAtom({name => $CN_name, code => $mthId, sab => $vsab});
	$newbie_CN = 1;
	$CN_id = $cnAtom->getLastId();
	$seen_CN{"$CN_name"} = $CN_id;
	# create the sty here for this CN atom.
	# if classtype = 2 => sty = 'Organism Attribute'
	# if classtype = 1 => get sty from etc/ABBREVS.CLASS file.
	my $cls1 = $FieldVals[9];

	# lookup the CN_sty_valis for sty. Note: can have more than 1 stys.
	if (defined ($CN_sty_valids{"$cls1"})) {
	  foreach my $sty (split (/\;/, $CN_sty_valids{"$cls1"})) {
	    $sty = &stripBlanks($sty);
	    if ($sty ne "****"){
	      $styAttr->dumpAttr({id => $CN_id, aval => $sty,
				  sab => "E-$vsab"});
	    }
	  }
	}

	
	$CX_id = 0;
	if ($CX_name ne "") {
	  if ($CX_name ne $CN_name) {
	    $CX_name =~ s/\~/\^/g;
	    if (defined($seen_CX{"$CX_name"})) {
	      $CX_id = $seen_CX{"$CX_name"};
	    } else {
	      $mthId = &getMthId("CX|$CX_name");
	      $cxAtom->dumpAtom({name => $CX_name, code => $mthId,
				 sab => $vsab});
	      $CX_id = $cxAtom->getLastId();
	      $seen_CX{"$CX_name"} = $CX_id;
	    }

	    #mergeset EX CN-CX
	    $exMerge->dumpMerge({id1 => $CN_id, id2 => $CX_id,
				sab => $vsab});

	    # rel CN-CX - SFO/LFO - expanded_form_of
	    $sRel->dumpRel({id1 => $CN_id, id2 => $CX_id,
			   rname => 'SFO/LFO', rela => 'expanded_form_of',
			   sab => $vsab, sol => $vsab});
			
	  }
	}
      }


      #rel LN2 - CN -  RT - rela by classType[F35 using %class2Rela]
      $rela = $class2rela{"$FieldVals[35]"};
      if ($obsl{"$code"} == 1) {	$tmp = 'LO'; }
      else { $tmp = 'LN'; }
      $sRel->dumpRel({id1 => $LN2_id, id2 => $CN_id,
		      rname => 'RT', rela => $rela, sab => $vsab2,
		      sol => $vsab2});
    }
	
    #----------------------------------------------
    #make SN names - F32 sperated by ",". Also create SX names - NOCODE
    # set this to null - nothing is seen for this code yet.
    %seen_SNinLN = ();
    my $sn_def = '';
    #$SN_name = $FieldVals[32];
    foreach $SN_name (split(/\;/, $FieldVals[32])) {
      $SN_name = &stripBlanks($SN_name);
      if ($SN_name =~ /^[,*.?:;]$/) { $SN_name = ''; }
      if ($SN_name ne "") {
	my $newbie_SN = 0;
	if (defined($seen_SN{"$SN_name"})) {
	  $SN_id = $seen_SN{"$SN_name"};
	} else {
	  $mthId = &getMthId("SN|$SN_name");
	  $snAtom->dumpAtom({name => $SN_name, code => $mthId, sab => $vsab});
	  $SN_id = $snAtom->getLastId();
	  $seen_SN{"$SN_name"} = $SN_id;
	  $newbie_SN = 1;

	  # create SX names- NOCODE
	  $SX_id = 0;
	  $SX_name = &expand_Analyte($SN_name);
	  $SX_name =~ s/\^//g;	# remove ^
	  $SX_name = &stripBlanks($SX_name);

	  if ($SX_name ne "") {
	    if ($SX_name ne $SN_name) {
	      $SX_name =~ s/\~/\^/g;
	      if (defined($seen_SX{"$SX_name"})) {
		$SX_id = $seen_SX{"$SX_name"};
	      } else {
		$mthId = &getMthId("SX|$SX_name");
		$sxAtom->dumpAtom({name => $SX_name, code => $mthId,
				   sab => $vsab});
		$SX_id = $sxAtom->getLastId();
		$seen_SX{"$SX_name"} = $SX_id;
	      }
	      #mergeset  AB  SN-SX
	      #mergeset changes to LNC-EX per si_proposal (2/1/2007)
	      $exMerge->dumpMerge({id1 => $SN_id, id2 => $SX_id,
				  sab => $vsab});

	      #rel SN - SX - SFO/LFO - expanded_of
	      $sRel->dumpRel({id1 => $SN_id, id2 => $SX_id,
			     rname => 'SFO/LFO', rela => 'expanded_form_of',
			     sab => $vsab, sol => $vsab});
	    }
	  }
	}

	#merge set SY CN-SN - duplicate if both are seen before.
	# ie CN has to exist and one of either CN or SN must be new.
	# is $CN_id > 0; either newbie_SN or $newbie_CN
	$temp = "$CN_id|$SN_id";
	
	if (!defined($CN2SNMerges{"$temp"})) {
	  $syMerge->dumpMerge({id1 => $CN_id, id2 => $SN_id,
			      sab => $vsab});
	  $CN2SNMerges{"$temp"}++;
	}

	if (!defined ($seen_SNinLN{$SN_name})) {
	  # here we remember all distinct SN names for this atom.
	  $seen_SNinLN{"$SN_name"}++;
	  $sn_def .= ";$SN_name";
	}
      }
    }
    if ($sn_def ne "") {
      # here create a definition attribute to the parent LN2 atom.
      $sn_def = substr($sn_def, 1);
      $defAttr->dumpAttr({id => $LN2_id, aname => 'DEFINITION', 
			  aval => "SN for CN: $sn_def",
			 sab => $vsab2});
    }
  }

  # now dump LS semantic types.
  # no need to do this. 
    foreach my $key (keys (%LS_sty)) {
      my ($id, $ty) = split(/\|/, $key);
      if ($ty == 1) {
	$styAttr->dumpAttr({id => $id, aval => 'Body Substance',
			    sab => "E-$vsab"});
      } elsif ($ty == 2) {
	$styAttr->dumpAttr({id => $id, 
			   aval => 'Body Part, Organ, or Organ Component',
			    sab => "E-$vsab"});
      }
    }


  # LO to LN rels. only applicable if it is an LO atom
  foreach my $oldCode (keys (%obsl2new)) {
    $newCode = $obsl2new{"$oldCode"};
    $sRel->dumpRel({id1 => $LN2_ids{"$oldCode"}, id2 => $LN2_ids{"$newCode"},
		    rname => 'RT?', rela => 'mapped_from',
		    sab => $vsab2, sol => $vsab2});
  }
  close(LOINC);
}

sub processParts {
  %part_ids=();
  $bad_parts = 0;
  $bad_types = 0;
  %rej_type=();
  $no_terms=0;

  # create  map of code to tty[LN/LO]
  # from partlinks file - create map of Lcode to Pcode - check existance 
  # of Lcode

  open (PARTS, "<:utf8", $glInv->getEle('File.INT.LOINC_PARTS')) or
    die "Could not open INT.LOINC_PARTS(cfg) file.\n";
  # ignore the first header record.
  <PARTS>;
  my ($pCode, $pType, $pName, $pDName, $pSName, $lCode, $lkType);
  my ($LPN_name, $LPN_id, $LPDN_name, $LPDN_id);
  my $ignorePart = 'NEW DISPLAY PART FOR COMPONENT TREE';
  while (<PARTS>) {
    # this file has double quotes in it. remove them
    chomp;
    ($pCode, $pType, $pName, $pDName, $pSName) = split(/\|/);
    $pCode = &stripBlanks($pCode, 1);
    $pType = &stripBlanks($pType, 1);
    $pName = &stripBlanks($pName, 1);
    $pDName = &stripBlanks($pDName, 1);
    $pSName = &stripBlanks($pSName, 1);
    unless ($pCode =~/^LP/) {
      print ERR "WARN:Bad part1: $pCode|$pType|$pName|$pDName|$pSName\n";
      $bad_parts++; next;
    }

    #check if it is a valid part.
    if (!defined($partRela{"$pType"})) {
      $bad_types++;
      $rej_type{"$pType"}++;
      print ERR "WARN:Bad part2: $pCode|$pType|$pName|$pDName|$pSName\n";
      next;
    }

    if ($pName eq "") {
      print ERR "WARN:Bad part3: $pCode|$pType|$pName|$pDName|$pSName\n";
      $no_terms++;  next;
    }
    # if pname has "NEW DISPLAY PART FOR COMPONENT TREE"
    # ignore these. These are supposed to be internal notes.
    next if ($pName =~ /NEW DISPLAY PART FOR/i);

    # dump lpn atom.
    $lpnAtom->dumpAtom({name => $pName, code => $pCode, sab => $vsab});
    $LPN_id = $lpnAtom->getLastId();
    $codeorAb2Said{"$pCode"} = $LPN_id;
    $said2Marker{"$LPN_id"} = $pCode;
    $said2Name{"$LPN_id"} = $pName;


	
    # if the display name is an Allegen Mix then make an atom.
    $LPDN_id = 0;
    if ($pDName =~ /Allergen Mix/) {
      $lpdnAtom->dumpAtom({name => $pDName, code => $pCode, sab => $vsab});
      $LPDN_id = $lpdnAtom->getLastId();

      #mergeset AB PCODE - PCODE - CDTG LPN to LPDN
      #mergeset change to LNC-EX per si_proposal (2/1/2007)
      $tgexMerge->dumpMerge({id1 => $pCode, id2 => $pCode, sab => $vsab});
    }

    $part_ids{"$pCode"} = $LPN_id;
    $part_types{"$pCode"} = $pType;

    my $atval;
    # make SOS attribute
    if ($pDName ne '') {
      $atval = $pType.":".$pDName;
      $atval =~ s/\s*$//;
      $sosTgAttr->dumpAttr({id => $pCode, aval => $atval, sab => $vsab});
    }

    # make STY?
    my $temporalConcept_seen = 0;
    if (my $sty = $parttype2sty{"$pType"}) {
      $styTgAttr->dumpAttr({id => $pCode, aval => $sty, sab => "E-$vsab"});
      if ($sty eq "Temporal Concept") {
	$temporalConcept_seen = 1;
      }
    }

    # don't make a duplicate STY
    if ($temporalConcept_seen == 0) {
      if ($pName =~ /POST/) {
	#$atval = 'Temporal Concept';
	$styTgAttr->dumpAttr({id => $pCode, aval => 'Temporal Concept',
			      sab => "E-$vsab"});
      }
    }
  }

  # now read links file and create rels between LPN and LN2

  open (PLINK, "<:utf8", $glInv->getEle('File.INT.LOINC_LINKS')) or
    die "Could not open INT.LOINC_LINKS(cfg) file.\n";
  # ignore the first header record.
  <PLINK>;
  my ($tty, $rela);
  while (<PLINK>) {
    chomp;
    # may have to skip blanks here
    # ignoring link type ???bk???
    ($lCode, $pCode,$lkType) = split(/\|/);
    $lCode = &stripBlanks($lCode, 1);
    $pCode = &stripBlanks($pCode, 1);

    if (defined($LN2_ids{"$lCode"}) && defined($part_ids{"$pCode"})) {
      $tty = 'LN';
      if ($obsl{"$lCode"} == 1) {
	$tty = 'LO';
      }
      $pType =$part_types{"$pCode"};
      if ($pType ne 'UNKNOWN') {
	$rela = $partRela{"$pType"};

	#reading form vsab2hash to find out the vsab for the LN atoms
	$vsab2 = $vsab2hash{$lCode};

	# rel LPN - LN2 - RT - has_adj, hs_div etc.. - CDTG LPN to LN2
	#combine into one type, no distinction between LPN_LN or LPN_LO
	$tgRel->dumpRel({id1 => $lCode, id2 => $pCode,
			 rname => 'RT', rela => $rela,
			 idq1 => "$vsab2/$tty", idq2 => "$vsab/LPN",
			 sab => $vsab, sol => $vsab});
      }
    }
  }
  close(PLINK);

  # debug begin
  open (OUT, ">codeorab2said") or die "Could not open codeorab2said.\n";
  my $temp;
  foreach $temp (keys (%codeorAb2Said)) {
    print OUT "$temp|$codeorAb2Said{$temp}\n";
  }
  close(OUT);
  # debug end
}

sub doOnce {
  # Call the following only once to create the ABBREVS file in etc2
  # then hand modify etc2/ABBREVS.PROPERTY file and move the 
  # entry "Ratio|Ratios" to the top of the file and then run lnc.pl
  &genAbbrevFilesByLength;
  &makeLOINCIntFiles;
  print "makeLOINCIntFiles done!\n";
}

# -----------------------------------------------------------------------------
# main main main
#------------------------------------------------------------------------------
sub runMain {
  open (ERRS, "> $ofErrors") or die "Couldn't open erros.loinc file\n";

  open (PRCH, "> parchld") or die "Couldn't open parchld file\n";
  $glInv = new NLMInv($cfgFile, *ERRS);
  $glInv->prTime("Begin");


  #$glInv->dumpAll;
  if (defined $options{g}) {
    &init0;
    &doOnce;
    $glInv->prTime("End");
    exit;
  }

  $glInv->invBegin;
  &init;			# initialize

  # dump source meta data.
  #$glInv->processSrc('LNC');
  $glInv->processSrc();

  #open (SADAT, ">:utf8", $glInv->getEle('File.INT.SADAT'))
   # or die "Could not open File.INT.SADAT\n";

  #&dumpCXTInfo;			# dump HS and HC atoms
  &makeClsTypesHier;
  &processLOINCFile;		# process each input record from LOINCDB

  # at this stage we can release the memory for seen_LS/CN/SN
  %seen_LS=();
  %seen_CN=();
  %seen_SN=();


  &processParts;		# process part and links files
  close(PRCH);
  &dumpCXTInfo;

  &dumpXMapSets;
  &dumpXMaps;
  $glInv->invEnd;
  $glInv->prTime("End");
  close(ERRS);
  close(PRCH);
}

##############################################################################3
##  Quick FIX to the MAPPINGS between CPT2005 and LNC215
##   REVISIT THIS FOR NEXT INVERSION/INSERTION
##   SOMA LANKA
##############################################################################
# dump crossmap sets. We will have 1 for each crossmap set, ie, between
# LNC2005 and CPT2005 we are mapping to.  So we will have only 1 XMAP set.
sub dumpXMapSets {
  my ($mId, $mNm, $mType, $mScId, $mScNm, $mScVr, $mRId, $mSep, $mRType, $ign);
  my $sos = 'This set maps LOINC CODES to CPT2005 codes; a single LOINC concept id may be  mapped to one or more CPT2005 codes';
  my ($x1);
  open (XMSETS, "<:utf8", $glInv->getEle('File.INP.XMSets'))
    or die "Could not open File.INP.XMSets.\n";

  while (<XMSETS>) {
    chomp;
    my ($fVsab, $tVsab, $fRsab, $tRsab, $mVsab, $mRsab,$mType, $mComp,
	$mfComp,$mtComp,$mfExhaustive,
	$mtExhaustive,$mVersion) = split(/\|/, $_);


    $xmapAttr->dumpAttr({aname => 'MAPSETTYPE', aval => $mType});
    $xmapAttr->dumpAttr({aname => 'FROMVSAB',aval => $fVsab });
    $xmapAttr->dumpAttr({aname => 'TOVSAB',aval => $tVsab });
    $xmapAttr->dumpAttr({aname => 'FROMRSAB', aval => $fRsab});
    $xmapAttr->dumpAttr({aname => 'TORSAB', aval => $tRsab});
    $xmapAttr->dumpAttr({aname => 'MAPSETVSAB', aval => $mVsab});
    $xmapAttr->dumpAttr({aname => 'MAPSETRSAB', aval => $mRsab});
    $xmapAttr->dumpAttr({aname => 'MTH_MAPSETCOMPLEXITY', aval => $mComp});
    $xmapAttr->dumpAttr({aname => 'MTH_MAPTOCOMPLEXITY', aval => $mtComp});
    $xmapAttr->dumpAttr({aname => 'MTH_MAPFROMCOMPLEXITY', aval => $mfComp});
    $xmapAttr->dumpAttr({aname => 'MTH_MAPFROMEXHAUSTIVE',
			 aval => $mfExhaustive});
    $xmapAttr->dumpAttr({aname => 'MTH_MAPTOEXHAUSTIVE',
			 aval => $mtExhaustive});
    $xmapAttr->dumpAttr({aname => 'MAPSETVERSION', aval => $mVersion});

    $xmAtom->dumpAtom({name => "$fVsab to $tVsab Mappings", code => 'NOCODE'});
    my $id = $xmAtom->getLastId();
    $styAttr->dumpAttr({id => $id, aval => 'Intellectual Product',
			sab => "E-$vsab"});

  }
  close(XMSETS);
}
# need to find the rels here - 
# foreach xmap set (we only have one), dump each map as an attribute.
sub dumpXMaps {
  my ($mid, $ign, $tid, $mCid, $mOpt, $mPrt, $mRule, $mAdvc, $rel, $rela);
  my ($tCd, @inp, $ln);

  my %fromId=();
  my %toId=();
  my $fCtr = 1000;
  my $tCtr = 1000;

  #    XMAP ATTRIBUTE Values
  #    MAPSUBSETID: Map sub set identifier
  #    MAPRANK: Order in which mappings in a subset should be applied
  #    FROMID: Identifier mapped from
  #    REL: Relationship
  #    RELA: Relationship attribute
  #    TOID: Identifier mapped to
  #    MAPRULE: Machine processable rule for when to apply mapping
  #    MAPTYPE: Type of mapping
  #    MAPATN: Row level attribute name associated with this mapping
  #    MAPATV: Row level attribute value associated with this mapping
  #    MAPSID: Source asserted Mapping ID
  #    MAPRES: Human readable restriction use of mapping
  ############################################################################

  #  1007-4|DIRECT ANTIGLOBULIN TEST.POLY SPECIFIC REAGENT:ACNC:PT:RBC:ORD::
  #        |86880|Antihuman globulin test (Coombs test); direct, each 
  #        antiserum|=|ok||||||||
  #  fid = 1007-4   -- LOINC CODE
  #  tid = 86880  --  CPT2005 CODE
  # XMAP Should be 
  #   MAPSUBSETID  == null
  #   MAPRANK  == null
  #   FROMID = fid
  #    REL = RN
  #    RELA NULL
  #    TOID =  <Id defined in XMAPTO attribute for 86880 CPT CODE>
  #    Rest of the fields are null as source is not providing them.
  #   ~~1007-4~RN~~1001~~~~~~
  #############################################################################

  open (XMAPS, "<:utf8", $glInv->getEle('File.INP.XMaps'))
    or die "Could not open File.INP.XMaps\n";
  while (<XMAPS>) {
    my ($fid, $mName, $tid, $mRule) = split(/\|/,$_);

    # First define the XMAPFROM and XMAPTO
    if (!defined $fromId{"$fid"}) {
      $fromId{"$fid"} = $fid;
      # define the from Id
      #ID: Identifier mapped to  --  This the Loinc Code or can be unique
      #                              number
      #SID: Source asserted identifier mapped to  -- THis is null as source is
      #                                              not providing them
      #EXPR: Expression mapped to   --- This is the LOINC Code
      #TYPE: Type of mapped to expression   --- Type of LOINC expressing ( in
      #                                         this case CODE)
      #RULE: Machine processable rule for when this "mapped from/to" is
      #                                  valid  --- NULL
      # RES: Restriction on when this "mapped from/to" should be used -- NULL
      # 1007-4~~1007-4~CODE~~|
      $xmapAttr->dumpAttr({aname =>'XMAPFROM', aval=>"$fid~~$fid~CODE~~"});
    }
    if (!defined $toId{"$tid"}) {

      # define the from Id
      #ID: Identifier mapped to  --  Can be CPT Code or a unique number used
      #     unique number ( starts 100) for backword compatibility
      #SID: Source asserted identifier mapped to  -- THis is null as source is
      #        not providing them
      #EXPR: Expression mapped to   --- This is the CPT Code
      #TYPE: Type of mapped to expression   --- Type of LOINC expressing ( in
      #      this case BOOLEAN EXPRESSION_CODE)
      #RULE: Machine processable rule for when this "mapped from/to" is valid
      #      --- NULL
      # RES: Restriction on when this "mapped from/to" should be used -- NULL
      # 1007-4~~1007-4~CODE~~|
      $tCtr++;
      $xmapAttr->dumpAttr({aname =>'XMAPTO',
			   aval=>"$tCtr~~$tid~BOOLEAN_EXPRESSION_CODE~~"});
      $toId{"$tid"} = $tCtr;
    }

    # Having Defined the From and to Just write the XMAP Attribute
    my $toid = $toId{"$tid"};
    $xmapAttr->dumpAttr
      ({aname => 'XMAP',
	aval => "~~$fid~RN~~$toid~~~~~~"});
  }
  close(XMAPS);
}

&runMain;

