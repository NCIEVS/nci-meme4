#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";


# Some URIS-related utilities
# suresh@nlm 3/2004

package UrisUtils;

#use lib "/site/umls/lib/perl";
#use lib "/share_nfs/perl/5.8.6/lib/site_perl";
#use lib "/share_nfs/perl/5.8.6/lib/5.8.6";
#use lib "/share_nfs/perl/5.8.6/lib";
#use lib "/umls_prod/Webapp_Root/meme4/cgi-bin";
#use lib "/umls_prod/Webapp_Root/meme4/cgi-bin/lib";
push @INC, "$ENV{EMS_HOME}/lib";
use lib "$ENV{EMS_HOME}/lib";
use lib "$ENV{URIS_HOME}/lib";

use Getopt::Std;
use XML::Simple;
use Data::Dumper;
use GeneralUtils;
use ParallelExec;
use File::Basename;

# release format RRF or ORF?
sub getReleaseFormat {
  my($self, $metadir) = @_;
  my($mrfiles) = join('/', $metadir, "MRFILES.RRF");

  if (-e $mrfiles) {
    return "RRF";
  } else {
    $mrfiles = join('/', $metadir, "MRFILES");
    return (-e $mrfiles ? "ORF" : "");
  }
}

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

# returns the columns for a table
sub getColsForTable {
  my($self, $metadir, $table) = @_;
  my($mrfiles) = $self->getPath($metadir, "MRFILES");
  my(@cols);

  open(F, $mrfiles) || return ();
  while (<F>) {
    chomp;
    @_ = split /\|/, $_;
    next unless $table eq $_[0];
    @cols = split /\,/, $_[2];
    last;
  }
  close(F);
  return @cols;
}

# returns the index of a column in a table (file)
# if $base is 1, first index is 1
sub getColIndex {
    my($self, $metadir, $table, $col, $base) = @_;
    my($mrfiles) = $self->getPath($metadir, "MRFILES");
    my($num) = ($base ? 0 : -1);

    open(F, $mrfiles) || return $num;
    while (<F>) {
      chomp;
      @_ = split /\|/, $_;
      next unless $table eq $_[0];
      @_ = split /\,/, $_[2];
      foreach (@_) {
	$num++;
	next unless $col eq $_;
	last;
      }
      last;
    }
    close(F);
    return $num;
}

# returns the names of word index files
sub getWordIndexFiles {
    my($self, $metadir) = @_;
    my($mrfiles) = $self->getPath($metadir, "MRFILES");
    my(@wd);

    open(F, $mrfiles) || return ();
    while (<F>) {
      chomp;
      @_ = split /\|/, $_;
      next unless $_[0] =~ /^MRXW/;
      push @wd, $_[0];
    }
    close(F);
    return @wd;
}

