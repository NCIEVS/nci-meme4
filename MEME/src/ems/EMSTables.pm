# This package defines methods for creating EMS tables

# suresh@nlm.nih.gov 3/2005

package EMSTables;
BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}
use EMSNames;

# the spec for each table (list of column names and types)
%TABLESPEC = (

# MAXTAB stores int/char and timestamp information keyed to a arbitrary string
	 $EMSNames::MAXTABTABLE=>
	 [
	  {key=>'varchar2(128)'},
	  {valueint=>'integer'},
	  {valuechar=>'varchar2(512)'},
	  {timestamp=>'DATE'},
	 ],

# This table partitions the concept space into ME bins
	 $EMSNames::MEBINSTABLE=>
	 [
	  {bin_name=>'varchar2(32)'},
	  {concept_id=>'integer'},
	  {cluster_id=>'integer'},
	  {ischemical=>'char(1)'},
	 ],

# Stores configuration information for ME bins
	 $EMSNames::MECONFIGTABLE=>
	 [
	  {bin_name=>'varchar(20)'},
	  {description=>'varchar(256)'},
	  {editable=>'char(1)'},
	  {predefined=>'char(1)'},
	  {rank=>'integer'},
	  {content_type=>'varchar(32)'},
	  {generator=>'varchar2(128)'},
	  {orderer=>'varchar(128)'},
	  {chemalgo=>'varchar(128)'},
	 ],

# Stores statistical information for all bins
	 $EMSNames::BININFOTABLE=>
	 [
	  {bin_name=>'varchar(32)'},
	  {bin_type=>'char(2)'},
	  {generation_date=>'date'},
	  {generation_time=>'integer'},
	  {generation_user=>'varchar(128)'},
	  {nextWorklistNum=>'integer'},
	  {nextChemWorklistNum=>'integer'},
	  {nextNonchemWorklistNum=>'integer'},
	  {nextClinicalWorklistNum=>'integer'},
      {nextOtherWorklistNum=>'integer'},
	  {totalClusters=>'integer'},
	  {totalConcepts=>'integer'},
	  {totalUneditableClusters=>'integer'},
	  {chemClusters=>'integer'},
	  {chemConcepts=>'integer'},
	  {chemUneditableClusters=>'integer'},
	  {clinicalClusters=>'integer'},
	  {clinicalConcepts=>'integer'},
	  {clinicalUneditableClusters=>'integer'},
	  {otherClusters=>'integer'},
	  {otherConcepts=>'integer'},
	  {otherUneditableClusters=>'integer'},
	 ],

# Stores data for all QA bins
	 $EMSNames::QABINSTABLE=>
	 [
	  {bin_name=>'varchar2(32)'},
	  {concept_id=>'integer'},
	  {cluster_id=>'integer'},
	  {ischemical=>'char(1)'},
	 ],

# Stores metadata for QA bins configuration
	 $EMSNames::QACONFIGTABLE=>
	 [
	  {bin_name=>'varchar(20)'},
	  {description=>'varchar(256)'},
	  {rank=>'integer'},
	  {generator=>'varchar2(128)'},
	  {content_type=>'varchar(32)'},
	  {chemalgo=>'varchar(128)'},
	 ],

# Stores data for AH bins
	 $EMSNames::AHBINSTABLE=>
	 [
	  {bin_name=>'varchar2(32)'},
	  {concept_id=>'integer'},
	  {cluster_id=>'integer'},
	  {ischemical=>'char(1)'},
	 ],

# Stores metadata for AH bins configuration
	 $EMSNames::AHCONFIGTABLE=>
	 [
	  {bin_name=>'varchar(20)'},
	  {description=>'varchar(256)'},
	  {generator=>'varchar2(256)'},
	  {content_type=>'varchar(32)'},
	  {chemalgo=>'varchar(128)'},
	  {track_history=>'char(1)'},
	  {rank=>'integer'},
	 ],

# Stores information about bins and associated locks
         $EMSNames::BINLOCKTABLE=>
	 [
	  {bin_name=>'varchar(32)'},
	  {reason=>'varchar(128)'},
	  {timestamp=>'date'},
	  {owner=>'varchar(32)'},
	 ],

# Identifies concepts that are currently on worklists
	 $EMSNames::BEINGEDITEDTABLE=>
	 [
	  {bin_name=>'varchar(32)'},
	  {worklist_name=>'varchar(32)'},
	  {concept_id=>'integer'}
	 ],

# maps AH bins to canonical names, if different
	 $EMSNames::AHCANONICALNAMETABLE=>
	 [
	  {bin_name=>'varchar(32)'},
	  {canonical_name=>'varchar(32)'},
	 ],

# stores the history of AH bins
	 $EMSNames::AHHISTORYTABLE=>
	 [
	  {bin_name=>'varchar(32)'},
	  {canonical_name=>'varchar(32)'},
	  {worklist_name=>'varchar(32)'},
	  {cluster_id=>'integer'},
	  {min_concept_id=>'integer'},
	  {md5=>'char(32)'},
	 ],

# Stores metadata about worklists
	 $EMSNames::WORKLISTINFOTABLE=>
	 [
	  {worklist_name=>'varchar2(64)'},
	  {worklist_description=>'varchar2(256)'},
	  {worklist_status=>'char(2)'},
	  {n_concepts=>'integer'},
	  {n_clusters=>'integer'},
	  {grp=>'varchar2(16)'},
	  {editor=>'varchar2(16)'},
	  {create_date=>'date'},
	  {created_by=>'varchar2(100)'},
	  {assign_date=>'date'},
	  {return_date=>'date'},
	  {edit_time=>'integer'},
	  {stamp_date=>'date'},
	  {stamped_by=>'varchar2(100)'},
	  {stamp_time=>'integer'},
	  {exclude_flag=>'char'},
	  {n_actions=>'integer'},
	  {n_approved=>'integer'},
	  {n_approved_by_editor=>'integer'},
	  {n_stamped=>'integer'},
	  {n_not_stamped=>'integer'},
	  {n_rels_inserted=>'integer'},
	  {n_stys_inserted=>'integer'},
	  {n_splits=>'integer'},
	  {n_merges=>'integer'},
	 ],


# Stores metadata about checklists
	 $EMSNames::CHECKLISTINFOTABLE=>
	 [
	  {checklist_name=>'varchar2(32)'},
	  {owner=>'varchar2(40)'},
	  {bin_name=>'varchar2(32)'},
	  {bin_type=>'char(2)'},
	  {create_date=>'date'},
	  {concepts=>'integer'},
	  {clusters=>'integer'},
	 ],

# Information about editing epochs
	 $EMSNames::EDITINGEPOCHTABLE=>
	 [
	  {epoch=>'char(3)'},
	  {create_date=>'date'},
	  {active=>'char(1)'},
	 ],

# Identifies concepts that are chemicals (populated by cron script)
	 $EMSNames::CHEMCONCEPTSTABLE=>
	 [
	  {concept_id=>'integer'}
	 ],

         $EMSNames::DAILYSNAPSHOTTABLE=>
         [
          {snapshot_date=>'DATE'},
	  {snapshot_type=>'char(16)'},
	  {snapshot_attr=>'varchar2(100)'},
	  {snapshot_count=>'integer'}
         ],

         $EMSNames::DAILYACTIONCOUNTTABLE=>
         [
          {report_date=>'DATE'},
	  {authority=>'varchar2(200)'},
	  {total_actions=>'integer'},
	  {concepts_touched=>'integer'},
	  {concepts_approved=>'integer'},
	  {rels_inserted=>'integer'},
	  {stys_inserted=>'integer'},
	  {splits=>'integer'},
	  {merges=>'integer'},
         ],

         $EMSNames::SOURCESTATSTABLE=>
         [
	  {vsab=>'varchar2(100)'},
          {generation_date=>'DATE'},
	  {generation_time=>'integer'},
	  {htmldata=>'CLOB'},
         ],

         $EMSNames::STYCOOCTABLE=>
         [
	  {degree=>'integer'},
	  {stys=>'varchar2(500)'},
	  {frequency=>'integer'},
          {generation_date=>'DATE'},
	  {generation_time=>'integer'},
         ],
);

