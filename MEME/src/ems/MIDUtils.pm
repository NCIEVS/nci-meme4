# MID related utility functions
# suresh@nlm.nih.gov 3/2005

package MIDUtils;
BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use Data::Dumper;
use GeneralUtils;

# Gets the preferred name of a concept
sub conceptPreferredName {
  my($class, $dbh, $concept_id) = @_;
  my($sql);

  $sql = <<"EOD";
select a.atom_name from atoms a, concept_status b
where  a.atom_id=b.preferred_atom_id
and    b.concept_id=$concept_id
EOD
  return $dbh->selectFirstAsScalar($sql);
}

# maps a CUI to concept_id(s) using the data in concept_status
sub cui2concept_id {
  my($class, $dbh, $cui) = @_;
  my($sql);

  if (ref($cui) eq "ARRAY") {
    my(@concept_ids);
    foreach (@$cui) {
      push @concept_ids, $class->cui2concept_id($dbh, $_);
    }
    return @concept_ids;
  }

  $sql = "select concept_id from concept_status where cui=" . $dbh->quote($cui);
  return $dbh->selectAllAsArray($sql);
}

# maps one or more atom_ids to concept_ids
sub atom_id2concept_id {
  my($class, $dbh, $atom_id, $preserveorder) = @_;
  my($sql);

  if (ref($atom_id) eq "ARRAY") {
    my(%concept_id, @concept_id, $concept_id);
    foreach (@$atom_id) {
      $concept_id = $class->atom_id2concept_id($dbh, $_);
      $concept_id{$concept_id}++;
      if ($preserveorder) {
	push @concept_id, $concept_id unless $concept_id{$concept_id};
      }
    }
    if ($preserveorder) {
      return @concept_id;
    } else {
      return sort { $a <=> $b } keys %concept_id;
    }
  }
  $sql = "select concept_id from classes where atom_id=$atom_id";
  return $dbh->selectFirstAsScalar($sql);
}

# maps LUIs to atom_id's
sub lui2atom_id {
  my($class, $dbh, $lui) = @_;
  my($sql);

  if (ref($lui) eq "ARRAY") {
    my(%atom_id, $atom_id);
    foreach (@$lui) {
      foreach $atom_id ($class->lui2atom_id($dbh, $_)) {
	$atom_id{$atom_id}++;
      }
    }
    return sort { $a <=> $b } keys %atom_id;
  }
  $lui = $class->makeLUI($lui);
  $sql = "select atom_id from classes where LUI=" . $dbh->quote($lui);
  return $dbh->selectAllAsArray($sql);
}

# maps LUIs to concept_id's
sub lui2concept_id {
  my($class, $dbh, $lui) = @_;
  my(@atom_ids) = $class->lui2atom_id($dbh, $lui);

  return $class->atom_id2concept_id($dbh, \@atom_ids);
}

# maps SUIs to atom_id's
sub sui2atom_id {
  my($class, $dbh, $sui) = @_;
  my($sql);

  if (ref($sui) eq "ARRAY") {
    my(%atom_id, $atom_id);
    foreach (@$sui) {
      foreach $atom_id ($class->sui2atom_id($dbh, $_)) {
	$atom_id{$atom_id}++;
      }
    }
    return sort { $a <=> $b } keys %atom_id;
  }
  $sui = $class->makeSUI($sui);
  $sql = "select atom_id from classes where SUI=" . $dbh->quote($sui);
  return $dbh->selectAllAsArray($sql);
}

# maps SUIs to concept_id's
sub sui2concept_id {
  my($class, $dbh, $sui) = @_;
  my($sql);

  my(@atom_ids) = $class->sui2atom_id($dbh, $sui);

  return $class->atom_id2concept_id($dbh, \@atom_ids);
}

# maps AUIs to atom_id's
sub aui2atom_id {
  my($class, $dbh, $aui) = @_;
  my($sql);

  if (ref($aui) eq "ARRAY") {
    my(%atom_id, $atom_id);
    foreach (@$aui) {
      foreach $atom_id ($class->aui2atom_id($dbh, $_)) {
	$atom_id{$atom_id}++;
      }
    }
    return sort { $a <=> $b } keys %atom_id;
  }
  $aui = $class->makeAUI($aui);
  $sql = "select atom_id from classes where AUI=" . $dbh->quote($aui);
  return $dbh->selectAllAsArray($sql);
}

