# Interface to lvg "norm" services for the EMS
# suresh@nlm.nih.gov 4/98
# Updated for new norm 7/99
# Updated for EMS 3

# Uses LVGIF to compute normalized form
# 3/2005 suresh@nlm.nih.gov

package LVG;

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use GeneralUtils;

# computes the luiNorm form of a string
sub luinorm {
  my($class, $str) = @_;
  my($inputfile, $outputfile);
  my($cmd);

  $inputfile = join("/", $EMSNames::TMPDIR, "lvg_in.$$");
  $outputfile = join("/", $EMSNames::TMPDIR, "lvg_out.$$");

  GeneralUtils->str2file($str, $inputfile);
  die "ERROR: LVGIF_HOME not set\n" unless $main::EMSCONFIG{LVGIF_HOME};
  $cmd = $main::EMSCONFIG{LVGIF_HOME} . "/bin/cluinorm.pl < $inputfile > $outputfile";
  system $cmd;
  $x = GeneralUtils->file2str($outputfile);
  chomp($x);

  unlink $inputfile;
  unlink $outputfile;

  return "" unless $x;
  @_ = split /\|/, $x, 2;
  return $_[1];
}

# computes the norm forms of a string
# returns list of norm forms
sub norm {
  my($class, $str) = @_;
  my($inputfile, $outputfile);
  my($cmd);
  my(@norm);

  $inputfile = join("/", $EMSNames::TMPDIR, "lvg_in.$$");
  $outputfile = join("/", $EMSNames::TMPDIR, "lvg_out.$$");
  die "ERROR: LVGIF_HOME not set\n" unless $main::EMSCONFIG{LVGIF_HOME};
  $cmd = $main::EMSCONFIG{LVGIF_HOME} . "/bin/cnorm.pl < $inputfile > $outputfile";

  GeneralUtils->str2file($str, $inputfile);

  system $cmd;

  $x = GeneralUtils->file2str($outputfile);
  chomp($x);

  unlink $inputfile;
  unlink $outputfile;

  return () unless $x;

  @_ = split /\|/, $x;
  shift @_;
  return @_;
}

# a string tokenizer (returns tokens in a list)
sub wordind {
  my($class, $str) = @_;
  my($inputfile, $outputfile);
  my($cmd);
  my(@tokens);

  $inputfile = join("/", $EMSNames::TMPDIR, "lvg_in.$$");
  $outputfile = join("/", $EMSNames::TMPDIR, "lvg_out.$$");

  GeneralUtils->str2file($str, $inputfile);
  die "ERROR: LVGIF_HOME not set\n" unless $main::EMSCONFIG{LVGIF_HOME};
  $cmd = $main::EMSCONFIG{LVGIF_HOME} . "/bin/cwordind.pl < $inputfile > $outputfile";
  system $cmd;

  my($x) = GeneralUtils->file2str($outputfile);
  chomp($x);

  unlink $inputfile;
  unlink $outputfile;

  return () unless $x;
  @_ = split /\|/, $x;
  shift @_;
  return @_;
}

#----------------------------------------------
1;
