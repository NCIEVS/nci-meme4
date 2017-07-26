# suresh@nlm.nih.gov - 8/97
# Script to aid in STY Q/A - the old "QA Collector"
# Looks for concepts with combinations of STY and words
# suresh@nlm.nih.gov 7/99
# suresh@nlm 8/00 - MEME-3 port
# EMS3 - 1/2006
# 03/26/2009 BAC (1-LKB6X): remove appending of empty string to attribute_name when comparing against SEMANTIC_TYPE value,
#      does not make a difference in Oracle10+ environment, but better in Oracle9.  Some were left unchanged as changes
#      were not specifically called for.
# Look for concepts satisfying a boolean combination of STYs and words in atoms

# CGI params:
# db=
# config=
# subaction={query|search|showsql|checklist}
# table= (a stateful table of matching concepts)
# doit=

sub do_styqa {
  my($sql);
  my($html);
  my($title) = "Semantic Type and Word Search";
  my($subaction) = $query->param('subaction') || 'query';

  my(@subactions) = qw(query search showsql checklist);

  $subaction =~ s/\s+//g;
  $subaction =~ tr/A-Z/a-z/;

  &printhtml({printandexit=>0, body=>"ERROR: unknown subsction: $subaction"}) unless grep { $_ eq $subaction } @subactions;

  &$subaction;
  return;
}

# presents a query page with Javascript
sub query {
  my($javascriptfile) =  $ENV{EMS_HOME} . "/log/styqa.js";
  my($staticjavascriptfile) =  $ENV{EMS_HOME} . "/www/js/styqa-static.js";
  my($stydata);
  my($sql);

  &printhtml({printanddie=>1, body=>"ERROR: missing static Javascript file: $staticjavascriptfile"})
    unless -e $staticjavascriptfile;

  &remove_state_tables;

  &stydata2javascript($javascriptfile);
  system "/bin/cat $staticjavascriptfile >> $javascriptfile";

# load the full Javascript
  my($javascript) = GeneralUtils->file2str($javascriptfile);

# Get all sources
  $sql = "select distinct current_name from source_version order by current_name";
  @sources = $dbh->selectAllAsArray($sql);

# Get all termtypes
  $sql = "select distinct tty from termgroup_rank order by tty";
  @ttys = $dbh->selectAllAsArray($sql);


  $html .= <<"EOD";
Submitting this form will find concepts that have the selected STYs and
whose atoms have the selected words.
All words and STYs are AND\'ed in the query.
<P>
EOD

  my(@d, @d1);

  $html .= $query->start_form(-method=>'POST', -name=>'styframe', onSubmit=>"return verify();");
  $html .= $DBpost;

  push @d1, [$query->h2("How to sort STYs?")];
#  push @d1, [$query->radio_group(
#                                -name=>'stysort',
#                                -values=>["alpha", "tree", "tui"],
#                                -default=>"alpha",
#                                -labels=>{alpha=>'Alphabetical', tree=>'Tree', tui=>'TUI'},
#                                -onClick=>"styDisplay();",
#                               )
#           ];
  push @d1, [$query->popup_menu(
                                 -name=>'stysort',
                                 -values=>["alpha", "tree", "tui"],
                                 -labels=>{alpha=>'Alphabetical', tree=>'Tree', tui=>'TUI'},
                                 -onChange=>"styDisplay();",
                                )
            ];

  push @d1, [$query->h2("Select an STY")];
  push @d1, [$query->scrolling_list(-name=>"stylist", -values=>["Disease or Syndrome plaus some random chars"], -size=>6)];
  push @d1, [
             $query->checkbox(-name=>"stynegate", -label=>"Negate STY? ") .
             $query->button(-name=>"styadd", value=>' Add STY', onClick=>"styaddfn();")
            ];

  push @d1, [$query->h2("STYs Selected")];
  push @d1, [$query->textarea(-name=>'styselected', -rows=>4, -cols=>40)];
  push @d1, [$query->button(-value=>"Clear STYs", -type=>'reset', onClick=>"reinitsty();")];

  push @d2, [$query->h2("Enter Single Word")];
  push @d2, [$query->textfield(-name=>'textfield', -size=>30, onSubmit=>"return(false);")];
  push @d2, [
             $query->checkbox(-name=>"textnegate", -label=>'Negate Word? ') .
             $query->checkbox(-name=>"textexact", -label=>'Exact Word? ') .
             $query->button(-name=>"textadd", value=>' Add Word', onClick=>"textaddfn();")
            ];

  push @d2, [$query->h2("Words Selected")];
  push @d2, [$query->textarea(-name=>'textselected', -rows=>4, -cols=>40)];
  push @d2, [$query->button(-value=>"Clear Words", -type=>'reset', onClick=>"reinittext();")];

  push @d2, [$query->h2("Restrict Matches to Source(s)")];
  push @d2, [$query->scrolling_list(-name=>"sourcelist", -values=>\@sources, -size=>4, -multiple=>1)];
  push @d2, [$query->button(-value=>"Clear Selected Sources", -type=>'reset', onClick=>"clearsources();")];
  push @status, [' ','R','N'];

  push @d2, [$query->h2("Restrict Matches to TTY(s)")];
  push @d2, [$query->scrolling_list(-name=>"ttylist", -values=>\@ttys, -size=>4, -multiple=>1)];
  push @d2, [$query->button(-value=>"Clear Selected TTY", -type=>'reset', onClick=>"clearttys();")];
  push @status, [' ','R','N'];



  push @d2,[$query->h2("Restrict Matches to Concept Status")];
  push @d2, [$query->scrolling_list(-name=>"conceptStatus", -values=>@status, -size=>3, -multiple=>0)];

  push @d3, [$query->submit(-name=>"subaction", -value=>"Search"),
             $query->submit(-name=>"subaction", -value=>"Show SQL"),
             $query->button(-name=>"clearall", -value=>"Clear All", onClick=>"reinitsty();reinittext();clearsources();clearttys();")];

  $html .= &toHTMLtable($query, {-border=>1},
                        [
                         [
                          &toHTMLtable($query, {-border=>0, -cellspacing=>5}, \@d1),
                          &toHTMLtable($query, {-border=>0, -cellspacing=>5}, \@d2)
                         ],
                         [
                          [{-colspan=>2}, &toHTMLtable($query, {-border=>0, -cellspacing=>10}, \@d3)],
                         ],
                        ]);

  $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
  $html .= $query->end_form;

  $html .= $query->script({-lang=>"JavaScript"}, $javascript);

  &printhtml({body=>$html});
}

