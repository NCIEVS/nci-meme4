#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";


# Sample concepts to review
# suresh@nlm.nih.gov 02/2001

#use lib "/site/umls/release";
use ReleaseUtilsFile;

#require "/site/umls/lvg/lvg3.pl";
require "$ENV{LVG_HOME}/bin/lvg3.pl";

sub testsample {
  local($samplefile) = "$urisTopDir/uris-sample-concepts";
  my($html);
  local($releaseconfig) = UrisUtils->loadReleaseConfig($query->param('configfile'));
  local($releaseHandle) = new ReleaseUtilsFile({ releaseversion=>$releaseconfig->{releaseversion} });

  if (-e $samplefile) {
    $html = &dump_data($samplefile, $releaseHandle);
  } else {
    $html = "File that contains sample concepts: $samplefile does not exist.";
  }

  print join('',
	     $query->header,
	     $query->start_html($uristitle),
	     $html,
	     $query->end_html);
  return;
}

sub dump_data {
  my(@rows);
  my($historylink, $name, $cui, $n);
  my($noname) = $query->font({color=>'red'}, "&lt;Concept does not exist&gt;");

  push @rows, $query->th(['', 'Term or CUI', 'Link to concept report', 'History']);
  open(S, $samplefile) || die "Cannot open $samplefile\n";
  while (<S>) {
    chomp;
    next if /^\#/ || /^\s*$/;

    if (/^[cC]\d{7}/) {
      $cui = $_;

      $n++;
      $name = $releaseHandle->conceptName($cui) || $noname;
      $history = ($name ne $noname ? "" : &hist($cui));

      push @rows, $query->td([$n, &wrap($cui), $name, $history]);

    } else {
      my(@cuis);

      $name = $_;
      $luinorm = &lvg::luinorm($_);
      @cuis = $releaseHandle->str2cuis($name) || $releaseHandle->nstr2cuis($luinorm);
      %cuis = map { $_=>1 } @cuis;
      if (@cuis) {
	foreach $cui (sort keys %cuis) {
	  $n++;
	  push @rows, $query->td([$n, &wrap($cui), $name, '']);
	}
      }
    }
  }
  return join("\n",
	      $query->h1("Sample concepts for review") .
	      $query->table({border=>1, cellspacing=>0, cellpadding=>5, width=>'80%'}, $query->Tr(\@rows))
	      );
}

sub wrap {
  my($cui) = @_;
  my($u) = "http://meow.nlm.nih.gov/cgi-bin/release-file-cgi.pl";
  my(@u);

  push @u, 'action=searchbycui';
  push @u, "arg=$cui";
  push @u, "releaseversion=" . $releaseHandle->{releaseversion};
  return "<A HREF=\"" . $u . '?' . join('&', @u) . "\">$cui</A>";
}

sub hist {
  my($cui) = @_;
  my($u) = "http://meow.nlm.nih.gov/cgi-bin/history-cgi.pl?cui=$cui";

  return "<A HREF=\"" . $u . "\">History for $cui</A>";
}
1;
