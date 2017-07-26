#!@PATH_TO_PERL@
# File:     assign_src_atom_id_range.cgi
# Author:   Tim Kao
#
#
# Description: 04-04-2008 (1-GZSMP) TK: This application allows the inverter to
# 				request, edit, and search for VSAB and its SAID ranges.
#
# Changes:

$release           = "1";
$version           = "1.0";
$version_authority = "TK";
$version_date      = "03/19/2008";

unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Parse command line arguments to determine if -v{ersion}, or --help
# is being used
#
while (@ARGV)
{
	$arg = shift(@ARGV);
	if ( $arg !~ /^-/ )
	{
		push @ARGS, $arg;
		next;
	}
	if ( $arg eq "-v" )
	{
		$print_version = "v";
	} elsif ( $arg eq "-version" )
	{
		$print_version = "version";
	} elsif ( $arg =~ /-{1,2}help$/ )
	{
		$print_help = 1;
	}
}

&PrintHelp                    && exit(0) if $print_help;
&PrintVersion($print_version) && exit(0) if $print_version;

#
# Get Parameters
# If request method is GET, parameters are in variable $QUERY_STRING
# If request method is POST, parameters are in STDIN
#
if ( $ENV{"REQUEST_METHOD"} eq "GET" )
{
	$_ = $ENV{"QUERY_STRING"};
} else
{
	$_ = <STDIN> || die "Method $ENV{REQUEST_METHOD} not supported.";
}

#
# This is the set of Valid arguments used by readparse.
# Altering it alters what CGI variables are read.
#
%meme_utils::validargs = (
						   "state"       => 1,
						   "log_name"    => 1,
						   "command"     => 1,
						   "db"          => 1,
						   "meme_home"   => 1,
						   "vsab"        => 1,
						   "range"       => 1,
						   "min"         => 1,
						   "oracle_home" => 1,
						   "max"         => 1,
						   "orig_vsab"   => 1,
						   "orig_range"  => 1,
						   "arg"         => 1
);

#
# Set environment variables after parsing arguments
#

#
# Set environment variables
# This section must be updated when script is moved to another machine
#
$inc = $ENV{"SCRIPT_FILENAME"};
$inc =~ s/(.*)\/.*$/$1/;
unshift @INC, "$inc";
require "meme_utils.pl";
require DBI;
require DBD::Oracle;

#
# This is the HTTP header, it always gets printed
#
print qq{Expires: 20 Mar 2008 01:01:01 GMT\n};
&meme_utils::PrintHTTPHeader;

#
# Readparse translates CGI argument string into variables
# that can be used by the script
#
&meme_utils::ReadParse($_);

#
# Default Settings for CGI parameters
#
$ENV{"MEME_HOME"} = $meme_home || $ENV{"MEME_HOME"} || $inc;
$ENV{"ORACLE_HOME"} = $oracle_home
  || $ENV{"ORACLE_HOME"}
  || die "\$ORACLE_HOME must be set.";
$state = "CHECK_JAVASCRIPT" unless $state;

#
# Set Other variables, including:
#   style sheet, cgi location, sql command, unedited MID, current MID
#
$db          = &meme_utils::midsvcs("editing-db") unless $db;
$date        = `/bin/date +%Y%m%d`;
$cgi         = $ENV{"SCRIPT_NAME"};
$start_time  = time;
$style_sheet = &meme_utils::getStyle;

#
# Open Database Connection
#
# set variables
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
( $user, $password ) = split /\//, $userpass;
chop($password);

# open connection
$dbh = DBI->connect( "dbi:Oracle:$db", "$user", "$password" )
  || ( ( print "<span id=red>Error opening $db ($DBI::errstr).</span>" )
	   && return );

# set sort and hash areas
$dbh->do("alter session set sort_area_size=67108864");
$dbh->do("alter session set hash_area_size=67108864");

