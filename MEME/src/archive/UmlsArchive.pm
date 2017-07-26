#
# File: UmlsArchive.pm
# Author: Brian Carlsen (2008)
#
# Changes
# 10/30/2008 BAC: first version
#
package UmlsArchive;
use strict 'vars';
use strict 'subs';
use Digest::MD5 qw(md5_hex);
use File::Find;

#
# Configuration parameters
#
our $distRootDir     = "";
our $release         = "";
our $targetDirPath   = "";
our $baseDir         = "";
our %prefsui         = ();
our %newuis          = ();
our $globalBitPos    = 0;
our $maxGlobalBitPos = 0;

#
# Dir structure
#
our @archiveDirStructure = (
 "Comparable",          "Comparable/ORF",
 "Comparable/ORF/META", "Full",
 "Full/ORF",            "Full/RRF",
 "Original",            "Prod",
 "Prod/uris",           "Subset",
 "tmp", "Prod/qa", "Prod/log"
);
our %validDirStructure = (
                           ""                      => "ERROR",
                           "Comparable"            => "ERROR",
                           "Comparable/ORF"        => "ERROR",
                           "Comparable/ORF/META"   => "ERROR",
                           "Full"                  => "ERROR",
                           "Full/ORF"              => "ERROR",
                           "Full/ORF/META"         => "ERROR",
                           "Full/ORF/META/CHANGE"  => "ERROR",
                           "Full/RRF"              => "ERROR",
                           "Full/RRF/META"         => "ERROR",
                           "Full/RRF/META/CHANGE"  => "ERROR",
                           "Full/RRF/META/indexes" => "ERROR",
                           "Misc"                  => "WARNING",
                           "Original"              => "ERROR",
                           "Prod"                  => "ERROR",
                           "Prod/uris"             => "ERROR",
                           "Prod/qa"               => "ERROR",
                           "Prod/log"               => "ERROR",
                           "Subset"                => "WARNING",
                           "tmp"                   => "ERROR"
);
############################################################
# Configuration Procedures
############################################################
#
# Sets package configuration variables
# Returns void
#
sub configure {
 my ( $distRootDir, $release ) = @_;
 if ( !-e $distRootDir ) {
  die "Invalid input path: $distRootDir\n";
 }
 if ( !defined $ENV{"UMLS_ARCHIVE_ROOT"} ) {
  die '$UMLS_ARCHIVE_ROOT must be set\n';
 }
 setDistRootDir($distRootDir);
 setRelease($release);
}

#
# Sets release
# Returns void
#
sub setRelease {
 ($release) = @_;
 $baseDir = "$ENV{UMLS_ARCHIVE_ROOT}/$release";
}

#
# Sets "dist root" path
# Returns void
#
sub setDistRootDir {
 ($distRootDir) = @_;
}
########################################################
# UmlsArchive functions
########################################################
#
# Creates archive directories
# Returns void
#
sub createDirectories {
 unless ( mkdir $baseDir ) {
  die "Unable to create $baseDir ($! $?)\n";
 }
 my $dir;
 foreach $dir (@archiveDirStructure) {
  unless ( mkdir "$baseDir/$dir" ) {
   die "Unable to create $baseDir/$dir ($! $?)\n";
  }
 }
}

#
# Unpack DVDIMAGE.tar into "Original" directory
# Return void
#
sub copyOriginal {
 if ( -e "$distRootDir/DVD_Image/DVDIMAGE.tar" ) {
  my $CMD;
  open(
   $CMD,
"(cd $distRootDir/DVD_Image/DVDIMAGE; tar cf - .) | (cd $baseDir/Original; tar xvf -) |"
    )
    || die
    "Unable to move $distRootDir/DVD_Image/DVDIMAGE to $baseDir/Original\n";
  while (<$CMD>) {
   print "      $_";
  }
  close($CMD);
  if ( !-e "$baseDir/Original/MMSYS" ) {
   open( $CMD, "echo A | (cd $baseDir/Original; unzip mmsys.zip) |" )
     || die "Unable to unzip $baseDir/Original/mmsys.zip ($? $!)\n";
   while (<$CMD>) {
    print "      $_";
   }
   close($CMD);
  }
 } else {
  die "Could not find $distRootDir/DVD_Image/DVDIMAGE.tar\n";
 }
}

#
# Copy URIS info from dist root dir
# Return void
#
sub copyUris {
 if ( -e "$distRootDir/uris" ) {
  system(
    "(cd $distRootDir/uris; tar cf - .) | (cd $baseDir/Prod/uris; tar xf -)") ==
    0
    or die "can't tar or move from $distRootDir/uris to $baseDir/Prod/uris: $?";
 } else {
  die "Could not find $distRootDir/uris\n";
 }
}

#
# Copy Production QA data
# Return void
#
sub copyProductionQa {
 if ( -e "$distRootDir/RRF_usr/QA" ) {
  system(
   "(cd $distRootDir/RRF_usr/QA; tar cf - .) | (cd $baseDir/Prod/qa; tar xf -)")
    == 0
    or die "can't tar or move from $distRootDir/RRF_usr/QA to $baseDir/Prod/uris: $?";
 } else {
  die "Could not find $distRootDir/RRF_usr/QA\n";
 }
}

#
# Copy Production log info
# Return void
#
sub copyProductionLogs {
 if ( -e "$distRootDir/RRF_usr/log" ) {
  system(
"(cd $distRootDir/RRF_usr/log; tar cf - .) | (cd $baseDir/Prod/log; tar xf -)"
    ) == 0
    or die "can't tar or move from $distRootDir/RRF_usr/log to $baseDir/Prod/uris: $?";
  system(
"(cd $distRootDir/DVD_Image/log; tar cf - .) | (cd $baseDir/Prod/log; tar xf -)"
    ) == 0
    or die "can't tar or move from $distRootDir/DVD_Image/log to $baseDir/Prod/uris: $?";
  system(
"(cd $distRootDir/DVD_Image/; tar cf - packit*log*) | (cd $baseDir/Prod/log; tar xf -)"
    ) == 0
    or die "can't tar or move from $distRootDir/DVD_Image/packit.log to $baseDir/Prod/uris: $?";
 } else {
  die "Could not find $distRootDir/RRF_usr/log\n";
 }
}