# maps AUIs to concept_id's
sub aui2concept_id {
  my($class, $dbh, $aui) = @_;

  my(@atom_ids) = $class->aui2atom_id($dbh, $aui);
  return $class->atom_id2concept_id($dbh, \@atom_ids);
}

# maps CODE to atom_ids
sub code2atom_id {
  my($class, $dbh, $code, $sourceref) = @_;
  my($sql);

  if (ref($code) eq "ARRAY") {
    my(%atom_id, $atom_id);
    foreach (@$code) {
      foreach $atom_id ($class->code2atom_id($dbh, $_)) {
	$atom_id{$atom_id}++;
      }
    }
    return sort { $a <=> $b } keys %atom_id;
  }

  $sql = "select atom_id from classes where CODE=" . $dbh->quote($code);
  if ($sourceref && @$sourceref>0) {
    $sources = join(',', map { $dbh->quote($_) } map { $class->makeVersionedSAB($dbh, $_) } @$sourceref);
    $sql .= " and source in ($sources)";
  }
  return $dbh->selectAllAsArray($sql);
}

# maps CODE to concept_id's
sub code2concept_id {
  my($class, $dbh, $code, $sourceref) = @_;
  my($sql);

  my(@atom_ids) = $class->code2atom_id($dbh, $code, $sourceref);
  return $class->atom_id2concept_id($dbh, \@atom_ids);
}

# searches for an exact string
sub str2atom_id {
  my($class, $dbh, $str, $sourceref) = @_;
  my($sql);
  my($prefixlength) = $main::EMSCONFIG{STRING_UI_PREFIX_LENGTH} || 10;

  if (ref($str) eq "ARRAY") {
    my(%atom_id, $atom_id);
    foreach (@$str) {
      foreach $atom_id ($class->str2atom_id($dbh, $_)) {
	$atom_id{$atom_id}++;
      }
    }
    return sort { $a <=> $b } keys %atom_id;
  }

  $prefix = $dbh->quote(lc(substr($str, 0, $prefixlength)));
  $str = $dbh->quote($str);
  $sql = <<"EOD";
select atom_id from classes
where sui in (select distinct sui from string_ui
              where  lowercase_string_pre=$prefix
              and    string=$str)
EOD
  if ($sourceref && @$sourceref>0) {
    $sources = join(',', map { $dbh->quote($_) } map { $class->makeVersionedSAB($dbh, $_) } @$sourceref);
    $sql .= " and source in ($sources)";
  }
  return $dbh->selectAllAsArray($sql);
}

# maps exact string to concept_ids
sub str2concept_id {
  my($class, $dbh, $str, $sourceref) = @_;

  my(@atom_ids) = $class->str2atom_id($dbh, $str, $sourceref);
  return $class->atom_id2concept_id($dbh, \@atom_ids);
}

# searches for an lowercase string
sub lowerstr2atom_id {
  my($class, $dbh, $str, $sourceref) = @_;
  my($sql);
  my($prefixlength) = $main::EMSCONFIG{STRING_UI_PREFIX_LENGTH} || 10;

  if (ref($str) eq "ARRAY") {
    my(%atom_id, $atom_id);
    foreach (@$str) {
      foreach $atom_id ($class->lowerstr2atom_id($dbh, $_)) {
	$atom_id{$atom_id}++;
      }
    }
    return sort { $a <=> $b } keys %atom_id;
  }

  $prefix = $dbh->quote(lc(substr($str, 0, $prefixlength)));
  $str = lc($dbh->quote($str));
  $sql = <<"EOD";
select atom_id from classes
where sui in (select distinct sui from string_ui
              where  lowercase_string_pre=$prefix
              and    lower(string)=$str)
EOD
  if ($sourceref && @$sourceref>0) {
    $sources = join(',', map { $dbh->quote($_) } map { $class->makeVersionedSAB($dbh, $_) } @$sourceref);
    $sql .= " and source in ($sources)";
  }
  return $dbh->selectAllAsArray($sql);
}

# maps lowercase string to concept_ids
sub lowerstr2concept_id {
  my($class, $dbh, $str, $sourceref) = @_;

  my(@atom_ids) = $class->lowerstr2atom_id($dbh, $str, $sourceref);
  return $class->atom_id2concept_id($dbh, \@atom_ids);
}