#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
	   "CHECK_JAVASCRIPT" => [
							 "PrintHeader",
							 "PrintCHECK_JAVASCRIPT",
							 "PrintFooter",
							 "Check JavaScript",
							 "This page will redirect if JavaScript is enabled."
	   ],
	   "INDEX" => [
					"PrintHeader", "PrintINDEX", "PrintFooter",
					"Assign SRC_ATOM_ID Ranges- Index Page",
					"Assign SRC_ATOM_ID Ranges"
	   ],
	   "REQUEST_RANGE" => [
							"PrintHeader", "PrintRANGE",
							"PrintFooter", "SRC_ATOM_ID Ranges",
							"SRC_ATOM_ID Ranges"
	   ],
	   "EDIT_RANGE" => [
						 "PrintHeader", "PrintEDIT_RANGE",
						 "PrintFooter", "Edit SRC_ATOM_ID Ranges",
						 "Edit SRC_ATOM_ID Ranges"
	   ],
	   "EDIT_RANGE_DONE" => [
							  "PrintHeader",
							  "PrintEDIT_RANGE_DONE",
							  "PrintFooter",
							  "New SRC_ATOM_ID Ranges Results",
							  "New SRC_ATOM_ID Ranges Results"
	   ],
	   "" => [ "PrintHeader", "None", "PrintFooter", "" ]
);

#
# Check to see if state exists
#
if ( $states{$state} )
{

	#
	# Print Header, Body, and Footer
	#
	$header = $states{$state}->[0];
	$body   = $states{$state}->[1];
	$footer = $states{$state}->[2];

	#    print "$header, $body, $footer\n";
	#    exit(0);

	&$header( $states{$state}->[3], $states{$state}->[4] );
	&$body;
	&$footer;

	$dbh->disconnect if $dbh;
}

#
# We're done, exit
#
exit(0);

################################# PROCEDURES #################################
#
#  The following procedures print HTML code for the script
#
#############################################################################

###################### Procedure None ######################
#
# This prints either No header, No body, or No footer
#
sub None
{

}

###################### Procedure PrintHeader ######################
#
# If no_form is passed in, print header without the form
#
sub PrintHeader
{

	my ( $title, $header ) = @_;

	&meme_utils::PrintSimpleHeader( $title, $style_sheet, &HeaderJavascript,
									"<h2><center>$header</center></h2>" );

}

###################### Procedure HeaderJavascript ######################
#
# This procedure contains the javascript for the standard header
#
sub HeaderJavascript
{

	return qq{
    <script language="javascript">

	function verifyDelete (check,form) {
	    var yesno = confirm('Are you sure you want to delete this check: '+check+'?');
	    if (yesno) {
		form.submit();
	    }
	}; // end verifyDelete


    </script>
};
}    # end HeaderJavascript

###################### Procedure PrintFooter ######################
#
# This procedure prints a standard footer including time to generate
# the page, the current date, and some links
#
sub PrintFooter
{

	#
	# Compute the elapsed time
	#
	$end_time     = time;
	$elapsed_time = $end_time - $start_time;

	#
	# Print the Footer
	#
	print qq{
    <hr width="100%">
	<table BORDER=0 COLS=2 WIDTH="100%" NOSAVE >
	  <tr NOSAVE>
	    <td ALIGN=LEFT VALIGN=TOP NOSAVE>
	      <address><a href="$cgi?state=INDEX&db=$db" onMouseOver="window.status='Return to index page.'; return true;" onMouseOut="window.status=''; return true;">Back to Index</a></address>
            </td>
	    <td ALIGN=RIGHT VALIGN=TOP NOSAVE>
	      <font size="-1"><address>Contact: <a href="mailto:tim.kao@lmco.com">Tim Kao</a></address>
	      <address>Generated on:}, scalar(localtime), qq{</address>
              <address>This page took $elapsed_time seconds to generate.</address>
	      <address>};
	&PrintVersion("version");
	print qq{</address></font>
            </td>
          </tr>
        </table>
    </body>
</html>
};
}    # End of PrintFooter

