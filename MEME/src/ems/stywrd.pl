#!@PATH_TO_PERL@

# Looks for concepts with given combinations of STY and words.  Has a simple
# expression parser for word combinations.
# suresh@nlm.nih.gov 8/2001
# suresh@nlm.nih.gov 5/2005 - EMS-3 mods

# Options:
# -d database
# -e <expression to evaluate>
# -c configuration file containing the expression
# -x (show only the count of matching concepts)
# -g (debug)
#
# Expression BNF:
#
# <expr> ::= <expr> <op> <expr>
# <expr> ::= "(" <expr> ")"
# <expr> ::= <literal>
# <op> ::= {"&" | "|" | "&&" | "||"}
# <literal> ::= {"sty=.*" | "word=.*" | "normword=.*" | "str=.*" | "normstr=.*" | src=.* | srcfamily=.* | tty=.* }

# Expression consists of terms linked by boolean operators & (or &&) and | (or ||).  Use parenthesis
# to alter default precedence in evaluation.  Terms can be:
#   sty=, word=, str=, normword=, normstr=, src=, srcfamily=
#   negation can be expressed with !=, i.e., word!=dog
#   For STYs, you can use CHEM and NONCHEM to mean all chemical STYs and non-chemical STYs
# Examples:
# -e '(sty=Plant & normword=homeopathic)'
# -e '((sty=Plant|sty=Alga) & (word!=dog))'

# the positive terms are existentially qualified (i.e., there exists an STY that matches..) while the
# negation is universally qualified (i.e., there are no STYs that match..) as is usual.

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use OracleIF;
use EMSUtils;
use GeneralUtils;
use ZipUtils;
use Midsvcs;
use EMSNames;
use EMSTables;
use LVG;

use File::Basename;
use Getopt::Std;

getopts("d:c:e:gx");
$starttime = time;

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "Cannot connect to $db" unless $dbh;

@slots = qw(sty word str normword normstr src srcfamily tty);

if ($opt_e) {
  $expression = $opt_e;
} elsif ($opt_c) {
  $configfile = join("/", $ENV{EMS_HOME}, "etc", $opt_c);
  open(CONFIG, $configfile) || die "Need a config file in -c or an expression in -e\n";
  while (<CONFIG>) {
    chomp;
    next if /^\#/ || /^\s*$/;
    $expression .= $_;
  }
  close(CONFIG);
} else {
  die "Need an expression in -e or in a config file -c.\n";
}

%precedence = ('|' => 1, '&' => 2, '(' => 3, ')' => 3);
%isOperator = ('&' => 1, '|' => 1, '(' => 1, ')' => 1);

$sql = 'select distinct STY_RL from SRDEF where RT=' . $dbh->quote("STY");
foreach ($dbh->selectAllAsArray($sql)) {
  $legitsty{$_}++;
}
$legitsty{'chem'}++;
$legitsty{'CHEM'}++;
$legitsty{'Chem'}++;
$legitsty{'nonchem'}++;
$legitsty{'Nonchem'}++;
$legitsty{'NONCHEM'}++;

# parse expression
&tokenize($expression, \@tokens);
$parsetree = &parse_expression(@tokens);
&check_tree($parsetree);
$table = &walk_tree($parsetree);

if ($opt_x) {
  $sql = "SELECT COUNT(*) FROM $table";
} else {
  $sql = "SELECT DISTINCT concept_id FROM $table ORDER BY concept_id";
}
&dodirectives({ 'type' => 'SELECT', 'value' => $sql });
&dodirectives({ 'type' => 'DROP', 'value' => $table });
exit 0;

