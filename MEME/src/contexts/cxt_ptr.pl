#!@PATH_TO_PERL@
#
# File:     cxt_ptr.pl
# Author:   Brian Carlsen
#
# This script is used to process parent_treenum
# of contexts.src OUTSIDE the MID.  This vastly increases
# performance of this recipe step.
#
# Changes:
# 02/23/2007 BAC (1-DKO45): This script takes a contexts.src file
# and maps the parent_treenum and rela to alleviate the need
# for MEME_SOURCE_PROCESSING.map_to_meme_ids to have to do this.
#
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set Defaults & Environment
#
unless ( $ENV{MIDSVCS_HOME} ) {
        $badvalue = "MIDSVCS_HOME";
        $badargs  = 4;
}

#
# Check options
#
@ARGS = ();
while (@ARGV) {
        $arg = shift(@ARGV);
        if ( $arg !~ /^-/ ) {
                push @ARGS, $arg;
                next;
        }

        if ( $arg eq "-version" ) {
                print "Version $version, $version_date ($version_authority).\n";
                exit(0);
        }
        elsif ( $arg eq "-v" ) {
                print "$version\n";
                exit(0);
        }
        elsif ( $arg eq "-help" || $arg eq "--help" ) {
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
if ( scalar(@ARGS) == 2 ) {
        ( $database, $cxt_file ) = @ARGS;
}
else {
        $badargs  = 3;
        $badvalue = scalar(@ARGS);
}

#
# Process errors
#
%errors = (
        1 => "Illegal switch: $badvalue",
        3 => "Bad number of arguments: $badvalue",
        4 => "$badvalue must be set"
);

if ($badargs) {
        &PrintUsage;
        print "\n$errors{$badargs}\n";
        exit(1);
}

#
# Open database connection
#
use DBD::Oracle;

# set variables
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $database`;
( $user, $password ) = split /\//, $userpass;
chop($password);

# open connection
$dbh = DBI->connect( "dbi:Oracle:$database", "$user", "$password" )
  || ( die "Error opening $database ($DBI::errstr)." );

open( F, "$cxt_file" )
  || die "Could not open $cxt_file: $! $?\n";
while (<F>) {
        chop;
        ( $a, $rel, $rela, $d, $e, $f, $g, $ptr, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q )
          = split /\|/;
        if ($rel eq "SIB") {
            print "$_\n";
            next;
        }
        if ( !$ptr ) { print; next; }
        @auis = map { &GetAUI($_) } split /\./, $ptr;
        $ptr = join ".", @auis;
        if ($rela) { $rela = &GetRELA($rela); }
        print join "|",
          ($a, $rel, $rela, $d, $e, $f, $g, $ptr, $h,
                $i, $j, $k, $l, $m, $n, $o, $p, $q );
        print "\n";
}
close(F);
$dbh->disconnect;

exit 0;

#
# Lookup an AUI for an src_atom_id.
#
sub GetAUI {
        my ($said) = @_;

        if ( $cache{$said} ) {
                return $cache{$said};
        }
        else {
                $sh = $dbh->prepare(
                        qq{
        SELECT aui FROM classes a, source_id_map b
        WHERE a.atom_id = b.local_row_id
          AND table_name = 'C'
          and source_row_id = ?}
                  )
                  || die "Error preparing query 1 ($DBI::errstr).";
                $sh->execute($said)
                  || die "Error executing query 1 ($DBI::errstr).";
                my ($aui) = $sh->fetchrow_array;
                $sh = "";
                if ($aui) { $cache{$said} = $aui;}
                else {
                  $sh = $dbh->prepare(
                        qq{
        SELECT aui FROM source_classes_atoms a, source_id_map b
        WHERE a.atom_id = b.local_row_id
          AND table_name = 'C'
          and source_row_id = ?}
                  )
                    || die "Error preparing query 1 ($DBI::errstr).";
                  $sh->execute($said)
                    || die "Error executing query 1 ($DBI::errstr).";
                  ($aui) = $sh->fetchrow_array;
                  if ($aui) { $cache{$said} = $aui;}
                  $sh = "";
                }
                if (!$aui) { die "No match for $said\n"; }
                else { return $aui; }
        }
}

#
# Lookup an inverse RELA for a RELA
#
sub GetRELA {
        my ($rela) = @_;

        if ( $cache{$rela} ) {
                return $cache{$rela};
        }
        else {
                $sh = $dbh->prepare(
                        qq{
        SELECT inverse_rel_attribute
        FROM inverse_rel_attributes
        WHERE NVL(relationship_attribute,'null') = NVL(?,'null')}
                  )
                  || die "Error preparing query 1 ($DBI::errstr).";
                $sh->execute($rela)
                  || die "Error executing query 1 ($DBI::errstr).";
                my ($irela) = $sh->fetchrow_array;
                $sh = "";
                if ($irela) { $cache{$rela} = $irela; }
                else { die "No match for $rela\n"; }
        }
}

######################### LOCAL PROCEDURES #######################

sub PrintUsage {

        print qq{ This script has the following usage:
    cxt_ptr.pl [-v[ersion]] [-help] <db> <contexts.src>
};
}

sub PrintHelp {
        &PrintUsage;
        print qq{
 This script is used to perform lookups of parent_treenum
 src atom ids and convert them to auis.

 Options:
       -v[ersion]:          Print version information.
       -[-]help:            On-line help

 Arguments:
       database:            db name
       contexts.src         context.src file

 Version $version, $version_date ($version_authority)
};
}