###################### Procedure PrintCHECK_JAVASCRIPT ######################
#
# This procedure prints a page that verifies that a javascript
# enabled browser is being used
#
sub PrintCHECK_JAVASCRIPT
{
	print qq{
	<script language="javascript">
	    document.location='$cgi?state=INDEX&db=$db';
	</script>
        <p><blockquote>
	    You must use a JavaScript enabled browser to run this
	    application (<a href="http://www.netscape.com">Netscape</a>
	    is recommended).  If you are using
	    a JavaScript enabled browser and just have JavaScript
	    disabled, please enable it and click
	    <a href="$cgi?state=INDEX&db=$db">here</a>.
        </blockquote></p>
	};
};    # end printCHECK_JAVASCRIPT

###################### Procedure PrintINDEX ######################
#
# This procedure prints the index page for the application with
# links to the other functionality
#
sub PrintINDEX
{
	$selected{$db}  = "SELECTED";
	$dbs            = &meme_utils::midsvcs("databases");
	$mrd            = &meme_utils::midsvcs("mrd-db");
	$dbs            = "$dbs,$mrd";
	$db_select_form = qq{
                                     <form action="$cgi">
			               <input type="hidden" name="state" value="INDEX">
			               <select name="db" onChange="this.form.submit(); return true;">
			                  <option>-- SELECT A DATABASE --</option>
};

	foreach $db ( sort split /,/, $dbs )
	{
		$db_select_form .=
		  "			                  <option $selected{$db}>$db</option>\n";
		$databases{$db} = $db;
	}
	$db_select_form .= "			                </select></form>\n";

	print qq{
		                <center><table border=1>
<form method="POST" action="$cgi">
    <input type="hidden" name="state" value="REQUEST_RANGE">
    <input type="hidden" name="db" value="$db">
        <tr><th>New VSAB</th><th># of src_atom_id</th><th>Submit</th></tr>
        <tr><td><input type="text" name="vsab" size=50></td>
        <td><input type="number" name="range" size=15></td>
<td><center><input type="submit" value="Request Range"></center></td>
</tr>
</form>
</table></center>
<p>
<p>
		
			
		                <center><table border=1>
<form method="POST" action="$cgi">
    <input type="hidden" name="state" value="EDIT_RANGE">
    <input type="hidden" name="db" value="$db">
        <tr><th>VSAB</th><th>Search</th></tr>
        <tr><td><input type="text" name="vsab" size=40></td>
<td><center><input type="submit" value="Search"></center></td>
</tr>
</form>
</table></center>
<p>
<p>
        };
}

###################### Procedure PrintRANGE ######################
#
# This procedure outputs the range
#
sub PrintRANGE
{
	#check for valid range, must be greater than 0
	if ( $range <= 0 )
	{
		print "<span id=red>Error: range should be greater than 0.</span>";
		return;
	}

	#check for duplicate VSAB
	$sh = $dbh->prepare(
		qq{
	SELECT count(*)
	FROM src_atom_id_range
	WHERE vsab like '%$vsab%'
    }
	  )
	  || (
		   (
			 print
			 "<span id=red>Error preparing query for src_atom_id_range.</span>"
		   )
		   && return
	  );

	$sh->execute
	  || ( ( print "<span id=red>Error reading from src_atom_id_range.</span>" )
		   && return );

	my ($present) = $sh->fetchrow_array;

	if ( $present != 0 )
	{
		print
"<span id=red>Error: $vsab already exist in src_atom_id_range.</span>";
		return;
	}

	$sh = $dbh->prepare(
		qq{
	SELECT max(max)
	FROM src_atom_id_range
    }
	  )
	  || (
		   (
			 print
			 "<span id=red>Error preparing query for src_atom_id_range.</span>"
		   )
		   && return
	  );

	$sh->execute
	  || ( ( print "<span id=red>Error reading from src_atom_id_range.</span>" )
		   && return );

	my ($max)   = $sh->fetchrow_array;
	my $min     = $max + 1;
	my $new_max = $min + $range;

	$sh = $dbh->prepare(
		qq{
	INSERT INTO src_atom_id_range (vsab,min,max,timestamp)
	VALUES ('$vsab',$min,$new_max,sysdate)
    }
	  )
	  || (
		(
		   print
"<span id=red>Error preparing insert query for src_atom_id_range.</span>"
		)
		&& return
	  );

	$sh->execute
	  || ( ( print "<span id=red>Error insert into src_atom_id_range.</span>" )
		   && return );

	print qq{
<center><table border=1 width="80%">
<tr><th width="40%">VSAB</th><th width="20%">Min</th><th width="20%">Max</th><th width="20%"># of src_atom_id</th></tr>
<tr><td>$vsab</td><td>$min</td><td>$new_max</td><td><center>$range</center>
</td></tr></center></table><p>
	};
}