#
# Create full Subset (run MetamorphoSys from "Original")
#
sub createFullSubset {
 my ($format)  = @_;
 my $metaDir   = "$baseDir/Original";
 my $destDir   = "$baseDir/Full/$format/META";
 my $mmsysHome = "$baseDir/Original/MMSYS";
 unless ( mkdir $destDir ) {
  die "Unable to create $destDir ($! $?)\n";
 }

 #
 # Check if we're using -P or -T
 #
 if ( -e "$baseDir/Original/plugins" ) {

  #
  # MMSYS-T
  #
  $mmsysHome = "$baseDir/Original";
  my $classpath = "$mmsysHome:$mmsysHome/lib/jpf-boot.jar";
  my $java      = "$mmsysHome/jre/solaris/bin/java";
  system "$java -version";
  if ( $? != 0 ) {
   $java = "$mmsysHome/jre/linux/bin/java";
   system "$java -version";
   if ( $? != 0 ) {
    die "$mmsysHome/jre does not work for linux or solaris\n";
   }
  }

  # Config file - need NLM input, $format output, keep everything
  my $IN;
  my $OUT;
  open( $IN, "$mmsysHome/config/$release/user.a.prop" )
    || die "Could not open $mmsysHome/config/$release/user.a.prop ($?, $!)\n";
  open( $OUT, ">$destDir/tmp.prop" )
    || die "Could not open $destDir/tmp.prop ($? $!)\n";
  while (<$IN>) {
   s/^(mmsys_input_stream)=.*/$1=gov.nih.nlm.umls.mmsys.io.NLMFileMetamorphoSysInputStream/;
   s/^(mmsys_output_stream)=.*/$1=gov.nih.nlm.umls.mmsys.io.${format}MetamorphoSysOutputStream/;
   s/^(.*)\.selected_sources=.*/$1.selected_sources=/;
   print $OUT $_;
  }
  close($IN);
  close($OUT);
  my $CMD;
  chdir($mmsysHome);
  open(
   $CMD, "$java -cp '$classpath' -Djava.awt.headless=true -Djpf.boot.config=$mmsysHome/etc/subset.boot.properties -Dlog4j.configuration='file://$mmsysHome/log4j.properties' -Dscript_type=.csh -Dfile.encoding=UTF-8 -Xms600M -Xmx1400M -Dinput.uri=$metaDir -Doutput.uri=$destDir -Dmmsys.config.uri=$destDir/tmp.prop org.java.plugin.boot.Boot |"
  ) || die "Could not open java command ($! $?)\n";
 } else {

  #
  # MMSYS-P
  #
  my $classpath =
    "$mmsysHome:$mmsysHome/lib/mms.jar:$mmsysHome/lib/objects.jar";
  my $java = "$mmsysHome/jre/solaris/bin/java";
  system "$java -version ";
  if ( $? != 0 ) {
   $java = "$mmsysHome/jre/linux/bin/java";
   system "$java -version ";
   if ( $? != 0 ) {
    die "$mmsysHome/jre does not work for linux or solaris\n";
   }
  }

  # Config file - need NLM input, $format output, keep everything
  my $IN;
  my $OUT;
  open( $IN, "$mmsysHome/config/mmsys.a.prop" )
    || die "Could not open $mmsysHome/config/mmsys.a.prop ($?, $
!)\n";
  open( $OUT, ">$destDir/tmp.prop" )
    || die "Could not open $destDir/tmp.prop ($? $!)\n";
  my $formatStream;
  if ( $format eq "ORF" ) { $formatStream = "OriginalMR"; }
  if ( $format eq "RRF" ) { $formatStream = "RichMR"; }

  while (<$IN>) {
   s/^(.*)\.remove_utf8=true/$1.remove_utf8=false/;
   s/^(mmsys_input_stream)=.*/$1=gov.nih.nlm.mms.NLMFileMetamorphoSysInputStream/;
   s/^(mmsys_output_stream)=.*/$1=gov.nih.nlm.mms.${formatStream}MetamorphoSysOutputStream/;
   s/^(.*)\.selected_sources=.*/$1.selected_sources=/;
   print $OUT "$_";
  }
  close($IN);
  close($OUT);

  # Run MMSYS
  my $CMD;
  open(
   $CMD, "$java -cp '$classpath' -Dinput.dir=$metaDir -Doutput.dir=$destDir -Dmmsys.dir=$mmsysHome -Dmmsys.config=$destDir/tmp.prop -Xms300M -Xmx1000M gov.nih.nlm.mms.BatchMetamorphoSys |"
  ) || die "Could not open java command ($? $!)\n";
  while (<$CMD>) {
   print "      $_";
  }
  close($CMD);
  unlink "$destDir/tmp.prop";
 }
}

#
# Build RRF MRCXT
#
sub buildMrcxtRRF {
 my $metaDir   = "$baseDir/Original";
 my $destDir   = "$baseDir/Full/RRF/META";
 my $mmsysHome = "$baseDir/Original/MMSYS";
 if ( !-e $destDir ) {
  die "Expected dir does not exist: $destDir\n";
 }

 #
 # Check if we're using -P or -T
 #
 if ( -e "$baseDir/Original/plugins" ) {

  #
  # MMSYS-T
  #
  $mmsysHome = "$baseDir/Original";
  my $classpath = "$mmsysHome:$mmsysHome/lib/jpf-boot.jar";
  my $java      = "$mmsysHome/jre/solaris/bin/java";
  system "$java -version";
  if ( $? != 0 ) {
   $java = "$mmsysHome/jre/linux/bin/java";
   system "$java -version";
   if ( $? != 0 ) {
    die "$mmsysHome/jre does not work for linux or solaris\n";
   }
  }
  my $CMD;
  chdir($mmsysHome);
  open(
   $CMD, "$java -cp '$classpath' -Djava.awt.headless=true -Djpf.boot.config=$mmsysHome/etc/cxt.boot.properties -Dlog4j.configuration='file://$mmsysHome/etc/cxt.log4j.properties' -Dfile.encoding=UTF-8 -Xms600M -Xmx1400M -Dbuild.sibs=true -Dbuild.children=true -Dcompute.xc=true -Dversioned.sabs=false -Dadd.unicode.bom=false -Dwrite.mrcxt.file.statistics=false -Dmax.contexts=false -Dsource.dir=$baseDir/Full/RRF/META org.java.plugin.boot.Boot |"
  ) || die "Could not open java command ($? $!)\n";
  while (<$CMD>) {
   print "      $_";
  }
  close($CMD);
 } else {

  #
  # MMSYS-P
  #
  my $classpath =
    "$mmsysHome:$mmsysHome/lib/mms.jar:$mmsysHome/lib/objects.jar";
  my $java = "$mmsysHome/jre/solaris/bin/java";
  system "$java -version ";
  if ( $? != 0 ) {
   $java = "$mmsysHome/jre/linux/bin/java";
   system "$java -version ";
   if ( $? != 0 ) {
    die "$mmsysHome/jre does not work for linux or solaris\n";
   }
  }

  # Run MRCXT Builder
  my $CMD;
  open(
   $CMD, "$java -cp '$classpath' -Djava.awt.headless=true -Dwrite.mrcxt.file.statistics=true -Xms200M -Xmx800M -Dbuild.sibs=true -Dbuild.children=true -Dcompute.xc=true -Dversioned.sabs=false -Dadd.unicode.bom=false -Dmax.contexts=false -Dsource.dir=$baseDir/Full/RRF/META gov.nih.nlm.mms.cxt.BatchMRCXTBuilder |"
  ) || die "Could not open java command ($? $!)\n";
  while (<$CMD>) {
   print "      $_";
  }
  close($CMD);
 }
}

#
# Normalize Metadata files
#
sub buildComparableMetadataORF {
 my ( $inDir, $outDir ) = @_;
 $inDir  = "$baseDir/$inDir";
 $outDir = "$baseDir/$outDir";
 system "/bin/cp -f $inDir/MRSAB $outDir";
 if ( $? != 0 ) {
  die "Could not copy MRSAB ($! $?)\n";
 }
 system "/bin/cp -f $inDir/MRRANK $outDir";
 if ( $? != 0 ) {
  die "Could not copy MRRANK ($! $?)\n";
 }
 system "/bin/cp -f MRDOC $outDir";
 if ( $? != 0 ) {
  system "/bin/cp -f $inDir/../../RRF/META/MRDOC.RRF $outDir/MRDOC";
  if ( $? != 0 ) {
   die "Could not copy MRDOC ($! $?)\n";
  }
 }
 system "/bin/cp -f $inDir/MRFILES $outDir";
 if ( $? != 0 ) {
  die "Could not copy MRFILES ($! $?)\n";
 }
 system "/bin/cp -f $inDir/MRCOLS $outDir";
 if ( $? != 0 ) {
  die "Could not copy MRCOLS ($! $?)\n";
 }
}