sub search {
  my($showsql) = @_;
  my(@sources) = $query->param('sourcelist');
  my(@ttys) = $query->param('ttylist');
  my($conceptStatus) = $query->param('conceptStatus');
  my($sql);
  my($i) = 0;
  my($sty, $neg);
  my(@stytables);
  my(@stysql, @wordsql);

  my(@s); # helps with debugging
  @s = split /\n/, $query->param('styselected');
  @s = $query->param('styselected') if (@s < 2);

  foreach $sty (@s) {
    $sty =~ s/\s*$//;
    $sty =~ s/^\s*//;
    $sty =~ s/\r//g;

    $neg = ($sty =~ /^!=\s*(.*)$/) ? 1 : 0;
    $sty = $1 if $neg;

    $i++;
    $tmptable = $dbh->tempTable(sprintf("%s_styqas_%.2d", $EMSNames::TMPTABLEPREFIX, $i));

    $dbh->dropTable($tmptable);
    push @stytables, $tmptable;
    $sql = &make_sty_sql($sty, $tmptable, $neg);
    push @stysql, $sql;
  }
  $styconcepts = &reduce(\@stytables, \@stysql) if (@stytables);

  $i=0;
  foreach $word (split /\n/, $query->param('textselected')) {
    $word =~ s/\s*$//;
    $word =~ s/^\s*//;
    $word =~ s/\r//g;

    $neg = ($word =~ /^!=\s*(.*)$/) ? 1 : 0;
    $word = $1 if $neg;
    next if $seen{$word}++;

    $i++;
    $tmptable = $dbh->tempTable(sprintf("%s_styqaw_%.2d", $EMSNames::TMPTABLEPREFIX, $i));

    $dbh->dropTable($tmptable);
    push @wordtables, $tmptable;
    $sql = &make_word_sql($word, $tmptable, $neg);
    push @wordsql, $sql;
  }
  $wordconcepts = &reduce(\@wordtables, \@wordsql, 50) if @wordtables;

  foreach (@stysql, @wordsql) {
    push @sql, $_;
  }

  my($t1) = $dbh->tempTable(sprintf("%s_styqat1_%.2d", $EMSNames::TMPTABLEPREFIX));
  my($t2) = $dbh->tempTable(sprintf("%s_styqat2_%.2d", $EMSNames::TMPTABLEPREFIX));
  my($t3) = $dbh->tempTable(sprintf("%s_styqat3_%.2d", $EMSNames::TMPTABLEPREFIX));
  my($t4) = $dbh->tempTable(sprintf("%s_styqat4_%.2d", $EMSNames::TMPTABLEPREFIX));

  if (@stytables && @wordtables) {
    $t1 = &reduce([$styconcepts, $wordconcepts], \@sql, 90);
  } elsif (@stytables) {
    $t1 = $styconcepts;
  } elsif (@wordtables) {
    $t1 = $wordconcepts;
  } else {
  }

  if (@sources) {
    my($q) = join(", ", map { $dbh->quote($_) } @sources);
    $sql = <<"EOD";
create table $t2 as
select distinct a.concept_id from $t1 a, classes b
where  a.concept_id=b.concept_id
and    b.source in ($q)
EOD
    push @sql, $sql;
  } else {
    $t2 = $t1;
  }

  if (@ttys) {
    my($q) = join(", ", map { $dbh->quote($_) } @ttys);
    $sql = <<"EOD";
create table $t3 as
select distinct a.concept_id from $t2 a, classes b
where  a.concept_id=b.concept_id
and    b.tty in ($q)
EOD
    push @sql, $sql;
  } else {
    $t3 = $t2;
  }


 if ($conceptStatus =~ / /) {
  $conceptStatus = 0;
 }
 if ($conceptStatus) {
   my ($q) = join(", ", $dbh->quote($conceptStatus));
   $sql = <<"EOD";
create table $t4 as
select distinct a.concept_id from $t3 a, concept_status b
where a.concept_id = b.concept_id
and b.status in ($q)
EOD
    push @sql, $sql;
 } else {
   $t4 = $t3;

 }

  my($finaltable) = &state_table_name;

  push @sql, "create table $finaltable as select * from $t4";
  push @sql, "drop table $t1";
  push @sql, "drop table $t2";
  push @sql, "drop table $t3";

  if ($showsql) {
    &printhtml({printandexit=>1, body=>$query->pre(join("\n", @sql))});
    return;
  }

  foreach $sql (@sql) {
    chomp($sql);

    if ($sql =~ /^drop table (.*)$/) {
      $dbh->dropTable($1);
    } else {
      $dbh->executeStmt($sql);
    }
  }

  my($matches) = $dbh->selectFirstAsScalar("select count(*) from $finaltable");

  $url = $query->a({-href=>$main::EMSCONFIG{EMSURL} . "?$DBget&action=make_checklist&table_name=$finaltable"}, "Make a checklist.");
  if ($matches == 0) {
    $html .= <<"EOD";
There were no matches to the query.  Please try again.
EOD
  } elsif ($matches == 1) {
    $html .= <<"EOD";
There was one match to the query.
<P>$url
EOD
  } else {
    $html .= <<"EOD";
There were $matches matching concepts to the query.
<P>$url
EOD
  }
  &printhtml({printandexit=>1, body=>$html});
}

