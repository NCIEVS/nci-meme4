#!@PATH_TO_PERL@
#
# Changes
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
# 12/22/2005 BAC (1-719SM): use open ":utf8" added
#
# This script generates an HTML page that 
# is a template for documenting the schema.
#

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use DBI;
use DBD::Oracle;
use open ":utf8";


############## procedures #############

#
# We will print a HTML table for each table.
# row 1: table name
# row 2: fields
# row 3: involvement in source insertion
# row 4: involvement in editing
# row 5: involvement in production
#
sub Printtable {
    my($table_name) = @_;
    my($sh);
    my($type);
    my($type_dsc);

    $table_name = lc($table_name);

    $short_dsc = $descriptions{$table_name};
    $short_dsc =~ s/([^\.]*)\..*/$1\./;
    if ($mid_table_types{$table_name}) {
      $type = "$mid_table_types{$table_name}";
      $morm = "MID "; 
    }
    if ($mrd_table_types{$table_name}) {
      $type = "$mrd_table_types{$table_name}";
      $morm = "MRD "; 
    }
    @types = split /,/, $type;
    $type_dsc = "";
    foreach $t (@types) {
      $type_dsc .= "<p>$table_type_dsc{$t}</p>";
    }
    $type = qq{<a href="javascript:openDescription('$type','$type_dsc')"><tt>$morm$type</tt></a>} if $type;

    # write to tables_all.html
    print TA qq{
   <tr><td valign="top"><a href="/MEME4/Training/tables/${table_name}.html"
       onMouseOver="window.status='Click here to see table $table_name'; return true;"
       onMouseOut="window.status=''; return true;"><tt>$table_name</tt></a></td>
       <td valigh="top">$type</td>
       <td valigh="top"><font size="-1">$short_dsc</font></td>
  </tr>
};

    # open this table
    open (T,">$ENV{MEME_HOME}/www/MEME4/Training/tables/${table_name}.html") || 
	die "Error, could not open /MEME4/Training/tables/$table_name.html: $! $?\n";

    print T qq{
<html>
<head>
   <title>$meme Tables Documentation - $table_name</title>
    <script language="javascript">
	function openDescription (thing,dsc) {
	    var html = "<html><head><title>Description: "+thing;
	    html = html + "</title></head><body bgcolor=#ffffff>" + dsc + "<center><form><input type=button onClick='window.close(); return true' value='Close'></form></center></body></html>";
	    var win = window.open("","","scrollbars,width=500,height=250,resizable");
	    win.document.open();
	    win.document.write(html);
	    win.document.close();
	}; // end openDescription
    </script>
</head>

<body text="#000000" bgcolor="#FFFFFF" link="#3333FF" vlink="#999999" alink="#FF0000">

<center>

<h2>$meme Tables</h2></center>

<hr width="100%">

<!-- Nav bar -->
  <p><center>[ <a href="#overview">overview</a> | <a href="#details">details</a> | <a href="#references">references</a> ]</center></p>

<!-- Image -->
<p><center><tt><b>$table_name</b></tt><br>
   <img src="/images/table.gif" alt="Table Icon"></center></p>

<!-- Content section -->

<blockquote>

  <a name="overview"></a><h3>Overview/Objective</h3>
  This document contains a description of <tt>$table_name</tt> 
     and its various fields.  It also (may) include information
  about how this table is used in the various stages of MID processing.
<br>&nbsp;
<a name="details"></a><h3>Details</h3>

};

    if ($mid_table_types{$table_name}) {
      $type = "$mid_table_types{$table_name}";
      $morm = "MID "; 
    }
    if ($mrd_table_types{$table_name}) {
      $type = "$mrd_table_types{$table_name}";
      $morm = "MRD "; 
    }
    unless ($type) { $type = "unknown"; }
    @types = split /,/, $type;
    $type_dsc = "";
    foreach $t (@types) {
      $type_dsc .= "<p>$morm$table_type_dsc{$t}</p>";
    }

    print T qq{
    <p>
	
	<!-- $table_name -->

    <a name="$table_name"></a>
    <center>
      <table border="0" width="90%">
        <tr><td valign="top" width="20%"><b>Table name:</b></td><td valign="top" width="80%"><b><tt>$table_name</tt></b></td></tr>
        <tr><td valign="top"><b>Table Type:</b></td><td valign="top">
          <a href="javascript:openDescription('$type','$type_dsc')">$type</a></td></tr>
        <tr><td valign="top"><b>Description:</b></td><td valign="top">$descriptions{$table_name}</td></tr>
        <tr><td valign="top"><b>Fields:</b></td><td valign="top">
	    <table border="1" cellpadding="2" width="90%">
};

    #
    # Get columns.
    #
    $sh = $dbh->prepare(qq{
	SELECT column_name,data_type,data_length,data_precision,nullable
	FROM user_tab_columns where upper(table_name)=upper(?) 
        });
    $sh->execute($table_name);
    while (($column_name,$data_type,$data_length,$data_precision,$nullable)=
	   $sh->fetchrow_array) {

	$data_type = uc($data_type);
	$column_name = lc($column_name);
	if ($data_type eq "NUMBER") {
	    $data_type = "$data_type($data_precision)";
	} elsif ($data_type eq "VARCHAR2") {
	    $data_type = "$data_type($data_length)";
	}
	if ($nullable eq "N") {
	    $data_type = "<b>$data_type</b>";
	}

	# print column name, data type(size), description
	$comments = $tables_fields_to_comments{"$table_name-$column_name"};
	$comments = $fields_to_comments{$column_name} unless $comments;
	print T qq{
	<tr><td width="20%" valign="top"><tt>$column_name</tt></td>
	    <td width="20%" valign="top"><font size="-1">$data_type</font></td>
 	    <td width="60%" valign="top"><font size="-1">$comments&nbsp;</font></td>
	</tr>}
    }

    print T qq{
	    </table>
        </td></tr>
        <tr><td valign="top"><b>Indexes:</b></td><td valign="top">
};

    #
    # Get indexes
    #
    $sh = $dbh->prepare(qq{
	SELECT index_name, column_name FROM meme_ind_columns
	WHERE upper(table_name) = upper(?)
	ORDER BY index_name,column_position
    });
    $sh->execute($table_name);
    $found =0;
    %indexes = ();
    while (($index_name, $column_name) = $sh->fetchrow_array) {
	$found=1;
	$index_name = lc($index_name);
	$column_name= lc($column_name);
	$indexes{$index_name} .= ", $column_name";
    }
    unless ($found) {
	print T qq{
            <font size="-1"><tt>No indexes</tt></font>
	    }
    }

    foreach $key (sort keys %indexes) {
	$col_list = $indexes{$key};
	$col_list =~ s/^, //;
	print T qq{
	     <li><tt>$key ON $col_list</tt></li>
};
    };

    # print sections for roles.
    print T qq{
        </td></tr>
};

    if ($insertion_roles{$table_name}) {
	print T qq{
	<tr><td valign="top"><font size="-1"><b>Insertion&nbsp;Role:</b></font></td><td valign="top"><font size="-1">$insertion_roles{$table_name}&nbsp;</font></td></tr>
};
    };
    
    if ($editing_roles{$table_name}) {
	print T qq{
        <tr><td valign="top"><font size="-1"><b>Editing&nbsp;Role:</b></font></td><td valign="top"><font size="-1">$editing_roles{$table_name}&nbsp;</font></td></tr>
};
    };

    if ($production_roles{$table_name}) {
	print T qq{
	<tr><td valign="top"><font size="-1"><b>Production&nbsp;Role:</b></font></td><td valign="top"><font size="-1">$production_roles{$table_name}&nbsp;</font></td></tr>
};
    };

    print T qq{
      </table>
    </center>
    </p>
};

    ($d,$d,$d,$pday,$pmon,$year) = localtime;
    $pmon++;
    $year+=1900;
    $pday = "00$pday";
    $pday =~ /(..)$/;
    $day = $1;
    $pmon = "00$pmon";
    $pmon =~ /(..)$/;
    $mon = $1;
    $date = $mon."/".$day."/".$year;
    $comments_date = $year."/".$mon."/".$day;

    print T  qq{
<p>
<a name="references"></a><h3>References/Links</h3>
Use the following references for related information.
<ol>
  <li><a href="/MEME4/Training/tables/tables_all.html" alt="All Tables Info">All $meme tables</a></li>
</ol>
</p>
</blockquote>

<p><center>[ <a href="#overview">overview</a> | <a href="#details">details</a> | <a href="#references">references</a> ]</center></p>

<hr WIDTH="100%">
<table BORDER=0 COLS=2 WIDTH="100%" NOSAVE >
<tr NOSAVE>
<td ALIGN=LEFT VALIGN=TOP NOSAVE>
<address>
<a href="/MEME/">MEME Home</a></address>
</td>

<td ALIGN=RIGHT VALIGN=TOP NOSAVE>
<address>
<font size=-1>Contact: <a href="mailto:bcarlsen\@apelon.com">Brian A. Carlsen</a></font></address>

<address>
<font size=-1>Created: 7/27/2001</font></address>

<address>
<font size=-1>Last Updated: $date</font></address>

</td>
</tr>
</table>

</body>
<!-- These comments are used by the What\'s new Generator -->
<!-- Changed On: $comments_date -->
<!-- Changed by: Brian Carlsen -->
<!-- Change Note: MEME Schema documentation - $table_name  -->
<!-- Fresh for: 1 month -->
</html>
}



}