#
# Normalize content files
#
sub buildComparableContentORF {
 my ( $inDir, $outDir ) = @_;
 $inDir  = "$baseDir/$inDir";
 $outDir = "$baseDir/$outDir";
 my $IN;
 my %uis = ();

 #
 # Get STR->SUI map
 #
 print "      Load SUI,LUI map\n";
 open( $IN, "$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalSUILUILATSTR.txt" )
   || die
"Could not open $ENV{UMLS_ARCHIVE_ROOT}/global/GlobalSUILUILATSTR.txt ($! $?)\n";
 while (<$IN>) {
  my ( $sui, $lui, $latstr ) = split /\|/;
  my ( $lat, @str ) = split /\@/, $latstr;
  my $str = join "@", @str;

  # Add trailing @, if necessary
  if ( $latstr =~ /\@$/ ) {
   $str .= "\@";
  }
  $uis{"$lat|$str"} = "$sui|$lui";
 }
 close($IN);

 #
 # MRCON: fix LUI/SUI
 #
 print "      Fix MRCON ...", scalar(localtime), "\n";
 my $OUT;
 open( $IN, "$inDir/MRCON" )
   || die "Could not open $inDir/MRCON ($! $?)\n";
 open( $OUT, ">$outDir/MRCON" )
   || die "Could not open $outDir/MRCON ($! $?)\n";
 while (<$IN>) {
  my ( $cui, $lat, $ts, $lui, $stt, $sui, $str, $lrl ) = split /\|/;
  my ( $nsui, $nlui ) = split /\|/, $uis{"$lat|$str"};
  $newuis{"$cui|$sui"} = "$nsui|$nlui";
  print $OUT "$cui|$lat|$ts|$nlui|$stt|$nsui|$str|$lrl|\n";
 }
 close($IN);
 close($OUT);

 #
 # MRSO: fix LUI/SUI
 #
 print "      Fix MRSO ...", scalar(localtime), "\n";
 open( $IN, "$inDir/MRSO" )
   || die "Could not open $inDir/MRSO ($! $?)\n";
 open( $OUT, ">$outDir/MRSO" )
   || die "Could not open $outDir/MRSO ($! $?)\n";
 while (<$IN>) {
  my ( $cui, $lui, $sui, $sab, $tty, $code, $srl ) = split /\|/;
  my ( $nsui, $nlui ) = split /\|/, $newuis{"$cui|$sui"};
  ## Only older releases have "LUI" attributes.
  if ( $release le "2004" ) {
   $prefsui{"$cui|$lui"} = $sui if $prefsui{"$cui|$lui"} eq '';
  }
  print $OUT "$cui|$nlui|$nsui|$sab|$tty|$code|$srl|\n";
 }
 close($IN);
 close($OUT);

 #
 # Re-rank MRCON, MRSO
 #
 print "      Re-rank MRCON ...", scalar(localtime), "\n";
 system "$ENV{ARCHIVE_HOME}/bin/ranker.pl $outDir";
 if ( $? != 0 ) {
  die "Ranker failed: $! $?\n";
 }
 unlink "$outDir/MRCON";
 rename "$outDir/MRCON.out", "$outDir/MRCON"
   || die "Failed to rename MRCON.out to MRCON: $! $?\n";
 unlink "$outDir/MRSO";
 rename "$outDir/MRSO.out", "$outDir/MRSO"
   || die "Failed to rename MRSO.out to MRSO: $! $?\n";

 #
 # MRATX: Fix VSAB
 #
 print "      Fix MRATX ...", scalar(localtime), "\n";
 open( $IN, "$inDir/MRATX" )
   || die "Could not open $inDir/MRATX ($! $?)\n";
 open( $OUT, ">$outDir/MRATX" )
   || die "Could not open $outDir/MRATX ($! $?)\n";
 while (<$IN>) {
  s/\r//;
  s/^([^|]*)\|([A-Z]*)[0-9_]*\|/$1|$2|/;
  print $OUT "$_";
 }
 close($IN);
 close($OUT);

 #
 # MRDEF: Truncate long values
 #
 print "      Fix MRDEF ... ", scalar(localtime), "\n";
 open( $IN, "$inDir/MRDEF" )
   || die "Could not open $inDir/MRDEF ($! $?)\n";
 open( $OUT, ">$outDir/MRDEF" )
   || die "Could not open $outDir/MRDEF ($! $?)\n";
 while (<$IN>) {
  my ( $cui, $sab, $def ) = split(/[|\n]/);
  if ( length($def) > 4000 ) {
   $def = substr( $def, 0, 3986 );
   $def =~ s/ [^ ]*$/[...truncated]/;
  }
  print $OUT "$cui|$sab|$def|\n";
 }
 close($IN);
 close($OUT);

 #
 # MRJOIN: create
 #
 print "      Make MRJOIN ... ", scalar(localtime), "\n";
 system "$ENV{ARCHIVE_HOME}/bin/makeMrjoin.csh $outDir";
 if ( $? != 0 ) {
  die "Failed to make MRJOIN ($! $?)\n";
 }

 #
 # MRREL: fix SL
 #
 print "      Fix MRREL ... ", scalar(localtime), "\n";
 open( $IN, "$inDir/MRREL" )
   || die "Could not open $inDir/MRREL ($! $?)\n";
 open( $OUT, ">$outDir/MRREL" )
   || die "Could not open $outDir/MRREL ($! $?)\n";
 while (<$IN>) {
  my ( $cui1, $rel, $cui2, $rela, $sab, $sl, $mg ) = split(/[|\n]/);
  print $OUT "$cui1|$rel|$cui2|$rela|$sab|$sab|$mg|\n";
 }
 close($IN);
 close($OUT);

 #
 # MRSAT: Fix SUI, LUI and truncate long ATV
 #
 print "      Fix MRSAT ... ", scalar(localtime), "\n";
 open( $IN, "$inDir/MRSAT" )
   || die "Could not open $inDir/MRSAT ($! $?)\n";
 open( $OUT, ">$outDir/MRSAT" )
   || die "Could not open $outDir/MRSAT ($! $?)\n";
 while (<$IN>) {
  my ( $cui, $lui, $sui, $code, $atn, $sab, $atv ) = split /\|/;
  if ( $sui ne '' ) {
   ( $sui, $lui ) = split /\|/, $newuis{"$cui|$sui"};
  } elsif ( $lui ne '' ) {
   $sui = $prefsui{"$cui|$lui"};
   ( $sui, $lui ) = split /\|/, $newuis{"$cui|$sui"};
  }
  if ( length($atv) > 4000 ) {
   $atv = substr( $atv, 0, 3986 );
   $atv =~ s/ [^ ]*$/[...truncated]/;
  }
  print $OUT "$cui|$lui|$sui|$code|$atn|$sab|$atv|\n";
 }
 close($IN);
 close($OUT);

 #
 # MRSTY
 #
 print "      Copy MRSTY ... ", scalar(localtime), "\n";
 system "/bin/cp -f $inDir/MRSTY $outDir";
 if ( $? != 0 ) {
  die "Could not copy MRSTY ($! $?)\n";
 }
}

