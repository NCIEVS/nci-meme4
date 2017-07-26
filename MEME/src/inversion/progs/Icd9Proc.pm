#!@PATH_TO_PERL@

package Icd9Proc;

unshift(@INC, ".");
use lib "/umls_dev/NLM/inv/bin";
use strict 'vars';
use strict 'subs';

use base qw(XML::SAX::Base);
use NLMInv;
use Atom;
use Attribute;
use Relation;
use Merge;
use Context;
#use SrcBldr;
use IdGen;


use constant DEBUG => 1;

our $glERR;
our $glInvRef;

our $mainGenId;


# variables to save info regarding the current concept.
our $name = '';
our $code = '';
our @parents = ();
our %props = ();
our $propName = '';
our $propVal = '';


our $inConDef = 0;
our $inProps = 0;
our $inProp = 0;
our $inDefCon = 0;
our $textCollected = '';

# to store all concepts.

our %cd2Name = ();            # cd2Name
our %cd2RealName = ();          # cd2RealName
our %name2Cd =();             # name2Cd
our %cd2Props = ();           # cd2Props
our %cd2Parents = ();         # cd2Parents
our %cd2Said=();
our %said2Cd=();
# code to AB atom's said. AB and PT has same code, but different said
our %cd2ABSaid=();

our ($phAtom, $etAtom);
our ($styAttr, $sAttr, $cxtAttr);
our ($abMerge);
our ($etnsRel, $abRel);
our ($pCxt, $sCxt);

our $rootNode;

# args: errorhander, NLMInv object
sub new {
  my $class = shift;
  $glInvRef = shift;

  $glERR = $$glInvRef->getEle('ofhERR');
  my $ref = { };
  return bless($ref, $class);
}

sub start_document {  &init; }

sub end_document {  &process; }

sub start_element{
  my $self = shift;
  my $el = shift;
  my $elName = $el->{"Name"};
  # reset text collect so far.
  $textCollected = '';

  if ($elName eq "conceptDef") {
    $inConDef = 1;
    $name = '';
    $code = '';
  } elsif ($elName eq "definingConcepts") {
    $inDefCon = 1;
    @parents = ();
  } elsif ($elName eq "properties") {
    $inProps = 1;
    %props = ();
  } elsif ($elName eq "property") {
    $inProp = 1;
    $propName = '';
    $propVal = '';
  }
}


sub end_element{
  my $self = shift;
  my $el = shift;
  my $elName = $el->{Name};
  if ($elName eq "conceptDef") {
    # save everything into hashtables.
    $cd2Name{"$code"} = $name;
    $name2Cd{"$name"} = $code;
    @{$cd2Parents{"$code"}} = @parents;
    %{$cd2Props{"$code"}} = %props;
    $inConDef = 0;
  } elsif ($elName eq "name") {
    if ($inConDef != 1) {
      print ERR "<name> encountered in the wrong place.\n";
    } else {
      if ($inProp == 1) {
	$propName = &getText;
      } else {
	$name = &getText;
      }
    }
  } elsif ($elName eq "code") {
    $code = $textCollected;
  } elsif ($elName eq "definingConcepts") {
    $inDefCon = 0;
  } elsif ($elName eq "concept") {
    # save in parents.
    push (@parents, &getText());
  } elsif ($elName eq "properties") {
    $inProps = 0;
  } elsif ($elName eq "property") {
    push (@{$props{"$propName"}}, $propVal);
    $inProp = 0;
  } elsif ($elName eq "value") {
    # collect value and save name/value property.
    $propVal = &getText;
  }
}


sub characters {
    my $self = shift;
    my $text = shift;
    $textCollected .= $text->{Data};
}

# not callable from outside.
sub trim {
 my($str) = @_;

  $str =~ s/\s+/ /g;            # more than 1 blanks to 1 blank
  $str =~ s/^\s+//;             # strip leading blanks
  $str =~ s/\s+$//;             # strip trailing blanks
  $str =~ s/^\s+$//;            # strip lines with blanks
  return $str;
}

sub getText {  return &trim($textCollected); }


# =================== actual processing.
sub init {
  # dump source meta data.
  $$glInvRef->processSrc;

  # create a main generator with proper seeds.
  $mainGenId = new IdGen();

  # create templates.
  # ATOM templates.
  $phAtom = new Atom;
  $etAtom = new Atom('Atom.ET');

  # ATTRIBUTE templates.
  $styAttr = new Attribute('Attribute.SN');
  $sAttr = new Attribute('Attribute.S');
  $cxtAttr = new Attribute('Attribute.CXT');

  # MERGE templates
  $abMerge = new Merge('Merge.ICD9');

  # RELATION templates
  # extensions templates
  $etnsRel = new Relation('Relation.ICD9');
  # abbreviations templates
  $abRel = new Relation('Relation.AB');

  # CONTEXT templates
  $pCxt = new Context('Context.ICD9');
  $sCxt = new Context('Context.SIB');
}