# searches for a normalized string (actually uses luinorm)
sub normstr2atom_id {
  my($class, $dbh, $str, $sourceref) = @_;
  my($sql);

  if (ref($str) eq "ARRAY") {
    my(%atom_id, $atom_id);
    foreach (@$str) {
      foreach $atom_id ($class->normstr2atom_id($dbh, $_)) {
	$atom_id{$atom_id}++;
      }
    }
    return sort { $a <=> $b } keys %atom_id;
  }

  $str = $dbh->quote($str);
  $sql = <<"EOD";
select a.atom_id from classes a, normstr b
where  a.atom_id=b.normstr_id
and    b.normstr=$str
EOD
  if ($sourceref && @$sourceref>0) {
    $sources = join(',', map { $dbh->quote($_) } map { $class->makeVersionedSAB($dbh, $_) } @$sourceref);
    $sql .= " and a.source in ($sources)";
  }
  return $dbh->selectAllAsArray($sql);
}

# maps normalized string to concept_ids
sub normstr2concept_id {
  my($class, $dbh, $str, $sourceref) = @_;

  my(@atom_ids) = $class->normstr2atom_id($dbh, $str, $sourceref);
  return $class->atom_id2concept_id($dbh, \@atom_ids);
}

# maps source_row_id to atom_ids
sub source_row_id2atom_id {
  my($class, $dbh, $source_row_id) = @_;
  my($sql);

  if (ref($source_row_id) eq "ARRAY") {
    my(%atom_id, $atom_id);
    foreach (@$source_row_id) {
      foreach $atom_id ($class->source_row_id2atom_id($dbh, $_)) {
	$atom_id{$atom_id}++;
      }
    }
    return sort { $a <=> $b } keys %atom_id;
  }

  $sql = <<"EOD";
SELECT local_row_id FROM source_id_map
WHERE  source_row_id=$source_row_id
AND    table_name='C'
EOD
  return $dbh->selectAllAsArray($sql);
}

# maps source_row_id to concept_id's
# passable options (in optref) are: hitsperpage, pagenum, prefix
sub source_row_id2concept_id {
  my($class, $dbh, $source_row_id) = @_;
  my(@atom_ids) = $class->source_row_id2atom_id($dbh, $source_row_id);
  return $class->atom_id2concept_id($dbh, \@atom_ids);
}

# maps one or more words to atom_ids using the word_index table (which is lowercase)
# returns a hash ref total number of matches in $outputref->{matches} and requested
# atoms in $outputref->{atom_ids}
sub lowerword2atom_id {
  my($class, $dbh, $word, $sourceref, $optref) = @_;
  my($sql);
  my(@tables, $table, $count, $atom_id, $w);
  my(%outputref);
  my($prefix) = $optref->{prefix} || "EMSTMP";

  $word = [$word] unless ref($word);

  foreach $w (@$word) {
    next unless $w;

    $count = 0;
    $sql = "select count(*) as c from word_index where word=" . $dbh->quote($w);
    $count = $dbh->selectFirstAsScalar($sql);
    next if $count == 0;

    $table = $dbh->tempTable($prefix);
    push @tables, $table;

    if ($sourceref && @$sourceref>0) {
      my($qw) = $dbh->quote($w);
      $sources = join(',', map { $dbh->quote($_) } map { $class->makeVersionedSAB($dbh, $_) } @$sourceref);
      $sql = <<"EOD";
create table $table as select a.atom_id, 1/$count as weight from word_index a, classes b
where  a.word=$qw
and    a.atom_id=b.atom_id
and    b.source in ($sources)
EOD
    } else {
      $sql = "create table $table as select atom_id, 1/$count as weight from word_index where word=" . $dbh->quote($w);
    }
    $dbh->executeStmt($sql);
  }

  return () unless @tables;

  my(@u, $u);
  foreach $table (@tables) {
    push @u, "select atom_id, weight from $table";
  }
  $u = join("\nunion ", @u);

  my($hitsperpage) = $optref->{hitsperpage} || 100;
  my($pagenum) = $optref->{pagenum} || 0;

  my($from_rownum) = $pagenum*$hitsperpage+1;
  my($to_rownum) = $from_rownum+$hitsperpage;

  $sql = <<"EOD";
select count(atom_id) from (
 select atom_id, sum(weight) as weight from ($u)
 group by atom_id
)
EOD
  $outputref{matches} = $dbh->selectFirstAsScalar($sql) || 0;

  $sql = <<"EOD";
select atom_id from (
  select atom_id, r from (
    select atom_id, rownum as r from (
      select atom_id, sum(weight) as weight from ($u)
      group by atom_id
      order by weight desc
    )
  ) where r>=$from_rownum and r<$to_rownum
)
EOD
  my(@atom_ids) = $dbh->selectAllAsArray($sql);
  $outputref{atom_ids} = \@atom_ids;
  $dbh->dropTables(\@tables);
  return \%outputref;
}