#
# Make comparable MRCXT ORF from RRF MRCXT
#
sub buildComparableHistoryORF {
 my ( $inDir, $outDir ) = @_;
 print "      No Operation\n";
}

#
# Make comparable MRCXT ORF from RRF MRCXT
#
sub buildComparableMrcxtFromRRF {
 my ( $inDir, $outDir ) = @_;
 $inDir  = "$baseDir/$inDir";
 $outDir = "$baseDir/$outDir";

 #
 # MRCXT: Borrow from RRF, fix SUIs
 #
 print "      Make MRCXT (from MRCXT.RRF) ... ", scalar(localtime), "\n";
 my $prevLine = "";
 my $IN;
 my $OUT;
 open( $IN, "$inDir/MRCXT.RRF" )
   || die "Could not open $inDir/MRCXT.RRF ($! $?)\n";
 open( $OUT, ">$outDir/MRCXT" )
   || die "Could not open $outDir/MRCXT ($! $?)\n";
 while (<$IN>) {
  my (
       $cui, $sui,  $aui,  $sab, $code, $cxn, $cxl, $rnk,
       $cxs, $cui2, $aui2, $hcd, $rela, $xc,  $cvf
  ) = split /\|/;
  my $lui = "";
  ( $sui, $lui ) = split /\|/, $newuis{"$cui|$sui"};
  my $line = "$cui|$sui|$sab|$code|$cxn|$cxl|$rnk|$cxs|$cui2|$hcd|$rela|$xc|\n";
  if ( $line ne $prevLine ) { print $OUT $line; }
  $prevLine = $line;
 }
 close($IN);
 close($OUT);
}

#
# Fix up MRCXT SUI/LUI values
#
sub buildComparableMrcxtFromORF {
 my ( $inDir, $outDir ) = @_;
 $inDir  = "$baseDir/$inDir";
 $outDir = "$baseDir/$outDir";

 #
 # MRCXT: Borrow from RRF, fix SUIs
 #
 print "      Make MRCXT (from MRCXT) ... ", scalar(localtime), "\n";
 my $IN;
 my $OUT;
 open( $IN, "$inDir/MRCXT" )
   || die "Could not open $inDir/MRCXT ($! $?)\n";
 open( $OUT, ">$outDir/MRCXT" )
   || die "Could not open $outDir/MRCXT ($! $?)\n";
 while (<$IN>) {
  my ( $cui, $sui, $sab, $code, $cxn, $cxl, $rnk, $cxs, $cui2, $hcd, $rela,
       $xc ) = split /\|/;
  my $lui = "";
  ( $sui, $lui ) = split /\|/, $newuis{"$cui|$sui"};
  print $OUT "$cui|$sui|$sab|$code|$cxn|$cxl|$rnk|$cxs|$cui2|$hcd|$rela|$xc|\n";
 }
 close($IN);
 close($OUT);
}

#
#
# Make comparable ORF word index files
#
sub buildComparableIndexes {
 my ( $format, $outDir ) = @_;
 $outDir = "$baseDir/$outDir";
 $format = lc($format);
 system "$ENV{ARCHIVE_HOME}/bin/makeMrx.csh -$format $outDir";
 if ( $? != 0 ) {
  die "Failed to make MRX files ($! $?)\n";
 }
}

#
# Make comparable AMBIG files (run after MRCON/MRSO)
#
sub buildComparableAmbig {
 my ( $format, $outDir ) = @_;
 $outDir = "$baseDir/$outDir";
 $format = lc($format);
 system "$ENV{ARCHIVE_HOME}/bin/makeAmbig.csh -$format $outDir";
 if ( $? != 0 ) {
  die "Failed to make AMBIG files ($! $?)\n";
 }
}

#
# Make comparable MRCOLS/MRFILES
#
sub buildComparableColsFiles {
 my ( $format, $outDir ) = @_;
 $outDir = "$baseDir/$outDir";
 $format = lc($format);

 #
 # Remake
 #
 system "$ENV{ARCHIVE_HOME}/bin/makeMrcolsfiles.csh -$format $outDir";
 if ( $? != 0 ) {
  die "Failed to make MRCOLS/FILES files ($! $?)\n";
 }
}

#
# Clean up dist root dir
#
sub cleanupDistRootDir {
 system "/bin/rm -rf $distRootDir";
 if ( $? != 0 ) {
  die "Could not clean up $distRootDir\n";
 }
}

#
# Recursively go through the input location and deletes invalid links
# Returns void
#
sub validateLinks {
 my @badLinks = ();
 find sub {
  if (-l) {
   my $target = readlink("$_");
   unless ( -e $target ) { unshift @badLinks, $File::Find::name; }
  }
 }, "$baseDir";
 our $file = "";
 foreach $file ( sort @badLinks ) {
  print "      ERROR: Bad link $file\n";
 }
}

#
# Validates the dir structure
#
sub validateStructure {
 my $errorFlag = 0;
 my $dir;
 foreach $dir (@archiveDirStructure) {
  unless ( -e "$baseDir/$dir" ) {
   $errorFlag++;
   print "Missing expected dir: $baseDir/$dir\n";
  }
 }
 return $errorFlag;
}

#
# Updates GlobalReleaseBitPos.txt for the configured $release
#
sub updateGlobalReleaseBitPos {
 my $IN;
 my $OUT;
 open( $IN, "$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalReleaseBitPos.txt" )
   || die "Could not open GlobalReleaseBitPos.txt : $! $?\n";
 open( $OUT, ">$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalReleaseBitPos.txt.out" )
   || die "Could not open GlobalReleaseBitPos.txt.out : $! $?\n";
 my $i     = 0;
 my $found = 0;
 while (<$IN>) {
  my ( $gRelease, $bitpos ) = split /\|/;
  $i++;
  if ( $i != $bitpos ) {
   die "Error: bitpos $bitpos is out of order\n";
  }

  # Reuse existing entry
  if ( $release eq $gRelease ) {
   $found        = 1;
   $globalBitPos = $bitpos;
   print "      Reusing bitpos $bitpos for $release\n";
  }
  print $OUT "$_";
  $maxGlobalBitPos = $bitpos if $bitpos > $maxGlobalBitPos;
 }
 close($IN);
 if ( !$found ) {
  $i++;
  print "      Adding bitpos $i for $release\n";
  print $OUT "$release|$i|\n";
  $globalBitPos = $i;
 }
 close($OUT);

 #
 # Rename GlobalReleaseBitPos.txt.out to GlobalReleaseBitPos.txt
 #
 unlink "$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalReleaseBitPos.txt";
 rename
   "$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalReleaseBitPos.txt.out",
   "$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalReleaseBitPos.txt"
   || die "Could not rename GlobalReleaseBitPos.txt.out: $! $?\n";
}

