#!@PATH_TO_PERL@
#
# File:     insert.pl
# Author:   Brian Carlsen
#
# This script is used to batch insert core data.
#
# Version Information
#
# 05/16/2003 4.2.0:  -view=false (memerun.pl)
# 05/14/2003 4.1.0:  attributes/rels require sg_ids
# 03/19/2003 4.1.0:  Release
# 02/07/2003 4.1.0:  First version
#
$release = "4";
$version = "2.0";
$version_date = "05/18/2003";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set Defaults & Environment
#
unless ($ENV{MEME_HOME}) {
    $badvalue="MEME_HOME";
    $badargs=4;
}
$work_id = 0;

# Check options
@ARGS=();
while (@ARGV) {
  $arg = shift(@ARGV);
  if ($arg !~ /^-/) {
    push @ARGS, $arg;
    next;
   }

  if ($arg =~ /^-atts$/) {
    $core_data_type = "A"; }
  elsif ($arg =~ /^-rels$/) {
    $core_data_type = "R"; }
  elsif ($arg =~ /^-atoms$/) {
    $core_data_type = "C"; }
  elsif ($arg =~ /^-w=(.*)/) {
    $work_id = $1; }
  elsif ($arg =~ /^-w/) {
    $work_id = shift(@ARGV) }
  elsif ($arg =~ /^-prop=(.*)/) {
    $prop = "-prop=$1"; }
  elsif ($arg =~ /^-prop/) {
    $prop = "-prop=".shift(@ARGV) }
  elsif ($arg =~ /^-host=(.*)/) {
    $host = "-host=$1"; }
  elsif ($arg =~ /^-host$/) {
    $host = "-host=".shift(@ARGV); }
  elsif ($arg =~ /^-port=(.*)/) {
    $port = "-port=$1"; }
  elsif ($arg =~ /^-port$/) {
    $port = "-port=".shift(@ARGV); }
  elsif ($arg eq "-version") {
    print "Version $version, $version_date ($version_authority).\n";
    exit(0);
   }
  elsif ($arg eq "-v") {
    print "$version\n";
    exit(0);
   }
  elsif ($arg eq "-help" || $arg eq "--help") {
    &PrintHelp;
    exit(0);
   }
  else {
    # invalid merge switches may
    # be valid switches for the class being called
    push @ARGS, $arg;
  }
}

#
# Get command line params
#
if (!$core_data_type) {
  $badargs = 5;
} elsif (scalar(@ARGS) == 3) {
  ($table_name, $database, $authority) = @ARGS;
} else {
  $badargs = 3;
  $badvalue = scalar(@ARGS);
}

#
# Process errors
#
%errors = (1 => "Illegal switch: $badvalue",
	   3 => "Bad number of arguments: $badvalue",
	   4 => "$badvalue must be set",
           5 => "Required parameter missing, please specify -atts, -rels, or -atoms"
          );

if ($badargs) {
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(1);
}


#
# Make the call
#
open (CMD,qq{$ENV{MEME_HOME}/bin/memerun.pl -view=false $prop $host $port gov.nih.nlm.meme.client.ActionClient $database do_batch I $core_data_type $table_name $authority $work_id Y X X |}) || die "Error executing command: $! $?\n";

while (<CMD>) {
  print;
}
close(CMD);

exit(0);

######################### LOCAL PROCEDURES #######################

sub PrintUsage {

	print qq{ This script has the following usage:
    insert.pl [-prop=<file>] [-host=<host>] [-port=<port>] [-w=<work_id>]
		  -{atts,rels,atoms} <table_name> <database> <authority>
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 This script is used to batch insert core data.

 Options:
       -prop=<file>:        Name properties file 
	                      Default is $ENV{MEME_HOME}/bin/meme.prop
       -host=<host>:        Name of the machine where server is running 
	                      Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-host`,qq{       -port=<port>:        The port number that the server is listening on 
	                      Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-port`,qq{       -w:                  Specify a work_id
       -atts:               Indicate that attributes are to be inserted.
       -rels:               Indicate that relationships are to be inserted.
       -atoms:              Indicate that atoms are to be inserted.
       -v[ersion]:          Print version information.
       -[-]help:            On-line help

 Arguments:
       table_name:          The table containing data to insert
       database:            The database
       authority:           The authority responsible for this merge set

 Source Tables
 
   Attributes:    The source table must contain the following fields.
                   concept_id,atom_id,attribute_id, attribute_level,
                   attribute_name,attribute_value,source,status,
                   generated_status, released,tobereleased, suppressible,
                   sg_id, sg_type, sg_qualifier, source_atui

   Relationships: The source table must contain these fields.
                   concept_id_1,concept_id_2,atom_id_1,atom_id_2,
                   relationship_name,relationship_attribute,
                   source, source_of_label,status,generated_status,
                   relationship_level,released,tobereleased,
                   relationship_id, suppressible,
                   sg_id_1, sg_type_1, sg_qualifier_1,
                   sg_id_2, sg_type_2, sg_qualifier_2,
		   source_rui, relationship_group


   Atoms:         The source table must contain these fields
                   concept_id,atom_id,atom_name,termgroup,source,code,
                   status,generated_status,released,tobereleased, suppressible,
		   source_aui, source_cui, source_dui

 Version $version, $version_date ($version_authority)
};
}
