#!/usr/bin/perl

unshift(@INC, ".");
use lib "$ENV{INV_HOME}/bin";
use lib "$ENV{INV_HOME}/lib";

use strict 'vars';
use strict 'subs';
use Getopt::Std;

our %options = ();
getopts("c", \%options);


use Atom;
use Attribute;
use Relation;
use Merge;
use Context;
use SrcBldr;
use NLMInv;

our ($Inv, $Log, $Cfg);
our ($styAttr);
our ($cfgFile);

if (!defined $options{c}) {
  print "Must supply a valid config file as -c option.\n";
  exit;
}
$cfgFile = $options{c};


#-----------------------------------------------------------------------------
# Main
#-----------------------------------------------------------------------------
&main;
sub main {
  # create a new NLMInv obj and get common objects.
  $Inv  = new NLMInv($cfgFile);
  $Log  = $Inv->getLog;
  $Cfg  = $Inv->getCfg;

  $Inv->prTime("Begin");
  $Inv->invBegin('Append');

  $styAttr = new Attribute('Attribute.STY');
  $Inv->inheritSTYs(\$styAttr, 0);   # uses File.TINS.StyTermIds

  $Inv->invEnd;
  $Inv->prTime("Done Adding STY Attributes");
}