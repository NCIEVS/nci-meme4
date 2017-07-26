#!@PATH_TO_64PERL@
#!@PATH_TO_PERL@
#
unshift(@INC,".");
use lib "$ENV{INV_HOME}/lib";
use lib "$ENV{INV_HOME}/bin";

use OracleIF;
use Midsvcs;

use strict 'vars';
use strict 'subs';

package MakeDoc;
{
  my ($_inDir, $_pbar_cb);
  my $logwin = '';
  my $_pbar_present = 0;

  my %doc12to34=();
  my %rela2irela=();
  my %infoFromSrc=();

  sub new {
	my $class = shift;
	$_inDir = "../src";
	my $ref = {};
	return bless ($ref, $class);
  }

  sub setLogwin {
	my $class = shift;
	if (@_ > 0) {
	  $logwin = shift;
	}
  }

  sub setPbar {
	my $class = shift;
	$_pbar_cb = shift;
	$_pbar_present = 1;
  }

  sub msg {
	my $msg = shift;
	print "$msg";
	if ($logwin ne '') {
	  $logwin->insert('end', $msg);
	}
  }

  sub getMrDoc {
	my $db = Midsvcs->get('editing-db');
	my $oracleuser = 'meow';
	my $oraclepassword = GeneralUtils->getOraclePassword($oracleuser,$db);
	my $dbh = new OracleIF("db=$db&user=$oracleuser&password=$oraclepassword");

	my ($val, $irela);

	# first get rel|inv_rel from inverse_rel_attributes
	my @dbrela2irela = $dbh->selectAllAsRef
	  ("SELECT relationship_attribute, inverse_rel_attribute
	 FROM inverse_rel_attributes");

	foreach $val (@dbrela2irela) {
	  $rela2irela{"$val->[0]"} = $val->[1];
	}



	# get the entries from meme_properties and inverse_rel_attributes
	my @dbmemeProps = $dbh->selectAllAsRef
	  ("SELECT key_qualifier, value, key, description
        FROM meme_properties
        WHERE key_qualifier IN ('TTY','RELA','ATN')
      UNION
      SELECT 'RELA', relationship_attribute, 'rela_inverse',
	     'inverse_rel_attribute'
	FROM inverse_rel_attributes a
	WHERE nvl(relationship_attribute,'null') IN
	      (SELECT NVL(value,'null')
                 FROM meme_properties b
	         WHERE key = 'expanded_form'
	           AND key_qualifier = 'RELA')");


	foreach $val (@dbmemeProps) {
	  if ($val->[0] eq 'RELA' && $val->[2] eq 'rela_inverse') {
		$irela = $rela2irela{"$val->[1]"};
		push(@{$doc12to34{"$val->[0]|$val->[1]"}}, "$val->[2]|$irela");
	  } else {
		push(@{$doc12to34{"$val->[0]|$val->[1]"}}, "$val->[2]|$val->[3]");
	  }
	}
  }

  sub getTtyAtnRela {
	my $dt;
	my @F;
	# read TTYs from termgroups
	open(IN, "<:utf8", "$_inDir/termgroups.src")
	  or die "Could not open $_inDir/termgroups.src file\n";
	while (<IN>) {
	  chomp;
	  @F = split(/\|/, $_);
	  $infoFromSrc{"TTY|$F[5]"}++;
	}
	close(IN);
	$dt = `date`;
	&msg("Got info from $_inDir/termgroups.src <$dt>\n");
	&$_pbar_cb(20) if ($_pbar_present == 1);

	# read RELAs from relationships
	open(IN, "<:utf8", "$_inDir/relationships.src")
	  or die "Could not open $_inDir/relationships.src file\n";
	while (<IN>) {
	  chomp;
	  @F = split(/\|/, $_);
	  $infoFromSrc{"RELA|$F[4]"}++;
	}
	close(IN);

	$dt = `date`;
	&msg("Got info from $_inDir/relationships.src <$dt>\n");
	&$_pbar_cb(50) if ($_pbar_present == 1);

	# read ATNs from attributes. ignore CONTEXT, SEMANTIC_TYPE, XMAP, XMAPFROM,
	#       XMAPTO, DEFINITION, LEXICAL_TAG, COMPONENTHISTORY
	my %ignore = (CONTEXT => 0,
				  SEMANTIC_TYPE => 0,
				  XMAP => 0,
				  XMAPFROM => 0,
				  XMAPTO => 0,
				  DEFINITION => 0,
				  LEXICAL_TAG => 0,
				  COMPONENTHISTORY => 0);

	open(IN, "<:utf8", "$_inDir/attributes.src")
	  or die "Could not open $_inDir/attributes.src file\n";
	while (<IN>) {
	  chomp;
	  @F = split(/\|/, $_);
	  
	  #get the Hidden attribute names in ATV field for ATN=SUBSET_MEMBER
            if (($F[3] eq "SUBSET_MEMBER") && ($F[4] =~ /\~/)){

          my  @tempfield = split(/~/, $F[4]);
          my  $subfield = @tempfield[1];
             $infoFromSrc{"ATN|$subfield"}++;

             }
	  next if (defined($ignore{"$F[3]"}));
	  $infoFromSrc{"ATN|$F[3]"}++;
	}
	close(IN);
	$dt = `date`;
	&msg("Got info from $_inDir/attributes.src <$dt>\n");
	&$_pbar_cb(90) if ($_pbar_present == 1);

  }

  sub makeMrDoc {
	my $class;
	($class, $_inDir) = @_;

	if (-e "$_inDir/MRDOC.RRF") {
	  &msg("MRDOC.RRF already exists. Delete it first and run. Exiting.\n");
	  return;
	}

	my $dt = `date`;
	&msg("Starting <$dt>\n");

	&getMrDoc;
	$dt = `date`;
	&msg("Got data from db <$dt>\n");
	&$_pbar_cb(15) if ($_pbar_present == 1);


	&getTtyAtnRela;
	$dt = `date`;
	&msg("Got data from src files <$dt>\n");

	open (OUT, ">:utf8", "$_inDir/MRDOC.RRF")
	  or die "Could not open $_inDir/MRDOC.RRF\n";
	my ($key, $f1, $f2, $val);
	foreach $key (keys %infoFromSrc) {
	  if (defined($doc12to34{"$key"})) {
		# existing entry
		foreach $val (@{$doc12to34{"$key"}}) {
		  print OUT "$key|$val|\n";
		}
	  } else {
		# create new entries.
		($f1, $f2) = split(/\|/, $key);
		if ($f1 eq 'TTY') {
		  print OUT "$key|expanded_form|####|\n";
		  print OUT "$key|tty_class|####|\n";
		} elsif ($f1 eq 'RELA') {
		  print OUT "$key|expanded_form|####|\n";
		  print OUT "$key|rela_inverse|####|\n";

		  print OUT "$f1|###|expanded_form|####|\n";
		  print OUT "$f1|###|rela_inverse|$f2|\n";

		} elsif ($f1 eq 'ATN') {
		  print OUT "$key|expanded_form|###|\n";
		} else {
		  &msg("Illegal value $key present in files. skipping.\n");
		}
	  }
	}
	close(OUT);
	$dt = `date`;
	&msg("Done <$dt>\n");
	&$_pbar_cb(100) if ($_pbar_present == 1);
  }
}
1
