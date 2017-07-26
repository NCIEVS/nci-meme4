#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";


# The UMLS Release Information System (URIS) version 2.0
# suresh@nlm.nih.gov 10/2003

# Displays character frequencies (Unicode aware)

# CGI params:
# sortby=char|freq
# showchar=any|nonascii|suspicious|...
# showblock=any|...
# showfile=

#use lib "/site/umls/lib/perl";

# my($mrsat) = getPath('/umls/Releases/2005AA-2/META/MRSAT', 'MRSAT');
# print "$mrsat hello \n";

my($val1) =  evalfile('/export/home/chebiyc/file1');
print "hello $val1\n";

sub getPath {
  my($metadir, $file) = @_;

  return $file if (($file =~ m@^/@));
  my $abc = m@^/@;
  print "klkl $abc\n";
  if (getReleaseFormat($metadir) eq "RRF") {
     print "aaaaaaaaa\n";
    $file .= ".RRF" unless $file =~ /\.RRF$/;
  } else {
    if ($file =~ /^(.*)\.RRF$/) {
      $file = $1;
    }
  }
  print "$metadir ldfsdfkj\n";
  print "$file opiuiopu\n";
  return join('/', $metadir, $file);
}

sub getReleaseFormat {
  my($metadir) = @_;
  my($mrfiles) = join('/', $metadir, "MRFILES.RRF");

  if (-e $mrfiles) {
    return "RRF";
  } else {
    $mrfiles = join('/', $metadir, "MRFILES");
    return (-e $mrfiles ? "ORF" : "");
  }
}
sub evalfile {
  my($file) = @_;

  @_ = ();
  print "hakdhfdsfh\n";
  open(F, $file) || return undef;
  while (<F>) {
  print "oppiip\n";
    push @_, $_;
    print @_;
  }
  close(F);
  return eval(join("\n", @_));
}

1;