# can create a table (or all tables) according to the spec
sub createTable {
  my($self, $dbh, $table) = @_;
  my($t);

  return if $dbh->tableExists($table);
  $dbh->createTable($table, $TABLESPEC{$table});
}

# creates all known EMS table
sub createAllTables {
  my($class, $dbh) = @_;
  my($t);

  foreach $t ($class->tables) {
    $class->createTable($dbh, $t);
  }
}

# returns all table names
sub tables {
  my($class) = @_;
  return sort keys %TABLESPEC;
}

# returns the names of the columns in the order specified in %TABLESPEC
sub columns {
  my($class, $table) = @_;
  my($r);
  my(@col);

  foreach $r (@{ $TABLESPEC{$table} }) {
    unless (ref($r)) {
      push @col, $r;
    } elsif (ref($r) eq "HASH") {
      @_ = keys %$r;
      push @col, $_[0];
    }
  }
  return @col;
}

# returns a row of data from an EMS table as a hash
sub row2hash {
  my($class, $dbh, $table, $key, $value) = @_;
  my(@cols) = $class->columns($table);
  my($sql, $c);

  $c = join(",", @cols);
  $sql = "select $c from $table where $key=$value";
  return $dbh->selectFirstAsHash($sql, \@cols);
}

# inserts or updates a row of an EMS table
sub hash2row {
  my($class, $dbh, $table, $key, $row) = @_;
  my(@cols) = $class->columns($table);
  my($sql, $c);

  $sql = "select $key from $table where $key=" . $row->{$key};
  my($ref) = $dbh->selectFirstAsScalar($sql);
  if ($ref) {
    $dbh->insertRow($table, $row);
  } else {
    $dbh->updateRow($table, $key, $row->{$key}, $row);
  }
}

# returns the type of a column in a table
sub coltype {
  my($class, $table, $col) = @_;
  my($r, $k);

  foreach $r (@{ $TABLESPEC{$table} }) {
    if (ref($r) && ref($r) eq "HASH") {
      @_ = keys %$r;
      $k = $_[0];
      return uc($r->{$k}) if uc($k) eq uc($col);
    } else {
      return "INTEGER" if uc($r) eq uc($col);
    }
  }
  return "";
}

# returns the size of a varchar column in a table
sub colsize {
  my($class, $table, $col) = @_;
  my($r, $k);

  foreach $r (@{ $TABLESPEC{$table} }) {
    if (ref($r) && ref($r) eq "HASH") {
      @_ = keys %$r;
      $k = $_[0];
      if (uc($k) eq uc($col) && ($r->{$k} =~ /varchar.?\((\d+)\)$/i)) {
	return $1;
      }
    }
  }
  return 0;
}

#----------------------------------------
1;