# maps one or more words to atom_ids using the normwrd table
# returns a hash ref total number of matches in $outputref->{matches} and requested
# atoms in $outputref->{atom_ids}
sub normword2atom_id {
  my($class, $dbh, $word, $sourceref, $optref) = @_;
  my($sql);
  my(@tables, $table, $count, $atom_id, $w);
  my(%outputref);
  my($prefix) = $optref->{prefix} || "EMSTMP";

  $word = [$word] unless ref($word);

  foreach $w (@$word) {
    next unless $w;

    $count = 0;
    $sql = "select count(*) as c from normwrd where normwrd=" . $dbh->quote($w);
    $count = $dbh->selectFirstAsScalar($sql);
    next if $count == 0;

    $table = $dbh->tempTable($prefix);
    push @tables, $table;

    if ($sourceref && @$sourceref>0) {
      my($qw) = $dbh->quote($w);
      $sources = join(',', map { $dbh->quote($_) } map { $class->makeVersionedSAB($dbh, $_) } @$sourceref);
      $sql = <<"EOD";
create table $table as select b.atom_id, 1/$count as weight from normwrd a, classes b
where  a.normwrd=$qw
and    a.normwrd_id=b.atom_id
and    b.source in ($sources)
EOD
    } else {
      $sql = "create table $table as select normwrd_id as atom_id, 1/$count as weight from normwrd where normwrd=" . $dbh->quote($w);
    }
    $dbh->executeStmt($sql);
  }

  return () unless @tables;

  my(@u, $u);
  foreach $table (@tables) {
    push @u, "select atom_id, weight from $table";
  }
  $u = join("\nunion ", @u);

  my($hitsperpage) = $optref->{hitsperpage} || 100;
  my($pagenum) = $optref->{pagenum} || 0;

  my($from_rownum) = $pagenum*$hitsperpage+1;
  my($to_rownum) = $from_rownum+$hitsperpage;

  $sql = <<"EOD";
select count(atom_id) from (
 select atom_id, sum(weight) as weight from ($u)
 group by atom_id
)
EOD
  $outputref{matches} = $dbh->selectFirstAsScalar($sql) || 0;

  $sql = <<"EOD";
select atom_id from (
  select atom_id, r from (
    select atom_id, rownum as r from (
      select atom_id, sum(weight) as weight from ($u)
      group by atom_id
      order by weight desc
    )
  ) where r>=$from_rownum and r<$to_rownum
)
EOD
  my(@atom_ids) = $dbh->selectAllAsArray($sql);
  $outputref{atom_ids} = \@atom_ids;
  $dbh->dropTables(\@tables);
  return \%outputref;
}

# maps an atom to its name
sub atomName {
  my($class, $dbh, $atom_id) = @_;
  my($sql);

  $sql = "select atom_name from atoms where atom_id=$atom_id";
  return $dbh->selectFirstAsScalar($sql);
}

# returns information about an atom (SAB, TTY, etc) from classes in a hash
sub atomInfo {
  my($class, $dbh, $atom_id) = @_;
  my($sql);
  my(@cols) = qw(source tty termgroup code termgroup_rank concept_id sui lui aui last_release_cui
		 status authority timestamp insertion_date released tobereleased suppressible
		 isui source_aui source_cui source_dui language);

  $_ = join(',', @cols);
  $sql = "select $_ from classes where atom_id=$atom_id";
  my($x) = $dbh->selectFirstAsRef($sql);
  my($i) = 0;
  my(%r);
  foreach (@cols) {
    $r{$_} = $x->[$i++];
  }
  $r{atom_name} = $class->atomName($dbh, $atom_id);
  return \%r;
}