# The expression is represented as a parse tree with a boolean
# operator at the non-leaf nodes and terms at the leaf nodes.
sub parse_expression {
  my(@tokens) = @_;
  local(@operandStack);
  local(@operatorStack);
  my($token);

  foreach $token (@tokens) {
    $token = '&' if $token eq '&&';
    $token = '|' if $token eq '||';

    if ($isOperator{$token}) {
      if ($token eq ')') {
	while ($operatorStack[$#operatorStack] ne '(') {
	  &reduce;
	  last if $error;
	}
	pop @operatorStack;
      } else {
	while (@operatorStack &&
	       $operatorStack[$#operatorStack] ne '(' &&
	       $precedence{$token} <= $precedence{$operatorStack[$#operatorStack]}) {
	  &reduce;
	  last if $error;
	}
	push @operatorStack, $token;
      }
    } else {
      push @operandStack, $token;
    }
  }

  die $error if $error;

  while (@operatorStack) {
    &reduce;
    die $error if $error;
  }

  if (@operandStack != 1) {
    die "Error in expression\n";
  }
  return $operandStack[0];
}

sub reduce {
  my($operator, $operandLeft, $operandRight);

  $operator = pop @operatorStack;
  $operandRight = pop @operandStack;
  $operandLeft = pop @operandStack;

  $error = "Unknown operator: \"$operator\"" unless $isOperator{$operator};
  $error = "Missing operand(s)" unless $operandLeft && $operandRight;
  push @operandStack, [ $operandLeft, $operator, $operandRight ];
}

sub check_tree {
  my($tree) = @_;

  if (ref $tree) {
    check_tree($tree->[0]);
    die "Bad operator: $tree->[1]\n" unless $isOperator{$tree->[1]};
    check_tree($tree->[2]);
  } else {
    @_ = split /\!?=/, $tree;
    if (lc($_[0]) eq "sty") {
      $_[1] =~ s/\s*$//;
      die "Sorry can't do all STYs!\n" if ($_[1] eq "*");
      die "Sorry don't know what that could mean ($_[1])!\n" if (lc($_[1]) eq "chem*" || lc($_[1]) eq "nonchem*");
      $_[1] =~ s/\*$//;
      die "Missing argument in \"$tree\"?\n" if (@_ > 2 || @_ < 1);
      die "Typo in STY name? ($_[1])\n" if $_[1] && !$legitsty{$_[1]};
    } else {
      die "Missing argument for $_[0] in \"$tree\"?\n" if (@_ != 2);
    }
    my($slot) = $_[0];
    $slot =~ tr/A-Z/a-z/;
    die "Bad slot: $slot\n" unless grep { $_ eq $slot } @slots;
  }
}

# evaluates expression
sub walk_tree {
  my($tree) = @_;
  my($newtable);
  my(@Directives);

  if (ref $tree) {
    my($t1) = &walk_tree($tree->[0]);
    my($t2) = &walk_tree($tree->[2]);

    push @Directives, { 'type' => 'DROP', 'value' => $newtable };

    if ($tree->[1] eq '&') {

      $newtable = &name("styAND");

      push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $newtable AS
SELECT DISTINCT t1.concept_id FROM $t1 t1, $t2 t2
WHERE  t1.concept_id=t2.concept_id
EOD

    } else {

      $newtable = &name("styOR");

      push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $newtable AS
SELECT DISTINCT concept_id FROM $t1
UNION
SELECT DISTINCT concept_id FROM $t2
EOD

    }

    push @Directives, { 'type' => 'DROP', 'value' => [ $t1, $t2 ] };

    &dodirectives(\@Directives);

  } else {
    $newtable = &evaluate($tree);
  }
  return $newtable;
}

# returns the table where the results are
sub evaluate {
  my($x) = @_;
  my($ref);

  $_ = $x;
  s/^\s*//;
  s/\s*$//;

  my($slot, $neg, $value) = split /([\!]?)=/, $_;
  $slot =~ tr/A-Z/a-z/;

  if ($slot eq "sty") {
    $ref = &stysql($value, $neg);
  } elsif ($slot eq "word") {
    $ref = &wordsql($value, $neg);
  } elsif ($slot eq "normword") {
    $ref = &normwordsql($value, $neg);
  } elsif ($slot eq "str") {
    $ref = &strsql($value, $neg);
  } elsif ($slot eq "normstr") {
    $ref = &normstrsql($value, $neg);
  } elsif ($slot eq "src") {
    $ref = &srcsql($value, $neg);
  } elsif ($slot eq "srcfamily") {
    $ref = &srcsql($value, $neg, "family");
  } elsif ($slot eq "tty") {
    $ref = &ttysql($value, $neg);
  } else {
    die "Unknown directive: $_\n";
  }

  &dodirectives($ref->[1]);
  return $ref->[0];
}

# these return a reference to an array containing the table name and the SQL
sub stysql {
  my($sty, $neg) = @_;
  my($table) = &name("stysql");
  my(@Directives);

  if (lc($sty) eq "chem") {
    return ($neg ? &nonchemdirectives : &chemdirectives);
  } elsif (lc($sty) eq "nonchem") {
    return ($neg ? &chemdirectives : &nonchemdirectives);
  }

  push @Directives, { 'type' => 'DROP', 'value' => $table };

  if ($neg) {
    my($index) = &name("stysqlX");

    if ($sty eq "") {
      push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS SELECT DISTINCT concept_id FROM attributes
WHERE  attribute_name='SEMANTIC_TYPE'
AND    tobereleased IN ('y', 'Y')
EOD
    } else {

      push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS SELECT concept_id FROM concept_status
EOD

      push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE INDEX $index ON $table(concept_id)
EOD

      my($x) = "";
      if ($sty =~ /\*$/) {
	$sty =~ s/\*$//;
	@_ = &explode_sty($sty);
	if (@_ > 1) {
	  $x = "attribute_value in (" . join(', ', map { "'$_'" } @_) . ")";
	} else {
	  $x = "attribute_value=\'$sty\'";
	}
      } else {
	$x = "attribute_value=\'$sty\'";
      }

      push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
DELETE FROM $table
WHERE  concept_id IN
(SELECT concept_id FROM attributes
	WHERE  attribute_name='SEMANTIC_TYPE'
        AND $x
        AND    tobereleased IN ('y', 'Y'))
EOD
    }

  } else {

    if ($sty eq "") {
      my($index) = &name("styIndexX");
      push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS SELECT concept_id FROM concept_status
EOD

      push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
DELETE FROM $table
WHERE  concept_id IN
(SELECT DISTINCT concept_id FROM attributes
WHERE  attribute_name='SEMANTIC_TYPE'
AND    tobereleased IN ('y', 'Y'))
EOD
    } else {

      my($x) = "";
      if ($sty =~ /\*$/) {
	$sty =~ s/\*$//;
	@_ = &explode_sty($sty);
	if (@_ > 1) {
	  $x = "attribute_value in (" . join(', ', map { "'$_'" } @_) . ")";
	} else {
	  $x = "attribute_value=\'$sty\'";
	}
      } else {
	$x = "attribute_value=\'$sty\'";
      }

      push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS
SELECT DISTINCT concept_id FROM attributes
WHERE  attribute_name='SEMANTIC_TYPE'
AND    $x
EOD
    }
  }
  return [ $table, \@Directives ];
}

sub chemdirectives {
  my($chemtable) = &name("chemstysql");
  my($chemindex) = &name("chemstysqlX");
  my(@Directives);

  push @Directives, { 'type' => 'DROP', 'value' => $chemtable };
  push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $chemtable AS
SELECT DISTINCT concept_id FROM attributes
WHERE  attribute_name = 'SEMANTIC_TYPE'
AND    attribute_value IN (SELECT semantic_type FROM semantic_types WHERE is_chem = 'Y')
AND    tobereleased IN ('y', 'Y')
EOD

  push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE INDEX $chemindex ON $chemtable(concept_id)
EOD

  return [ $chemtable, \@Directives ];
}

sub nonchemdirectives {
  my($nonchemtable) = &name("nonchemstysql");
  my($chemtable) = &name("nonchemstysql");
  my(@Directives);

  push @Directives, { 'type' => 'DROP', 'value' => [ $nonchemtable, $chemtable ] };
  push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $chemtable AS
SELECT DISTINCT concept_id FROM attributes
WHERE  attribute_name = 'SEMANTIC_TYPE'
AND    attribute_value IN (SELECT semantic_type FROM semantic_types WHERE is_chem = 'Y')
AND    tobereleased IN ('y', 'Y')
EOD

  push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $nonchemtable AS SELECT concept_id FROM concept_status
EOD

  push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
DELETE FROM $nonchemtable WHERE concept_id IN (SELECT concept_id FROM $chemtable)
EOD

  push @Directives, { 'type' => 'DROP', 'value' => $chemtable };

  return [ $nonchemtable, \@Directives ];
}

sub wordsql {
  my($word, $neg) = @_;
  my($table) = &name("wordsql");
  my(@Directives);

  push @Directives, { 'type' => 'DROP', 'value' => $table };

  if ($neg) {
    my($index) = &name("wordsqlX");

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS SELECT DISTINCT concept_id FROM concept_status
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE INDEX $index ON $table(concept_id)
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
DELETE FROM $table
WHERE   concept_id IN
(SELECT DISTINCT c.concept_id FROM word_index w, classes c
	WHERE  w.word=\'$word\'
	AND    w.atom_id=c.atom_id
        AND    c.tobereleased IN ('y', 'Y'))
EOD

  } else {

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS
SELECT DISTINCT c.concept_id FROM word_index w, classes c
WHERE  w.word = \'$word\'
AND    w.atom_id=c.atom_id
AND    c.tobereleased IN ('y', 'Y')
EOD
  }

  return [ $table, \@Directives ];
}

sub normwordsql {
  my($word, $neg) = @_;
  my($table) = &name("normwordsql");
  my($normword) = LVG->luinorm($word);
  my(@Directives);

  push @Directives, { 'type' => 'DROP', 'value' => $table };

  if ($neg) {
    my($index) = &name("normwordsqlX");

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS SELECT DISTINCT concept_id FROM concept_status
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE INDEX $index ON $table(concept_id)
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
DELETE FROM $table
WHERE   concept_id IN
(SELECT DISTINCT c.concept_id FROM normwrd n, classes c
	WHERE  n.normwrd=\'$normword\'
	AND    n.normwrd_id=c.atom_id
        AND    c.tobereleased IN ('y', 'Y'))
EOD

  } else {

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS
SELECT DISTINCT c.concept_id FROM normwrd n, classes c
WHERE  n.normwrd = \'$normword\'
AND    n.normwrd_id=c.atom_id
AND    c.tobereleased IN ('y', 'Y')
EOD
  }

  return [ $table, \@Directives ];
}

sub strsql {
  my($str, $neg) = @_;
  my($table) = &name("strsql");
  my(@Directives);

  push @Directives, { 'type' => 'DROP', 'value' => $table };

  if ($neg) {
    my($index) = &name("strsqlX");

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS SELECT DISTINCT concept_id FROM concept_status
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE INDEX $index ON $table(concept_id)
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
DELETE FROM $table
WHERE   concept_id IN
(SELECT DISTINCT c.concept_id FROM atoms a, classes c
	WHERE  a.atom_name=\'$str\'
	AND    a.atom_id=c.atom_id
        AND    c.tobereleased IN ('y', 'Y'))
EOD

  } else {

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS
SELECT DISTINCT c.concept_id FROM atoms a, classes c
WHERE  a.atom_name = \'$str\'
AND    a.atom_id=c.atom_id
AND    c.tobereleased IN ('y', 'Y')
EOD
  }

  return [$table, \@Directives];
}

sub normstrsql {
  my($str, $neg) = @_;
  my($table) = &name("normstrsql");
  my($normstr) = LVG->luinorm($str);
  my(@Directives);

  push @Directives, { 'type' => 'DROP', 'value' => $table };

  if ($neg) {
    my($index) = &name("normstrsqlX");

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS SELECT DISTINCT concept_id FROM concept_status
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE INDEX $index ON $table(concept_id)
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
DELETE FROM $table
WHERE   concept_id IN
(SELECT DISTINCT c.concept_id FROM normstr n, classes c
	WHERE  n.normstr=\'$normstr\'
	AND    n.normstr_id=c.atom_id
        AND    c.tobereleased IN ('y', 'Y'))
EOD

  } else {

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS
SELECT DISTINCT c.concept_id FROM normstr n, classes c
WHERE  n.normstr = \'$normstr\'
AND    n.normstr_id=c.atom_id
AND    c.tobereleased IN ('y', 'Y')
EOD
  }

  return [$table, \@Directives];
}

sub srcsql {
  my($src, $neg, $family) = @_;
  my($table) = &name("srcsql");
  my(@Directives);

  $src =~ s/^\s*//;
  $src =~ s/\s*$//;
  $src = MIDUtils->makeVersionedSAB($dbh, $src);

  push @Directives, { 'type' => 'DROP', 'value' => $table };

  if ($neg) {
    my($index) = &name("srcsqlX");

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS SELECT concept_id FROM concept_status
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE INDEX $index ON $table(concept_id)
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
DELETE FROM $table
WHERE   concept_id IN
(SELECT DISTINCT concept_id FROM classes
	WHERE  source=\'$src\'
        AND    tobereleased IN ('y', 'Y'))
EOD

  } else {

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS
SELECT DISTINCT concept_id FROM classes
WHERE  source=\'$src\'
AND    tobereleased IN ('y', 'Y')
EOD
  }

  return [$table, \@Directives];
}

sub ttysql {
  my($tty, $neg) = @_;
  my($table) = &name("ttysql");
  my(@Directives);

  push @Directives, { 'type' => 'DROP', 'value' => $table };

  if ($neg) {
    my($index) = &name("ttysqlX");

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS SELECT concept_id FROM concept_status
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE INDEX $index ON $table(concept_id)
EOD
    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
DELETE FROM $table
WHERE   concept_id IN
(SELECT DISTINCT concept_id FROM classes
	WHERE  termgroup like \'%/$tty\'
        AND    tobereleased IN ('y', 'Y'))
EOD

  } else {

    push @Directives, { 'type' => 'SQL', 'value' => <<"EOD" };
CREATE TABLE $table AS
SELECT DISTINCT concept_id FROM classes
WHERE  termgroup like \'%/$tty\'
AND    tobereleased IN ('y', 'Y')
EOD
  }

  return [$table, \@Directives];
}

sub dodirectives {
  my($ref) = @_;

  if (ref($ref) eq "SCALAR") {
    &dodirectives($$ref);
  } elsif (ref($ref) eq "HASH") {

    if (($ref->{'type'} eq "DROP") && (defined $ref->{'value'})) {
      &drop($ref->{'value'});
    } elsif (($ref->{'type'} eq "SELECT") && (defined $ref->{'value'})) {
      &select2stdout($ref->{'value'});
    } elsif (($ref->{'type'} eq "SQL") && (defined $ref->{'value'})) {
      &dodirectives($ref->{'value'});
    }

  } elsif (ref($ref) eq "ARRAY") {

    foreach (@{ $ref }) {
      &dodirectives($_);
    }

  } elsif (!ref($ref)) {
    if ($opt_g) {
      print $ref, "\n";
    } else {
      $dbh->executeStmt($ref);
    }
  }
}

sub name {
  my($base) = @_;
  $base = "stywrd" unless $base;
  $Num{$base} = $$ unless $Num{$base};
  $Num{$base}++;
  return sprintf("%s_%s_%d", $EMSNames::TMPTABLEPREFIX, $base, $Num{$base});
}

sub drop {
  my($t) = @_;
  if (ref($t) eq "SCALAR") {
    &drop($$t);
  } elsif (ref($t) eq "ARRAY") {
    foreach (@{ $t }) {
      &drop($_);
    }
  } elsif (!ref($t)) {
    if ($opt_g) {
      print "DROP TABLE $t;\n";
    } else {
      $dbh->dropTable($t);
    }
  }
}

sub select2stdout {
  my($sql) = @_;

  if ($opt_g) {
    print "$sql\n";
  } else {
    $dbh->selectToFile($sql, \*STDOUT);
  }
}

sub print_tree {
  my($tree, $indent) = @_;

  unless (ref $tree) {
    print $tree if $tree;
    return;
  }
  print $tree->[1], "(";
  if (ref $tree->[0]) {
    &print_tree($tree->[0]);
  } else {
    print $tree->[0];
  }
  print ", ";
  if (ref $tree->[2]) {
    &print_tree($tree->[2]);
  } else {
    print $tree->[2];
  }
  print ")";
}

sub dbg {
  print "Operators: ", join(' ', @operatorStack), "\n";
  print "Operands: ", join(' ', @operandStack), "\n";
}

# Breaks the expression into tokens
sub tokenize {
  my($input, $tokensRef) = @_;
  
  return unless $input;
  if ($input =~ /^(\|\|)(.*)/ || $input =~ /^(\&\&)(.*)/ || $input =~ /^([\(\)\&\|])(.*)/ || $input =~ /^([^\(\)\&\|]+)(.*)/) {
    push @{ $tokensRef }, $1;
    $input = $2;
    $input =~ s/^\s*//;
  } else {
    push @{ $tokensRef }, $input;
    $input = "";
  }
  &tokenize($input, $tokensRef);
}

# returns a list containing the STY and all its descendants
sub explode_sty {
  my($sty) = @_;
  my($sql);
  my(@results);

  $sql = <<"EOD";
SELECT DISTINCT sty1 FROM SRSTRE2 WHERE sty2=\'$sty\' AND rel='isa'
EOD
  @results = $dbh->selectAllAsArray($sql);
  push @results, $sty;
  return @results;
}

