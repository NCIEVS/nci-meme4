#!@PATH_TO_PERL@


# Returns the Oracle password given the user name.
# You must have permission to access $SAFEBOX_HOME/oracle.passwd

# Changes:
# 12/22/2005 BAC (1-719SM): use open ":utf8"  added

# Command line options:
# -u <username> returns the username/password for this user

# For backward compatibility with BAC's scripts, if no args
# are specified it returns the user/password for the "default" user,
# i.e., the first user in the password file

# Return status: 0 OK, 1: bad user, 2: no read permission

# suresh@nlm.nih.gov 6/00

use Getopt::Std;



unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";
$use_old_style = $ENV{RETRIEVE_ORACLE_PASSWORD_STYLE};
if ( $use_old_style eq "NEW" || $use_old_style eq "new") {
getopts("u:d:");
$pwdfile = "$ENV{SAFEBOX_HOME}/oracle.passwd";

exit 2 unless (-e $pwdfile && -r $pwdfile);


$user = $opt_u;
$database = $opt_d;
$userpasswd = &get_new_passwd($pwdfile, $database, $user);
exit 1 if $userpasswd =~ m|/$|;
print $userpasswd, "\n";
exit 0;

} else {
getopts("u:");
$pwdfile = "$ENV{SAFEBOX_HOME}/oracle.passwd";

exit 2 unless (-e $pwdfile && -r $pwdfile);

$user = $opt_u;
$userpasswd = &get_passwd($pwdfile, $user);
exit 1 if $userpasswd =~ m|/$|;
print $userpasswd, "\n";
exit 0;
}
# Read authentication information from file
sub get_passwd {
    my($passwdFile, $user) = @_;
    my($passwd) = "";

    open(PASSWD, $passwdFile) || return $passwd;
    while (<PASSWD>) {
        chomp;
        next if /^\#/ || /^\s*$/;
        my($u, $p) = split /\|/, $_;
        unless ($user) {
            $user = $u;
            $passwd = $p;
            last;
        }
        next unless $u eq $user;
        $passwd = $p;
        last;
    }
    close(PASSWD);
    $passwd =~ tr/a-zA-Z/n-za-mN-ZA-M/;
    return join('/', $user, $passwd);
}
# Read authentication information from file
sub get_new_passwd {
    my($passwdFile, $db, $user) = @_;
    my($passwd) = "";
    open(PASSWD, $passwdFile) || return $passwd;
    while (<PASSWD>) {
        chomp;
        next if /^\#/ || /^\s*$/;
        my($u, $p,$d) = split /\|/, $_;
        unless ($user) {
          unless ($db) {
            $user = $u;
            $passwd = $p;
            last;
          }
          if (lc($d) eq lc($db)) {
            $user = $u;
            $passwd = $p;
            last;
          }
        }
        unless ($db) {
          unless ($user) {
            $user = $u;
            $passwd = $p;
            last;
          }
          if (lc($u) eq lc($user)) {
            $user = $u;
            $passwd = $p;
            last;
          }
        }
        next unless (lc($u) eq lc($user) && lc($d) eq lc($db));
        $passwd = $p;
        last;
    }
    close(PASSWD);
    $passwd =~ tr/a-zA-Z/n-za-mN-ZA-M/;
    return join('/', $user, $passwd);
}