#
# Updates GlobalMRSAB.RRF file for the configured $release
#
sub updateGlobalMRSAB {
 my $IN;
 my $OUT;
 open( $IN, "$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalMRSAB.RRF" )
   || die "Could not open GlobalMRSAB.RRF : $! $?\n";
 open( $OUT, ">$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalMRSAB.RRF.out" )
   || die "Could not open GlobalMRSAB.RRF.out : $! $?\n";
 my $found = 0;
 while (<$IN>) {
  my ( $gRelease, $d ) = split /\|/;

  # replace any existing entries
  if ( $release ne $gRelease ) {
   print $OUT "$_";
  }
 }
 close($IN);
 open( $IN, "$ENV{UMLS_ARCHIVE_ROOT}/$release/Full/RRF/META/MRSAB.RRF" )
   || die "Could not open Full RRF MRSAB.RRF for $release : $! $?\n";
 while (<$IN>) {
  my (
       $d1,  $d2,  $vsab, $rsab, $d5,  $d6,  $d7,  $d8,
       $d9,  $d10, $d11,  $d12,  $d13, $d14, $d15, $d16,
       $d17, $d18, $d19,  $d20,  $d21, $curver
  ) = split /\|/;
  if ( $curver eq "Y" ) {
   print "      Adding: $release|$vsab|$rsab|\n";
   print $OUT "$release|$vsab|$rsab|\n";
  }
 }
 close($IN);
 close($OUT);

 #
 # Rename GlobalMRSAB.RRF.out to GlobalMRSAB.RRF
 #
 unlink "$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalMRSAB.RRF";
 rename "$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalMRSAB.RRF.out",
   "$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalMRSAB.RRF"
   || die "Could not rename GlobalMRSAB.RRF.out: $! $?\n";
}

#
# Updates global index files (e.g. GlobalMRXW_ENG.RRF)
#
sub updateGlobalIndexes {
 print "      No Operation\n";

 # Currently a noop
}

#
# Updates global UI/STR files (e.g. GlobalSUI.RRF, GlobalLUISUI.RRF, etc)
#
sub updateGlobalUI {

 #
 # Add Global RRF UIs
 #  GlobalAUI.txt
 #  GlobalCUI.txt
 #  GlobalSUI.txt
 #  GlobalSUILUI.txt
 #  GlobalSUILUILATSTR.txt
 #  GlobalSTR.txt
 #  GlobalLSTR.txt
 #
 #
 if ( -e "$ENV{UMLS_ARCHIVE_ROOT}/$release/Full/RRF/META/MRCONSO.RRF" ) {
  my %auis          = ();
  my %cuis          = ();
  my %suis          = ();
  my %strs          = ();
  my %lstrs         = ();
  my %suiluis       = ();
  my %suiluilatstrs = ();
  my $type          = "";

  foreach $type (
              ( "AUI", "CUI", "SUI", "STR", "LSTR", "SUILUI", "SUILUILATSTR" ) )
  {

   #
   # ${type}s
   #
   my $ucType = uc($type);
   my $lcType = lc($type);
   print "      Cache MRCONSO.RRF ($type) ..." . scalar(localtime) . "\n";
   my $MRCONSO;
   open( $MRCONSO,
         "$ENV{UMLS_ARCHIVE_ROOT}/$release/Full/RRF/META/MRCONSO.RRF" )
     || die "Can't open MRCONSO.RRF: $! $?\n";
   while (<$MRCONSO>) {

# CUI,LAT,TS,LUI,STT,SUI,ISPREF,AUI,SAUI,SCUI,SDUI,SAB,TTY,CODE,STR,SRL,SUPPRESS,CVF|
    my (
         $cui, $lat, $d2,  $lui, $d4,  $sui, $d6,  $aui,
         $d8,  $d9,  $d10, $d11, $d12, $d13, $str, $d14
    ) = split /\|/;
    if ( $lcType eq "suilui" ) {
     $suiluis{"$sui|$lui"} = 1;
    } elsif ( $lcType eq "suiluilatstr" ) {
     $suiluilatstrs{"$sui|$lui|$lat\@$str"} = 1;
    } elsif ( $lcType eq "lstr" ) {
     $lstrs{ lc($str) } = 1;
    } else {
     ${"${lcType}s"}{ ${$lcType} } = 1;
    }
   }
   close($MRCONSO);
   print "      Add $ucType to Global${ucType}.txt ..."
     . scalar(localtime) . "\n";
   my $GLOBAL;
   my $OUT;
   open( $GLOBAL, "$ENV{UMLS_ARCHIVE_ROOT}/global/Global${ucType}.txt" )
     || die "Could not open Global${ucType}.txt: $! $?\n";
   open( $OUT, ">$ENV{UMLS_ARCHIVE_ROOT}/global/Global${ucType}.txt.out" )
     || die "Could not open Global${ucType}.txt.out: $! $?\n";
   while (<$GLOBAL>) {
    chomp;
    my @f = split /\|/;
    if ( $lcType eq "suilui" ) {
     if ( ${"${lcType}s"}{"$f[0]|$f[1]"} ) {
      delete ${"${lcType}s"}{"$f[0]|$f[1]"};
     }
     print $OUT "$_\n";
    } elsif ( $lcType eq "suiluilatstr" ) {
     if ( ${"${lcType}s"}{"$f[0]|$f[1]|$f[2]"} ) {
      delete ${"${lcType}s"}{"$f[0]|$f[1]|$f[2]"};
     }
     print $OUT "$_\n";
    } else {
     my @bitmask = split //, $f[1];
     if ( ${"${lcType}s"}{ $f[0] } ) {
      $bitmask[ $globalBitPos - 1 ] = 1;
      delete ${"${lcType}s"}{ $f[0] };
     } else {
      $bitmask[ $globalBitPos - 1 ] = 0;
     }
     $f[1] = join "", @bitmask;
     print $OUT join "|", @f;
     print $OUT "|\n";
    }
   }
   close($GLOBAL);
   my $ui;
   foreach $ui ( sort keys %{"${lcType}s"} ) {
    if ( $lcType eq "suilui" || $lcType eq "suiluilatstr" ) {
     print $OUT "$ui|\n";
    } else {
     print $OUT "$ui|";
     my $i;
     for ( $i = 0 ; $i < $globalBitPos - 1 ; $i++ ) {
      print $OUT "0";
     }
     print $OUT "1";
     for ( $i = $globalBitPos ; $i < $maxGlobalBitPos - 1 ; $i++ ) {
      print $OUT "0";
     }
     print $OUT "|\n";
    }
   }
   %{"${lcType}s"} = ();
   close($OUT);

   #
   # Rename Global${ucType}.txt.out
   #
   unlink "$ENV{UMLS_ARCHIVE_ROOT}/global/Global${ucType}.txt";
   rename "$ENV{UMLS_ARCHIVE_ROOT}/global/Global${ucType}.txt.out",
     "$ENV{UMLS_ARCHIVE_ROOT}/global/Global${ucType}.txt"
     || die "Could not rename Global${ucType}.txt.out: $! $?\n";
  }
 }
}

