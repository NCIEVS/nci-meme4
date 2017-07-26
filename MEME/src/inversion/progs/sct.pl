#!@PATH_TO_PERL@
#
unshift(@INC, ".");
unshift(@INC,"/umls_dev/NLM/inv/bin");

use lib "/umls_dev/NLM/inv/lib";
use lib "/umls_dev/NLM/inv/bin";
use strict 'vars';
use strict 'subs';
#using ("::");

our %options=();
use Getopt::Std;
getopts("gshl:x", \%options);

if (defined $options{h}) {
  print "Usage: lnc.pl -hg\n";
  print " This file inverts SNOMEDCT_US and produces the standard files.\n";
  print "\t-h prints this message.\n";
  print "\t-l specify the language. SPA is for spanish. otherwise English.\n";
  print "\t-g generate inital files so actual inversion can be done.\n\n";
  print "\t-s generate sematic type attributes after the inversion and a\n";
  print "\t a test insertion. This process uses a file sty_term_ids that\n";
  print "\t is produced as a result of test insertion. \n\n";

  print "\t1) First prepare the input files. For a list of these and\n";
  print "\t   and their descriptions, see OCCS_SNOMED.readme file.\n";
  print "\t2) Edit the .cfg file\n";
  print "\t3) run sct.pl -g to generate the necessary files.\n\n";
  print "\t4) Update the generated etc/..files. [MG_URcid2did]\n";
  print "\t5) run sct.pl.\n";
  print "\tThis generates all the standard output files.[~ 90 min]\n";
  print "\nNote: Any special notes here.\n";
  exit;
}


use NLMInv;
use Atom;
use Attribute;
use Relation;
use Merge;
use Context;
use IdGen;

our $cfgFile;
our $ofErrors;
our $tempDir;

our $glInv;
our $mainGenId;
our $lvgProg;
our $sab;
our $sabVersion;
our $curCidOrd = 100;
our $atomOrderInc = 10;
our $langMode = '';          # defaults to Eng.
our $sctvsab;                # for spanish

#---------------------
# constant data
#--------------------

our %tid2mid=();      # tid2mid
our %dsl2tty=();      # dsl2tty
our %dsl2sup=();      # dsl2sup

our %cid2did=();      # cid2did
our %did2aid=();      # did2aid
our %aid2term=();     # aid2term

our %adv2Rel = ();    # adv2Rel
our %adv2Rela = ();   # adv2Rela


our %engDids = ();    # engDids
our %spaDids = ();    # spaDids
our %rxnCids = ();    # to store rxnorm cids.

# ---------------------
# standard output files
# ---------------------
our ($genAtom, $mthAtom, $xmAtom, $sbAtom, $rtrdAtom);
our ($utfRel, $sssRel, $ssxRel, $rscui2rscuiRel, $xmRel);
our ($sbMerge, $sameasMerge, $mthUSMerge, $did2cuiMerge, $selfNamingMerge);
our ($selfNamingMerge2);
our ($xmapAttr, $cxtAttr, $styAttr, $rscuiAttr, $sauiAttr, $sauiEngAttr);
our ($rsruiAttr);
our ($genCxt);

#---------------
# gen util begin
# strip blanks (at begin, end and more than 1 blank) in the given line.
sub stripBlanks {
  my($str, $doQuotes, $doBars) = @_;

  if ($doQuotes == 1) {
	$str =~ s/^\"//;	# strip leading double quote
	$str =~ s/\"$//;	# strip trailing double quote
  }
  if ($doBars == 1) {           # remove bars if any at ends.
    $str =~ s/^\|//;
    $str =~ s/\|$//;
  }
  $str =~ s/  */ /g;		# more than 1 blanks to 1 blank
  $str =~ s/^ *//;		# strip leading blanks
  $str =~ s/ *$//;		# strip trailing blanks
  $str =~ s/^ *//;		# strip leading blanks
  $str =~ s/ *$//;		# strip trailing blanks

  return $str;
}

# line, [seperator - \t]  => chop and trim individual elements and return 
# a list containing the elements.
sub catLine {
  my $str = shift;
  my $sep = '\t';
  $sep = shift if (scalar(@_) > 0);

  chomp($str);
  # remove dos line endings, if any.
  $str =~ s/\r$//;
  return map {&stripBlanks($_, 0, 1)} (split (/$sep/,$str));
}

# given a list of strings, checks if all of them are same or not.
sub sameStrings {
  my @strs = @_;
  my $str = @strs[0];
  my $ele;
  foreach $ele (@strs) {
    return 0 if ($str ne $ele);
  }
  return 1;
}

# ------------------------------------------------------------
# BEGIN - for the one time generation of required input files.
# ------------------------------------------------------------
# this is to save existing sauis assigned in the previous version.
# we should reuse them.
sub getKnownMthSauis {
  my $resDE2MSA = shift;  # resulting didExt2MthSaui array.
  my ($did, $ext, $ign, $saui, %maxMthSaui, $inFile);

  # initialize max to 0 for each extention.
  foreach $ext ('U', 'S', 'X', 'US', 'UX') { $maxMthSaui{"$ext"} = 0; }

  # now read existing sauis from prev files and populate the res array.
  foreach $inFile ('File.INT.PrevMthUtfTerms', 'File.INT.PrevMthSSTerms') {
    open(IN, "<:utf8", $glInv->getEle($inFile))
      or die "could not open $inFile.\n";
    <IN>;  # skip heading.
    while(<IN>) {
      chomp;
      ($did, $ext, $ign, $ign, $saui) = split(/\t/, $_);
      $$resDE2MSA{"$did|$ext"} = "$saui$ext";
      $maxMthSaui{"$ext"} = $saui if ($saui > $maxMthSaui{"$ext"});
    }
    close(IN);
  }
  # remember max sauis in the same res array.
  foreach $ext (keys (%maxMthSaui)) {
    $$resDE2MSA{"max$ext"} = $maxMthSaui{"$ext"};
  }
}

sub getMthSaui {
  my $resDE2MSA = shift;  # didExt2MthSaui array.
  my $did = shift;
  my $ext = shift;
  my $res;

  # for english, we set saui to ''. Ideally it should be $did$ext.
  # some thing to change in future releases. Otherwise, it can
  # produce duplicate atoms.
  return '' if ($langMode ne 'Spa');

  # spanish case.
  if (defined($$resDE2MSA{"$did|$ext"})) { return $$resDE2MSA{"$did|$ext"}; }

  # not there. so construct a new one and return.
  $res = $$resDE2MSA{"max$ext"}++;
  return "$res$ext";
}

our %cat=();
our %allc = ();

for (my $ch = 'a'; $ch le 'z'; $ch++) { $cat{$ch} = 'lc'; $allc{$ch}++; }
for (my $ch = 'A'; $ch le 'Z'; $ch++) { $cat{$ch} = 'uc'; $allc{$ch}++; }
for (my $ch = '0'; $ch le '9'; $ch++) { $cat{$ch} = 'nc'; $allc{$ch}++; }
$cat{''} = 'nl';

# remove subscripts and superscripts.
sub removeSSOld {
  my $a = shift;
  my $b = shift;
  my $c = shift;

  my ($p, $q, $r, $s);

  $p = $1;

  if (($cat{$a} ne '' && $cat{$a} eq $cat{substr($b,1,1)}) 
      || ($cat{$a} eq 'uc' && $cat{substr($b,1,1)} eq 'lc')) {
    $q = ' '; 
  } else { $q = ''; }

  $r = substr($b,1,length($b)-2);

  if (($cat{$c} ne '' && $cat{$c} eq $cat{substr($b,length($b)-2,1)}) 
      || ($cat{$c} eq 'uc' 
	  && substr($cat{substr($b,length($b)-2,1)},1,1) eq 'c')
      || ($cat{substr($2,length($b)-2,1)} eq 'nc' 
	  && $cat{$c} eq 'lc')) {
    $s = ' ';}
  else { $s = '';}

  return "$p$q$r$s$c";
}

# remove subscripts and superscripts.
sub removeSS {
  # we decide whether to insert spaces before and after the string.
  my $prevChar = shift;
  my $str = shift;
  my $nextChar = shift;

  # remove the delimeters [<> or ^^] from the begin and end.
  my $actStr = substr($str,1,length($str)-2);
  my $firstChar = substr($actStr,0,1);
  my $lastChar = substr($actStr, length($actStr)-1,1);

  # insert a spapce if prevChar and firstChar are of the same type.
  # insert a space if precChar is UC and firstChar is LC
  my $prevSpace = '';
  if ((defined($cat{$prevChar}) && $cat{$prevChar} eq $cat{$firstChar})
      || ($cat{$prevChar} eq 'uc' && $cat{$firstChar} eq 'lc')) {
    $prevSpace = ' ';
  }

  # insert a space if both lastChar and nextChar are of the same type.
  # insert a space if lastChar is either UC/LC/NC and nextChar is UC
  # insert a space if lastChar is NC and nextChar is LC
  my $nextSpace = '';
  if ((defined($cat{$nextChar}) && $cat{$nextChar} eq $cat{$lastChar}) 
      || ($cat{$nextChar} eq 'uc' && defined($allc{$lastChar}))
      || ($cat{$nextChar} eq 'lc' && $cat{$lastChar} eq 'nc')) {

    $nextSpace = ' ';
  }

  return "$prevChar$prevSpace$actStr$nextSpace$nextChar";
}

sub change2tagsA {
  my $a = shift;
  return "<sub>$a</sub>";
}


sub change2tagsB {
  my $a = shift;
  return "<sup>$a</sup>";
}

# pat1 has 3 parts:
# 1st - (.?) - match 0 or 1 of any char
#
# 2nd - (>[^<]*[^ <]<|\^[^^]*[^ ^]\^) - broken into (A or B)
# A is >[^<]*[^ <]<  and B is \^[^^]*[^ ^]\^
# A is match ">(all chars except < and space)<"
# B is match "^(all chars except ^ and spcae)^"
#
# 3rd - (.?) - match 0 or 1 of any char
my $pat1 = qr#(.?)(>[^<]*[^ <]<|\^[^^]*[^ ^]\^)(.?)#;

# pat2 is the middle part of pat1.
my $pat2a = qr#(>)([^<]*[^ <])(<)#;
my $pat2b = qr#(\^)([^^]*[^ ^])(\^)#;

sub getNoSSTerm {
  my $str = shift;
  while ($str =~ s/$pat1/&removeSS($1,$2,$3)/e) {}
  return $str;
}