sub process {

  # After xml parse. Now process the information.

  ## form parent child relationships
  my ($cd, $parName, $parCd);
  foreach $cd (keys (%cd2Name)) {
    foreach $parName (@{$cd2Parents{"$cd"}}) {
      # get par code.
      $parCd = $name2Cd{"$parName"};
      $$glInvRef->addParChild($parCd, $cd);
    }
  }
  # for QA check, dump par chld rels into files in etc dir
  $$glInvRef->dumpParChild;

  ## form atom ordering.
  $$glInvRef->prepareAtomOrdering;

  ## dump all the data(atoms, attributes, merges, and relationships
  &dumpIcdData;

  # dump contexts.
  &dumpCxts;
}

sub dumpIcdData {


  my ($cd, $props, $name, $val, $atOrd, $ptId, $htId, $abId, $thisId);
  my(@dupAttrs);

  # remember the root atomid here - this is fixed to be 1500165.
  $rootNode = $$glInvRef->getReqEle('HC.RootNode');
  $cd2Said{$rootNode} = $$glInvRef->getReqEle('HC.RootSaid');
  $said2Cd{$$glInvRef->getReqEle('HC.RootSaid')} = $rootNode;

  foreach $cd (sort keys (%cd2Name)) {
    #next if ($cd eq 'V-ICD9CM');
    if ($cd eq 'V-ICD9CM') {
      if (defined ($$props{'Hierarchical Name'})) {
	($val) = @{$$props{'Hierarchical Name'}};
	$cd2RealName{'V-ICD9CM'} = $val;
	print "Root name is set to <$val>\n";
      } else {
	# use a default name.
	my $temp = $$glInvRef->getReqEle('HC.RootNodeNameDefault');
	$cd2RealName{'V-ICD9CM'} = $temp;
	print "Root name is set to <$temp>\n";
      }
      next;
    }

    $atOrd = $$glInvRef->getAtomOrd("$cd");
    $ptId = $htId = 0;

    $props = $cd2Props{"$cd"};
    if (defined ($$props{'Preferred Name'})) {
      # create a PF atom.
      ($val) = @{$$props{'Preferred Name'}};
      $cd2RealName{"$cd"} = $val;
      $phAtom->dumpAtom({name => $val, tty => 'PT', code => $cd,
			 sdui => $cd, ordid => $atOrd});
      $atOrd++;
      $ptId = $phAtom->getLastId();
      $cd2Said{"$cd"} = $ptId;
      $said2Cd{"$ptId"} = $cd;

      # QA check.
      if (defined ($$props{'Hierarchical Name'})) {
	print $glERR "code <$cd> has both PF and HT. Skipping HT.\n";
      }
    } elsif (defined ($$props{'Hierarchical Name'})) {
      # create a HT atom.
      ($val) = @{$$props{'Hierarchical Name'}};
      $cd2RealName{"$cd"} = $val;
      $phAtom->dumpAtom({name => $val, tty => 'HT', code => $cd,
			 sdui => $cd, ordid => $atOrd});
      $atOrd++;
      $htId = $phAtom->getLastId();
      $cd2Said{"$cd"} = $htId;
      $said2Cd{"$htId"} = $cd;
    } else {
      print $glERR "code <$cd> has neither PT not HT. !!! \n";
    }


    # now dump other ones.
    # dump entry terms.
    if (defined ($$props{'Entry Term'})) {
      # can we have more than 1 here??
      foreach $val (@{$$props{'Entry Term'}}) {
	$etAtom->dumpAtom({name => $val, code => $cd, sdui => $cd, 
			   ordid => $atOrd});
	$thisId = $phAtom->getLastId();
	$atOrd++;
  
	# since 2008 version, RT? for all Entry Terms
	# create the rel
	$etnsRel->dumpRel({id1 => $thisId, id2 => $cd});
      }
    }
    if (defined ($$props{'Entry Term (non-synonymous)'})) {
      # can have more than 1 here.
      foreach $val (@{$$props{'Entry Term (non-synonymous)'}}) {
	$etAtom->dumpAtom({name => $val, code => $cd, sdui => $cd,
			   ordid => $atOrd});
	$thisId = $phAtom->getLastId();
	$atOrd++;
	# create the rel
	$etnsRel->dumpRel({id1 => $thisId, id2 => $cd});
      }
    }
=pod
    # now dump attributes. Make sure not to dump any duplicates.
    if (defined ($$props{'UMLS Semantic Type'})) {
      @dupAttrs = ();
      foreach $val (@{$$props{'UMLS Semantic Type'}}) {
	if (!grep(/^\Q$val\E$/, @dupAttrs)) {
	  $styAttr->dumpAttr({id => $cd, aval => $val});
	  push(@dupAttrs, $val);
	}
      }
    }
=cut
    if (defined ($$props{'SOS'})) {
      @dupAttrs = ();
      foreach $val (@{$$props{'SOS'}}) {
	if (!grep(/^\Q$val\E$/, @dupAttrs)) {
	  $sAttr->dumpAttr({id => $cd, aname =>  'SOS', aval => $val});
	  push(@dupAttrs, $val);
	}
      }
    }
    if (defined ($$props{'ICA'})) {
      @dupAttrs = ();
      foreach $val (@{$$props{'ICA'}}) {
	if (!grep(/^\Q$val\E$/, @dupAttrs)) {
	  $sAttr->dumpAttr({id => $cd, aname => 'ICA', aval => $val});
	  push(@dupAttrs, $val);
	}
      }
    }
    if (defined ($$props{'ICC'})) {
      @dupAttrs = ();
      foreach $val (@{$$props{'ICC'}}) {
	if (!grep(/^\Q$val\E$/, @dupAttrs)) {
	  $sAttr->dumpAttr({id => $cd, aname => 'ICC', aval => $val});
	  push(@dupAttrs, $val);
	}
      }
    }
    if (defined ($$props{'ICE'})) {
      @dupAttrs = ();
      foreach $val (@{$$props{'ICE'}}) {
	if (!grep(/^\Q$val\E$/, @dupAttrs)) {
	  $sAttr->dumpAttr({id => $cd, aname => 'ICE', aval => $val});
	  push(@dupAttrs, $val);
	}
      }
    }
    if (defined ($$props{'ICF'})) {
      @dupAttrs = ();
      foreach $val (@{$$props{'ICF'}}) {
	if (!grep(/^\Q$val\E$/, @dupAttrs)) {
	  $sAttr->dumpAttr({id => $cd, aname => 'ICF', aval => $val});
	  push(@dupAttrs, $val);
	}
      }
    }
    if (defined ($$props{'ICN'})) {
      @dupAttrs = ();
      foreach $val (@{$$props{'ICN'}}) {
	if (!grep(/^\Q$val\E$/, @dupAttrs)) {
	  $sAttr->dumpAttr({id => $cd, aname => 'ICN', aval => $val});
	  push(@dupAttrs, $val);
	}
      }
    }

=pod
    if (defined ($$props{'Short Name'})) {
      @dupAttrs = ();
      foreach $val (@{$$props{'Short Name'}}) {
	if (!grep(/^\Q$val\E$/, @dupAttrs)) {
	  push(@dupAttrs, $val);
	  # a special fix. to include a space before '<' in short name.
	  # to handle a data error! (not really an error!!)
	  $val =~ s/(.)</\1 </;
	  $sAttr->dumpAttr({id => $cd, aname => 'ICS', aval => $val});
	}
      }
    }
=cut
  }

  # since 2008 version, all Short Names represented as atoms
  foreach $cd (sort keys (%cd2Name)) {
    $atOrd = $$glInvRef->getAtomOrd("$cd") + $$glInvRef->getEle('AtomOrder.AB.Offset');
    $props = $cd2Props{"$cd"};
    if (defined ($$props{'Short Name'})) {
      # create a AB atom.
      ($val) = @{$$props{'Short Name'}};
  #    $cd2RealName{"$cd"} = $val;
      $phAtom->dumpAtom({name => $val, tty => 'AB', code => $cd,
                         status=>'R', supp=>'Y',
			 sdui => $cd, ordid => $atOrd});
      $atOrd++;
      $thisId = $phAtom->getLastId();

      $cd2ABSaid{"$cd"} = $thisId;

      # create the relation
      $abRel->dumpRel({id1 => $thisId, id2 => $cd2Said{"$cd"}});
      # create the merge
      $abMerge->dumpMerge({id1 => $thisId, id2 => $cd});
    }

   # since 2008 version, SEMANTIC_TYPE use said as id
    # now dump attributes. Make sure not to dump any duplicates.
    if (defined ($$props{'UMLS Semantic Type'})) {
      @dupAttrs = ();
      foreach $val (@{$$props{'UMLS Semantic Type'}}) {
	if (!grep(/^\Q$val\E$/, @dupAttrs)) {
	  $styAttr->dumpAttr({id => $cd2Said{$cd}, aval => $val});
	  $styAttr->dumpAttr({id => $cd2ABSaid{$cd}, aval => $val}) if (exists $cd2ABSaid{$cd});
	  push(@dupAttrs, $val);
	}
      }
    }

  }

  # dump cd to said values
  open (OUT, "> ../tmp/cd2said") or die "Could not open cd2said file.\n";
  foreach $cd (sort keys (%cd2Said)) {
    $val = $cd2Said{"$cd"};
    print OUT "$cd|$val\n";
  }
  close(OUT);
}


