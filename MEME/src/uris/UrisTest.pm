#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

# Some URIS-related utilities
# suresh@nlm 3/2004

package UrisUtils;

#use lib "/site/umls/lib/perl";
use lib "$ENV{EXT_LIB}";

sub getPath {
  my($self, $metadir, $file) = @_;

  return $file if (($file =~ m@^/@) && (-e $file));
  if ($self->getReleaseFormat($metadir) eq "RRF") {
    $file .= ".RRF" unless $file =~ /\.RRF$/;
  } else {
    if ($file =~ /^(.*)\.RRF$/) {
      $file = $1;
    }
  }
  return join('/', $metadir, $file);
}
1;
