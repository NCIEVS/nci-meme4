# Parallel execution of one or more scripts/subroutines
# Author: suresh@nlm.nih.gov 12/2002

package ParallelExec;

# recognized initialization parameters, e.g.,
# degree (degree of parallelization, i.e., how many subprocesses to run in parallel: default 1)
# debug=1 (turn on debug)

# $p=new ParallelExec(3);
# $p=new ParallelExec("degree=3");
# $p=new ParallelExec({degree=>3});


use Proc::Simple;

use strict;
use warnings;

# CONSTRUCTOR
sub new {
  my($class, $initializer) = @_;
  my($self) = {};

  bless $self, $class;
  return $self;
}

# The exit status and run times of each command is returned in a array
# of hashes, e.g., [ {status=>0, runtime=>123}, {}, ..].
# This call *will* block
sub run {
  my($self, $cmdArray, $degree) = @_;
  my($i);

  my($naptime) = 1;
  my($nextcmd) = 0;
  $degree = $degree || $self->{degree} || $self->{cpu} || 1;

  #
  # Reset object variables
  #
  my(@children, @reaped, @returnvals);
  $self->{reaped} = \@reaped;
  $self->{children} = \@children;
  $self->{returnvals} = \@returnvals;
  $self->{running} = 0;

  while (1) {
    last if (($self->{running} == 0) && ($nextcmd == @{ $cmdArray }));
    if ($self->{running} < $degree && $nextcmd < @{ $cmdArray }) {
      $self->start($cmdArray->[$nextcmd], $nextcmd);
      $nextcmd++;
    } else {
      sleep($naptime);
      $self->poll();
    }
  }

  # Clear the child signal handler because
  # it can affect return value from system command in perl 5.8
  delete $SIG{CHLD};

  return @returnvals;
}

sub poll {
  my($self) = @_;


#  print STDERR "Polling: running=$self->{running}, next: $nextcmd\n";

  for (my $i=0; $i < @{$self->{children}}; $i++) {
    next if $self->{reaped}[$i];
    my $proc = $self->{children}[$i];
    unless ($proc && $proc->poll()) {
      $self->{returnvals}[$i]{runtime} = time - $self->{returnvals}[$i]{runtime};
      $self->{returnvals}[$i]{status} = $proc->exit_status();
      $self->{reaped}[$i] = 1;
      $self->{running}--;
      $self->{proc}[$i] = undef;
    }
  }
}

sub start {
  my($self, $cmd, $i) = @_;
  my($proc) = new Proc::Simple->new();
#  $proc->debug($self->{debug});

  $self->{children}->[$i] = $proc;
  $self->{returnvals}[$i] = { runtime=>time };

  if (ref($cmd) eq "ARRAY") {
    my(@x) = @{ $cmd };
#    print STDERR "Starting: $i ($x[0])\n";
    $proc->start(@x);
  } else {
#    print STDERR "Starting: $i ($cmd)\n";
    $proc->start($cmd);
  }
  $self->{running}++;
}

sub killall {
  my($self) = @_;
  my($proc);

  foreach $proc (@$self->{child}) {
    $proc->kill();
  }
}
1;