sub dumpCxts {
  my ($nd, $cxt, $sgidTree, $ndSaid, $par,$parSaid, $attrNum);
  my ($myName, $treeNames, $chldNames, $sibNames, @chldSibSaids);
  my ($thisId, $nextId, $parRef, $chldRef, $pathRef);


  # first call INV to prepare the contexts.
  $$glInvRef->prepareCxts;

  $parRef = $$glInvRef->getParentsRef;
  $chldRef = $$glInvRef->getChildrenRef;
  $pathRef = $$glInvRef->getParentPathsRef;

  # first dump sibs of the root node here (as root node won't be in the
  # parent's list. 
  my ($rootNd) = $$glInvRef->getRoots;
  @chldSibSaids = (map {$cd2Said{$_}} @{$$chldRef{"$rootNd"}});

  #add root node to context attribute, for 2008
  $chldNames = join('~', (map {&getCd2NdNameHat($_)} @{$$chldRef{"$rootNd"}}));
  $cxtAttr->dumpAttr({id => $cd2Said{$rootNd}, 
                     aval => "1\t::~~~~~~\t\t$cd2RealName{$rootNd}\t$chldNames\t"});

  while (@chldSibSaids > 1) {
    $thisId = shift(@chldSibSaids);
    foreach $nextId (@chldSibSaids) {
      $sCxt->dumpCxt({id1 => $thisId, id2 => $nextId, ptnm => '',
		      sgid1 => $said2Cd{$thisId}, sgid2 => $said2Cd{$nextId}});
    }
  }


  foreach $nd (keys ( %{$parRef} )) {
    $ndSaid = $cd2Said{"$nd"};	# this nodes said
    $attrNum = 0;
    # foreach cxt dump PAR context and its attributes

    foreach $cxt (@{$$pathRef{$nd}}) {

      $sgidTree = join('.', (map {$cd2Said{$_}} (split(/\|/, $cxt))));

      ### dump context for PAR
      ($par) = reverse (split(/\|/, $cxt));
      $parSaid = $cd2Said{"$par"};
      $pCxt->dumpCxt({id1 => $ndSaid, id2 => $parSaid, ptnm => $sgidTree,
		      sgid1 => $said2Cd{$ndSaid}, sgid2 => $said2Cd{$parSaid}});
      $attrNum++;
      if ($attrNum < 11) {
	## now dump attribute.
	# get my name
	$myName = $cd2RealName{"$nd"};

	# ancestors
	$treeNames = join('~', (map {$cd2RealName{$_}} (split(/\|/, $cxt))));

	# chidlren
	$chldNames = join('~', (map {&getCd2NdNameHat($_)} 
				@{$$chldRef{"$nd"}}));

	# siblings.
	$sibNames = join('~', (map {&getCd2NdNameHat($_)} 
			       $$glInvRef->getSiblings("$nd")));

	$cxtAttr->dumpAttr({id => $ndSaid, 
			    aval => "$attrNum\t::~~~~~~\t$treeNames\t$myName\t$chldNames\t$sibNames"});
	
      } elsif ($attrNum == 11) {
	# dump the special attribute stating that it has more 
	# than 10 attrs here
      }
    }
    # dump Context for SIB (children)
    @chldSibSaids = (map {$cd2Said{$_}} @{$$chldRef{"$nd"}});
    while (@chldSibSaids > 1) {
      $thisId = shift(@chldSibSaids);
      foreach $nextId (@chldSibSaids) {
	$sCxt->dumpCxt({id1 => $thisId, id2 => $nextId, ptnm => '',
			sgid1 => $said2Cd{$thisId}, sgid2 => $said2Cd{$nextId}});
      }
    }
  }

}


sub getCd2NdNameHat {
  my $cd = shift;
  my $name = $cd2RealName{"$cd"};
  return "$name^" if ($$glInvRef->hasChildren($cd) == 1);
  return $name;
}



1;