# Gets the STYs of a concept (list of names)
sub getSTYs {
  my($class, $dbh, $concept_id) = @_;
  my($sql);

  $sql = <<"EOD";
select distinct attribute_value from attributes
where  attribute_name='SEMANTIC_TYPE'
and    concept_id=$concept_id
and    tobereleased in ('y', 'Y')
EOD
  return $dbh->selectAllAsArray($sql);
}

# converts a numeric id to a CUI
sub makeCUI {
  my($class, $cui) = @_;
  return $class->ui2str($cui, "C");
}

# converts a numeric id to a LUI
sub makeLUI {
  my($class, $lui) = @_;
  return $class->ui2str($lui, "L");
}

# converts a numeric id to a SUI
sub makeSUI {
  my($class, $sui) = @_;
  return $class->ui2str($sui, "S");
}

# converts a numeric id to a AUI
sub makeAUI {
  my($class, $aui) = @_;
  return $class->ui2str($aui, "A");
}

# converts numeric argument into a CUI/LUI/SUI/AUI padded string
sub ui2str {
  my($class, $id, $letter) = @_;
  return $id if $id =~ /^$letter[0-9]{7}$/;
  return sprintf("%s%.7d", $letter, $id) if $id =~ /^\d*$/;
  return $id;
}

# returns some randomly selected concept_ids in an array
# if sources are specified only those concepts having atoms from
# these sources are included
sub random_concepts {
  my($class, $dbh, $howmany, $sourceref) = @_;
  my($sql);

  $howmany = 10 unless $howmany;

  if ($sourceref && @$sourceref>0) {
# the foreign/english distinction is not very clean.  We need to search both classes and foreign_classes
    my(@foreign_sources, @english_sources, $source);

    foreach $source (@$sourceref) {
      $_ = $dbh->quote($class->makeVersionedSAB($dbh, $source));
      $sql = "select count(*) as c from classes where source=$_ and rownum<2";
      if ($dbh->selectFirstAsScalar($sql) == 0) {
	push @foreign_sources, $source;
      } else {
	push @english_sources, $source;
      }
    }

    $foreign_sources = join(',', map { $dbh->quote($_) } map { $class->makeVersionedSAB($dbh, $_) } @foreign_sources);
    $english_sources = join(',', map { $dbh->quote($_) } map { $class->makeVersionedSAB($dbh, $_) } @english_sources);

    $sql1 = <<"EOD" if @english_sources;
select distinct concept_id from classes
where  source in ($english_sources)
EOD
    $sql2 = <<"EOD" if @foreign_sources;
select distinct concept_id from foreign_classes
where  source in ($foreign_sources)
EOD
    if ($sql1 && $sql2) {
      $sql = <<"EOD";
select * from (select * from ($sql1 union $sql2) order by dbms_random.value) where rownum<=$howmany
EOD
    } elsif ($sql1) {
      $sql = <<"EOD";
select * from (select * from ($sql1) order by dbms_random.value) where rownum<=$howmany
EOD
    } elsif ($sql2) {
      $sql = <<"EOD";
select * from (select * from ($sql2) order by dbms_random.value) where rownum<=$howmany
EOD
    }
  } else {
    $sql = "select * from (select concept_id from concept_status order by dbms_random.value) where rownum<=$howmany";
  }
  return $dbh->selectAllAsArray($sql);
}

# returns some randomly selected atoms in an array
# if sources are specified only atoms from
# these sources are included
sub random_atom_ids {
  my($class, $dbh, $howmany, $sourceref) = @_;
  my($sql, $sourcesql);

  $howmany = 10 unless $howmany;

  if ($sourceref && @$sourceref>0) {
    $sourcesql = " where source in (" . join(',', map { $dbh->quote($_) } map { $class->makeVersionedSAB($dbh, $_) } @$sourceref) . ")";
  }
  $sql = <<"EOD";
select * from
(
 select atom_id from classes $sourcesql order by dbms_random.value
) where rownum<=$howmany
EOD
  return $dbh->selectAllAsArray($sql);
}