# counts fields in files
# $prop is a hash ref of:
# file=>
# table=>
# fieldindex=> (first field is 1) scalar or list ref
# field=> (scalar or list ref)
# unique=>
# restrictions=>[] list of hashrefs, e.g., {CURVER=>'Y'}
# sep=>
# groupindex=> (groups by this col in the output)
# postfilter=> a filter to apply before counting
sub fieldcount {
  my($self, $metadir, $prop) = @_;
  my($cmd);

  $prop->{file} = $self->getPath($metadir, $prop->{table}) if $prop->{table};
  $prop->{table} = basename($prop->{file}) unless $prop->{table};
  die "FILE: $prop->{file} does not exist in UrisUtils->fieldcount" unless -e $prop->{file};

  $cmd = "/bin/gawk -F\'" . ($prop->{sep} || '|') . "\' \'";
  if ($prop->{restrictions}) {
    my($r);
    my(@x);
    foreach $r (@{$prop->{restrictions}}) {
      foreach $k (keys %{$r}) {
	$_ = $self->getColIndex($metadir, $prop->{table}, $k)+1;
	push @x, "\$$_" . $r->{$k};
      }
    }
    $cmd .= join(" && ", @x);
  }

  $cmd .= "{";
  if ($prop->{field}) {
    if (ref($prop->{field}) eq "ARRAY") {
      my(@x);
      foreach (@{ $prop->{field} }) {
	$_ = $self->getColIndex($metadir, ($prop->{table} || basename($prop->{file})), $_)+1;
	push @x, $_;
      }
      $cmd .= "print " . join("\"|\"", map { "\$$_" } @x) . "}";

    } else {
      $_ = $self->getColIndex($metadir, ($prop->{table} || basename($prop->{file})), $prop->{field})+1;
      $cmd .= "print \$$_" . "}";
    }

  } elsif ($prop->{fieldindex}) {

    if (ref($prop->{fieldindex}) eq "ARRAY") {
      $cmd .= "print " . join("\"|\"", map { "\$$_" } @{$prop->{fieldindex}}) . "}";
    } else {
      $cmd .= "print \$" . $prop->{fieldindex} . "}";
    }
  } else {
    $cmd .= "print \$0}";
  }

  $cmd .= "\' " . $prop->{file};
  $cmd .= "|" . $prop->{postfilter} if $prop->{postfilter};
  $cmd .= "|/bin/sort -u" if $prop->{unique};

  if ($prop->{groupindex}) {
    my($i) = $prop->{groupindex};
    my(%g);
    $cmd .= "|/bin/gawk -F'|' '{print \$$i}'|/bin/sort|/bin/uniq -c|";
    open(C, $cmd) || die "Cannot open pipe to $cmd\n";
    while (<C>) {
      chomp;
      s/^\s*//;
      s/\s*$//;
      @_ = split /\s+/, $_;
      $g{$_[1]} = $_[0];
    }
    close(C);
    return \%g;

  } else {
    $cmd .= "|/bin/wc -l|/bin/awk '{print \$1}'|";
    open(C, $cmd) || die "Cannot open pipe to $cmd\n";
    $_ = <C>;
    if ($_) {
      chomp;
      $lines = $_;
    } else {
      $lines = 0;
    }
    close(C);
  }
  return $lines;
}

# loads the URIS config file and returns all the individual config structures
# in an array
sub loadUrisConfig {
  my($self, $config) = @_;
  my(@releaseconfig);
  my($dir) = dirname($config);

  if (-e $config) {
    open(F, $config) || die "Cannot open $config";
    while (<F>) {
      chomp;
      next if /^\s*$/ || /^\#/;
      push @releaseconfig, $self->loadReleaseConfig("$dir/$_");
    }
    close(F);
  }
  return @releaseconfig;
}

# reads the XML for a release's configuration and returns a list
# reference containing the script hashes
sub loadReleaseConfig {
  my($self, $configfile) = @_;
  my($x) = new XML::Simple(rootname=>'urisconfig', keyattr=>'script');
  my($z) = $x->XMLin($configfile);
  $z->{configfile} = $configfile;
  return $z;
}

# reads the client side configuration XML and returns the actions
sub loadClientConfig {
  my($self, $configfile) = @_;
  my($x) = new XML::Simple(rootname=>'urisactions', keyattr=>'action');
  my($z) = $x->XMLin($configfile);
  return $z;
}

sub evalfile {
  my($self, $file) = @_;

  @_ = ();
  open(F, $file) || return undef;
  while (<F>) {
    push @_, $_;
  }
  close(F);
  return eval(join("\n", @_));
}

sub make_url {
  my($self, $q, $p) = @_;
  my($u) = $q->url();

  if ($p) {
    $u .= "?" . join("&", map { $_ . "=" . $p->{$_} } keys %{$p});
  }
  return $u;
}

# makes hidden fields
sub make_hidden {
  my($self, $cgi, $r) = @_;
  my($html);

  if (ref($r)) {
    if (ref($r) eq "HASH") {
      foreach (keys %{ $r }) {
	$html .= $cgi->hidden(-name=>$_, -values=>$r->{$_}, -override=>1);
      }
    } elsif (ref($r) eq "ARRAY") {
      for ($i=0; $i<@{$r}-1; $i++) {
	$_[0] = $r->[$i];
	$_[1] = $r->[$i+1];
	$html .= $cgi->hidden(-name=>$_[0], -values=>$_[1], -override=>1);
      }
    } elsif (ref($r) eq "SCALAR") {
      foreach (split /\&/, $$r) {
	@_ = split /\=/, $_;
	$html .= $cgi->hidden(-name=>$_[0], -values=>$_[1], -override=>1);
      }
    }
  } else {
    foreach (split /\&/, $r) {
      @_ = split /\=/, $_;
      $html .= $cgi->hidden(-name=>$_[0], -values=>$_[1], -override=>1);
    }
  }
  return $html;
}