sub showsql {
  &search("showsql");
}

# returns the SQL for a single table that is the intersection
# of all the tables specified
sub reduce {
  my($tables, $sqlreturn, $recursionlevel) = @_;
  my($t1, $t2);
  my($sql, $intersectiontable);
  my(@newtables);

  return "" unless @$tables;
  return $tables->[0] if @$tables == 1;

  $recursionlevel = 1 unless $recursionlevel;

# other wise do pairwise intersections
  $t1 = $tables->[0];
  $t2 = $tables->[1];
  $intersectiontable = $dbh->tempTable(sprintf("%s_styqar_%.2d", $EMSNames::TMPTABLEPREFIX, $recursionlevel));

  $dbh->dropTable($intersectiontable);
  $sql = <<"EOD";
create table $intersectiontable as select distinct a.concept_id from $t1 a, $t2 b
where  a.concept_id=b.concept_id
EOD
  push @$sqlreturn, $sql;
  push @$sqlreturn, "drop table $t1\n";
  push @$sqlreturn, "drop table $t2\n";

  $recursionlevel++;

  my(@rest) = ($intersectiontable);
  for ($i=2; $i<@$tables; $i++) {
    push @rest, $tables->[$i];
  }

  if (@rest) {
    return &reduce(\@rest, $sqlreturn, $recursionlevel);
  } else {
    return $intersectiontable;
  }
}