#
# See if $release dir has a SemGroups.txt file, use it
#
sub updateSemGroups {

 # Start by looking for an updated semantic groups file
 # Also build up the STY-group map
 my %styToSg   = ();
 my $semGroups = "";
 find sub { $semGroups = $File::Find::name if $_ =~ "SemGroups.txt" },
   "$ENV{UMLS_ARCHIVE_ROOT}/$release";
 my $IN;
 my $OUT;
 if ($semGroups) {
  print "      Sem Groups file found: $semGroups\n";
  unlink "$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroups.txt";
  open( $IN, "$semGroups" ) || die "Could not open $semGroups: $! $?\n";
  open( $OUT, ">$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroups.txt" )
    || die
    "Could not open $ENV{UMLS_ARCHIVE_ROOT}/global/SemGroups.txt: $! $?\n";
 } else {
  print "      Sem Groups file not found, using existing one\n";
  open( $IN, "$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroups.txt" )
    || die
    "Could not open $ENV{UMLS_ARCHIVE_ROOT}/global/SemGroups.txt: $! $?\n";
 }
 while (<$IN>) {
  my @f = split /\|/;

  # CHEM|Chemicals & Drugs|T126|Enzyme|
  $styToSg{ $f[3] } = $f[0];
  if ($semGroups) {
   print $OUT;
  }
 }
 close($IN);
 if ($semGroups) {
  close($OUT);
 }
 print "      Done mapping STY to SG\n";

 #
 # Count STYs by group
 #
 my %styCt = ();
 open( $IN, "$ENV{UMLS_ARCHIVE_ROOT}/$release/Full/RRF/META/MRSTY.RRF" )
   || die
"Could not open $ENV{UMLS_ARCHIVE_ROOT}/$release/Full/RRF/META/MRSTY.RRF: $! $?\n";
 while (<$IN>) {
  my ( $cui, $tui, $stn, $sty, $atui, $cvf ) = split /\|/;
  $styCt{ $styToSg{$sty} }++;
 }
 close($IN);
 print "      Done counting by SG\n";

 #
 # Update SemGroupCountsByRelease.txt
 #
 open( $IN, "$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroupCountsByRelease.txt" )
   || die "Could not open SemGroupCountsByRelease.txt : $! $?\n";
 open( $OUT, ">$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroupCountsByRelease.txt.out" )
   || die "Could not open SemGroupCountsByRelease.txt.out : $! $?\n";
 my $found = 0;
 while (<$IN>) {
  my ( $gRelease, $sg, $ct ) = split /\|/;

  # replace any existing entries
  if ( $release ne $gRelease ) {
   print $OUT "$_";
  }
 }
 close($IN);
 my $sg;
 foreach $sg ( keys %styCt ) {
  print "      Adding: $release|$sg|$styCt{$sg}|\n";
  print $OUT "$release|$sg|$styCt{$sg}|\n";
 }
 close($OUT);

 #
 # Rename SemGroupCountsByRelease.txt
 #
 unlink "$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroupCountsByRelease.txt";
 rename "$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroupCountsByRelease.txt.out",
   "$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroupCountsByRelease.txt"
   || die "Could not rename SemGroupCountsByRelease.txt.out: $! $?\n";
}

#
# Computes and updates the release grouping data
#
sub updateRRFStatistics {
 my $IN;
 my $OUT;
 open( $IN, "$ENV{UMLS_ARCHIVE_ROOT}/global/GroupingData_RRF.txt" )
   || die "Could not open GroupingData_RRF.txt : $! $?\n";
 open( $OUT, ">$ENV{UMLS_ARCHIVE_ROOT}/global/GroupingData_RRF.txt.out" )
   || die "Could not open GroupingData_RRF.txt.out : $! $?\n";
 while (<$IN>) {
  my ( $gRelease, $d ) = split /\|/;

  # Skip matching release entries
  if ( $release ne $gRelease ) {
   print $OUT "$_";
  }
 }
 close($IN);
 my $target;
 foreach $target (
                   (
                     "AMBIG", "MRAUI", "MRCONSO", "MRCUI", "MRDEF",
                     "MRDOC", "MRHIER", "MRHIST", "MRMAP",   "MRREL", "MRSAB",
                     "MRSAT", "MRSTY",  "MRX"
                   )
   )
 {
  my $lcTarget = lc($target);
  print "      Computing qa_${lcTarget}_$release ..."
    . scalar(localtime) . "\n";

  # Produces a "qa_target_RELEASE" file
  if ( !-e "$ENV{UMLS_ARCHIVE_ROOT}/$release/Prod/qa/qa_${lcTarget}_$release" )
  {
   print
"        $ENV{UMLS_ARCHIVE_ROOT}/$release/Prod/qa/qa_{$lcTarget}_$release does not exist, computing it\n";
   system
"$ENV{MRD_HOME}/bin/qa_counts.csh $ENV{UMLS_ARCHIVE_ROOT}/$release/Full/RRF/META mrd-db $release $target";
   rename "qa_${lcTarget}_$release",
     "$ENV{UMLS_ARCHIVE_ROOT}/$release/Prod/qa/qa_${lcTarget}_$release"
     || die "Unable to rename qa_${lcTarget}_$release: $! $?\n";
  }
  open( $IN,
        "$ENV{UMLS_ARCHIVE_ROOT}/$release/Prod/qa/qa_${lcTarget}_$release" )
    || die
"Could not open $ENV{UMLS_ARCHIVE_ROOT}/$release/Prod/qa/qa_${lcTarget}_$release: $? $!\n";
  while (<$IN>) {
   chop;
   my ( $name, $val, $ct ) = split /~/;
   print $OUT "$release|$target|$name|$val|$ct|\n";
  }
  close($IN);
 }
 close($OUT);

 #
 # Rename GroupingData_RRF.txt
 #
 unlink "$ENV{UMLS_ARCHIVE_ROOT}/global/GroupingData_RRF.txt";
 rename "$ENV{UMLS_ARCHIVE_ROOT}/global/GroupingData_RRF.txt.out",
   "$ENV{UMLS_ARCHIVE_ROOT}/global/GroupingData_RRF.txt"
   || die "Could not rename GroupingData_RRF.txt.out: $! $?\n";
}

#
# Computes and updates the release grouping data
#
sub updateORFStatistics {

 # TODO: Call ORF counting algorihtm
 print "      No Operation\n";
}

#
# Returns the global bitpos for the specified release
#
sub getGlobalBitPos {
 my $BITPOS;
 open( $BITPOS, "$ENV{UMLS_ARCHIVE_ROOT}/global/GlobalReleaseBitPos.txt" )
   || die "Could not open GlobalReleaseBitPos.txt: $! $?\n";
 my $bitpos = 0;
 while (<$BITPOS>) {
  chomp;
  my ( $rel, $bp ) = split /\|/;
  if ( $rel eq $release ) {
   $bitpos = $bp;
   last;
  }
 }
 close($BITPOS);
 if ( !$bitpos ) {
  print "ERROR: Global bit position is not defined for $release\n";
 }
 return $bitpos;
}

