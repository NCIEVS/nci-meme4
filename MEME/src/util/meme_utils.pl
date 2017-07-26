#
# File:     meme_utils.pl
# Author:   Brian Carlsen  (1999)
# 

package meme_utils;


#############################################################################
# Version info:
#  08/08/2005:  All ORWeL stuff removed
#  11/25/2003:   Additional style sheet classes
#  8/09/2000:    ORWeL.pl doesn't take db_mode flag anymore
#  6/02/2000:    ORWeL.pl doesn't take db_mode flag anymore
#  7/13/1999:    First try, code harvested from action.cgi
#
$release = "4";
$version = "3.0";
$version_authority = "BAC";
$version_date = "08/09/2000";


#############################################################################
#
#    Variables
#############################################################################

#
# Oracle Variables
#
$oracle_passwd = "$ENV{SAFEBOX_HOME}/oracle.passwd";
$midsvcs_pl = "$ENV{MIDSVCS_HOME}/bin/midsvcs.pl";
$db_mode = "oracle";

%error_messages = (
		   -10 => "\$MEME_HOME is not set",
		   -15 => "\$ORACLE_HOME is not set",
		   -20 => "Error opening command",
		   -25 => "Error opening sql command",
		   -40 => "Error code not found",
		   -50 => "Error opening file",
		   -60 => "Error opening SQL command",
		   -70 => "SQL Error"
);

%database_settings = (
		    "sql" => "$ENV{ORACLE_HOME}/bin/sqlplus",
		    "sql_init" => "set serveroutput on size 100000\n set pages 5000\n set lines 100\n set colsep '|'\n set autocommit on\n",
		    "end_statement" => ";",
		    "results_start_pattern" => "^-{1,}",
		    "results_end_pattern" => "^\$",
		    "error_pattern" => "(ORA_.{5}): (.*)",
		    "to_date" => "to_date",
		    "to_char" => "to_char",
		    "to_number" => "to_number",
		    "now" => "SYSDATE",
		    "today" => "trunc(SYSDATE,'ddd')"
		    );

################################# PROCEDURES #################################
#
#  The following procedures Are HTML/CGI utilities
#
#############################################################################

###################### Procedure None ######################
#
# This procedure does nothing
#
sub None {

}

###################### Procedure PrintHTTPHeader ######################
#
# This prints the content type and other info
#
sub PrintHTTPHeader {

    #
    # Set start time
    #
    $start_time = time;

    #
    # Set NLS_LANG
    #
    $ENV{"NLS_LANG"} = "AMERICAN_AMERICA.UTF8";

    #
    # print header
    #
    print qq{Content-type: text/html; charset=UTF-8

};
}

###################### Procedure PrintSimpleHeader ######################
#
# This procedure prints a simple
#
sub PrintSimpleHeader {

    my ($title,$style_sheet,$script,$header) = @_;
    print qq{
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>$title</title>
$script
$style_sheet

</HEAD>

<BODY bgcolor="#ffffff">
$header
<HR WIDTH=100%>
}; 
} # End of PrintSimpleHeader


###################### Procedure PrintSimpleFooter ######################
#
# This procedure ends the body and HTML sections with a HR
#
sub PrintSimpleFooter {
    
    my($generation_info,$contact_info,$version_info);

    print qq{
<HR WIDTH=100%>
$generation_info
$version_info
$contact_info
</BODY>
</HTML>
};
}

###################### Procedure CGIError ####################
#
# This prints an error page
#
sub CGIError {
    my($msg) = @_;
    print qq{
<html>
<head><title>Error</title></head>
<body bgcolor="#ffffff">
<table width=80% align=center><tr><td>
<font color="#cc0000">$msg</font>
</td></tr>
</table>
</body>
</html>
};

}; # end CGIError

###################### Procedure ReadParse ######################
#
# This procedure is responsible for breaking down the CGI argument
# string into variables.  Only if the $name part is valid will it
# succeed.
#
sub ReadParse {
    my($string) =@_;
    @pairs=split(/\&/,$string);
    foreach $pair (@pairs) {
        if ($pair =~/(.*?)=(.*)/) {
	    $name= $1;
	    $value = $2; 
#	    print "$name: $validargs{$name}\n";
	    next if (!$validargs{$name});
	    $value =~ s/\+/ /g;
	    $name =~ s/%([0-9a-fA-F]{2})/pack("c",hex($1))/ge;
	    $value =~ s/%([0-9a-fA-F]{2})/pack("c",hex($1))/ge;
	    $mainname="main::$name";
	    $mainencname="main::$encname";
#            print "$mainname = ",qq{$value};
            $$mainname = qq{$value};
	    push @$mainname, qq{$value};
	    $mainencname="main::enc_$name";
	    $$mainencname = escape(qq{$value});
        };
    };
};

###################### Procedure escape ######################
#
# This procedure escapes special characters for CGI submission
#
sub escape {
    my($toencode) = @_;
    $toencode=~s/([^a-zA-Z0-9_\-.])/uc sprintf("%%%02x",ord($1))/eg;
    return $toencode;
}