###################### Procedure PrintEDIT_RANGE ######################
#
# This procedure outputs the range to edit
#
sub PrintEDIT_RANGE
{

	#seach for vsab
	$sh = $dbh->prepare(
		qq{
	SELECT vsab, max, min, max - min, timestamp
	FROM src_atom_id_range
	WHERE vsab like '%$vsab%'
	order by 2 desc
    }
	  )
	  || (
		   (
			 print
			 "<span id=red>Error preparing query for src_atom_id_range.</span>"
		   )
		   && return
	  );

	$sh->execute
	  || ( ( print "<span id=red>Error reading from src_atom_id_range.</span>" )
		   && return );

	my $found = 0;
	while ( ( $vsab, $max, $min, $range, $timestamp ) = $sh->fetchrow_array )
	{
		$found++;
		if ( $found == 1 )
		{
			print qq{
			<center>
  <table BORDER=1 WIDTH="70%">
  <form method="POST" action="$cgi">
    <input type="hidden" name="state" value="EDIT_RANGE_DONE">
    <input type="hidden" name="db" value="$db">
  <tr><th width="40%">VSAB</th><th width="20%">Min</th><th width="20%">Max</th><th width="10%"># of src_atom_ids</th><th width="10%">Timestamp</th></tr>
  };
		}
		print qq{
	<tr><td>
	<input type="text" name="vsab" value="$vsab" size="50">
	<input type="hidden" name="orig_vsab" value="$vsab">
	</td>
	<td>$min
	<input type="hidden" name="min" value="$min">
	</td>
	<td>$max</td>
	<td><input type="text" name="range" value="$range">
		<input type="hidden" name="orig_range" value="$range">
	</td>
	<td>$timestamp</td></tr>
	};

	}
	if ($found)
	{
		print qq{
<tr><td colspan="5">&nbsp</td></tr>
<tr><td colspan="5">
<center><input type="submit" value="Submit Adjustments"></center>
</td></tr>
};
	} else
	{
		print qq{
	    <tr><td>$vsab not found in src_atom_id_range at $db.</td></tr>
};
	}

	print qq{
</table></center>
<p>
};
}