#
# Verify RRF UIs
#  GlobalAUI.txt
#  GlobalCUI.txt
#  GlobalSUI.txt
#  GlobalSUILUI.txt
#  GlobalSUILUILATSTR.txt
#  GlobalSTR.txt
#  GlobalLSTR.txt
#
#
sub verifyRrfUis {
 my ($inDir) = @_;
 my $bitpos = getGlobalBitPos();
 if ( -e "$baseDir/$inDir/MRCONSO.RRF" ) {
  print "    Validate full MRCONSO.RRF ..." . scalar(localtime) . "\n";
  my $MRCONSO;
  open( $MRCONSO, "$baseDir/$inDir/MRCONSO.RRF" )
    || die "Can't open MRCONSO.RRF: $! $?\n";
  my %auis          = ();
  my %cuis          = ();
  my %suis          = ();
  my %strs          = ();
  my %lstrs         = ();
  my %suiluis       = ();
  my %suiluilatstrs = ();

  while (<$MRCONSO>) {

# CUI,LAT,TS,LUI,STT,SUI,ISPREF,AUI,SAUI,SCUI,SDUI,SAB,TTY,CODE,STR,SRL,SUPPRESS,CVF|
   my (
        $cui, $lat, $d1, $lui, $d2, $sui, $d, $aui,
        $d3,  $d4,  $d5, $d6,  $d7, $d8,  $str
   ) = split /\|/;
   $auis{$aui}                          = 1;
   $cuis{$cui}                          = 1;
   $suis{$sui}                          = 1;
   $strs{$str}                          = 1;
   $lstrs{ lc($str) }                   = 1;
   $suiluis{"$sui$lui"}                 = 1;
   $suiluilatstrs{"$sui$lui$lat\@$str"} = 1;
  }
  close($MRCONSO);
  my $type = "";
  foreach $type (
              ( "AUI", "CUI", "SUI", "STR", "LSTR", "SUILUI", "SUILUILATSTR" ) )
  {

   #
   # ${type}s
   #
   my $ucType = uc($type);
   my $lcType = lc($type);
   print "      $ucType to Global${ucType}.txt ..." . scalar(localtime) . "\n";
   my $GLOBAL;
   open( $GLOBAL, "$ENV{UMLS_ARCHIVE_ROOT}/global/Global${ucType}.txt" )
     || die "Could not open Global${ucType}.txt: $! $?\n";
   while (<$GLOBAL>) {
    chomp;
    my @f = split /\|/;
    if ( $lcType eq "suilui" ) {
     if ( ${"${lcType}s"}{"$f[0]$f[1]"} ) {
      delete ${"${lcType}s"}{"$f[0]$f[1]"};
     }
    } elsif ( $lcType eq "suiluilatstr" ) {
     if ( ${"${lcType}s"}{"$f[0]$f[1]$f[2]"} ) {
      delete ${"${lcType}s"}{"$f[0]$f[1]$f[2]"};
     }
    } else {
     my @bitmask = split //, $f[1];
     if ( ${"${lcType}s"}{ $f[0] } && $bitmask[ $bitpos - 1 ] ) {
      delete ${"${lcType}s"}{ $f[0] };
     }
    }
   }
   close($GLOBAL);
   my $ui;
   foreach $ui ( sort keys %{"${lcType}s"} ) {
    print
"      ERROR: $ui in MRCONSO.RRF but not in Global${ucType}.txt with bit $bitpos set\n";
   }
   %{"${lcType}s"} = ();
  }
 }
}

#
# Verify RRF SemGroupCountsByRelease.txt
#
# Check that $release is represented for each SemGroup in SemGroups.txt
#
sub verifyRrfStyGroups {
 my ($inDir) = @_;
 my $bitpos = getGlobalBitPos();
 if ( -e "$baseDir/$inDir/MRSTY.RRF" ) {
  print "    Validate full MRSTY.RRF ..." . scalar(localtime) . "\n";
  my $SEMGROUPS;
  open( $SEMGROUPS, "$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroups.txt" )
    || die "Could not open SemGroups.txt: $! $?\n";
  my %semGroupMap = ();
  while (<$SEMGROUPS>) {
   my ( $group, $d1, $d2, $sty ) = split /\|/;
   $semGroupMap{$sty} = $group;
  }
  close($SEMGROUPS);
  my $MRSTY;
  open( $MRSTY, "$baseDir/$inDir/MRSTY.RRF" )
    || die "Can't open MRSTY.RRF: $! $?\n";
  my %semGroups = ();
  while (<$MRSTY>) {

   # CUI,TUI,STN,STY,ATUI,CVF
   my ( $cui, $d1, $d2, $sty, $d3, $d4 ) = split /\|/;
   $semGroups{ $semGroupMap{$sty} }++;
  }
  close($MRSTY);
  open( $SEMGROUPS,
        "$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroupCountsByRelease.txt" )
    || die "Could not open SemGroupCountsByRelease.txt: $! $?\n";
  while (<$SEMGROUPS>) {
   my ( $rel, $grp, $ct ) = split /\|/;
   if ( $rel eq $release ) {
    if ( $semGroups{$grp} != $ct ) {
     print "      ERROR: $grp count does not match ($semGroups{$grp}, $ct)\n";
     delete $semGroups{$grp};
    } elsif ( !$semGroups{$grp} ) {
     print "      ERROR: $grp count does not match (0, $ct)\n";
    } elsif ( $semGroups{$grp} == $ct ) {
     delete $semGroups{$grp};
    }
   }
  }
  close($SEMGROUPS);
  my $key;
  foreach $key ( keys %semGroups ) {
   print "      ERROR: $key does not appear in global file\n";
  }
 }
}

#
# Verify GroupingData_RRF.txt entries exist
#
# TODO: check Comparable/RRF/META/ entries in GroupingData_RRF.txt
#
sub verifyRrfGroupingData {
}

#
# Verify ORF UIs
#  GlobalCUI.txt
#  GlobalSUI.txt
#  GlobalSUILUI.txt
#  GlobalSUILUILATSTR.txt
#  GlobalSTR.txt
#  GlobalLSTR.txt
#
sub verifyOrfUis {
 my ($inDir) = @_;
 my $bitpos = getGlobalBitPos();
 if ( -e "$baseDir/$inDir/MRCON" ) {
  print "    Validate full MRCON ..." . scalar(localtime) . "\n";
  my $MRCON;
  open( $MRCON, "$baseDir/$inDir/MRCON" )
    || die "Can't open MRCON: $! $?\n";
  my %cuis          = ();
  my %suis          = ();
  my %strs          = ();
  my %lstrs         = ();
  my %suiluis       = ();
  my %suiluilatstrs = ();

  while (<$MRCON>) {

   # CUI,LAT,TS,LUI,STT,SUI,STR,LRL
   my ( $cui, $lat, $d1, $lui, $d2, $sui, $str, $d3 ) = split /\|/;
   $cuis{$cui}                          = 1;
   $suis{$sui}                          = 1;
   $strs{$str}                          = 1;
   $lstrs{ lc($str) }                   = 1;
   $suiluis{"$sui$lui"}                 = 1;
   $suiluilatstrs{"$sui$lui$lat\@$str"} = 1;
  }
  close($MRCON);
  my $type = "";
  foreach $type ( ( "CUI", "SUI", "STR", "LSTR", "SUILUI", "SUILUILATSTR" ) ) {

   #
   # ${type}s
   #
   my $ucType = uc($type);
   my $lcType = lc($type);
   print "      $ucType to Global${ucType}.txt ..." . scalar(localtime) . "\n";
   my $GLOBAL;
   open( $GLOBAL, "$ENV{UMLS_ARCHIVE_ROOT}/global/Global${ucType}.txt" )
     || die "Could not open Global${ucType}.txt: $! $?\n";
   while (<$GLOBAL>) {
    chomp;
    my @f = split /\|/;
    if ( $lcType eq "suilui" ) {
     if ( ${"${lcType}s"}{"$f[0]$f[1]"} ) {
      delete ${"${lcType}s"}{"$f[0]$f[1]"};
     }
    } elsif ( $lcType eq "suiluilatstr" ) {
     if ( ${"${lcType}s"}{"$f[0]$f[1]$f[2]"} ) {
      delete ${"${lcType}s"}{"$f[0]$f[1]$f[2]"};
     }
    } else {
     my @bitmask = split //, $f[1];
     if ( ${"${lcType}s"}{ $f[0] } && $bitmask[ $bitpos - 1 ] ) {
      delete ${"${lcType}s"}{ $f[0] };
     }
    }
   }
   close($GLOBAL);
   my $ui;
   foreach $ui ( sort keys %{"${lcType}s"} ) {
    print
"      ERROR: $ui in MRCONSO.RRF but not in Global${ucType}.txt with bit $bitpos set\n";
   }
   %{"${lcType}s"} = ();
  }
 }
}