sub PrintHeader {

    print TA qq{
<html>
<head>
   <title>$meme Tables Documentation</title>
    <script language="javascript">
	function openDescription (thing,dsc) {
	    var html = "<html><head><title>Description: "+thing;
	    html = html + "</title></head><body bgcolor=#ffffff>" + dsc + "<center><form><input type=button onClick='window.close(); return true' value='Close'></form></center></body></html>";
	    var win = window.open("","","scrollbars,width=500,height=250,resizable");
	    win.document.open();
	    win.document.write(html);
	    win.document.close();
	}; // end openDescription
    </script>
</head>
<body text="#000000" bgcolor="#FFFFFF" link="#3333FF" vlink="#999999" alink="#FF0000">

<center>
<h2>$meme Tables</h2></center>

<!-- Nav bar -->
  <p><center>[ <a href="#overview">overview</a> | <a href="#details">details</a> | <a href="#references">references</a> ]</center></p>

<!-- Image -->
<p><center><img src="/images/table.gif" alt="Table Icon">
<img src="/images/table.gif" alt="Table Icon">
<img src="/images/table.gif" alt="Table Icon">
</center></p>

<!-- Content section -->

<blockquote>

  <a name="overview"></a><h3>Overview/Objective</h3>
  This document links to more detailed descriptions of the $meme tables.
<br>&nbsp;
<a name="details"></a><h3>Details</h3>
This document was machine generated from the list of tables found in 
the <tt>meme_tables</tt> table in the $db database.  The links below
contain specific details about the tables themselves, including: 
the field data types, whether they are null or not, the indexes present
on each table, and how the table is used during insertion, editing,
and production.
<br>&nbsp;

<a name="details"></a><h3>References/Links</h3>
<center><table width=90%>
   <tr><th valign="top">Table Name</th>
       <th valigh="top">Type</th>
       <th valigh="top">Description</th>
  </tr>
};

}

