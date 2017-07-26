#!@PATH_TO_PERL@
#
# File:  cloneSims.pl
# Author: BAC
#
# CHANGES
# 11/21/2011 BAC: first version
#
my $release = "4";
my $version = "1.0";
my $version_authority = "BAC";
my $version_date = "11/21/2011";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";
use strict 'vars';

#
# is being used.  Do Not Change This Part.
#
my @ARGS;
my $arg;
my $badargs;
my $badopt;
my $newMetaVer = "CURRENT";
while (@ARGV) {
    $arg = shift(@ARGV);
    if ($arg !~ /^-/) {
        push @ARGS, $arg;
        next;
    }
    if ($arg eq "-v") {
        &PrintVersion($arg) && exit(0);
    }
    elsif ($arg eq "-version") {
        &PrintVersion($arg) && exit(0);
    }
    elsif ($arg =~ /-{1,2}help$/) {
        &PrintHelp && exit(0);
    } else  {
        $badargs = 1;
        $badopt = $arg;
    }
}


#
# Get arguments
#
my ($oldSab, $newSab, $db);
if ($badargs) {
}
elsif (scalar(@ARGS) == 3) {
    ($oldSab, $newSab, $db) = @ARGS;
}
else {
    $badargs = 2;
    $badopt = $#ARGS+1;
}

#
# Print bad argument errors if any found
#
if ($badargs) {
    my %errors = (1 => "Illegal switch: $badopt",
               2 => "Wrong number of arguments: $badopt");
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(0);
}

print "-----------------------------------------------------------------\n";
print "Starting ... ".scalar(localtime)."\n";
print "-----------------------------------------------------------------\n";
print "oldSab:     $oldSab\n";
print "newSab:     $newSab\n";
print "db:         $db\n";
print "newMetaVer: $newMetaVer\n";

#
# Configure Oracle
#
require DBI;
require DBD::Oracle;
$ENV{"NLS_LANG"} = "AMERICAN_AMERICA.WE8ISO8859P1";

# set variables to use database
my $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
my ($user,$password) = split /\//, $userpass;
chop($password);

# open connection
my $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    die "ERROR: failed to connect to DB\n";
$dbh->do("ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY'")||
    die "ERROR: Failed to set date format\n";

print "    Cloning $oldSab to $newSab as meta_ver=$newMetaVer \n";
PerformClone($oldSab, $newSab);

$dbh->disconnect;

print "-----------------------------------------------------------------\n";
print "Finished ... ".scalar(localtime)."\n";
print "-----------------------------------------------------------------\n";
exit(0);