sub getTagsTerm {
  my $str = shift;
  $str =~ s/$pat2a/&change2tagsA($2)/ge;
  $str =~ s/$pat2b/&change2tagsB($2)/ge;
  $str =~ s/\\/</g;
  $str =~ s/\`/>/g;
  return $str;
}

# foreach term in the desc file, removes the sub/super scripts and writes
# the term into ssterms file if the modified string is different from the
# original term.
sub makeSSTerms {
  my $result = shift;
  my $resDE2MSA = shift;  # didExt2MthSaui array.
  my ($id, $saui, $did, $ext);

  open (IN, "<:utf8", $glInv->getEle('File.INT.DESCRIPTIONS'))
    or die "Could not open File.INT.DESCRIPTIONS.\n";
  open (OUT, ">:utf8", $glInv->getEle('File.INT.MthSSTerms'))
	or die "Could not open File.INT.MthSSTerms\n";
  print OUT "DescriptionId\tEXT\tTerm\tSSTerm\tsaui\n";

  my ($term, @F, $term_tags, $term_noss, $inp);
  foreach $id (keys (%$result)) {
    $term = $$result{$id};
    ($did, $ext) = split(/\|/, $id);
    $term_noss = $term;
    $term_noss = &getNoSSTerm($term_noss);
    if ($term_noss ne $term) {
      $$result{"${did}|US"} = "$term|$term_noss";
      $saui = &getMthSaui($resDE2MSA, $did, 'US');
      #print "$did, US: saui is $saui\n";
      print OUT "$did\tUS\t$term\t$term_noss\t$saui\n";
      $term_tags = $term;
      $term_tags = &getTagsTerm($term_tags);
      if ($term_tags ne $term) {
	$$result{"${did}|UX"} = "$term|$term_tags";
	$saui = &getMthSaui($resDE2MSA, $did, 'UX');
	#print "$did, UX: saui is $saui\n";
	print OUT "$did\tUX\t$term\t$term_tags\t$saui\n";
      }
    }
  }

  while (<IN>) {
    @F = &catLine($_);
    $did = $F[0];
    $term = &stripBlanks($F[3]);
    next if ($langMode eq 'Spa' && $F[6] ne 'es');
    $term_noss = $term;
    $term_noss = &getNoSSTerm($term_noss);

    if ($term_noss ne $term) {
      $$result{"$did|S"} = "$term|$term_noss";
      $saui = &getMthSaui($resDE2MSA, $did, 'S');
      #print "$did, S: saui is $saui\n";
      print OUT "$did\tS\t$term\t$term_noss\t$saui\n";

      $term_tags = $term;
      $term_tags = &getTagsTerm($term_tags);
      if ($term_tags ne $term) {
	$$result{"$did|X"} = "$term|$term_tags";
	$saui = &getMthSaui($resDE2MSA, $did, 'X');
	#print "$did, X: saui is $saui\n";
	print OUT "$did\tX\t$term\t$term_tags\t$saui\n";
      }
    } else {
      $term_tags = $term;
      $term_tags =~ s/\\/</g;
      $term_tags =~ s/\`/>/g;
      if ($term_tags ne $term) {
	$$result{"$did|X"} = "$term|$term_tags";
	$saui = &getMthSaui($resDE2MSA, $did, 'X');
	print OUT "$did\tX\t$term\t$term_tags\t$saui\n";
      }
    }
  }
  close(IN);

  close(OUT);
}


# Prepares a file with did|tty|suppressive for the unknown status strings.
# it gets the tty and suppress values from the subsetmembers file via their
# status. Any remaining unknown terms are assgined "OB|O" values.
sub makeDid0TtySup {
  my $info = shift;
  my @all=();
  my ($ign, $id, $stat, $tty, $sup);

  open (DES, "<:utf8", $glInv->getEle('File.INT.DESCRIPTIONS'))
    or die "Could not open File.INT.DESCRIPTIONS\n";

  # assume that the english subset memebers are the first pair of
  # File.INP.subsets.0.
  my ($ign, $ssMems) = split(/\|/, $glInv->getEle('File.INP.Subsets.0'));
  #open (SSM, "<:encoding(cp1252)", $ssMems)
  open (SSM, "<:utf8", $ssMems)
    or die "Could not open File.INP.Subsets.0 <$ssMems>\n";

  # collect all the dids whose desc type = 0 [unknown] from desc file.
  $ign = <DES>;    # ignore header record.
  while (<DES>) {
    @all = &catLine($_);
    if (@all[5] == 0) {
      $$info{$all[0]} = '0';
    }
  }
  close(DES);
  my $ln = keys(%{$info});
  print ERRS "There are $ln terms with unknown type.\n\n";
  print "There are $ln terms with unknown type.\n\n";


  # now check if there is a status associated with these dids in
  # subsetmembers file.
  $ign = <SSM>;    # ignore header record.
  while (<SSM>) {
    ($ign, $id, $stat) = &catLine($_);
    if (defined($$info{$id})) {
      # found one of these dids whose status is 0 in desc file.
      if ($stat == 1) { $$info{$id} = "PT|N"; }
      elsif ($stat == 2) { $$info{$id} = "SY|N"; }
      elsif ($stat == 3) { $$info{$id} = "FN|N"; }
    }
  }
  close(SSM);

  # now for still unknown did types, assign "OB|O" as tty and suppressibility.
  foreach $id (keys (%{$info})) {
    if ($$info{$id} eq '0') { $$info{$id} = "OB|O"; }
  }

  # dump results
  open (OUT, ">:utf8", $glInv->getEle('File.INT.Did0Types'))
    or die "could not open File.INT.Did0Types\n";
  print OUT "DescriptionId\tTermType\tSuppressible\n";
  foreach $id (sort keys (%{$info})) {
    ($tty,$sup) = split(/\|/, $$info{$id});
    print OUT "$id\t$tty\t$sup\n";
  }
  close(OUT);

}

sub makeCid2Did {
  if ($langMode ne 'Spa') { &makeCid2DidEng; }
  else { &makeCid2DidSpa; }
}
sub makeCid2DidSpa {
  # spanish case.
  open (IN, "<:utf8", $glInv->getEle('File.INT.DESCRIPTIONS'))
    or die "Could not open File.INT.DESCRIPTIONS\n";
  <IN>;    # skip header.
  my ($did, $dstat, $cid, $term, $dtype, $lang, $isRetired1, $isRetired2);
  my ($ign, $key, @dids, @terms, $tty);
  my (%tempdid2term, %spaFN, %spaOP, %spaOF, %allCidsType );
  while (<IN>) {
    chomp;
    ($did, $dstat, $cid, $term, $ign, $dtype, $lang) = split(/\t/, $_);
    next if ($lang ne 'es');
    $allCidsType{"$cid"} = 0;

      # find if the given descriptor is obsolete.
    if ($term =~ /\Qprocedimiento reirado - RETIRADO -\E/) {
      $isRetired1 = 1;
    } else {
      $isRetired1 = 0;
    }

    if ($term =~ /\Qprocedimiento reirado - RETIRADO - (concepto no activo)\E/) {
      $isRetired2 = 1;
    } else {
      $isRetired2 = 0;
    }

    $key = "$dtype|$dstat|$lang";

    if ($isRetired1 == 0 && $isRetired2 == 0
	&& defined($dsl2tty{"$key"})
	&& defined($dsl2sup{"$key"})) {
      # regular case
      $tty = $dsl2tty{"$key"};
      if ($tty eq 'PT') { 
	$allCidsType{"$cid"} = 'PT';
	$cid2did{"$cid"} = $did;
	undef($spaFN{"$cid"});
	undef($spaOP{"$cid"});
	undef($spaOF{"$cid"});
      } elsif ($tty eq 'FN') {
	if (!defined($cid2did{"$cid"})) {
	  $spaFN{"$cid"} = $did;
	  undef($spaOP{"$cid"});
	  undef($spaOF{"$cid"});
	}
      } elsif ($tty eq 'OP') {
	if (!defined($cid2did{"$cid"}) && !defined($spaFN{"$cid"})) {
	  $tempdid2term{"$did"} = $term;
	  push(@{$spaOP{"$cid"}}, $did);
	  undef($spaOF{"$cid"});
	}
      } elsif ($tty eq 'OF') {
	if (!defined($cid2did{"$cid"}) 
	    && !defined($spaFN{"$cid"})
	    && !defined($spaOP{"$cid"})) {
	  $tempdid2term{"$did"} = $term;
	  push (@{$spaOF{"$cid"}}, $did);
	}
      }
    } elsif ($isRetired1 == 1 && $dtype == 1) {
      # retired with type 1 - OP
      if (!defined($cid2did{"$cid"}) && !defined($spaFN{"$cid"})) {
	$tempdid2term{"$did"} = $term;
	push(@{$spaOP{"$cid"}}, $did);
	undef($spaOF{"$cid"});
      }
    } elsif ($isRetired2 == 1 && $dtype == 3) {
      # retired with type 3 - OF
      if (!defined($cid2did{"$cid"}) 
	  && !defined($spaFN{"$cid"})
	  && !defined($spaOP{"$cid"})) {
	$tempdid2term{"$did"} = $term;
	push (@{$spaOF{"$cid"}}, $did);
      }
    }
  }
  close(IN);

  # now process.
  # assign FN's to unassigned cids.
  foreach $key (keys (%spaFN)) {
    if (!defined($cid2did{"$key"})) { 
      $allCidsType{"$key"} = 'FN';
      $cid2did{"$key"} = $spaFN{"$key"};
    }
  }

  # assign OPs to unassigned cids.
  foreach $key (keys (%spaOP)) {
    next if (defined($cid2did{"$key"}));
    $allCidsType{"$key"} = 'OP';
    @dids = @{$spaOP{"$key"}};
    if (1 == @dids) { $cid2did{"$key"} = $dids[0]; next; }

    # has multiple OPS. so remove leading parenthesized words and sort
    # and take the first one.
    # a better approach is to take the oldest did
    @terms = ();
    foreach $did (@dids) {
      $term = $tempdid2term{"$did"};
      $term =~ s/^\s*\([^\(]*\)\s*$//;
      push(@terms, "$term|$did");
    }
    $ign = (sort @terms)[0];
    ($term, $did) = split(/\|/, $ign);
    $cid2did{"$key"} = $did;
  }

  # now assign OFs to still unassigned cids.
  foreach $key (keys (%spaOF)) {
    next if (defined($cid2did{"$key"}));
    $allCidsType{"$key"} = 'OF';
    @dids = @{$spaOF{"$key"}};
    if (1 == @dids) { $cid2did{"$key"} = $dids[0]; next; }

    # has multiple OFS. so remove leading parenthesized words and sort
    # and take the first one.
    # a better approach is to take the oldest did
    @terms = ();
    foreach $did (@dids) {
      $term = $tempdid2term{"$did"};
      $term =~ s/^\s*\([^\(]\)\s*$//;
      push(@terms, "$term|$did");
    }
    $ign = (sort @terms)[0];
    ($term, $did) = split(/\|/, $ign);
    $cid2did{"$key"} = $did;
  }

  # now write the cid2did to a file
  # also check that all cids are mapped to dids.
  open (C2D, ">:utf8", $glInv->getEle('File.INT.Cid2Did'))
    or die "Could not open File.INT.Cid2Did\n";
  $ign = 0;
  @dids = ();
  foreach $key (keys (%allCidsType)) {
    if (!defined($cid2did{"$key"})) { push(@dids, $key); $ign++;}
    else { print C2D "1|$allCidsType{$key}|$key|$cid2did{$key}\n"; }
  }
  close(C2D);

  if ($ign > 0) {
    print ERRS "The follwoing cids<$ign> have no corresponding dids.\n";
    print "The follwoing cids<$ign> have no corresponding dids.\n";
    foreach $key (@dids) {
      print ERRS "\t$key\n";
      print "\t$key\n";
    }
    print ERRS "-----------------------------\n\n";
    print "-----------------------------\n\n";
  } else { 
    print ERRS "All cids are mapped to dids.\n\n";
    print "All cids are mapped to dids.\n\n"; 
  }
}


# relationships are specified between cids. In order to find the corresponding
# term, we use the following algorithm.
# 1) if there is pt term for a cid, use that term
# 2) if there is one and only one OP term (either english or british) term,
#    use that term.
# 3) If multiple english OP terms are present,
#    a) if all the op terms are same, use the first one.
#    b) if only one op is same as the of term, use it.
#    c) strip parenthesized items like (substance) [ambiguity) etc of the OF
#       term and compare it with the op terms. If there is only one op term
#       matches the stripped OF term, use it.
#    d) Removed OP terms containing "RETIRED" in them. If there is only one
#       remianing OP term, use it.
#    e) if the term is resolved, dump the OF term and all the OP terms into
#       a file (MG_URcid2did) and let Tammy or John pick one from each set
#       manually by setting the first field to 1.
# 4) If there are no OP terms, then use OF term.
# output:
#  File did2cid - containing the 1 | tty | cid | did for the cids where
#                 a corresponding did exists (without ambiguity per the above
#                 algorithm).
#  File MG_URdid2cid - containg a comment line displaying the OF term
#                      followed by all ambiguous OP terms. The format for 
#                      the OP terms is 0 | cid | did | opname
#
sub makeCid2DidEng {
  my ($ign, $did, $dstat, $cid, $term, $ics, $dtype, $lang);
  my ($retired, $key, $tty, $sup, $term_u, $term_ss, $saui);
  my ($cstat, $fsname, $ctv3id, $snid, $isp);
  my ($rel, $rela, $irela, $ext);
  my %did0i2ts=();

  my %cid2did=();     # cid2did
  my %did2term=();    # did2term
  my %cid2ope=();     # cid2ope
  my %cid2opb=();     # cid2opb
  my %cid2of=();      # cid2of


  # ----------------------------------
  # read all proper input files. BEGIN
  $glInv->prTime("Begin sct file");
  # next read didtype 0 recs - old types. the file has tty/supp for these
  # descriptionids
  open (DID0TYPE, "<:utf8", $glInv->getEle('File.INT.Did0Types'))
    or die "could not open File.INT.Did0Types.\n";
  <DID0TYPE>;			# ignore header record.
  while (<DID0TYPE>) {
    ($did, $tty, $sup) = &catLine($_);
    $did0i2ts{"$did"} = "$tty|$sup";
  }
  close(DID0TYPE);
  $glInv->prTime("done reading did 0 type atoms.");

  # read all proper input files. END
  # --------------------------------
  #&readDefInfo;

  my ($tmp1, $tmp2, $tmp3, $thisId, $modTerm);

  # collect all valid concept ids and initialize their tersm to 0.
  #open (CONS, "<:encoding(cp1252)", $glInv->getEle('File.INP.CONCEPTS')) or
  open (CONS, "<:utf8", $glInv->getEle('File.INP.CONCEPTS')) or
    die "Could not open File.INP.CONCEPTS file.\n";
  $ign = <CONS>;		# ignore first header record.
  while (<CONS>) {
    ($cid, $cstat, $fsname, $ctv3id, $snid, $isp) = &catLine($_);
    $cid2did{"$cid"} = 0;
  }
  close(CONS);
  $tmp1 = (keys (%cid2did));
  $glInv->prTime("Done reading valid cids. => $tmp1");


  # process atoms.
  open (DESC, "<:utf8", $glInv->getEle('File.INT.DESCRIPTIONS'))
    or die "Could not open File.INT.DESCRIPTIONS.\n";
  <DESC>;			# ignore the first headings line.

  while (<DESC>) {
    ($did, $dstat, $cid, $term, $ics, $dtype, $lang) = &catLine($_);
    if ($dtype == 0) {
      # Special case. Process by reading from the file.
      if (defined ($did0i2ts{"$did"})) {
	($tty, $sup) = split(/\|/, $did0i2ts{"$did"});
	if ($tty eq 'PT') {
	  # save the atom id and the term for context building later.
	  $cid2did{"$cid"} = $did;
	  # now that we found a PT, remove others.
	  # ???????
	  undef($did2term{"$did"});
	  undef($cid2ope{"$cid"});
	  undef($cid2opb{"$cid"});
	  undef($cid2of{"$cid"});
	}
      }
    } else {
      if ($term =~ /Retired procedure/) {
	# if its PT is not defined, then only remember.
	if ($cid2did{"$cid"} == 0) {
	  $did2term{"$did"} = $term;                  # remember the term
	  if ($dtype == 1) {	# OP case
	    if ($lang eq 'en-GB') {
	      push(@{$cid2opb{"$cid"}}, $did);        # remember opbri dids
	    } else {
	      push(@{$cid2ope{"$cid"}}, $did);        # remember openg dids
	    }
	  } elsif ($dtype == 3) { # OF case
	    $cid2of{"$cid"} = $did;                  # remember of dids
	  }
	}
      } else {
	# regular (non retired ones)
	$key = "$dtype|$dstat|$lang";
	if ((defined($dsl2tty{"$key"})) && (defined ($dsl2sup{"$key"}))) {

	  $tty = $dsl2tty{"$key"};
	  if ($tty eq 'PT') {
	    $cid2did{"$cid"} = $did;
	    undef($did2term{"$did"});
	    undef($cid2ope{"$cid"});
	    undef($cid2opb{"$cid"});
	    undef($cid2of{"$cid"});
	  } elsif ($cid2did{"$cid"} == 0) {
	    # if its PT is not defined, then only remember.
	    if ($tty eq 'OF') {
	      $did2term{"$did"} = $term;
	      $cid2of{"$cid"} = $did;
	    } elsif ($tty eq 'OP') {
	      $did2term{"$did"} = $term;
	      if ($lang eq 'en-GB') {push(@{$cid2opb{"$cid"}}, $did); }
	      else { push(@{$cid2ope{"$cid"}}, $did); }
	    }
	  }
	}
      }
    }
  }
  close(DESC);

  $glInv->prTime("Done reading valid dids.");

  open (C2D, ">:utf8", $glInv->getEle('File.INT.Cid2Did'))
    or die "Could not open File.INT.Cid2Did\n";
  open (URC2D, ">:utf8", $glInv->getEle('File.INP.URCid2Did'))
    or die "Could not open File.INP.URCid2Did file.\n";

  # now dump them.
  my (@vals, $ln, $ofTerm, $ofDid, $opDid, @matchedOPDids, $opTerm);
  my (@nonRetiredOPDids, $ln_nr, $sofTerm, @exactOPDids, $ln_ex);
  my (@opstrs);
  foreach $cid (keys (%cid2did)) {
    $did = $cid2did{"$cid"};
    if ($did != 0) {    # these are PT terms.
      print C2D "1|PT|$cid|$did\n";
    } elsif (defined ($cid2ope{"$cid"})) {
      @vals = @{$cid2ope{"$cid"}};
      $ln = @vals;
      if ($ln == 1) {
	# only one US OP term. so take it.
	print C2D "1|OPe1|$cid|@vals[0]\n";
      } else {
	# resolve ambiguity ?????????????
	# if all op terms are the same, use the first one.
	@opstrs = ();
	foreach $opDid (@{$cid2ope{"$cid"}}) {
	  push (@opstrs, $did2term{"$opDid"})
	}
	if (&sameStrings (@opstrs)) {
	  print C2D "1|OPems|$cid|@vals[0]\n";
	} else {
	  $ofDid = $cid2of{"$cid"};
	  $ofTerm = $did2term{"$ofDid"};
	  # strip ofterm of extra type info etc..
	  $sofTerm = $ofTerm;
	  $sofTerm =~ s/ \[Ambiguous\]//;
	  while ($sofTerm =~ s/[ ]*\([a-z,A-Z, ]*\)[ ]*$//) {
	  }

	  @exactOPDids = ();
	  @matchedOPDids = ();
	  @nonRetiredOPDids = ();
	  foreach $opDid (@{$cid2ope{"$cid"}}) {
	    $opTerm = $did2term{"$opDid"};
	    if ($opTerm eq $ofTerm) {
	      push(@exactOPDids, $opDid);
	    }
	    if ($opTerm eq $sofTerm) {
	      push(@matchedOPDids, $opDid);
	    }
	    if ($opTerm !~ /RETIRED/) {
	      push(@nonRetiredOPDids, $opDid);
	    }
	  }
	  $ln = @matchedOPDids;
	  $ln_ex = @exactOPDids;
	  $ln_nr = @nonRetiredOPDids;
	  if ($ln_ex > 0) {
	    # if multiple exact matches (ie. strings are same), take the 1st one.
	    print C2D "1|OPeme|$cid|@exactOPDids[0]\n";
	  } elsif ($ln == 1) {
	    print C2D "1|OPemm|$cid|@matchedOPDids[0]\n";
	  } elsif ($ln_nr == 1) {
	    # if after removing "RETIRED" from the terms, there exists
	    # only 1 OP term, take it.
	    print C2D "1|OPemnr|$cid|@nonRetiredOPDids[0]\n";
	  } else {
	    $ign = join('|', @{$cid2ope{"$cid"}});
	    print C2D "0|OPem|$cid|$ign\n";

	    print URC2D "#$cid|$ofTerm\n";
	    # these are ambiguious terms. dump them to file.
	    foreach $opDid (@{$cid2ope{"$cid"}}) {
	      print URC2D "0|$cid|$opDid|$did2term{$opDid}\n";
	    }
	    print URC2D "\n";
	  }
	}
      }
    } elsif (defined ($cid2opb{"$cid"})) {
      @vals = @{$cid2opb{"$cid"}};
      $ln = @vals;
      if ($ln == 1) {
	# only 1 brit op defined and no US op defined. so take it.
	print C2D "1|OPb1|$cid|@vals[0]\n";
      }
    } elsif (defined ($cid2of{"$cid"})
	     && (!defined($cid2ope{"$cid"}))
	     && (!defined($cid2opb{"$cid"}))) {
      # if OF is defined and OP is not defined, take it.
      print C2D "1|OF|$cid|$cid2of{$cid}\n";
    } else {
      print ERR "ERR: Term $cid has no corresponding term.\n";
    }
  }
  close(C2D);
  close(URC2D);
  $glInv->prTime("Done makeing cid2did");
}

# foreach term in the desc file, feed it (in batch) to lvg and find the 
# coresponding term. If the term is different from the original one, save
# it in utf3terms file for further use.
# format: did | term | utf8term.
sub makeutf8Terms {
  #my $d2term = shift;
  my $d2uterm = shift;
  my $resDE2MSA = shift;  # didExt2MthSaui array.
  my $ifile = "$tempDir/lvg_input";
  my $ofile = "$tempDir/lvg_output";
  my $saui = '';

  print ERRS "Reading desc file - " . `date` . "\n";
  print "Reading desc file - " . `date` . "\n";
  # first we need to create an intermediary file from desc.
  open (IN, "<:utf8", $glInv->getEle('File.INT.DESCRIPTIONS'))
    or die "Could not open File.INT.DESCRIPTIONS file.\n";
  open (OUT, ">:utf8", $ifile)   or die "Could not open $ifile file.\n";

  my @all;
  <IN>;
  while (<IN>) {
    @all = &catLine($_);
    next if ($langMode eq 'Spa' && $all[6] ne 'es');
    # dump did and term
    print OUT "$all[0]|$all[3]\n";
  }
  close(IN);
  close(OUT);
  print ERRS "Done Reading desc file - " . `date` . "\n";
  print "Done Reading desc file - " . `date` . "\n";

  # now call lvg prog to create normalized string.
  `$lvgProg -f:q5 -t:2 -F:1:2:3 < $ifile > $ofile`;
  print ERRS "Done with lvg - " . `date` . "\n";
  print "Done with lvg - " . `date` . "\n";

  # now remove the rows that are same from ofile
  my ($did, $term, $uterm);
  open(IN, "<:utf8", $ofile) or die "could not open $ofile.\n";
  open(OUT, ">:utf8", $glInv->getEle('File.INT.MthUtfTerms'))
    or die "could not open File.INT.MthUtfTerms.\n";
  print OUT "DescriptionId\tExt\tTerm\tUtfTerm\tsaui\n";
  while(<IN>) {
    ($did, $term, $uterm) = &catLine($_, '\|');
    if ($term ne $uterm) {
      #$$d2term{"$did"} = $term;
      $$d2uterm{"$did|U"} = $uterm;
      $saui = &getMthSaui($resDE2MSA, $did, 'U');
      #print "$did, U: saui is $saui\n";
      print OUT "$did\tU\t$term\t$uterm\t$saui\n";
    }
  }
  close(IN);
  close(OUT);
}

# foreach rid (relationship id from the rel file), assign and remember its
# rel/rela/irel for later use.
sub makeRelIdMap {
  my $vlRelNames = shift;	# relcid to name
  my $vlRels = shift;		# relcid to  rel
  my $vlRelas = shift;		# relcid to rela
  my $vlIRelas = shift;		# relcid to irela

  # read the rels file and record all valid relationship ids (rcids).
  my ($ign, $rname, $rel, $rela, $irela, $cid);
  my @all;
  #open (IREL, "<:encoding(cp1252)", $glInv->getEle('File.INP.RELATIONS'))
  open (IREL, "<:utf8", $glInv->getEle('File.INP.RELATIONS'))
    or die "Could not open File.INP.RELATIONS file.\n";
  $ign = <IREL>;		# ignore header record.
  while (<IREL>) {
    @all = &catLine($_);
    if (!defined ($$vlRelNames{$all[2]})) {
      $$vlRelNames{$all[2]} = ''; # blank for now. 
    }
  }
  close(IREL);


  # now open concepts file to find the relationship name of the above
  # relationship ids. (rids) and remember them.
  #open (CONS, "<:encoding(cp1252)", $glInv->getEle('File.INP.CONCEPTS'))
  open (CONS, "<:utf8", $glInv->getEle('File.INP.CONCEPTS'))
    or die "Could not open File.INP.CONCEPTS file.\n";
  $ign = <CONS>;		# ignore first header record.
  while (<CONS>) {
    @all = &catLine($_);
    if (defined $$vlRelNames{$all[0]}) {
      $$vlRelNames{"$all[0]"} = $all[2];
    } else {
      #print ERRS "Illegal rel id: $all[2] encountered. Skipping...\n";
      #print "Illegal rel id: $all[2] encountered. Skipping...\n";
    }
  }
  close(CONS);

  # now from file sct-relmap.txt, find rel/rela/irel for each relname.
  # this file is created manually by assignign rel/rela/irela for each
  # of the relationships (rcids).
  my %relNm2Info=();
  my ($val);
  foreach $val ($glInv->getList('SCT.RELMAP')) {
    ($rname, $rela, $irela, $rel) = split(/\|/, $val);
    $relNm2Info{"$rname"} = "$rela|$irela|$rel";
  }

  # now find and associate the rela/irela/rel for each rid.
  open(RMAP, ">:utf8", $glInv->getEle('File.INT.RelIdMap'))
    or die "could not open File.INT.RelIdMap.\n";
  print RMAP "ConceptId\tFSName\tRela\tIRela\tRel\n";
  # now fill vlRels, and vlRelas
  foreach $cid (keys (%{$vlRelNames})) {
    $rname = $$vlRelNames{"$cid"};
    if (defined ($relNm2Info{"$rname"})) {
      ($rela, $irela, $rel) = split(/\|/, $relNm2Info{"$rname"});
      $$vlRels{"$cid"} = $rel;
      $$vlRelas{"$cid"} = $rela;
      $$vlIRelas{"$cid"} = $irela;
      print RMAP "$cid\t$rname\t$rela\t$irela\t$rel\n";
    }
  }
  close(RMAP);
}


# generate a file to containing the mappings between mapconceptid to 
# its targetcode/rel for later use.
# mapadv file has - mapadvice to rel/rela (manually prepared one)
# map has ".., mapconid, .., .., targetid, .., advice"
# trg has "tid, .., tcode,.."
#
# out has - mapconid, targetcode, rel.
#
sub makeMidTgcdRel {
  my $mid2TcRel = shift;
  #open (MAP, "<:encoding(cp1252)", $glInv->getEle('File.INP.XMaps'))
  open (MAP, "<:utf8", $glInv->getEle('File.INP.XMaps'))
    or die "Could not open File.INP.XMaps.\n";
  <MAP>;     # ignore header record;
  #open (TRG, "<:encoding(cp1252)", $glInv->getEle('File.INP.XMTargets'))
  open (TRG, "<:utf8", $glInv->getEle('File.INP.XMTargets'))
    or die "Could not open File.INP.XMTargets.\n";
  <TRG>;     # ignore header record;
  open (OUT, ">:utf8", $glInv->getEle('File.INT.XM1T1'))
    or die "could not open File.INT.XM1T1.\n";

  my ($ign, $adv, $tcd, $rel, $trid, $mpid);

  # now remember targetid to targetcode.
  my %tgid2code=();
  while(<TRG>) {
    ($trid, $ign, $tcd) = &catLine($_);

    # dump only valid codes. if the code has multiple parts sep by bar (|),
    # then ignore it.
    if (($tcd ne "") && ($tcd !~ /\|/)) { $tgid2code{"$trid"} = $tcd; }
  }

  # now process mappings file.
  print OUT "MapConceptId\tTargetCode\tRel\n";
  while (<MAP>) {
    ($ign, $mpid, $ign, $ign, $trid, $ign, $adv) = &catLine($_);
    if (defined ($adv2Rel{"$adv"})) { $rel = $adv2Rel{"$adv"}; }
    else { $rel = 'RT?'; }   # default

    # if the target code is not available, just skip it.
    if (defined ($tgid2code{"$trid"})) { 
      $tcd = $tgid2code{"$trid"};
      print OUT "$mpid\t$tcd\t$rel\n";
      $$mid2TcRel{"mpid"} = "$tcd|$rel";
    }
  }
  close(TRG);
  close(MAP);
  close(OUT);
}

# prepare all the required files.
sub prepare_required_input_files {
  my $ln = @_;
  my $which = 15;
  if ($ln > 0) {$which = shift;}
  my ($id, $term, $uterm);
  #my %did2term=();              # did to tty|sup
  #my %did2uterm=();             # did to term|utfterm
  #my %ssterms=();               # did|ext to term|ssterm
  my %did2mthTerm = ();          # did2mthTerm

  my %vlrelNames = ();           # cid to relname
  my %vlRels = ();              # cid to rel
  my %vlRelas = ();             # cid to rela
  my %vlIRelas = ();            # cid to irela
  my %mid2tcdrel = ();           # mapid to targetcode|rel

  my $utfGen = 0;

  if ($which & 1) {
    if ($langMode ne 'Spa') {
      # first generate information of descriptors of unknown types.
      $glInv->prTime ("Begin did0 generation");
      # first replace high utf8 chars in desc file.
      &prepareINPFile($glInv->getEle('File.INP.DESCRIPTIONS'),
		       $glInv->getEle('File.INT.DESCRIPTIONS'));
      my %info=();
      &makeDid0TtySup(\%info);
      $glInv->prTime("End did0 generation");
    }

    &makeCid2Did;
    $glInv->prTime("End: genration of cid to did.");
  }

  if ($which & 2) {
    # if dealing with spanish, prepare mthSauis from prev version.
    # these are stored in didExt2mthSaui hash. (along with max sauis for
    # for each ext.
    my %didExt2mthSaui = ();    # didExt2mthSaui
    if ($langMode eq 'Spa') { &getKnownMthSauis(\%didExt2mthSaui); }

    # generate utf terms for terms with foreign chars.
    #$utfGen = 1;
    $glInv->prTime("Begin utf8 ");
    &makeutf8Terms(\%did2mthTerm, \%didExt2mthSaui );
    $glInv->prTime("End utf8 ");

    # next generate sup/sup terms for terms with sub/super scripts.
    $glInv->prTime("Begin SSTerms");;
    &makeSSTerms(\%did2mthTerm, \%didExt2mthSaui);
    $glInv->prTime("End SSTerms ");;
  }

  if ($which & 4 && $langMode ne 'Spa') {
    # generate relid map
    $glInv->prTime("Begin RelIdMap ");
    &makeRelIdMap(\%vlrelNames, \%vlRels, \%vlRelas, \%vlIRelas);
    $glInv->prTime("End RelIdMap ");
  }

  if ($which & 8 && $langMode ne 'Spa') {
    $glInv->prTime("Begin MapId2TrgCodeRel ");
    &makeMidTgcdRel(\%mid2tcdrel);
    $glInv->prTime("End MapId2TrgCodeRel ");
  }

  # release all memory
  #%did2term=();              # did to tty|sup
  #%did2uterm=();             # did to term|utfterm
  #%ssterms=();               # did|ext to term|ssterm
  %did2mthTerm = ();          # did2mthTerm

  %vlrelNames = ();           # cid to relname
  %vlRels = ();              # cid to rel
  %vlRelas = ();             # cid to rela
  %vlIRelas = ();            # cid to irela
  %mid2tcdrel = ();           # mapid to targetcode|rel

}

# ------------------------------------------------------------
# END - for the one time generation of required input files.
# ------------------------------------------------------------

#--------
# subs
#--------
sub init0 {
  $sab = $glInv->getEle('VSAB');
  $sabVersion = $glInv->getEle('VSABVersion');
  $lvgProg = $glInv->getEle('LVGProg');
  $curCidOrd = $glInv->getEle('AtomOrder.Begin', 100);
  $atomOrderInc = $glInv->getEle('AtomOrder.Increment', 10);
  $tempDir = $glInv->getEle('TEMPDIR', '../tmp');

  ## read defs : dty|stat|lang => tty|lang|sup
  my ($info, $desTy, $stat, $fLang, $tty, $tLang, $supp);
  foreach $info ($glInv->getList('SCT.TTYInfo')) {
    ($desTy, $stat, $fLang, $tty, $tLang, $supp) = split(/\|/, $info);
    $dsl2tty{"$desTy|$stat|$fLang"} = $tty;
    $dsl2sup{"$desTy|$stat|$fLang"} = $supp;
  }
  if ($langMode eq 'Spa') {
    $sctvsab = $glInv->getEle('SCTVSAB');
  } else {
    ## read mapadvice
    my %tempHash = $glInv->getHash('SCT.MAPADVICE');
    my ($advc, @inp);
    foreach $advc (keys (%tempHash)) {
      @inp = split(/\|/, $tempHash{"$advc"});
      $adv2Rel{"$advc"} = @inp[0];
      $adv2Rela{"$advc"} = (scalar(@inp) > 1) ? @inp[1] : '';
    }
  }
}

# initialize the application and all the necessary templates.
sub init {

  &init0;

  # create a main generator with proper seeds.
  $mainGenId = new IdGen();

  # make print templates
  # ATOM templates
  $genAtom = new Atom();
  $mthAtom = new Atom('Atom.MTH');

  $xmAtom = new Atom('Atom.XM');
  $sbAtom = new Atom('Atom.SB');
  $rtrdAtom = new Atom('Atom.RETIRED');


  # ATTRIBUTE templates
  $rscuiAttr = new Attribute('Attribute.RSCUI');
  $sauiAttr = new Attribute('Attribute.SAUI');
  $rsruiAttr = new Attribute('Attribute.RSRUI');
  $xmapAttr = new Attribute('Attribute.XMAP');
  $cxtAttr = new Attribute('Attribute.CXT');
  $styAttr = new Attribute('Attribute.STY');

  # in case of spanish, eng attributes.
  $sauiEngAttr = new Attribute('Attribute.SAUI_ENG');

  # MERGEFACT templates.
  $did2cuiMerge = new Merge('Merge.DID2CUI');
  $selfNamingMerge = new Merge('Merge.SELFNAMING');
  $selfNamingMerge2 = new Merge('Merge.SELFNAMING2');

  $mthUSMerge = new Merge('Merge.MTHUTFSS');
  $sbMerge = new Merge('Merge.SB');
  $sameasMerge = new Merge('Merge.SAMEAS');

  # RELATION templates.
  $rscui2rscuiRel = new Relation('Relation.RSCUI2RSCUI');
  $xmRel = new Relation('Relation.XM');

  $utfRel = new Relation('Relation.UTF');
  $sssRel = new Relation('Relation.SS_S');
  $ssxRel = new Relation('Relation.SS_X');

  # CONTEXT templates.
  $genCxt = new Context();

}

# --------- Context building begin -----------------
our %cui2rinfo=();
our $CRootNodeCID = 138875005;

# once the coxtext tree is build, it is used to assign atom ids.
our %cidOrder=();
# start with 100 so src atoms can have the first 100.
sub findCidOrder {
  my $cid = shift;
  my ($chld, @chlds);

  if (defined ($cidOrder{"$cid"})) { return; }
  $cidOrder{"$cid"} = $curCidOrd;
  $curCidOrd = $curCidOrd + $atomOrderInc;
  @chlds = $glInv->getChildren("$cid");
  foreach $chld (@chlds) {
    &findCidOrder($chld);
  }
}

sub dumpCXTInfo{
  # read rels file and get only those where relid = 11680003.
  # here dump the root node.
  print ERRS "Reading rels\n";
  print "Reading rels\n";
  &readRels;

  # now form atom ordering by first finding cid ordering.
  print ERRS "Forming Cid ordering\n";
  &findCidOrder($CRootNodeCID);

  print ERRS "Forming cxts\n";
  print "Forming cxts\n";
  &formCxts;

  # remember to find all descendants of the following cids for RxNorm
  # where cid = 373873005 (Pharmaceutical / biologic product)
  #       cid = 115668003 (Biological substance)
  #       cid = 410942007 (Drug or medicament)
  # ??????????????????
  my %rxnBranchCids = ();
  $glInv->getDescendants('373873005', \%rxnBranchCids);
  $glInv->getDescendants('115668003', \%rxnBranchCids);
  $glInv->getDescendants('410942007', \%rxnBranchCids);
  #$glInv->getDescendants('105590001', \%rxnBranchCids);

  # now find the intersection of this and rxnCids and keep them in rxnCids
  my $nd;
  foreach $nd (keys (%rxnCids)) {
    delete $rxnCids{"$nd"} if (!defined($rxnBranchCids{"$nd"}));
  }

  # dump RxNorm Cids.
  my $tempFile = $glInv->getEle('File.INT.RxnCids');
  open (RXNID, ">:utf8", $tempFile) 
    or die "could not open File.INT.RxnCids file.\n";
  my $cid;
  foreach $cid (keys (%rxnCids)) {
    print RXNID "$cid\n";
  }
  close(RXNID);


  # release memeory
  $glInv->releaseMemory;
  %cui2rinfo=();
}

# read all the relationships and make parent child rels.
sub readRels {
  if ($langMode ne 'Spa') { &readRelsEng; }
  else { &readRelsSpa; }
}

sub readRelsEng {
  my ($ign, $rid, $cid1, $rcid, $cid2, $chType, $rfnblt, $rgp);
  my ($rel, $rela, $ids, $rnam, $vnam, $key);

  # first read valid rels
  my %validRels=();
  my %validRelas=();
  my % aqrn=();
  my % aqvn=();
  open(VRELS, "<:utf8", $glInv->getEle('File.INT.RelIdMap'))
    or die "Could not open File.INT.RelIdMap\n";
  $ign = <VRELS>;   # ignore header record.
  while (<VRELS>) {
    ($cid1,$ign, $rela, $ign, $rel) = &catLine($_);
    $validRels{"$cid1"} = $rel;
    $validRelas{"$cid1"} = $rela;
  }
  close(VRELS);

  # process relationships file.
  #open (IREL, "<:encoding(cp1252)", $glInv->getEle('File.INP.RELATIONS'))
  open (IREL, "<:utf8", $glInv->getEle('File.INP.RELATIONS'))
    or die "Could not open File.INP.RELATIONS file.\n";
  <IREL>;       # ignore header record.
  while (<IREL>) {
    ($rid, $cid1, $rcid, $cid2, $chType, $rfnblt, $rgp) = &catLine($_);

    # dump attributes.
    # skip for invalid (skipped) rels.
    if (defined($validRels{"$rcid"})) {
      $rsruiAttr->dumpAttr({id => $rid, aname => 'CHARACTERISTICTYPE',
			    aval => $chType});
      $rsruiAttr->dumpAttr({id => $rid, aname => 'REFINABILITY',
			    aval => $rfnblt});
    }

    # dump AQ attributes.
    # 
    if ($chType == 1 && $rfnblt == 2) {
      $rnam = &getPFTerm($rcid);
      $vnam = &getPFTerm($cid2);
      $rscuiAttr->dumpAttr
	({id => $cid1, aname => 'AQ',
	  aval => "$rnam~$vnam~$rid~$rcid~$cid2~$chType~$rfnblt~$rgp"});
    }


    if ($rcid == 116680003) {
      # remember parents/chilren for context building later.
      #push(@{$parents{"$cid1"}}, $cid2);
      #push(@{$children{"$cid2"}}, $cid1);
      $glInv->addParChild($cid2,$cid1);
      $cui2rinfo{"$cid1|$cid2"} = "$rid|$rgp";
    } else {
      # for non hierarchical atoms
      if (defined ($validRels{"$rcid"})) {
	# dump relationships.
	$rscui2rscuiRel->dumpRel({id1 => $cid2, id2 => $cid1,
				  rname => $validRels{"$rcid"},
				  rela => $validRelas{"$rcid"},
				  srui => $rid, rgrp => $rgp});
      } else {
	print ERRS "Invalid rel encountered => $rcid. Skipping.\n";
      }
    }
    # dump mergefact for "same as" rel - 168666000
    if ($rcid == 168666000) {
      $sameasMerge->dumpMerge({id1 => $cid1, id2 => $cid2});
    }
  }
  close(IREL);

  print ERRS "done reading rels.\n";
  print "done reading rels.\n";
}

sub readRelsSpa {
  my ($ign, $rid, $cid1, $rcid, $cid2, $chType, $rfnblt, $rgp);
  my ($rel, $rela, $ids, $rnam, $vnam, $key, $cid, $did);

  # first read valid cids from the cid2did file. Rels file contains a lot
  # more (for the english). Not all rels are applicable in Spanish case.
  # we only take those rels where both cid1 and cid2 are present in the
  # descriptor file.

  # remember that the global cid2did is already populated.

  # process relationships file.
  #open (IREL, "<:encoding(cp1252)", $glInv->getEle('File.INP.RELATIONS'))
  open (IREL, "<:utf8", $glInv->getEle('File.INP.RELATIONS'))
    or die "Could not open File.INP.RELATIONS file.\n";
  <IREL>;       # ignore header record.
  while (<IREL>) {
    ($rid, $cid1, $rcid, $cid2, $chType, $rfnblt, $rgp) = &catLine($_);

    if ($rcid == 116680003) {
      # remember parents/chilren for context building later.
      if (defined($cid2did{"$cid1"}) && defined($cid2did{"$cid2"})) {
	#push(@{$parents{"$cid1"}}, $cid2);
	#push(@{$children{"$cid2"}}, $cid1);
	$glInv->addParChild($cid2,$cid1);
	$cui2rinfo{"$cid1|$cid2"} = "$rid|$rgp";
      }
    }
  }
  close(IREL);

  print ERRS "done reading rels.\n";
  print "done reading rels.\n";
}



# find the context by calling recursively findCxt on each node's parents.
sub formCxts {
  my $level = 1;
  my (@temp, $key);
  open (TCON, ">:utf8", "$tempDir/Cid2Did") or die "no con2did file.\n";
  foreach $key (keys (%cid2did)) {
    print TCON "$key|$cid2did{$key}\n";
  }
  close(TCON);
  open (TCON2, ">:utf8", "$tempDir/Did2Aid") or die "no did2aid file.\n";
  foreach $key (keys (%did2aid)) {
    print TCON2 "$key|$did2aid{$key}\n";
  }
  close(TCON2);
  open (TCON3, ">:utf8", "$tempDir/Aid2Term") or die "no aid2term file.\n";
  foreach $key (keys (%aid2term)) {
    print TCON3 "$key|$aid2term{$key}\n";
  }
  close(TCON3);

  # now form and get context info
  $glInv->prTime("Preparing context tress");
  $glInv->prepareCxts;
  $glInv->prTime("Done preparing context trees");

  my $parRef = $glInv->getParentsRef;
  my $chldRef = $glInv->getChildrenRef;
  my $pathRef = $glInv->getParentPathsRef;
  my ($nd, $nd1, $nd2, $cxt1, $cxt2, $cxtnum, $sgid1, $sgid2, $nd1Term);
  my ($aval, $srui, $sgrp, $hcd);

  foreach $nd (sort keys %{$parRef}) {
    $cxtnum = 0;
    foreach $cxt1 (@{$$pathRef{"$nd"}}) {
    $cxtnum++;

    $nd1 = &getCid2Aid($nd);
    $sgid1 = $nd;
    ($sgid2) = reverse (split(/\|/, $cxt1));;
    ($srui,$sgrp) = split(/\|/, $cui2rinfo{"$sgid1|$sgid2"});

    $cxt2 = join('.', (map {&getCid2Aid($_)} (split(/\|/, $cxt1))));

    $genCxt->dumpCxt2({id1 => $nd1, ptnm => $cxt2,
		       sgid1 => $sgid1, sgid2 => $sgid2, srui => $srui});
    # dump only the first 10 context attrs per node.
    if ($cxtnum < 11) {
      $aval = join('~', (map { $aid2term{$_}} (split(/\./, $cxt2))));

      ($nd2) = reverse (split(/\./, $cxt2));
      $hcd = "$cxtnum\t:$srui:$sgrp~$nd1~ROOT_SOURCE_CUI~SNOMEDCT_US~$nd2~ROOT_SOURCE_CUI~SNOMEDCT_US";
      $nd1Term = $aid2term{"$nd1"};
      $cxtAttr->dumpAttr({id => $nd1, aname => 'CONTEXT',
			  aval => "$hcd\t$aval\t$nd1Term\t\t"});
    } elsif ($cxtnum == 11) {
      # dump the "more exists" here. ??????????????
      $hcd = "$cxtnum\t::~$nd1~ROOT_SOURCE_CUI~SNOMEDCT_US~$nd2~ROOT_SOURCE_CUI~SNOMEDCT_US";
      $cxtAttr->dumpAttr({id => $nd1, aname => 'CONTEXT',
			  aval => "$hcd\t\tSNOMED CT Concept\tMore Concepts not shown\t\t"});
    }
    }
  }
}

# --------- Context building end -----------------



# --------- Xmaps begin --------------------------
# convert a char to xml char.
sub toXmlChar {
  my $str = shift;
  # do the conversion here.????????????????????
  $str = sprintf("&#x%02X;", ord($str));
  return $str;
}

# dump crossmap sets. We will have 1 for each crossmap set, ie, between
# snomedct and each source we are mapping to. Currently we are only mapping
# to icd9. So we will have only 1 XMAP set.
sub dumpXMapSets {
  my ($mId, $mNm, $mType, $mScId, $mScNm, $mScVr, $mRId, $mSep, $mRType, $ign);
  my $sos = 'This set maps SNOMEDCT_US concept identifiers to ICD-9-Cm codes; a single SNOMEDCT_US concept id may be  mapped to one or more ICD-9-CM codes';
  my ($x1);
  #open (XMSETS, "<:encoding(cp1252)", $glInv->getEle('File.INP.XMSets'))
  open (XMSETS, "<:utf8", $glInv->getEle('File.INP.XMSets'))
    or die "Could not open File.INP.XMSets.\n";
  <XMSETS>;            # ignore the first headings row.
  while (<XMSETS>) {
    chomp;
    ($mId, $mNm, $mType, $mScId, $mScNm, $mScVr, $mRId, $mSep, $mRType)
      = split(/\t/, $_);

    # for some reason this not workign when "|" is presetn in the input line.
    #($mId, $mNm, $mType, $mScId, $mScNm, $mScVr, $mRId, $mSep, $mRType)
     # = &catLine($_);
    #print "mId => <$mId>\nmNm => <$mNm>\nmType => <$mType>\nmScId => <$mScId>\n";
    #print "mScNm => <$mScNm\nmScVr> => <$mScVr>\nmRId => <$mRId>\n";
    #print "mSep => <$mSep>\nmRType => <$mRType>\n";

    # create an XM atom.
    # since 070131, name has been changed from
    #    "$sab mappings to ICD9CM_$mScVr" to
    #    "$sab mappings to ICD9CM_$mScVr Mappings", 
    $xmAtom->dumpAtom({name => "$sab to ICD9CM_$mScVr Mappings",
		       code => $mId, saui => $mId});

    # dump all the attributes (properties of this xmap).
    if (!defined ($mRType)) { $mRType = ''; }
    $x1 = &toXmlChar($mSep);
    # since 070131, changed MAPSETID to MAPSETSID
    #$xmapAttr->dumpAttr({ id => $mId, aname => 'MAPSETID', aval => $mId});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETSID', aval => $mId});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETNAME',aval => $mNm });
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETTYPE',aval => $mType });
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETSCHEMEID', aval => $mScId});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'TARGETSCHEMEID', aval => $mScId});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETSCHEMENAME', aval => $mScNm});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETSCHEMEVERSION', aval => $mScVr});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETREALMID', aval => $mRId});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETSEPARATORCODE', aval => $x1});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETRULETYPE', aval => $mRType});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MTH_UMLSMAPSETSEPARATOR', aval => ','});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'FROMVSAB', aval => $sab});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'FROMRSAB', aval => 'SNOMEDCT_US'});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'TOVSAB', aval => "ICD9CM_$mScVr"});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'TORSAB', aval => 'ICD9CM'});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETVSAB', aval => $sab});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MAPSETRSAB', aval => 'SNOMEDCT_US'});
    $xmapAttr->dumpAttr({ id => $mId, aname => 'SOS',aval => $sos});

    # since 070131, the value has been chnaged to N to ONE for
    # MTH_MAPSETCOMPLEXITY
    #$xmapAttr->dumpAttr({ id => $mId, aname => 'MTH_MAPSETCOMPLEXITY',
	#		   aval => 'ONE TO N'});
    $xmapAttr->dumpAttr({ id => $mId,
			  aname => 'MTH_MAPSETCOMPLEXITY',aval => 'N to ONE'});
    $xmapAttr->dumpAttr({ id => $mId, aname => 'MTH_MAPFROMCOMPLEXITY',
			   aval => 'SINGLE SCUI' });
    $xmapAttr->dumpAttr({ id => $mId, aname => 'MTH_MAPTOCOMPLEXITY',
			   aval => 'SINGLE CODE, MULTIPLE CODE'});
    $xmapAttr->dumpAttr({ id => $mId, aname => 'MTH_MAPFROMEXHAUSTIVE',
			   aval => 'N'});
    $xmapAttr->dumpAttr({ id => $mId, aname => 'MTH_MAPTOEXHAUSTIVE',
			   aval => 'N'});
    # since 070131, a new MAPSETVERSION is added.
    $xmapAttr->dumpAttr({ id => $mId, aname => 'MAPSETVERSION',
			   aval => $sabVersion});
  }
  close(XMSETS);
}


# need to find the rels here - 
# foreach xmap set (we only have one), dump each map as an attribute.
sub dumpXMaps {
  my ($mid, $ign, $tid, $mCid, $mOpt, $mPrt, $mRule, $mAdvc, $rel, $rela);
  my ($tCd, @inp, $ln);

  my %mcid2tcrel=();
  # now read mappings_1to1 info to get targetid adn rel.
  open (XM1T1, "<:utf8", $glInv->getEle('File.INT.XM1T1'))
    or die "Could not open File.INT.XM1T1.\n";
  $ign = <XM1T1>;    # ignore header record.
  while (<XM1T1>) {
    ($mCid, $tCd, $rel) = &catLine($_);
    $mcid2tcrel{"$mCid"} = "$tCd|$rel";
  }
  close(XM1T1);


  #open (XMAPS, "<:encoding(cp1252)", $glInv->getEle('File.INP.XMaps'))
  open (XMAPS, "<:utf8", $glInv->getEle('File.INP.XMaps'))
    or die "Could not open File.INP.XMaps\n";
  $ign = <XMAPS>;    # skip the first line.
  %tid2mid = ();
  while (<XMAPS>) {
    ($mid, $mCid, $mOpt, $mPrt, $tid, $mRule, $mAdvc) = &catLine($_);
    $tid2mid{"$tid"} = $mid;


    if (defined $adv2Rel{"$mAdvc"}) { $rel = $adv2Rel{"$mAdvc"}; }
    else { $rel = "RT?"; }
    if (defined $adv2Rela{"$mAdvc"}) { $rela = $adv2Rela{"$mAdvc"}; }
    else { $rela = 'mapped_to'; }

    $xmapAttr->dumpAttr
      ({id => $mid, aname => 'XMAP',
	aval => "$mOpt~$mPrt~$mCid~$rel~$rela~$tid~$mRule~$mAdvc~~"});
    $xmapAttr->dumpAttr
      ({id => $mid, aname => 'XMAPFROM',
	aval => "$mCid~~$mCid~SCUI~~"});

    if (defined ($mcid2tcrel{$mCid})) {
      ($tCd, $rel) = split(/\|/, $mcid2tcrel{$mCid});
      $xmRel->dumpRel({id1 => $tCd, id2 => $mCid,
		       rname => $rel, rela => 'mapped_to'});
    }
  }
  close(XMAPS);
}

# dump each target as an attribute.
sub dumpXMapTargets {
  my ($ign, $tid, $tCodes, $tRule, $tAdvc, $modCode);
  #open (XMTRGTS, "<:encoding(cp1252)", $glInv->getEle('File.INP.XMTargets'))
  open (XMTRGTS, "<:utf8", $glInv->getEle('File.INP.XMTargets'))
    or die "Could not open File.INP.XMTargets\n";
  $ign = <XMTRGTS>;   # ignore the first line.
  while (<XMTRGTS>) {
    ($tid, $ign, $tCodes, $tRule, $tAdvc) = &catLine($_);
    if (defined $tid2mid{"$tid"}) {
      #$modCode = "compute from $tCodes by replacing '|' with ','.
      $modCode = $tCodes;
      $modCode =~ s/\|/,/g;
      $xmapAttr->dumpAttr({id => $tid2mid{"$tid"}, aname => 'XMAPTO',
			   aval => "$tid~$tid~$modCode~CODE~$tRule~$tAdvc"});
    }
    if ($tCodes eq '' && defined($tid2mid{"$tid"})) {
      $xmapAttr->dumpAttr({id => $tid2mid{"$tid"},
			   aname => 'MAPSETXRTARGETID', aval => "$tid"});
    }
  }
  close(XMTRGTS);
}


# --------- Xmaps end --------------------------



# --------- History Begin --------------------------
# Is the supplied cid a valid conceptid.
sub isConcept {
  my $cid = shift;
  return 1 if (defined ($cid2did{"$cid"}));
  return 0;
}
# Is the supplied descid a valid description id.
sub isDesc {
  my $did = shift;
  return 1 if (defined ($did2aid{"$did"}));
  return 0;
}

# given a cid, get its aid (via did)
sub getCid2Aid {
  my $cid = shift;
  my $did = $cid2did{"$cid"};  # get this concepts PF term did.
  return $did2aid{"$did"};  # get its aid
}

# given a cid, returns its term
sub getPFTerm {
  my $cid = shift;
  my $did = $cid2did{"$cid"};  # get this concepts PF term did.
  my $aid = $did2aid{"$did"};  # get its aid
  return $aid2term{"$aid"};    # get this aids term. 
}

# read the history file and dump the info as attributes.
sub dumpComHistAttrs {
  my ($ign, $comid, $rver, $ctype, $stat, $reason);
  my ($iscon, $isdes);
  #open (CMPHST, "<:encoding(cp1252)", $glInv->getEle('File.INP.HISTORY'))
  open (CMPHST, "<:utf8", $glInv->getEle('File.INP.HISTORY'))
    or die "Could not open File.INP.HISTORY.\n";
  $ign = <CMPHST>;    # ignore header record.
  while (<CMPHST>) {
    ($comid, $rver, $ctype, $stat, $reason) = &catLine($_);
    $iscon = &isConcept($comid);
    $isdes = &isDesc($comid);
    if ($iscon == 1) {
      $rscuiAttr->dumpAttr
	({id => $comid, aname => 'COMPONENTHISTORY',
	  aval => "$comid~$rver~$ctype~CONCEPTSTATUS~$stat~$reason"});
    } elsif ($isdes == 1) {
      $sauiAttr->dumpAttr
	({ id => $comid, aname => 'COMPONENTHISTORY',
	   aval => "$comid~$rver~$ctype~DESCRIPTIONSTATUS~$stat~$reason"});
    } else {
      # ????????? is this a true error???????????
      print ERRS "Error: $comid is neither a concept not a desc.\n";
    }
  }
  close(CMPHST);
}
# --------- History end --------------------------

# --------- Subset begin --------------------------

# make sure to create a subset atom with tty = SB.
sub dumpSubsetInfo {
  my ($ign, $sid, $soid, $svr, $snam, $stype, $lang, $rlid, $cxid);
  my ($mid, $mstat, $lid);

  # Subsets need to be tied to the exisitng ones if any. So get old subsetids
  my %oldInfo=();
  #open(IN, "<:encoding(cp1252)", $glInv->getEle('File.INP.OldSubset'))
  open(IN, "<:utf8", $glInv->getEle('File.INP.OldSubset'))
    or die "could not open old subset file: File.INP.OldSubset.\n";
  while (<IN>) {
    ($sid, $soid) = &catLine($_);
    $oldInfo{"$soid"} = $sid;
  }
  close(IN);

  # process each subset
  my %subsets = $glInv->getHash('File.INP.Subsets');
  my ($eachSet, $ssDefFile, $ssMemFile);
  foreach $eachSet (keys (%subsets)) {
    ($ssDefFile, $ssMemFile) = split(/\|/, $subsets{"$eachSet"});

    # first create the subset atoms and its attributes.
    #open (SUBSET, "<:encoding(cp1252)", $ssDefFile)
    open (SUBSET, "<:utf8", $ssDefFile)
      or die "could not open subset file: $ssDefFile\n";
    $ign = <SUBSET>;		# ignore header record.
    while (<SUBSET>) {
      ($sid, $soid, $svr, $snam, $stype, $lang, $rlid, $cxid) = &catLine($_);
      $sbAtom->dumpAtom({name => $snam, code => $sid, saui => $sid});
      # merge with old subsetid, if any
      if (defined($oldInfo{"$soid"})) {
	$ign = $oldInfo{"$soid"};
	$sbMerge->dumpMerge({id1 => $sid, id2 => $ign});
      }

      $sauiAttr->dumpAttr({id => $sid, aname => 'SUBSETORIGINALID',
			   aval => $soid});
      $sauiAttr->dumpAttr({id => $sid, aname => 'SUBSETVERSION',
			   aval => $svr});
      $sauiAttr->dumpAttr({id => $sid, aname => 'SUBSETTYPE',
			   aval => $stype});
      $sauiAttr->dumpAttr({id => $sid, aname => 'SUBSETLANGUAGECODE',
			   aval => $lang});
      $sauiAttr->dumpAttr({id => $sid, aname => 'SUBSETREALMID',
			   aval => $rlid});
      $sauiAttr->dumpAttr({id => $sid, aname => 'SUBSETCONTEXTID',
			   aval => $cxid});
    }
    close(SUBSET);

    # now dump subset memeber attrs.
    #open(SSMEM, "<:encoding(cp1252)", $ssMemFile)
    open(SSMEM, "<:utf8", $ssMemFile)
      or die "Could not open subset memebers file: $ssMemFile\n";
    $ign = <SSMEM>;		# ignore header cord.
    while (<SSMEM>) {
      ($sid, $mid, $mstat, $lid) = &catLine($_);
      if ($langMode ne 'Spa') {
	$sauiAttr->dumpAttr({id => $mid, aname => 'SUBSETMEMBER',
			     aval => "$sid~$mstat~$lid"});
      } else {
	# spanish case.
	if (defined($engDids{"$mid"})) {
	  $sauiEngAttr->dumpAttr({id => $mid, aname => 'SUBSETMEMBER',
				  aval => "$sid~$mstat~$lid"});
	} elsif (defined($spaDids{"$mid"})) {
	  $sauiAttr->dumpAttr({id => $mid, aname => 'SUBSETMEMBER',
			       aval => "$sid~$mstat~$lid"});
	}
      }
    }
    close(SSMEM);
  }
}

# --------- Subset end --------------------------




# read the description file and create all the atoms/attributes/merges/rels.

sub processSCTFile {
  my ($ign, $did, $dstat, $cid, $term, $ics, $dtype, $lang);
  my ($retired, $key, $key1, $tty, $sup, $term_u, $term_ss, $saui);
  my ($cstat, $fsname, $ctv3id, $snid, $isp);
  my ($rel, $rela, $irela, $ext, $aid);
  my %did2mthTerm = ();		# did2mthTerm
  my %did2mthSaui = ();		# did2mthSaui
  my %did0i2ts=();		# did0i2ts
  my %mthsaui2said=();		# mthsaui2said
  my %cid2snid=();		# cid2snid

  %rxnCids=();		# rxnCids (to store msh cids)

  my %valTTY=();		# these are the valid ttys for msh.
  $valTTY{'PT'} = 1;
  $valTTY{'PTGB'} = 1;
  $valTTY{'FN'} = 1;
  $valTTY{'SY'} = 1;
  $valTTY{'SYGB'} = 1;

  my ($tmp1, $tmp2, $tmp3, $thisId, $modTerm, $mshTerm);

  # read cid2did mapping.
  if ($langMode ne 'Spa') {
    # english case. Just read the precomputed cid2did arrray from file.
    open (IN1, "<:utf8", $glInv->getEle('File.INP.URCid2Did'))
      or die "Could not open File.INP.URCid2Did file.\n";
    while (<IN1>) {
      chomp;
      next unless /^1\|/;
      ($ign, $cid, $did) = split(/\|/, $_);
      $cid2did{"$cid"} = $did;
    }
    close(IN1);
  }
  open(IN2, "<:utf8", $glInv->getEle('File.INT.Cid2Did'))
    or die "Could not open File.INT.Cid2Did file.\n";
  while (<IN2>) {
    chomp;
    ($ext, $ign, $cid, $did) = split(/\|/, $_);
    $cid2did{"$cid"} = $did if ($ext == 1);
  }
  close(IN2);


  # ----------------------------------
  # read all proper input files. BEGIN
  print ERRS "Begin sct file.\n";
  print "Begin sct file.\n";
  # read all valid conceptids
  #open (CONS, "<:encoding(cp1252)", $glInv->getEle('File.INP.CONCEPTS'))
  open (CONS, "<:utf8", $glInv->getEle('File.INP.CONCEPTS'))
    or die "Could not open File.INP.CONCEPTS file.\n";
  $ign = <CONS>;		# ignore first header record.
  while (<CONS>) {
    ($cid, $cstat, $fsname, $ctv3id, $snid, $isp) = &catLine($_);
    # dump concept attributes for only the English case.
    if ($langMode ne 'Spa') {
      $rscuiAttr->dumpAttr({id => $cid, aname => 'CONCEPTSTATUS',
			    aval => $cstat});
      $rscuiAttr->dumpAttr({id => $cid, aname => 'CTV3ID', aval => $ctv3id});
      $rscuiAttr->dumpAttr({id => $cid, aname => 'SNOMEDID', aval => $snid});
      $rscuiAttr->dumpAttr({id => $cid, aname => 'ISPRIMITIVE', aval => $isp});
    }
    $cid2snid{"$cid"} = $snid;
  }
  close(CONS);
  $glInv->prTime("done redaing coccepts");



  # first read utf8 and ss terms and save.
  foreach my $inFile ('File.INT.MthUtfTerms', 'File.INT.MthSSTerms') {
    open (IN, "<:utf8", $glInv->getEle($inFile))
      or die "Could not open $inFile.\n";
    <IN>;			# ignore headings row.
    while (<IN>) {
      ($did, $ext, $ign, $term_u, $saui) = &catLine($_);
      $did2mthTerm{"$did|$ext"} = $term_u;
      $did2mthSaui{"$did|$ext"} = $saui;
    }
    close(IN);
    $glInv->prTime("done reading $inFile.");
  }

  # next read didtype 0 recs - old types. the file has tty/supp for these
  # descriptionids
  if ($langMode ne 'Spa') {
    open (DID0TYPE, "<:utf8", $glInv->getEle('File.INT.Did0Types'))
      or die "could not open File.INT.Did0Types.\n";
    <DID0TYPE>;			# ignore header record.
    while (<DID0TYPE>) {
      ($did, $tty, $sup) = &catLine($_);
      $did0i2ts{"$did"} = "$tty|$sup";
    }
    close(DID0TYPE);
    $glInv->prTime("done reading did 0 type atoms.");
  }

  # read all proper input files. END
  # --------------------------------

  # process atoms. - regular English case.
  open (DESC, "<:utf8", $glInv->getEle('File.INT.DESCRIPTIONS'))
    or die "Could not open File.INT.DESCRIPTIONS.\n";
  <DESC>;			# ignore the first headings line.

  my $mshPat = qr/\(substance\)|\(product\)/;
  my $retPat1 = qw#procedimiento reirado - RETIRADO -#;
  my $retPat2 = qw#procedimiento reirado - RETIRADO - (concepto no activo)#;
  my ($isRetired1, $isRetired2, $uss_saui, $temp);
  my %mthTermVisited = ();

  my $debug = 0;
  while (<DESC>) {
    ($did, $dstat, $cid, $term, $ics, $dtype, $lang) = &catLine($_);

    if ($langMode ne 'Spa') {
      $mshTerm = 0;
      if ($term =~ $mshPat) { $mshTerm = 1; }

      # dump atoms.
      if ($dtype == 0) {
	# special case.
	# process by reading from the file.
	if (defined ($did0i2ts{"$did"})) {
	  ($tty, $sup) = split(/\|/, $did0i2ts{"$did"});
	  $genAtom->dumpAtom({name => $term, tty => $tty, code => $cid, 
			      saui => $did, scui => $cid, supp => $sup});
	  $aid = $genAtom->getLastId();
	  $did2aid{"$did"} = $aid;
	  $aid2term{"$aid"} = $term;
	  # remember if it is a msh cid.
	  if ($mshTerm == 1 && defined ($valTTY{"$tty"})) {
	    $rxnCids{"$cid"}++;
	  }
	} else {
	  print ERRS "WARN1: Ignoring atom $_\n";
	  next;
	}
      } else {
	if ($term =~ /Retired procedure/) {
	  $modTerm = "$term \[$cid2snid{$cid}\]";
	  if ($dtype == 1) {
	    $rtrdAtom->dumpAtom({name => $modTerm, tty => 'OP',code => $cid,
				  saui => $did, scui => $cid});
	    $aid = $rtrdAtom->getLastId();
	    $did2aid{"$did"} = $aid;
	    $aid2term{"$aid"} = $modTerm;
	  } elsif ($dtype == 3) {
	    $rtrdAtom->dumpAtom({name =>  $modTerm, tty => 'OF', code => $cid,
				  saui => $did, scui => $cid});
	    $aid = $rtrdAtom->getLastId();
	    $did2aid{"$did"} = $aid;
	    $aid2term{"$aid"} = $modTerm;
	  } else {
	    print ERRS "WARN2: Ignoring atom $_\n";
	    next;
	  }
	  # otherwise ignore it.
	} else {
	  # regular (non retired ones)
	  $key = "$dtype|$dstat|$lang";
	  if ((defined($dsl2tty{"$key"})) && (defined ($dsl2sup{"$key"}))) {
	    $tty = $dsl2tty{"$key"};
	    $genAtom->dumpAtom({name => $term, tty => $tty, code => $cid,
				saui => $did, scui => $cid,
				supp => $dsl2sup{"$key"}});
	    $aid = $genAtom->getLastId();
	    $did2aid{"$did"} = $aid;
	    $aid2term{"$aid"} = $term;
	    if ($mshTerm == 1 && defined ($valTTY{"$tty"})) {
	      $rxnCids{"$cid"}++;
	    }
	  } else {
	    print ERRS "WARN3: Ignoring atom $_\n";
	    next;
	  }
	}

      }
    } else {
      # spanish case.
      # remember if it is a spanish/english did.
      if ($lang eq 'es') { $spaDids{"$did"}++; }
      else { $engDids{"$did"}++; }

      # skip non spanish terms.
      next if ($lang ne 'es');

      # find if the given descriptor is obsolete.
      if ($term =~ /\Qprocedimiento retirado - RETIRADO -\E/) {
	$isRetired1 = 1;
      } else {
	$isRetired1 = 0;
      }
      if ($term =~ /\Qprocedimiento retirado - RETIRADO - (concepto no activo)\E/) {
	$isRetired2 = 1;
      } else {
	$isRetired2 = 0;
      }
      $key = "$dtype|$dstat|$lang";
      $modTerm = "$term \[$cid2snid{$cid}\]";

      # dump atoms.
      if ($isRetired1 == 0 && $isRetired2 == 0
	  && defined($dsl2tty{"$key"})
	  && defined($dsl2sup{"$key"})) {
	# regular case
	$tty = $dsl2tty{"$key"};
	$genAtom->dumpAtom({name => $term, tty => $tty, code => $cid,
			    saui => $did, scui => $cid,
			    supp => $dsl2sup{"$key"}});
	$aid = $genAtom->getLastId();
	$did2aid{"$did"} = $aid;
	$aid2term{"$aid"} = $term;
      } elsif ($isRetired1 == 1 && $dtype == 1) {
	# retired with type 1 - OP
	$genAtom->dumpAtom({name => $modTerm, tty => 'OP', code => $cid,
			    saui => $did, scui => $cid, supp => 'O'});
	$aid = $genAtom->getLastId();
	$did2aid{"$did"} = $aid;
	$aid2term{"$aid"} = $modTerm;
      } elsif ($isRetired2 == 1 && $dtype == 3) {
	# retired with type 3 - OF
	$genAtom->dumpAtom({name => $modTerm, tty => 'OF', code => $cid,
			    saui => $did, scui => $cid, supp => 'O'});
	$aid = $genAtom->getLastId();
	$did2aid{"$did"} = $aid;
	$aid2term{"$aid"} = $modTerm;
      } else {
	print ERRS "WARN : stray line in descritions\n\t$_.\tIgnoring..\n";
	next;
      }

    }

    # now check and create utf term if needed.
    if (defined ($did2mthTerm{"$did|U"})) {
      # it has a utf term. process it.
      $key = "$dtype|$dstat|$lang";

      if ((defined($dsl2tty{"$key"})) && (defined ($dsl2sup{"$key"}))) {
	$tty = $dsl2tty{"$key"};
	$tty = "MTH_$tty";
	$term_u = $did2mthTerm{"$did|U"};
	$uss_saui = $did2mthSaui{"$did|U"};
	# include cid so we do not use derived terms from other cids.
	$key1 = "$cid|U|$tty|$term_u|$uss_saui";
	if (defined($mthTermVisited{"$key1"})) {
	  $thisId = $mthTermVisited{"$key1"};
	} else {
	  $mthAtom->dumpAtom({name => $term_u, tty => $tty, code => $cid,
			      saui => $uss_saui, scui => $cid,
			      supp => $dsl2sup{"$key"}});
	  $thisId = $mthAtom->getLastId();
	  $mthTermVisited{"$key1"} = $thisId;
	}

	$mthsaui2said{"${did}U"} = $thisId;

	## now dump the rel and merge
	if ($langMode ne 'Spa') { $temp = $thisId; }
	else { $temp = $uss_saui; }

	# now dump the rel.
	$utfRel->dumpRel({id1 => $temp, id2 => $did});
	
	# dump merge.
	$mthUSMerge->dumpMerge({id1 => $temp, id2 => $cid});
      } else {
	print ERRS "utf term <$did|U> has no valid tty/sup. Ignoring..\n";
	print ERRS "\tkey is $key\n";
      }
    }

    # now check and create supsub term if needed.
    foreach $ext ('S', 'X', 'US', 'UX') {
      if (defined ($did2mthTerm{"$did|$ext"})) {
	# has a supsub term. process it.
	$key = "$dtype|$dstat|$lang";
	if ((defined($dsl2tty{"$key"})) && (defined ($dsl2sup{"$key"}))) {
	  $tty = $dsl2tty{"$key"};
	  $tty = "MTH_$tty";
	  $term_ss = $did2mthTerm{"$did|$ext"};
	  # NOTE: ideally saui here should be "${did}$ext". Otherwise, 
	  # this can produce duplicate atoms.
	  $uss_saui = $did2mthSaui{"$did|$ext"};
	  $mthAtom->dumpAtom({name => $term_ss, tty => $tty, code => $cid,
			      saui => $uss_saui, scui => $cid,
			      supp => $dsl2sup{"$key"}});
	  $thisId = $mthAtom->getLastId();
	  $mthsaui2said{"$did$ext"} = $thisId;

	  ## now dump the rel and merge
	  if ($langMode ne 'Spa') { $temp = $thisId; }
	  else { $temp = $uss_saui; }

	  # create rels here
	  if ($ext eq 'S' or $ext eq 'US') {
	    $sssRel->dumpRel({id1 => $temp, id2 => $did});
	  } elsif ($ext eq 'X' or $ext eq 'UX') {
	    $ssxRel->dumpRel({id1 => $temp, id2 => $did});
	    if ($ext eq 'UX') {
	      $sssRel->dumpRel({id1 => $temp, id2 => $did});
	    }
	  }

	  # dump merge.
	  $mthUSMerge->dumpMerge({id1 => $temp, id2 => $cid});
	} else {
	  print ERRS "supsub term <$did|$ext> has no valid tty/sup. ";
	  print ERRS "Ignoring.. \n\tkey is $key\n";
	}
      }
    }

    # done dumping atoms.
    # if we have come here, then we did create an atom.

    # dump desc attributes
    $sauiAttr->dumpAttr({id => $did, aname => 'DESCRIPTIONSTATUS',
			  aval => $dstat});
    $sauiAttr->dumpAttr({id => $did, aname => 'INITIALCAPITALSTATUS',
			  aval => $ics});
    $sauiAttr->dumpAttr({id => $did, aname => 'DESCRIPTIONTYPE',
			  aval => $dtype});
    $sauiAttr->dumpAttr({id => $did, aname => 'LANGUAGECODE',
			  aval => $lang});

    # create merge facts - SNOMEDCT_US-SCUI
    #merge SCUIs together, except the over-merged self-naming concept,
    # cid = 138875005
    if ($langMode ne 'Spa') {
      if ($cid != 138875005) {
	$did2cuiMerge->dumpMerge({id1 => $did, id2 => $cid});
      } else {
	if ($term !~ /Release/ || $dstat != 0) {
	  # merge SRC/RPT/V-SNOMEDCT_US with SNOMEDCT_US's non-versioned
	  # self-naming atoms in (SCUI=138875005)
	  $selfNamingMerge2->dumpMerge({id1 => $did, id2 => $did,
					mset => 'SNOMEDCT_US-OLDSRC'});
	} elsif ($dstat == 0 && $term =~ /Release/) {
	  # merge SRC/VPT/V-$sab with SNOMEDCT_US's current versioned
	  # self-naming atoms in (SCUI=138875005)
	  $selfNamingMerge->dumpMerge({id1 => "V-$sab", id2 => $did,
				       idq1 => 'SRC/VPT',
				       mset => 'SNOMEDCT_US-SRCVPT'});
	}
      }
    } else {
      # spanish case.
      if ($cid != 138875005) {
	  $did2cuiMerge->dumpMerge({id1 => $did, id2 => $cid});
      } else {
	# here create a merge between self naming SCT atoms (whose cid
	# is 138875005) and the SRC/VPT atom.
	$did2cuiMerge->dumpMerge({id1 => $did,id2 => '138875005'});
      }
    }
  }
  if ($langMode ne 'Spa') {
    # dump rel, rela for each concept that is used as a rel.
    open (RIDM, "<:utf8", $glInv->getEle('File.INT.RelIdMap'))
      or die "Could not open File.INT.RelIdMap.\n";
    $ign = <RIDM>;		# ignore header record.
    while (<RIDM>) {
      ($cid, $fsname, $rela, $irela, $rel) = &catLine($_);
      $rscuiAttr->dumpAttr({id => $cid, aname => 'UMLSREL', aval => $rel});
      $rscuiAttr->dumpAttr({id => $cid, aname => 'UMLSRELA', aval => $rela});
    }
    close(RIDM);
    close(DESC);

  }

}


# This is to generate atom order id.
sub assignAtomOrderIds {
  my ($rcid, $cid, $cid1, $cid2, $ign, $ordAtom);
  my @all=();
  my $srcOrder = 1;

  my $file1 = $glInv->getEle('File.Atoms');
  my $file2 = $glInv->getEle('File.Atoms2');
  open(IN, "<:utf8", $file1) or
    die "Could not open classes file: File.Atoms<$file1>\n";
  open (OUT, ">:utf8", $file2) or
    die "Could not open newAtoms file: File.Atoms2<$file2>.\n";
  while (<IN>) {
    chomp;
    @all = split(/\|/, $_);
    $cid = $all[10];
    if (defined ($cidOrder{"$cid"})) {
      $all[13] = $cidOrder{"$cid"}++;
    } else {
      $all[13] = $srcOrder++;
    }
    $ordAtom = join('|', @all);
    print OUT "$ordAtom\n";
  }
  close(IN);
  close(OUT);

  # now move atoms2 file to atoms.
  `/usr/bin/mv $file2 $file1`;
}

# generate mesh (rrf) files in /etc/rrf directory.
sub genRxnFiles {
  my %valTTY=();
  $valTTY{'PT'} = 1;
  $valTTY{'PTGB'} = 1;
  $valTTY{'FN'} = 1;
  $valTTY{'SY'} = 1;
  $valTTY{'SYGB'} = 1;
  my %ptCui2Saui=();
  my %code2cui=();
  my %saui2cui=();
  my %srui2cui1=();

  my ($aid, $src, $tg, $code, $i, $term, $sup, $saui, $tty);
  my ($id1, $id2, $rel, $rela, $sol, $t1, $srui, $rgrp, $saui1, $saui2);
  my ($id, $atn, $atv, $idt);


  # get all PT/FN/SY/PTGB atoms into CONSO whose cid is in rxnCids
  # MshCids is generated earlier - cids falling in certain trees of the hier.
  open (CLS, "<:utf8", $glInv->getEle('File.Atoms')) or 
    die "could not open File.Atoms file.\n";
  open (CONSO, ">:utf8", $glInv->getEle('File.OUT.RxnConso')) or
    die "could not open File.OUT.RxnConso file.\n";
  while (<CLS>) {
    chomp;
    ($aid, $src, $tg, $code, $i, $i, $i, $term, $sup, $saui) = split(/\|/, $_);
    next if ($src ne $sab);
    ($i, $tty) = split(/\//, $tg);

    if (defined ($rxnCids{$code})) {
      print CONSO "$code|ENG||||||$saui|$saui|$code||SNOMEDCT_US|$tty|$code|$term||$sup||\n" if (defined ($valTTY{"$tty"}));
      $code2cui{"$code"} = $code;
      $saui2cui{"$saui"} = $code;
      if ($tty eq 'PT') {
	$ptCui2Saui{"$code"} = $saui;
      }
    }
  }
  close(CONSO);

  # generate rels where both id1/id2 are in pt cuis.
  open (REL, "<:utf8", $glInv->getEle('File.Relations'))
    or die "Could not open File.Relations file. \n";
  open (DREL, ">:utf8", $glInv->getEle('File.OUT.RxnRel')) or
    die "no File.OUT.RxnRel file. \n";
  while (<REL>) {
    chomp;
    ($i, $i, $id1, $rel, $rela, $id2, $src, $sol, $i, $i,$i, $sup, $t1, $i, $i, $i, $srui, $rgrp) = split(/\|/, $_);
    if ($t1 eq 'ROOT_SOURCE_CUI'
	&& (defined($ptCui2Saui{"$id1"}))
	&& (defined($ptCui2Saui{"$id2"}))) {
      $saui1 = $ptCui2Saui{"$id1"};
      $saui2 = $ptCui2Saui{"$id2"};
      print DREL "$id1|$saui1|SCUI|$rel|$id2|$saui2|SCUI|$rela|$srui|$srui|$src|$sol|$rgrp|Y|$sup||\n";
      $srui2cui1{"$srui"} = $id1;
    }
  }
  close(REL);
  close(DREL);

  # generate sat. - NO NEED. Msh doesn't need this.
}

sub processSTYs {

  ## first prepare said2stys from the generated file sty_term_ids.
  # this file is generted during test insertion.
  my ($ign, $cid, $did, $said, $sty, $ptree, $pcid, $psaid, $thisCid);
  my %said2stys = ();
  open (IN, "<:utf8", $glInv->getEle('File.INP.StyTermIds'))
    or die "Could not open File.INP.StyTermIds file.\n";
  while(<IN>) {
    chomp;
    ($said, $sty) = split(/\|/, $_);
    push(@{$said2stys{"$said"}}, $sty);
  }
  close(IN);

  ## now read atoms file and assign stys to cids.(for known ones)
  # also remember cids whose stys are not populated in cidsNeedStys.
  my %cidsNeedStys = ();
  my %cid2stys = ();
  my %said2cid = ();
  my $said2cid = ();
  open(IN, "<:utf8", $glInv->getEle('File.Atoms'))
    or die "Could not open File.Atoms2 file.\n";
  while (<IN>) {
    chomp;
    ($said, $ign, $ign, $cid) = split(/\|/, $_);
    $said2cid{"$said"} = $cid;
    if (defined($said2stys{"$said"})) {
      foreach $sty (@{$said2stys{"$said"}}) {
	push(@{$cid2stys{"$cid"}}, $sty) unless grep(/$sty/, @{$cid2stys{"$cid"}});
      }
    } else {
      $cidsNeedStys{"$cid"} = 1;
    }
  }
  close(IN);

  #open (OUT, "> $tempDir/Said2Cid")
  open (OUT, ">:utf8", "$tempDir/Said2Cid")
    or die "Coudl not open $tempDir/Said2Cid\n";
  foreach $said (keys (%said2cid)) {
    print OUT "$said|$said2cid{$said}\n";
  }
  close(OUT);

  # by now most cids should have stys. find the remaining cids without stys.
  my $noStys = 0;
  foreach $cid (keys (%cidsNeedStys)) {
    if (defined($cid2stys{"$cid"})) { delete $cidsNeedStys{"$cid"}; }
    else { $noStys++; }
  }
  print ERRS "Concepts with no stys before context searching: $noStys\n";
  print "Concepts with no stys before context searching: $noStys\n";

  # for these walk through the context trees and assign stys.
  open (IN, "<:utf8", $glInv->getEle('File.Contexts'))
    or die "Could not open File.Contexts.\n";
  while (<IN>) {
    chomp;
    ($said, $ign, $ign, $ign, $ign, $ign, $ign, $ptree,
    $ign, $ign, $ign, $thisCid) = split(/\|/, $_);
    $cid = $said2cid{"$said"};
    if ($cid != $thisCid) {
      print ERRS "cids are not matching: <$cid> <$thisCid>\n";
      print "cids are not matching: <$cid> <$thisCid>\n";
      exit;
    }
    next if (!defined($cidsNeedStys{"$cid"}));

    # this cid doen'st have any assigned stys. so walk the tree and assign
    # one. May want to do that one in each context.
    foreach $psaid (reverse (split(/\./, $ptree))) {
      $pcid = $said2cid{"$psaid"};
      if (defined($cid2stys{"$pcid"})) {
	foreach $sty (@{$cid2stys{"$pcid"}}) {
	  push(@{$cid2stys{"$cid"}}, $sty) unless grep(/$sty/,@{$cid2stys{"$cid"}});
	}
	last;
      }
    }
  }
  close(IN);

  my $templn = 0;
  foreach $cid (keys (%cidsNeedStys)) {
    if (defined ($cid2stys{"$cid"})) { delete $cidsNeedStys{"$cid"}; $templn++;}
  }
  # find which cids are remaining.
  $noStys = keys (%cidsNeedStys);
  print ERRS 
    "After context searching\tResolved: $templn\tRemaining: $noStys\n";
  print "After context searching\tResolved: $templn\tRemaining: $noStys\n";

  # for these see if we can find them from history rels.
  #open (IN, "<:encoding(cp1252)", $glInv->getEle('File.INP.RELATIONS'))
  open (IN, "<:utf8", $glInv->getEle('File.INP.RELATIONS'))
    or die "Could not open File.INP.RELATIONS.\n";
  my ($cid1, $cid2, $ctype);
  $templn = 0;
  while(<IN>) {
    chomp;
    ($ign, $cid1, $ign, $cid2, $ctype) = split(/\|/, $_);
    next if ($ctype != 2);
    next if (!defined($cidsNeedStys{"$cid1"}));
    if (defined($cid2stys{"$cid2"})) { 
      @{$cid2stys{"$cid1"}} = @{$cid2stys{"$cid2"}};
      delete $cidsNeedStys{"$cid1"};
      $templn++;
    }
  }
  close(IN);

  # check if still any cids are remaining that need stys.
  $noStys = keys (%cidsNeedStys);
  print ERRS 
    "After history\tResolved: $templn\tRemaining: $noStys\n";
  print "After history\tResolved: $templn\tRemaining: $noStys\n";
  if ($noStys > 0) {
    foreach $cid (keys(%cidsNeedStys)) {
      print "\t cid: $cid\n";
      print ERRS "\t cid: $cid\n";
    }
    print "\n";
    print ERRS "\n";
  }


  # read cid2did, and did2said and attach stys to these saids.
  my %cid2did = ();
  my $did2said = ();
  #open(IN, "< $tempDir/Cid2Did")
  open(IN, "<:utf8", "$tempDir/Cid2Did")
    or die "Could not open $tempDir/Cid2Did\n";
  while (<IN>) {
    chomp;
    ($cid,$did) = split(/\|/, $_);
    $cid2did{"$cid"} = $did;
  }
  close(IN);
  my %did2said = ();
  #open(IN, "< $tempDir/Did2Aid")
  open(IN, "<:utf8", "$tempDir/Did2Aid")
    or die "Could not open $tempDir/Did2Aid\n";
  while (<IN>) {
    chomp;
    ($did,$said) = split(/\|/, $_);
    $did2said{"$did"} = $said;
  }
  close(IN);


  #open (OUT1, "> $tempDir/Cid2Sty") 
  open (OUT1, ">:utf8", "$tempDir/Cid2Sty") 
    or die "Could not open $tempDir/Cid2Sty\n";
  foreach $cid (sort keys (%cid2stys)) {
    if (defined($cid2did{"$cid"})) {
      $did = $cid2did{"$cid"};
      if (defined($did2said{"$did"})) {
	$said = $did2said{"$did"};
	foreach $sty (@{$cid2stys{"$cid"}}) {
	  print OUT1 "$cid|$sty\n";
	  $styAttr->dumpAttr({id => $said, aval => $sty});
	}
      }
    }
  }
  close(OUT1);
  #open (OUT2, "> $tempDir/Cid2NoSty")
  open (OUT2, ">:utf8", "$tempDir/Cid2NoSty")
    or die "Could not open $tempDir/Cid2NoSty\n";
  foreach $cid (sort keys (%cidsNeedStys)) {
    print OUT2 "$cid\n";
  }
  close(OUT2);
}

sub checkRequiredCfgElements {
  $glInv->getReqEle('SaidStart');
  $glInv->getReqEle('VSAB');
  $glInv->getReqEle('LVGProg');
  $glInv->getReqEle('File.INP.CONCEPTS');
  $glInv->getReqEle('File.INP.DESCRIPTIONS');
  $glInv->getReqEle('File.INP.RELATIONS');
  $glInv->getReqEle('File.INP.Subsets.0');
  #$glInv->getReqEle('File.INP.SSMems');
  $glInv->getReqEle('File.INP.OldSubset');
  $glInv->getReqEle('File.INT.MthUtfTerms');
  $glInv->getReqEle('File.INT.MthSSTerms');
  $glInv->getReqEle('File.INT.Cid2Did');

  $glInv->getReqEle('File.Atoms');
  $glInv->getReqEle('File.Atoms2');
  $glInv->getReqEle('File.Attributes');
  $glInv->getReqEle('File.Merges');
  $glInv->getReqEle('File.Relations');
  $glInv->getReqEle('File.Sources');
  $glInv->getReqEle('File.Termgroups');
  $glInv->getReqEle('File.Contexts');
  $glInv->getReqEle('SCT.TTYInfo.00');

  #$glInv->getReqEle('Atom.Defaults');
  #$glInv->getReqEle('Atom.SB');
  #$glInv->getReqEle('Atom.RETIRED');
  #$glInv->getReqEle('Attribute.Defaults');
  #$glInv->getReqEle('Attribute.CXT');
  #$glInv->getReqEle('Attribute.SAUI');
  #$glInv->getReqEle('Attribute.XMAP');
  #$glInv->getReqEle('Attribute.STY');
  #$glInv->getReqEle('Attribute.RSAUI');
  #$glInv->getReqEle('Attribute.RSRUI');
  #$glInv->getReqEle('Merge.Defaults');
  #$glInv->getReqEle('Merge.DID2CUI');
  #$glInv->getReqEle('Merge.MTHUTFSS');
  #$glInv->getReqEle('Merge.SELFNAMING');
  #$glInv->getReqEle('Merge.SB');
  #$glInv->getReqEle('Relation.Defaults');
  #$glInv->getReqEle('Relation.RSCUI2RSCUI');
  #$glInv->getReqEle('Relation.UTF');
  #$glInv->getReqEle('Relation.SS_S');
  #$glInv->getReqEle('Relation.SS_X');
  #$glInv->getReqEle('Relation.XM');
  #$glInv->getReqEle('Context.Defaults');

  if ($langMode ne 'SPA') {
    #$glInv->getReqEle('Attribute.SAUI_ENG');
  } else {
    $glInv->getReqEle('File.INP.URCid2Did');

    $glInv->getReqEle('File.INP.XMaps');
    $glInv->getReqEle('File.INP.XMSets');
    $glInv->getReqEle('File.INP.XMTargets');
    $glInv->getReqEle('File.INP.HISTORY');
    $glInv->getReqEle('File.INT.Did0Types');
    $glInv->getReqEle('File.INT.XM1T1');
    $glInv->getReqEle('File.INT.RelIdMap');

    $glInv->getReqEle('File.INT.RxnCids');
    $glInv->getReqEle('File.OUT.RxnConso');
    $glInv->getReqEle('File.OUT.RxnRel');

    $glInv->getReqEle('SCT.MAPADVICE.0');
    $glInv->getReqEle('SCT.RELMAP.00');

    #$glInv->getReqEle('Atom.XM');
    #$glInv->getReqEle('Merge.SAMEAS');
  }
}



# -----------------------------------------------------------------------------
# main main main
#------------------------------------------------------------------------------
sub doOnce {
  # call the following only once to create the necessary intermediary files.
  if ($langMode eq 'SPA') {
    # insert code for any specific gen files for SPA.
  } else {
    &prepare_required_input_files;
  }
  print ERRS "finished generting input files.\n";
  print "finished generting input files.\n";
}
sub main {
  $cfgFile = shift;
  $ofErrors = shift;

  if (defined $options{s}) {
    open (ERRS, ">>:utf8",$ofErrors) or die "Couldn't open $ofErrors file.\n";
  } else {
    open (ERRS, ">:utf8",$ofErrors) or die "Couldn't open $ofErrors file.\n";
  }

  $glInv = new NLMInv($cfgFile, *ERRS);

  $glInv->prTime("Begin");

  &checkRequiredCfgElements;

  if (defined $options{g}) {
    &init0;
    &doOnce;
    $glInv->prTime("End");

  } elsif (defined $options{s}) {
    if ($langMode ne 'Spa') {
      $glInv->invAppend;
      &init;
      &processSTYs;
      $glInv->invEnd;
      $glInv->prTime("End");
    }
    else {
      print ERRS "We do not produce stys for spanish snomed. Skipping.\n";
    }
  } elsif (defined $options{x}) {
    if ($langMode ne 'Spa') {
      $glInv->invBegin2;
      &init;
      $glInv->prTime("Start");
      &processNewXMaps;
      $glInv->invEnd;
      $glInv->prTime("End");
    }
  }else {
    # initialize
    $glInv->invBegin;
    &init;
    $glInv->prTime("Start");

    # dump source info
    $glInv->processSrc;
    if ($langMode eq 'Spa') {
      # here create a merge between V-SCTSPA of SRC/VPT atom's said to
      # the fixed cui 138875005.
      $selfNamingMerge->dumpMerge({id1 => $glInv->getVptSaid(),
				   id2 => '138875005'});
    }

    # process each input record
    &processSCTFile;
    $glInv->prTime("After SCTFile");


    if ($langMode ne 'Spa') {
      # process crossmaps
      &dumpXMapSets;		# dump mapsets
      &dumpXMaps;		# dump maps
      &dumpXMapTargets;		# dump targets.
      $glInv->prTime("After Xmaps");

      # process component history
      &dumpComHistAttrs;	# dump component history attributes
      $glInv->prTime("After HistAttrs.");
    }

    # process subset info.
    &dumpSubsetInfo;
    $glInv->prTime("After Subset");

    &dumpCXTInfo;		# dump HS and HC atoms
    $glInv->prTime("After Cxt");

    # main invertion ends here. close all files.
    $glInv->invEnd;


    # compute atom ordering and rewrite atoms file.
    $glInv->prTime("Begin Atom Ordering");
    &assignAtomOrderIds;
    $glInv->prTime("Done assigning atom ordering");

    # generate rrf files for msh.
    # after that copy mrdoc and create mrsab files there and give them to
    # Ratnakar or Robin.
    if ($langMode ne 'Spa') {
      &genRxnFiles;
      $glInv->prTime("End Mshfiles");
    }

    $glInv->prTime("End");
  }
  close(ERRS);
}

sub runMain {
  if (!defined $options{l} || uc($options{l}) eq 'ENG') {
    print ERRS "Processing English SNOMED\n";
    print "Processing English SNOMED\n";
    $langMode = '';
    &main("../etc/sct.cfg", "errors.sct");
  } elsif (uc($options{l}) eq 'SPA') {
    print ERRS "Processing Spanish SNOMED\n";
    print "Processing Spanish SNOMED\n";
    $langMode = 'Spa';
    &main("../trans/SCTSPA_2007_04_30/etc/sct_spa.cfg", "errors.sct_spa");
  }
}

sub prepareINPFile {
  my $origFile = shift;
  my $inpFile = shift;

  #open (IN, "<:encoding(cp1252)", $origFile) or
  open (IN, "<:utf8", $origFile) or
    die "Could not open $origFile file.\n";
  open (OUT, ">:utf8", $inpFile) or
    die "could not open $inpFile file.\n";

  while (<IN>) {
    chomp;
    # may not want to remove spaces
    #next if(/^\s*$/);

    s/\r//;                     # replace pc LB
    s/\x{00A0}/ /g;		# 0x00A0  - NO-BREAK SPAC (NBSP)

    # if the incoming file has any |s
    s/\|/&#124;/g;	        # any pipes need to converted to XML char. ent.

      # Replacements from cleanchar.pl
      s/\342\200\223/-/g;     # en dash
    s/\342\200\224/-/g;     # em dash
    s/\342\200\230/'/g;     # left single quote '
      s/\342\200\231/'/g;     # right single quote '
	s/\342\200\234/"/g;     # left double quote "
	  s/\342\200\235/"/g;     # right double quote "
	    #s/\342\211\245/>=/g;   # greater than or equal to
	    s/\302\255/-/g;         # soft hyphen

    s/\302\240/ /g;         # no-break space

    s/\x{2044}/\//g;		# Replace with a regular slash character

    print OUT "$_\n";
  }

  close(IN);
  close(OUT);
}

sub processNewXMaps {
  # dump xmapsets
  &dumpXMapSets;

  open (TRG, "<:utf8", $glInv->getEle('File.INP.XMTargets'))
    or die "Could not open File.INP.XMTargets.\n";
  <TRG>;     # ignore header record;

  my ($ign, $adv, $tcd, $rel, $trid, $mpid);
  my %mid2TcRel = ();
  # now remember targetid to targetcode.
  my %tgid2code=();
  while(<TRG>) {
    ($trid, $ign, $tcd) = &catLine($_);

    # dump only valid codes. if the code has multiple parts sep by bar (|),
    # then ignore it.
    if (($tcd ne "") && ($tcd !~ /\|/)) { $tgid2code{"$trid"} = $tcd; }
  }
  close(TRG);

  # now process mappings file.
  open (MAP, "<:utf8", $glInv->getEle('File.INP.XMaps'))
    or die "Could not open File.INP.XMaps.\n";
  <MAP>;     # ignore header record;

  my ($mid, $mCid, $mOpt, $mPrt, $tid, $mRule, $mAdvc, $rel, $rela);
  while (<MAP>) {
    ($mid, $mCid, $mOpt, $mPrt, $tid, $mRule, $mAdvc) = &catLine($_);

    if (defined ($adv2Rel{"$mAdvc"})) { $rel = $adv2Rel{"$mAdvc"}; }
    else { $rel = 'RT?'; }   # default
    if (defined ($adv2Rela{"$mAdvc"})) { $rela = $adv2Rela{"$mAdvc"}; }
    else { $rel = 'mapped_to'; }   # default


    $tid2mid{"$tid"} = $mid;
    $xmapAttr->dumpAttr
      ({id => $mid, aname => 'XMAP',
	aval => "$mOpt~$mPrt~$mCid~$rel~$rela~$tid~$mRule~$mAdvc~~"});
    $xmapAttr->dumpAttr({id => $mid, aname => 'XMAPFROM',
			 aval => "$mCid~~$mCid~SCUI~~"});

    # if the target code is not available, just skip it.
    if (defined ($tgid2code{"$tid"})) {
      $tcd = $tgid2code{"$tid"};
      $xmRel->dumpRel({id1 => $tcd, id2 => $mCid,
		       rname => $rel, rela => $rela});
    }
  }
  close(MAP);
  &dumpXMapTargets;
}

&runMain;