sub PrintFooter {
    
    ($d,$d,$d,$pday,$pmon,$year) = localtime;
    $pmon++;
    $year+=1900;
    $pday = "00$pday";
    $pday =~ /(..)$/;
    $day = $1;
    $pmon = "00$pmon";
    $pmon =~ /(..)$/;
    $mon = $1;
    $date = $mon."/".$day."/".$year;
    $comments_date = $year."/".$mon."/".$day;

    print TA  qq{
</table>
<p>
<hr WIDTH="100%">
<table BORDER=0 COLS=2 WIDTH="100%" NOSAVE >
<tr NOSAVE>
<td ALIGN=LEFT VALIGN=TOP NOSAVE>
<address>
<a href="/MEME/">MEME Home</a></address>
</td>

<td ALIGN=RIGHT VALIGN=TOP NOSAVE>
<address>
<font size=-1>Contact: <a href="mailto:bcarlsen\@apelon.com">Brian A. Carlsen</a></font></address>

<address>
<font size=-1>Created: 7/27/2001</font></address>

<address>
<font size=-1>Last Updated: $date</font></address>

</td>
</tr>
</table>

<!-- Nav bar -->
  <p><center>[ <a href="#overview">overview</a> | <a href="#details">details</a> | <a href="#references">references</a> ]</center></p>

</body>
<!-- These comments are used by the What\'s new Generator -->
<!-- Changed On: $comments_date -->
<!-- Changed by: Brian Carlsen -->
<!-- Change Note: Index for MEME Schema documentation -->
<!-- Fresh for: 1 month -->
</html>
}

}