# Makes up the SQL for STY searches
sub make_sty_sql {
  my($sty, $table, $neg) = @_;
  my($sql);
  my($q) = $dbh->quote($sty);

  if ($neg) {

    if ($sty eq "CHEM") {
      $sql = &nonchemsql($table);
    } elsif ($sty eq "NONCHEM") {
      $sql = &chemsql($table);
    } else {
      $sql = <<"EOD";
create table $table as
select concept_id from concept_status
minus
select distinct concept_id from attributes
where  attribute_name = 'SEMANTIC_TYPE'
and    attribute_value=$q
EOD
    }

  } else {

    if ($sty eq "CHEM") {
      $sql = &chemsql($table);
    } elsif ($sty eq "NONCHEM") {
      $sql = &nonchemsql($table);
    } else {
      $sql .= <<"EOD";
create table $table AS
select distinct concept_id from attributes
where  attribute_name = 'SEMANTIC_TYPE'
and    attribute_value=$q
EOD
    }
  }
  return $sql;
}

# Makes up the SQL for Word (case insensitive) searches
sub make_word_sql {
  my($word, $table, $neg) = @_;
  my($sql);
  my($q) = $dbh->quote($word);

  if ($neg) {

    $sql = <<"EOD";
create table $table as
select concept_id from concept_status
minus
select distinct concept_id from classes c, word_index w
where  w.word = $q
and    w.atom_id=c.atom_id
EOD

  } else {

    $sql .= <<"EOD";
create table $table as
select distinct concept_id from classes c, word_index w
where  w.word = $q
and    w.atom_id=c.atom_id
EOD
  }
  return $sql;
}

# concepts that have chemical STYs
sub chemsql {
    my($chemtable) = @_;

    $dbh->dropTable($chemtable);
    return <<"EOD";
create table $chemtable AS
select distinct concept_id from attributes
where  attribute_name = 'SEMANTIC_TYPE'
and    attribute_value IN (SELECT semantic_type FROM semantic_types where is_chem = 'Y');
EOD
}

# Nonchem SQL
sub nonchemsql {
    my($nonchemtable) = @_;

    $dbh->dropTable($nonchemtable);
    return <<"EOD";
create table $nonchemtable AS
select concept_id from concept_status
minus
select distinct concept_id from attributes
where  attribute_name = 'SEMANTIC_TYPE'
and    attribute_value in (select semantic_type from semantic_types where is_chem = 'Y');
EOD
}

# creates Javascript data structure for current STYs
sub stydata2javascript {
  my($javascriptfile) = @_;
  my($sql) = "select STY_RL, UI, STN_RTN from SRDEF where rt='STY'";
  my(@refs) = $dbh->selectAllAsRef($sql);
  my($r);
  my($stydata);
  my(@r);

  unlink $javascriptfile;

  push @r, '["CHEM", "_", "_" ]';
  push @r, '["NONCHEM", "_", "_" ]';

  foreach $r (@refs) {
    push @r, sprintf("[ \"%s\", \"%s\", \"%s\"]", $r->[0], $r->[1], $r->[2]);
  }

  $stydata = "var STYData = [\n\t" . join(",\n\t", @r) . "\n];\n";
  GeneralUtils->str2file($stydata, $javascriptfile);
}

sub state_table_prefix {
  return "STYQASTATE";
}

sub state_table_name {
  my($prefix) = "STYQASTATE";
  return $dbh->tempTable(sprintf("%s_%s", $EMSNames::TMPTABLEPREFIX, &state_table_prefix));
}

sub remove_state_tables {
  my($qp) = $dbh->quote('%' . &state_table_prefix . '%');
  my($staledays) = 10;
  my($sql) = <<"EOD";
select object_name from all_objects
where  object_type='TABLE'
and    object_name like $qp
and    created<(SYSDATE-$staledays)
EOD
  foreach $table ($dbh->selectAllAsArray($sql)) {
    $dbh->dropTable($table);
  }
}

sub log {
  my($m) = @_;
  open(T, ">>/tmp/foo");
  print T $m, "\n";
  close(T);
}
1;