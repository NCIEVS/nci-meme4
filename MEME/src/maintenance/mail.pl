#!/site/bin/perl5
#
# File:     mail.pl
# Author:   Bobby Edrosa (2006).
#
# Description: 
#			This is a generic mail script that interacts with mid services
#           but can be overridden with specific settings.
#
# 03/30/2006 RBE (1-ATTML): Generic mail script
#
# This script has the following usage:
#    mail.pl [-[smtp=<mail server>]] 
#            [-[midsvcs=<key>]]
#            [-[to=<to list>]] 
#            [-[from=<from>]] 
#            [-[subject=<subject>]] 
#            [-[message=<message>]]
#
# Options:
#       -smtp    : Mail server
#       -midsvcs : Mid service
#       -to      : To list. must be specified by -to
#       -from    : From. must be specified by -from
#       -subject : Subject
#       -message : Message
#
#       -v[ersion]  : Print version information.
#       -[-]help    : On-line help
#
# Parameters
#    [<mail server>]:    either MID service "<key>-smtp-host" or "smtp-host" or value of -smtp
#    [<key>]        :    either MID service "<key>-smtp-host" or "smtp-host" or value of -smtp
#    [<to list>]    :    value returned by MID service "<key>-list" 
#    [<from>]       :    value returned by MID service "<key>-from" 
#    [<subject>]    :    defaults to "Auto-generated Mail", overridden by -subject
#    [<message>]    :    defaults to "Auto-generated Mail", overridden by -message
#

# Version Information
# 03/30/2006 4.1.0: 1st version
#
$release = "4";
$version = "1.0";
$version_date = "03/30/2006";
$version_authority="RBE";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set defaults
#
$smtp_host = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s smtp-host`;
chop($smtp_host);
$mail_to = "";
$mail_from = "";
$subject = "Auto-generated Mail";
$message = "Auto-generated Mail";

# Check options
while (@ARGV) {
    $arg = shift(@ARGV);
    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg eq "-version") {
        $print_version="version";
    }
    elsif ($arg eq "-v") {
        $print_version="v";
    }
    elsif ($arg eq "-help" || $arg eq "--help") {
        $print_help=1;
    }
    elsif ($arg =~ /^-midsvcs=(.*)/ || $arg =~ /^-midsvcs$/) {
		if ($1) {
 			$midsvcs = $1;
		} else {
			$midsvcs = shift(@ARGV);
	    }
		unless ($smtp_flag) {
  	    	$smtp_host = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s ${midsvcs}-smtp-host`;
	    	chop($smtp_host);
		    unless ($smtp_host) {
		        $smtp_host = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s smtp-host`;
	    	    chop($smtp_host);
		    }
		    $badvalue="smtp-host" unless ($smtp_host);
		}
		$mail_to = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s ${midsvcs}-list`;
		chop($mail_to);
		$badvalue="mail-to" unless ($mail_to);
		$mail_from = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s ${midsvcs}-from`;
		chop($mail_from);
		$badvalue="mail-from" unless ($mail_from);
		if ($badvalue) {
		    $badargs = 4;
        }
    }
    elsif ($arg =~ /^-to=(.*)/ || $arg =~ /^-to$/) {
		if ($1) {
		    $mail_to = $1;
       	} else {
		    $mail_to = shift(@ARGV);
		}
    }
    elsif ($arg =~ /^-from=(.*)/ || $arg =~ /^-from$/) {
		if ($1) {
		    $mail_from = $1;
       	} else {
		    $mail_from = shift(@ARGV);
		}
    }
    elsif ($arg =~ /^-subject=(.*)/ || $arg =~ /^-subject$/) {
		if ($1) {
		    $subject = $1;
       	} else {
		    $subject = shift(@ARGV);
		}
    }
    elsif ($arg =~ /^-message=(.*)/ || $arg =~ /^-message$/) {
		if ($1) {
		    $message = $1;
       	} else {
		    $message = shift(@ARGV);
		}
    }
    elsif ($arg =~ /^-smtp=(.*)/ || $arg =~ /^-smtp$/) {
		if ($1) {
		    $smtp_host = $1;
       	} else {
		    $smtp_host = shift(@ARGV);
		}
		$smtp_flag = 1;
    }
    else {
		$badargs = 2; 
    }
    $found=1;
}

%badargs = (
	    1 => "",
	    2 => "Invalid switch",
	    3 => "Wrong number of arguments",
	    4 => "$badvalue value in mid service not found");


#
# If necessary print help or version information
#
&PrintHelp && exit(0) if $print_help;
&PrintVersion($print_version) && exit(0) if $print_version;

#
# Check for errors
#


$badargs = 3 unless $found;

if ($badargs) {
    print "$badargs{$badargs}\n";
    &PrintUsage;
    exit(0);
}

print "------------------------------------------------------------\n";
print "Starting  ...", scalar localtime,"\n";
print "------------------------------------------------------------\n";

#
# Display settings
#
print "SMTP Host: $smtp_host\n";
print "Mail To: $mail_to\n";
print "Mail From: $mail_from\n";
print "Subject: $subject\n";
print "Message: $message\n";

#
# Send Mail
#
use Mail::Sender;
$sender = new Mail::Sender{smtp => "$smtp_host", from => "$mail_from"};
$sender->MailMsg({to => "$mail_to", subject => "$subject", msg => "$message"});
 if ($sender->{error}) {
   die "Error sending mail: $sender->{error_msg}\n";
 }

print "------------------------------------------------------------\n";
print "Finished ...", scalar localtime,"\n";
print "------------------------------------------------------------\n";
exit(0);

############################# Local Procedures ############################

sub PrintHelp {
    
    &PrintUsage;
    print qq{
 Options:
       -smtp    : Mail server
       -midsvcs : Mid service
       -to      : To list. must be specified by -to
       -from    : From. must be specified by -from
       -subject : Subject
       -message : Message

       -v[ersion]  : Print version information.
       -[-]help    : On-line help
};
    
    &PrintVersion("version");
}

sub PrintVersion {
    my($type) = @_;
    print "Release $release: version $version, $version_date ($version_authority).\n" 
	if $type eq "version";

    print "$version\n" if $type eq "v";
    return 1;
}

sub PrintUsage {
    print qq{This script has the following usage:
    mail.pl [-[smtp=<mail server>]] [-[midsvcs=<key>]] [-[to=<to list>]] [-[from=<from>]] 
	    [-[subject=<subject>]] [-[message=<message>]]
}
};