###################### Procedure PrintEDIT_RANGE_DONE ######################
#
# This procedure outputs the results of edit range
#
sub PrintEDIT_RANGE_DONE
{
	for ( $i = 0 ; $i <= $#vsab ; $i++ )
	{
		if ( $vsab[$i] ne $orig_vsab[$i] )
		{

			#check for duplicate VSAB
			$sh = $dbh->prepare(
				qq{
	SELECT count(*)
	FROM src_atom_id_range
	WHERE vsab like '%$vsab[$i]%'
    }
			  )
			  || (
				(
				   print
"<span id=red>Error preparing query for src_atom_id_range.</span>"
				)
				&& return
			  );

			$sh->execute
			  || (
				  (
					print
					"<span id=red>Error reading from src_atom_id_range.</span>"
				  )
				  && return
			  );

			my ($present) = $sh->fetchrow_array;

			if ( $present != 0 )
			{
				print
"<span id=red>Error: $vsab already exist in src_atom_id_range.</span><br>\n";
				next;
			}

		  #print "$vsab[$i]: $orig_vsab[$i], $range[$i], $orig_range[$i]<br>\n";
			$sh = $dbh->prepare(
				qq{
	update src_atom_id_range set vsab = ? where vsab = ?
	}
			  )
			  || (
				(
				   print
"<span id=red>Error preparing update src_atom_id_range stmt.</span>"
				)
				&& return
			  );

			$sh->execute( $vsab[$i], $orig_vsab[$i] )
			  || (
				(
				   print
"<span id=red>Error updating src_atom_id_range ($vsab[$i], $orig_vsab[$i] $DBI::errstr).</span>"
				)
				&& return
			  );
			print
			  "$orig_vsab[$i] has been successfully changed to $vsab[$i]<br>\n";
		}

#if the new range is greater than the existing range, then the row will be deleted
#and a new range assigned.
		if ( $range[$i] > $orig_range[$i] )
		{
			$sh = $dbh->prepare(
				qq{
	SELECT max(max)
	FROM src_atom_id_range
    }
			  )
			  || (
				(
				   print
"<span id=red>Error preparing query for src_atom_id_range.</span>"
				)
				&& return
			  );

			$sh->execute
			  || (
				  (
					print
					"<span id=red>Error reading from src_atom_id_range.</span>"
				  )
				  && return
			  );

			my ($max) = $sh->fetchrow_array;

			#if the existing range is the max, then simply resize
			if ( $max == $min[$i] + $orig_range[$i] )
			{
				$sh = $dbh->prepare(
					qq{
	update src_atom_id_range set max = ? where vsab = ?
	}
				  )
				  || (
					(
					   print
"<span id=red>Error preparing update src_atom_id_range stmt.</span>"
					)
					&& return
				  );
				my $new_max = $min[$i] + $range[$i];
				$sh->execute( $new_max, $vsab[$i] )
				  || (
					(
					   print
"<span id=red>Error updating src_atom_id_range ($vsab[$i], $range[$i] $DBI::errstr).</span>"
					)
					&& return
				  );
				print
"The range of $vsab[$i] has been successfully changed to min $min[$i] to max $new_max<br>\n";
				next;
			}
			$sh = $dbh->prepare(
				qq{
	delete from src_atom_id_range where vsab = ?
	}
			  )
			  || (
				(
				   print
"<span id=red>Error preparing deleting src_atom_id_range stmt.</span>"
				)
				&& return
			  );

			$sh->execute( $vsab[$i] )
			  || (
				(
				   print
"<span id=red>Error deleting src_atom_id_range ($vsab[$i], $range[$i] $DBI::errstr).</span>"
				)
				&& return
			  );

			my $new_min = $max + 1;
			my $new_max = $new_min + $range[$i];

			$sh = $dbh->prepare(
				qq{
	INSERT INTO src_atom_id_range (vsab,min,max,timestamp)
	VALUES ('$vsab[$i]',$new_min,$new_max,sysdate)
    }
			  )
			  || (
				(
				   print
"<span id=red>Error preparing insert query for src_atom_id_range.</span>"
				)
				&& return
			  );

			$sh->execute
			  || (
				   (
					 print
					 "<span id=red>Error insert into src_atom_id_range.</span>"
				   )
				   && return
			  );

			print
"The range of $vsab[$i] has been successfully changed to min $new_min to max $new_max<br>\n";
		}

		#if the new range is less than the original, then resize.
		if ( $range[$i] < $orig_range[$i] )
		{
			my $new_max = $min[$i] + $range[$i];
			$sh = $dbh->prepare(
				qq{
	update src_atom_id_range set max = ? where vsab = ?
	}
			  )
			  || (
				(
				   print
"<span id=red>Error preparing update src_atom_id_range stmt.</span>"
				)
				&& return
			  );

			$sh->execute( $new_max, $vsab[$i] )
			  || (
				(
				   print
"<span id=red>Error updating src_atom_id_range ($vsab[$i], $range[$i] $DBI::errstr).</span>"
				)
				&& return
			  );
			print
"The range of $vsab[$i] has been successfully changed to min $min to max $new_max<br>\n";
		}
	}
}
