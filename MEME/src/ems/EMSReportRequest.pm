# Request for concept reports

# Requests are text property files stored in the reports directory
# When a request is satisfied by the report daemon, they are
# archived in the same directory (YYYY.zip)

# suresh@nlm.nih.gov 3/2005

package EMSReportRequest;

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use EMSNames;
use EMSTables;
use MIDUtils;
use WMSUtils;
use File::Basename;
use Data::Dumper;


$REQUESTDIR = $ENV{EMS_LOG_DIR}. "/requests";
$PREFIX = "request";

# A report request is a request stored in a file (has to be DB independent)
# as a properties file.

# Known properties

# where is the input
# worklist=
# checklist=
# list=

# status=
# errmsg=
# reporttype=
# reportfile=
#
# freezefile=
# whentorun=
# ordernum=
# requesttime=
# requestdate=
# requestuser=
# requestip=
# timestarted=
# datestarted
# timeended=
# dateended=
#
# mailto=
# mailmessage=
# domain=
#
# db=
# service=
# env_home=
# env_file=
# meme_host=
# meme_port=
#
# maxreviewedrels=
# lat=
# outputformat=
# formfeed=
# r=
# x=

# returns the oldest request that can be fulfilled
sub getOldest {
  my($class) = @_;
  my(@requests) = $class->getAllRequests();
  return @requests > 0 ? $requests[0] : undef;
}

# returns the next eligible request given a request
sub next {
  my($class, $current) = @_;
  my($request);

  foreach $request ($class->getAllRequests()) {
    next if $request->{whentorun} < $current->{whentorun};
    next if $request->{ordernum} <= $current->{ordernum};
    next if $request->{freezefile} eq $current->{freezefile};
    return $request;
  }
  return undef;
}

# returns all pending requests in an array of refs
sub getAllRequests {
  my($class) = @_;
  my($file, @files);
  my(@requests);

  opendir(D, $REQUESTDIR) || die "Cannot open $REQUESTDIR";
  foreach (readdir(D)) {
    $file = join("/", $REQUESTDIR, $_);
    next unless -f $file && $class->isFrozen($file);
    push @files, $file;
  }
  closedir(D);

  @requests = map { $class->thaw($_) } @files;
  @requests = grep { $_->{whentorun} <= time && $_->{status} eq "WAITING" } @requests;
  @requests = sort { $a->{whentorun} <=> $b->{whentorun} && $a->{ordernum} <=> $b->{ordernum} } @requests;

  return @requests;
}

# Gets all requests from $from to $to regardless of status
sub getAllRequestsIgnoreStatus {
  my($class, $from, $to) = @_;
  my($file, @files);
  my(@requests);
  my($n);

  opendir(D, $REQUESTDIR) || die "Cannot open $REQUESTDIR";
  foreach (readdir(D)) {
    $file = join("/", $REQUESTDIR, $_);
    next unless -f $file && $class->isFrozen($file);
    $n++;
    next unless $n >= $from && $n < $to;
    push @files, $file;
  }
  closedir(D);

  @requests =
    sort { $a->{whentorun} <=> $b->{whentorun} && $a->{ordernum} <=> $b->{ordernum} }
    map { $class->thaw($_) } @files;
  return @requests;
}

# writes a request to disk
sub freeze {
  my($class, $request) = @_;
  my(@x);

  $request->{ordernum} = 1 unless $request->{ordernum};
  $request->{requesttime} = time unless $request->{requesttime};
  $request->{requestdate} = GeneralUtils->date unless $request->{requestdate};
  $request->{freezefile} = $class->makeFrozenName unless $request->{freezefile};
  $request->{status} = "WAITING" unless $request->{status};

  foreach $key (keys %$request) {
    push @x, join('=', $key, $request->{$key});
  }
  unlink $request->{freezefile} if -e $request->{freezefile};
  GeneralUtils->str2file(join("\n", @x), $request->{freezefile});
  chmod(0775, $request->{freezefile}) || die "Cannot chmod 0775 " . $request->{freezefile};
}

# thaws a request from disk and returns as a hash ref
sub thaw {
  my($class, $file) = @_;
  my($line, %request, $prop, $value);

  return undef unless -e $file;
  foreach $line (split /\n/, GeneralUtils->file2str($file)) {
    $line =~ /^([^=]*)=(.*)$/;
    $prop = $1;
    $value = $2;
    $request{$prop} = $value;
  }
  return \%request;
}

# makes up frozen file name
sub makeFrozenName {
  my($class) = @_;
  my($t) = time;
  my($i, $path);


  unless (-d $REQUESTDIR) {
    mkdir $REQUESTDIR, 0775 || die "ERROR: Cannot mkdir $REQUESTDIR";
  }

  for ($i=1; $i<1000; $i++) {
    $path = sprintf("%s/%s.%d_%.3d", $REQUESTDIR, $PREFIX, $t, $i);
    last unless -e $path;
  }
  return $path;
}

# is this a legitimate frozen file?
sub isFrozen {
  my($class, $file) = @_;
  return basename($file) =~ /^$PREFIX\.([_\d]+)$/;
}

# generates the report for this request (for worklists and checklists)
sub report {
  my($class, $dbh, $request) = @_;
  my($report);
  my($db) = $dbh->getDB() || $request->{db};

  $request->{timestarted} = time;
  $request->{datestarted} = GeneralUtils->date;;
  $class->running($request);
  unlink $request->{reportfile} if $request->{reportfile};

  my($list) = $request->{list} || $request->{worklist} || $request->{checklist};
  my($conceptfile) = EMSUtils->tempFile("concepts");

  unless ($dbh->tableExists($list)) {
    $request->{status} = "ERROR";
    $request->{errormsg} = "Error: Table: $list does not exist in " . $request->{db};
    die $request->{errormsg};
  }

  eval {
    WMSUtils->worklist2file($dbh, $list, $conceptfile);
  };

  if ($@) {
    $request->{status} = "ERROR";
    $request->{errormsg} = $@;
    $class->freeze($request);
  } else {
    $request->{conceptfile} = $conceptfile;
    $report = WMSUtils->file2report($conceptfile, $request);
  }
  unlink $conceptfile;

  if ($request->{reportfile}) {

    unlink $request->{reportfile};
    my($reportsdir) = dirname($request->{reportfile});
    unless (-e $reportsdir) {
      die "Cannot mkpath $reportsdir: $@" unless (mkpath($reportsdir, 0, 0775));
      die "Cannot chmod $reportsdir: $@" unless chmod 0775, $reportsdir;
    }

    GeneralUtils->str2file($report, $request->{reportfile});
    chmod(0775, $request->{reportfile}) || die "Cannot chmod 0775 " . $request->{reportfile};
  }
  $request->{timeended} = time;
  $request->{dateended} = GeneralUtils->date;
  $class->done($request);
  return $report;
}

# executing the request
sub running {
  my($class, $request) = @_;

  $request->{status} = "RUNNING";
  $class->freeze($request);
}

# some error in processing
sub error {
  my($class, $request, $msg) = @_;

  $request->{status} = "ERROR";
  $request->{errmsg} = $msg || "unknown error";
  $class->freeze($request);
}

# done with request
sub done {
  my($class, $request) = @_;

  $request->{status} = "DONE" if grep { $_ eq $request->{status} } ("WAITING", "RUNNING");
  $class->freeze($request);
}
#----------------------------------------
1;