#
# Verify RRF SemGroupCountsByRelease.txt
#
# Check that $release is represented for each SemGroup in SemGroups.txt
#
sub verifyOrfStyGroups {
 my ($inDir) = @_;
 if ( -e "$baseDir/$inDir/MRSTY" ) {
  print "    Validate full MRSTY ..." . scalar(localtime) . "\n";
  my $SEMGROUPS;
  open( $SEMGROUPS, "$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroups.txt" )
    || die "Could not open SemGroups.txt: $! $?\n";
  my %semGroupMap = ();
  while (<$SEMGROUPS>) {
   my ( $group, $d1, $d2, $sty ) = split /\|/;
   $semGroupMap{$sty} = $group;
  }
  close($SEMGROUPS);
  my $MRSTY;
  open( $MRSTY, "$baseDir/$inDir/MRSTY" )
    || die "Can't open MRSTY: $! $?\n";
  my %semGroups = ();
  while (<$MRSTY>) {

   # CUI,TUI,STN,STY,ATUI,CVF
   my ( $cui, $d, $sty ) = split /\|/;
   $semGroups{ $semGroupMap{$sty} }++;
  }
  close($MRSTY);
  open( $SEMGROUPS,
        "$ENV{UMLS_ARCHIVE_ROOT}/global/SemGroupCountsByRelease.txt" )
    || die "Could not open SemGroupCountsByRelease.txt: $! $?\n";
  while (<$SEMGROUPS>) {
   my ( $rel, $grp, $ct ) = split /\|/;
   if ( $rel eq $release ) {
    if ( $semGroups{$grp} != $ct ) {
     print "      ERROR: $grp count does not match ($semGroups{$grp}, $ct)\n";
     delete $semGroups{$grp};
    } elsif ( !$semGroups{$grp} ) {
     print "      ERROR: $grp count does not match (0, $ct)\n";
    } elsif ( $semGroups{$grp} == $ct ) {
     delete $semGroups{$grp};
    }
   }
  }
  close($SEMGROUPS);
  my $key;
  foreach $key ( keys %semGroups ) {
   print "      ERROR: $key does not appear in global file\n";
  }
 }
}

#
# Verify GroupingData_ORF.txt entries exist
#
# TODO: check Comparable/ORF/META/ entries in GroupingData_ORF.txt
#
sub verifyOrfGroupingData {
 my ($inDir) = @_;
}

#
# Validate directory structure
#
sub validateDirStructure {
 my $dir;
 foreach $dir ( keys %validDirStructure ) {

  # Skip RRF checks for pre-RRF files
  if ( $dir =~ /RRF/ && $release le "2004AA" ) {
   next;
  }

  # Skipp Full checks for 1990,1991
  if ( $dir =~ /Full/ && $release le "1991AA" ) {
   next;
  }
  if ( !-e "$baseDir/$dir" ) {
   print "      $validDirStructure{$dir}: Missing dir $release/$dir\n";
  } elsif ( $dir ne "tmp" ) {
   if ( -l "$baseDir/$dir" ) {
    print "      ERROR: $baseDir/$dir is a link to another dir\n";
   }
   my @files = ();
   find sub { ( unshift @files, $File::Find::name ) unless $_ =~ /^\.{1,2}$/ },
     "$baseDir/$dir";
   if ( scalar(@files) == 0 ) {
    print
      "      $validDirStructure{$dir}: unexpected empty dir: $release/$dir\n";
   }
  }
 }

 #
 # Checking for Extra dirs
 #
 print "    Checking Extra Dirs ..." . scalar(localtime) . "\n";
 my @allDirs = ();
 find sub { unshift @allDirs, $File::Find::name if -d }, "$baseDir/$dir";
 foreach $dir ( sort @allDirs ) {
  $dir =~ s/$ENV{UMLS_ARCHIVE_ROOT}\/$release//;
  $dir =~ s/^\///;
  if ( $dir =~ /Original\/.*/ ) { next; }
  if ( $dir =~ /Misc\/.*/ )     { next; }
  if ( !$validDirStructure{$dir} ) {
   print "      ERROR: Unexpected EXTRA directory: $dir\n";
  }
 }

 #
 # Verify that META directories contain only
 # files in MRFILES
 #
 print "    Checking META Dir files (matching MRFILES) ..."
   . scalar(localtime) . "\n";
 our @metaDirs = ();
 find sub { unshift @metaDirs, $File::Find::name if -d $_ && /META$/ },
   "$ENV{UMLS_ARCHIVE_ROOT}/$release/Full";
 find sub { unshift @metaDirs, $File::Find::name if -d $_ && /META$/ },
   "$ENV{UMLS_ARCHIVE_ROOT}/$release/Comparable";
 foreach $dir ( sort @metaDirs ) {
  my $MRFILES;
  our %mrFiles = ();
  if ( -e "$dir/MRFILES" ) {
   open( $MRFILES, "$dir/MRFILES" )
     || die "Could not open $dir/MRFILES: $! $?\n";

   # Exception for MRJOIN
   $mrFiles{"MRJOIN"} = 1;
  } elsif ( -e "$dir/MRFILES.RRF" ) {
   open( $MRFILES, "$dir/MRFILES.RRF" )
     || die "Could not open $dir/MRFILES: $! $?\n";
  } else {
   print "      WARNING: No MRFILES found in $dir\n";
   next;
  }
  while (<$MRFILES>) {
   my ($file) = split /\|/;
   $mrFiles{$file} = 1;
  }
  close($MRFILES);
  my @dirFiles = ();
  my %mrFilesExceptions = (
                            "release.dat"       => 1,
                            "mrcxt_builder.log" => 1,
                            "mmsys.log"         => 1,
                            "config.prop"       => 1
  );
  find sub {
   unshift @dirFiles, $File::Find::name
     if ( !-d $File::Find::name && $File::Find::name !~ /indexes/ );
  }, $dir;
  my $file = "";
  foreach $file ( sort @dirFiles ) {
   $file =~ s/$dir\///;
   if ( !$mrFiles{$file} && !$mrFilesExceptions{$file} ) {
    print "      ERROR: $file in $dir not in MRFILES\n";
   }
  }
  foreach $file ( sort keys %mrFiles ) {
   if ( !-e "$dir/$file" && $file ne "MRJOIN" ) {
    print "      ERROR: $file in MRFILES not in $dir\n";
   }
  }
 }
}

#
# Verify RRF referential integrity
#
sub verifyRrfIntegrity {
 my ($inDir) = @_;
 if ( -e "$baseDir/$inDir" ) {
  print "    Verify Comparable RRF referential integrity ..."
    . scalar(localtime) . "\n";
  my $CMD;
  open( my $CMD, "$ENV{ARCHIVE_HOME}/bin/ref_integrity.csh $baseDir/$inDir |" )
    || die "Could not open ref_integrity.csh command: $! $?\n";
  while (<$CMD>) {
   print "      $_";
  }
  close($CMD);
 }
}

#
# Verify ORF referential integrity
#
sub verifyOrfIntegrity {
 my ($inDir) = @_;
 if ( -e "$baseDir/$inDir" ) {
  print "    Verify Comparable ORF referential integrity ..."
    . scalar(localtime) . "\n";
  my $CMD;
  open( my $CMD, "$ENV{ARCHIVE_HOME}/bin/orf_integrity.csh $baseDir/$inDir |" )
    || die "Could not open orf_integrity.csh command: $! $?\n";
  while (<$CMD>) {
   print "      $_";
  }
  close($CMD);
 }
}
return 1;