###################### Procedure Print_CLONE_COMPLETE ######################
#
# This procedure clones an existing record
#
sub PerformClone {
    my ($oldSab, $newSab) = @_;
    my $readHandle = $dbh->prepare(qq{
            SELECT source, date_created, meta_ver, init_rcpt_date,
            clean_rcpt_date, test_insert_date, real_insert_date, source_contact,
            inverter_contact, nlm_path, apelon_path, inversion_script,
            inverter_notes_file, conserve_file, sab_list,
            meow_display_name,source_desc,status,worklist_sortkey_loc,whats_new,
            termgroup_list, attribute_list, inversion_notes, internal_url_list,
            notes, nlm_editing_notes, inv_recipe_loc, suppress_edit_rec
            FROM sims_info WHERE source= ?
            }) || die "ERROR: Failed to prepare select statement\n";
    $readHandle-> execute($newSab) || die "ERROR: error executing query for $newSab\n";
    if ($readHandle->fetchrow_array) {
        die "ERROR: Record already exists for $newSab\n";
    }
    $readHandle-> execute($oldSab) || die "ERROR: error executing query for $oldSab\n";
    my ($source, $dateCreated, $metaVer, $initRcptDate,
        $cleanRcptDate,$testInsertDate, $realInsertDate, $sourceContact,
        $inverterContact, $nlmPath, $apelonPath, $inversionScript,
        $inverterNotesFile, $conserveFile, $sabList,
        $meowDisplayName, $sourceDesc, $status,
        $worklistSortkeyLoc, $whatsNew, $termgroupList, $attributeList,
        $inversionNotes, $internalUrlList, $notes, $nlmEditingNotes,
        $invRecipeLoc, $suppressEditRec);
    ($source, $dateCreated, $metaVer, $initRcptDate,
     $cleanRcptDate,$testInsertDate, $realInsertDate, $sourceContact,
     $inverterContact, $nlmPath, $apelonPath, $inversionScript,
     $inverterNotesFile, $conserveFile, $sabList,
     $meowDisplayName, $sourceDesc, $status,
     $worklistSortkeyLoc, $whatsNew, $termgroupList, $attributeList,
     $inversionNotes, $internalUrlList, $notes, $nlmEditingNotes,
     $invRecipeLoc, $suppressEditRec) = $readHandle->fetchrow_array;
    die "ERROR: record does not exist for ($oldSab)\n" unless $source;

    $readHandle->finish();

# create the new record
    my $insertHandle = $dbh->prepare(qq{
        INSERT INTO SIMS_INFO
        (source, date_created, meta_ver, init_rcpt_date,
         clean_rcpt_date, test_insert_date, real_insert_date,
         source_contact, inverter_contact, nlm_path, apelon_path,
         inversion_script, inverter_notes_file, conserve_file, sab_list,
         meow_display_name, source_desc, status,
         worklist_sortkey_loc, whats_new, termgroup_list, attribute_list,
         inversion_notes, internal_url_list, notes, nlm_editing_notes,
         inv_recipe_loc, suppress_edit_rec) VALUES
        (?,sysdate,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        });
    my($suppressEditRec)= "N";
    my $rowCount = $insertHandle->execute(
                                          $newSab, $newMetaVer, $initRcptDate,
                                          $cleanRcptDate,$testInsertDate, $realInsertDate, $sourceContact,
                                          $inverterContact, $nlmPath, $apelonPath, $inversionScript,
                                          $inverterNotesFile, $conserveFile, $sabList,
                                          $meowDisplayName, $sourceDesc, $status,
                                          $worklistSortkeyLoc, $whatsNew,$termgroupList, $attributeList,
                                          $inversionNotes, $internalUrlList, $notes, $nlmEditingNotes,
                                          $invRecipeLoc, $suppressEditRec);

    unless (defined($rowCount)) {
        die "ERROR: error adding row for $newSab\n";
    }

    print qq{    New Record $newSab successfully created:
               newMetaVer: $newMetaVer
                 init_rcpt_date: $initRcptDate,
                 clean_rcpt_date: $cleanRcptDate,
                 test_insert_date: $testInsertDate,
                 real_insert_date: $realInsertDate,
                 source_contact: $sourceContact,
                 inverter_contact: $inverterContact,
                 nlm_path: $nlmPath,
                 apelon_path: $apelonPath,
                 inversion_script: $inversionScript,
                 inverter_notes_file: $inverterNotesFile,
                 conserve_file: $conserveFile,
                 sab_list: $sabList,
                 meow_display_name: $meowDisplayName,
                 source_desc: $sourceDesc,
                 status: $status,
                 worklist_sortkey_loc: $worklistSortkeyLoc,
                 whats_new: $whatsNew,
                 termgroup_list: $termgroupList,
                 attribute_list: $attributeList,
                 inversion_notes: $inversionNotes,
                 internal_url_list: $internalUrlList,
                 notes: $notes,
                 nlm_editing_notes: $nlmEditingNotes,
                 inv_recipe_loc: $invRecipeLoc,
                 suppress_edit_rec: $suppressEditRec
};
}


###################### Procedure PrintVersion ######################
#
# This prints help information from the command line
#
sub PrintVersion {
        my($type) = @_;
        print "Release $release: version $version, $version_date ($version_authority).\n"
                if $type eq "version";
        print "$version\n" if $type eq "v";
        return 1;
}

sub PrintUsage {

    print qq{ This script has the following usage:
              cloneSims.pl <old source> <new source> <db>
};
}

###################### Procedure PrintHelp ######################
#
# This prints help information from the command line
#
sub PrintHelp {
        print qq{

  This script clones a SIMS info record like SIMS.cgi does.

Parameters:

    oldSab:   The old source value (required)
    newSab:   The old source value (required)
    db:       The database (required)

Version: $version, $version_date ($version_authority)

};
} # End Procedure PrintHelp

