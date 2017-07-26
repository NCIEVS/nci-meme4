#!@PATH_TO_PERL@

unshift(@INC, ".");
unshift(@INC, "umls_dev/NLM/inv/bin3");
use lib "/umls_dev/NLM/inv/bin3";

use XML::Parser::PerlSAX;
use Icd9Proc;
use strict 'vars';
use strict 'subs';

our $ofERRS  = "errors_icd9.txt";
our $cfgFile = "../etc/icd9.cfg";
sub runMain {
  open (ERRS, ">:utf8",$ofERRS) or die "Couldn't open $ofERRS file.\n";

  my $myInv = new NLMInv($cfgFile, *ERRS);
  $myInv->prTime("Begin");
  &checkNeededConfigInfo (\$myInv);
  $myInv->invBegin;

  my $cntHandler = new  Icd9Proc(\$myInv);
  my $parser = XML::Parser::PerlSAX->new(Handler => $cntHandler);

  my $inFile = $myInv->getReqEle('File.icd9IN');
  my %parser_args = (Source => {SystemId => $inFile});
  $parser->parse(%parser_args);

  print ERRS "Error parsing file: $@" if $@;

  $myInv->prTime("End");
  $myInv->invEnd;
  close(ERRS);
}

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
  $$inv->getReqEle('HC.RootSaid');
  $$inv->getReqEle('HC.RootNodeNameDefault');
}
&runMain;