# returns the current known list of sources
# returns versioned names or versionless names
sub getSources {
  my($class, $dbh, $versioned) = @_;
  my($sql);

  if ($versioned) {
    $sql = "select current_name from source_version where current_name is not null order by current_name";
  } else {
    $sql = "select source from source_version where current_name is not null order by source";
  }
  return $dbh->selectAllAsArray($sql);
}

# returns the list of sources with atoms in classes (no foreign_classes)
# returns versioned names or versionless names
sub getClassesSources {
  my($class, $dbh, $versioned) = @_;
  my($sql);
  my($source, $rsab, $vsab, @sources);

  $sql = "select current_name from source_version where current_name is not null";
  foreach $source ($dbh->selectAllAsArray($sql)) {
    $rsab = $class->makeVersionlessSAB($dbh, $source);
    $vsab = $class->makeVersionedSAB($dbh, $source);
    $_ = $dbh->quote($vsab);
    $sql = "select count(*) as c from classes where source=$_ and rownum<2";
    if ($dbh->selectFirstAsScalar($sql) > 0) {
      push @sources, ($versioned ? $vsab : $rsab);
    }
  }
  return sort @sources;
}

# maps a versioned source name to a versionless one; no-op if passed a versionless SAB
sub makeVersionlessSAB {
  my($class, $dbh, $sab) = @_;
  my($sql, $rsab);

  if (ref($sab) eq "ARRAY") {
    my($s, @s);
    foreach $s (@$sab) {
      push @s, $class->makeVersionlessSAB($dbh, $s);
    }
    return @s;
  } elsif (ref($sab) eq "HASH") {
    my($s, @s);
    foreach $s (keys %$sab) {
      push @s, $class->makeVersionlessSAB($dbh, $s);
    }
    return @s;
  }

  $sql = "select source from source_version where current_name=" . $dbh->quote($sab);
  $rsab = $dbh->selectFirstAsScalar($sql);
  return $rsab if $rsab;
  return $sab;
}

# maps a versionless source name to a versionled one; no-op if passed a versioned SAB
sub makeVersionedSAB {
  my($class, $dbh, $sab) = @_;
  my($sql, $vsab);

  if (ref($sab) eq "ARRAY") {
    my($s, @s);
    foreach $s (@$sab) {
      push @s, $class->makeVersionedSAB($dbh, $s);
    }
    return @s;
  } elsif (ref($sab) eq "HASH") {
    my($s, @s);
    foreach $s (keys %$sab) {
      push @s, $class->makeVersionedSAB($dbh, $s);
    }
    return @s;
  }

  $sql = "select current_name from source_version where source=" . $dbh->quote($sab);
  $vsab = $dbh->selectFirstAsScalar($sql);
  return $vsab if $vsab;
  return $sab;
}

# returns the offical name of a source from the SRC/VPT atom
sub officialSABName {
  my($class, $dbh, $vsab) = @_;
  my($sql, $q, $concept_id, $fullname);

  $vsab = $class->makeVersionedSAB($dbh, $vsab);
  $q = $dbh->quote($vsab);
  $sql = <<"EOD";
select /* +PARALLEL(cr) */ distinct concept_id from classes c, atoms a
where  c.source='SRC'
and    c.termgroup='SRC/VAB'
and    c.atom_id=a.atom_id
and    a.atom_name=$q
EOD
  $concept_id = $dbh->selectFirstAsScalar($sql);

  if ($concept_id) {
    $sql = <<"EOD";
select a.atom_name from classes c, atoms a
where  c.concept_id=$concept_id
and    c.termgroup='SRC/VPT'
and    c.atom_id=a.atom_id
EOD
    $fullname = $dbh->selectFirstAsScalar($sql);

  } else {

    $fullname = "";
  }
  return $fullname;
}

# returns the insertion date of the SRC/VAB atom
sub sourceInsertionDate {
  my($class, $dbh, $vsab) = @_;
  my($sql, $q, $concept_id);
  my($insertion_date);

  $vsab = $class->makeVersionedSAB($dbh, $vsab);
  $q = $dbh->quote($vsab);

  $sql = <<"EOD";
select c.insertion_date from classes c, atoms a
where  c.termgroup='SRC/VAB'
and    c.atom_id=a.atom_id
and    a.atom_name=$q
EOD
  $insertion_date = $dbh->selectFirstAsScalar($sql);
  return $insertion_date;
}

# -----------------------------
1;