sub switch_version {
  my($self, $query, $releaseconfig) = @_;
  my($html);
  my(@releaseconfig);

  unless ($releaseconfig) {
    @releaseconfig = UrisUtils->loadUrisConfig($urisconfig);
    $releaseconfig = \@releaseconfig;
  }

  $html .= $query->startform;
  $html .= "Switch to: ";

  my(@values) = sort { $b cmp $a } map { $_->{configfile} } @{ $releaseconfig };
  my(%labels) = map { $_->{configfile} => join(': ', $_->{releaseversion}, $_->{description}) } @{ $releaseconfig };
  $html .= $query->scrolling_list(-name=>'configfile', -values=>\@values, -labels=>\%labels, -size=>1, -onChange=>'submit();');
  $html .= $query->endform;
  return $html;
}

# Loads the block names by parsing NamesList.txt - returns array ref
sub parse_nameslist {
  my($self) = @_;
  my($blocks) = [];
  my($nameslistfile) = "$ENV{URIS_HOME}/etc/NamesList.txt";
  my($current, $nextI, $nextC, $blockname);
  my($i);

  open(F, $nameslistfile) || die "Cannot open $nameslistfile";
  while (<F>) {
    chomp;
    if (/^@@\t([^\t]+)\t([^\t]+)\t([^\t]+)$/) {

      if ($current) {
	for ($i=$nextI; $i<=$current->{end}; $i++) {
	  push @{ $current->{missing} }, $i;
	}
      }

      $nextI = hex($1);
      $blockname = $2;
      $end = $3;
      push @{ $blocks }, $current if $current;

      $current = {};
      $current->{name} = $blockname;
      $current->{startI} = $nextI;
      $current->{startH} = $1;
      $current->{endI} = hex($end);
      $current->{endH} = $end;
      $current->{missingI} = [];
      $current->{missingH} = [];
      $current->{notacharacterI} = [];
      $current->{notacharacterH} = [];
      $current->{reservedI} = [];
      $current->{reservedH} = [];

    } elsif (/([\dA-F]+)\t(.*)$/) {
      $foundH = $1;
      $foundI = hex($1);
      $name = $2;

      while ($nextI<$foundI) {
	push @{ $current->{missingI}}, $nextI;
	push @{ $current->{missingH}}, sprintf("%X", $nextI);
	$nextI++;
      }
      if ($name eq "<reserved>") {
	push @{ $current->{reservedI} }, $foundI;
	push @{ $current->{reservedH} }, $foundH;
      } elsif ($name eq "<not a character>") {
	push @{ $current->{notacharacterI} }, $foundI;
	push @{ $current->{notacharacterH} }, $foundH;
      }
      $nextI = $foundI+1;
    }
  }
  close(F);
  push @{ $blocks }, $current if $current;
  return $blocks;
}

# returns all block names
sub block_names {
  my($self, $blocks) = @_;

  $blocks = &parse_namelist unless $blocks;
  return GeneralUtils->uniquesort(map { $_->{name} } @$blocks);
}

# What block is a char (as integer) in?
# returns [block name, [features, e.g., missing, not a char]]
sub char2block {
  my($self, $blocks, $char) = @_;
  my(%features);
  my($r, $c);

  foreach $r (@$blocks) {
    if ($char >= $r->{startI} && $char <= $r->{endI}) {
      foreach $c (@{ $r->{notacharacterI} }) {
	$features{'Not a character'}++ if $c == $char;
      }
      foreach $c (@{ $r->{missingI} }) {
	$features{'Missing'}++ if $c == $char;
      }
      foreach $c (@{ $r->{reservedI} }) {
	$features{'Reserved'}++ if $c == $char;
      }
      return [$r->{name}, [ keys %features ]];
    }
  }
  return undef;
}

1;