###################### Procedure ConvertForHTML ######################
#
# This procedure converts spaces into &nbsp, newlines into <BR>
# < into &lt, and > into &gt
#
sub ConvertForHTML {
    my($string) = @_;
    $string =~ s/\n/<BR>\n/g;
    $string =~ s/  /&nbsp;&nbsp;/g;

# String may contain HTML tags, if so don't do the next two
    $string =~ s/</&lt;/g;
    $string =~ s/>/&gt;/g;

    return $string;
}

###################### Procedure GetStyle ######################
#
# This procedure returns the stylesheet for this document
#
sub getStyle {
    return qq{
<STYLE type=text/css>     
    BODY { background-color: #FFFFFF; }   
    TH { background-color: #FFFFCC; }
    INPUT.SNAZZY { background-color: #0000A0;
		   color: #FFFFFF; }
    INPUT.EXIT { background-color: #000000;
	         color: #FFFFFF; }
    INPUT.NORMAL { background-color: #A0A0A0;
		   color: #FFFFFF; }

    div.sql { color: #6600cc; font-weight: bold; }	     
    div.code { color: #009900; font-weight: bold; }	     
    div.error { color: #990000; font-weight: bold; }	     

    #red { color: #A00000; font: 150% Palatino, sans-serif; }
    #blue { color: #0000A0; font: bold 150% Palatino, serif; }
	
    ADDRESS  { font: italic 100% Palatino, Ariel, serif; }		      
		  
</STYLE>
    };
}



###################### Procedure PrintString ######################
#
# This procedure prints the contents of @ORWeL_string
# to STDOUT
#
sub PrintString {

    foreach $line (@ORWeL_string) {
	print $line;
    }
}

###################### Procedure PrintErrors ######################
#
# This procedure prints contents of @ORWeL_errors
# to STDOUT
#
sub PrintErrors {
    #
    # Print Header
    #
    print qq{
<SPAN id=red>ERROR(S)</SPAN><P>
<TABLE BORDER=1 CELLPADDING=5 ALIGN=CENTER WIDTH=90%>
<TR><TH>Error Id</TH><TH>Error Code</TH><TH>Error Message</TH></TR>
};

    foreach $error (@ORWeL_errors) {

	($id,$code,$msg) = split (/\|/,$error);
	print qq{
<TR>
<TD>$id</TD><TD>$code</TD><TD>$msg</TD>
</TR>
};
    }
 
    print qq{</TABLE>
};

} # End Procedure PrintErrors

################################# PROCEDURES #################################
#
#  The following procedures are for accessing SQL
#
#############################################################################

###################### Procedure runSQL ######################
#
# This procedure runs a SQL command file and returns the results
#
sub runSQL {
    my($cmdfile) = @_;
    @fields = ();

    open(CMD,"$sql < $cmdfile|") || 
	(($error_detail = "$sql,$db") && return -60);

    #
    # Read SQL results to the start pattern
    #
    while (<CMD>) {
	if (/$error_pattern/) { 
	    $SQL_error_code = $1; $SQL_error_msg = $2;  last;}
	last if /$result_start_pattern/;
    };

    # If ingres, skip two lines
    if ($db_mode eq "ingres") {
	$_=<CMD>; $_=<CMD>;
    }

    #
    # Read results until end_pattern
    #
    while (<CMD>) {
	chop;
	last if /$result_end_pattern/;
	if (/$error_pattern/) { 
	    $SQL_error_code = $1; $SQL_error_msg = $2;  last;
	}

	#
	# Ingres has an extra | at the beginning of the line, removeit
	#
	s/^\|// if $db_mode eq "ingres";

	#
	# Get fields & strip spaces & push onto @results
	#
	@fields = split /\|/;
	map { s/^\s*(.*?)\s*$/$1/ } @fields;
	$line = join('|',@fields);
	push @results, $line;

	print "fields; '$line'   field ct: ",scalar(@fields),"\n" if $debug;

    }

    close(CMD);

    unlink "$cmdfile";

    if ($SQL_error_code) {
	($error_detail = "$SQL_error_code $SQL_Error_msg") && return -70;
    } else {
	return @results;
    };

} # end runSQL

################################# PROCEDURES #################################
#
#  The following are useful procedures
#
#############################################################################

###################### Procedure Exit ######################
#
# This procedure prints a message and exits
#
sub Exit {
    print $_[0];
    exit ($_[1]);
}

###################### Procedure GetUserPassword ######################
#
# This procedure looks up in the password file and
# gets the default user/password
#
sub GetUserPassword {
    my ($u) = @_;
    $passwd = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    chop($passwd);
    return $passwd;

}

###################### Procedure midsvcs ###################
#
# This procedure calls the midsvcs script & chops output
#
sub midsvcs {
    my ($service) = @_;
    $res = `$midsvcs_pl -s $service`;
    chop($res);
    return $res;

}

###################### Procedure PrintVersion ######################
#
# This procedure prints version information
#
sub PrintVersion {
    my($type) = @_;

    print "Version $main::ingres_version, $main::version_date ($main::version_authority).\n"
        if $main::db_mode eq "ingres" && $main::type eq "version";
    print "Release $main::release: version $main::version, $main::version_date ($main::version_authority).\n" 
        if $main::db_mode eq "oracle" && $main::type eq "version";
    print "$main::ingres_version\n" if $main::db_mode eq "ingres" && $main::type eq "v";
    print "$main::version\n" if $main::db_mode eq "oracle" && $main::type eq "v";
    return 1;
}

###################### Procedure GetErrorMessage ######################
#
# This procedure takes an error code and returns the associated message
# and detail
#
sub GetErrorMessage {
    my($code) = @_;
    return "$error_messages{$code}: $error_detail" if $error_messages{$code};
}


###################### Procedure DateMath ######################
#
# This is a general utility procedure for doing date arithmetic
#
sub DateMath {
    my($time,$arg,$unit) = @_;
    %months = (
               'jan' => 1, 'feb' => 2, 'mar' => 3,
               'apr' => 4, 'may' => 5, 'jun' => 6,
               'jul' => 7, 'aug' => 8, 'sep' => 9,
               'oct' => 10, 'nov' => 11, 'dec' => 12);

    %revmonths = reverse(%months);

    %daysinmonth = ( 
               1 => 31, 2 => 28, 3 => 31,
               4 => 30, 5 => 31, 6 => 30,
               7 => 31, 8 => 31, 9 => 30,
               10 => 31, 11 => 30, 12 => 31,
                     0 => 31, 13 => 31);

    # if not passed,get time
    if ($time eq "" || $time eq "now") {
	$time = scalar(localtime); }
    elsif ($time eq "today") {
        $time = scalar(localtime);
        $time =~ s/[0-9][0-9]:[0-9][0-9]:[0-9][0-9]/00:00:00/;
    } else {
	$timepassedin=1;
    }

    # Get parts of date
    ($day,$month,$dayofmonth,$hhmmss,$year)=split(/ {1,}/,$time);
    # accept format that this function returns
    if ($timepassedin) {
        ($ddmmmyyyy,$hhmmss)=split(/ /,$time);
        ($dayofmonth,$month,$year) = split(/-/,$ddmmmyyyy);
        $dayofmonth =~ s/^0//;
    }
    $month =~ tr/A-Z/a-z/;
    $month = $months{$month};
    ($hour,$min,$sec) = split(/:/,$hhmmss);
    
    if ($unit =~ /sec/) { $unit = "\$sec";}
    elsif ($unit =~ /min/) { $unit = "\$min";}
    elsif ($unit =~ /hour/) { $unit = "\$hour";}
    elsif ($unit =~ /day/) { $unit = "\$dayofmonth";}
    elsif ($unit =~ /month/) { $unit = "\$month";}
    elsif ($unit =~ /year/) {  $unit = "\$year";}
    else { $arg = "b"; };

    if ($arg =~ /[0-9]*/) {
        eval "$unit = $unit + $arg";
#       print "$unit = $unit + $arg\n";
    } else {
        $month = $revmonths{$month};
        if (length($dayofmonth) == 1) {$dayofmonth = "0$dayofmonth";};
        if (length($hour)== 1) {$hour = "0$hour"};
        if (length($sec)==1) {$sec = "0$sec"};
        if (length($min) == 1) {$min = "0$min"};
        return "$dayofmonth-$month-$year $hour:$min:$sec";  
    }
    while(1) {
        if (($year%4==0 && $year%100!=0) || $year%400==0) {
            $daysinmonth{"2"} = 29; }
        else { $daysinmonth{"2"} = 28; };

        if ($sec < 0) {  $sec += 60; $min--; $changes=1;};
        if ($sec > 60) { $sec -= 60; $min++; $changes=1;}; 
        if ($min < 0) { $min += 60; $hour--; $changes=1;};
        if ($min > 60) { $min -= 60; $hour++; $changes=1;}; 
        if ($hour < 0) { $hour += 24; $dayofmonth--;; $changes=1;};
        if ($hour > 24) { $hour -= 24; $dayofmonth++; $changes=1;}; 
        if (!($unit eq "\$month") && $dayofmonth < 1) {
#       print "yeah: $dayofmonth += $daysinmonth{$month-1} $month\n";
                $dayofmonth += $daysinmonth{$month-1}; $month--;$changes=1; };
        if (!($unit eq "\$month") && $dayofmonth > $daysinmonth{$month}) { 
            $dayofmonth -= $daysinmonth{$month}; $month++; $changes=1; };
        if ($month < 1) { $month += 12; $year--; $changes=1; };
        if ($month > 12) { $month -= 12; $year++; $changes=1; };

        if (!$changes) {last; };
        $changes=0;
    };
 
    if (length($dayofmonth) == 1) {$dayofmonth = "0$dayofmonth";};
    if (length($hour)== 1) {$hour = "0$hour"};
    if (length($sec)==1) {$sec = "0$sec"};
    if (length($min) == 1) {$min = "0$min"};
    
    return "$dayofmonth-$revmonths{$month}-$year $hour:$min:$sec";
};
