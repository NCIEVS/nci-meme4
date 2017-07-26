# ZIP inferface
# suresh@nlm.nih.gov 3/2005

package ZipUtils;

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

#use Archive::Zip qw( :ERROR_CODES :CONSTANTS );
use Archive::Zip;
use Data::Dumper;
use GeneralUtils;

# Adds a file to a zip archive (creates it if it doesn't exist)
sub addFile {
  my($class, $file, $archive) = @_;
  my($status, $zip);

  unless (-e $archive) {
    $zip = Archive::Zip->new();
    $status = $zip->writeToFileNamed($archive);
    die "ERROR in zip" unless $status == AZ_OK;
  }
  $zip = Archive::Zip->new($archive);

  $zip->addFile($file);
  $status = $zip->overwrite();
  die "ERROR in zip" unless $status == AZ_OK;
}

# -----------------------------
1;
