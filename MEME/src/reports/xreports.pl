#!@PATH_TO_PERL@
#
# File:     reports.pl
# Author:   Brian Carlsen 
#
# Simple client to run a report
#
# Changes:
# 12/22/2005 BAC (1-719SM): use open ":utf8" added
#
# Version Information
#
# 04/22/2005 4.9.1:  Support these params:
#                    -url_mid_for_concept_id=
#                    -url_mid_for_code=
#                    -url_mid_for_cui=
#                    -url_release_for_cui=
#                    -url_release_for_sty=
# 06/14/2004 4.9.0:  Supports -lat parameter to restrict to specified 
#                    languages list
# 12/19/2003 4.8.0:  Supports -max parameter to restrict rels view.
# 04/25/2003 4.7.0:  Supports <div> as well as <pre> style HTML reports
# 04/25/2003 4.6.0:  Released
# 04/22/2003 4.5.1:  Better error reporting of badly formatted ids
# 04/09/2003 4.5.0:  Fixed minor bug in cluster numbering
# 04/02/2003 4.4.0:  Last release dropped transformations
#                    to support enscript-style escape sequences
#                    i.e. converting &#x0 into \000. replaced & released
# 03/19/2003 4.3.0:  Released
#                    - Fixed exception handling for new document type
# 03/14/2003 4.2.1:  -cluster flag can be use with list of ids.
# 09/03/2002 4.2.0:  Script was upgraded to conform with new XML document
#                    semantics.
# 03/26/2002 4.1.0:  Extended reports.pl to support crazy options
#
$release = "4";
$version = "9.0";
$version_date = "06/15/2004";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set Defaults & Environment
#
unless ($ENV{MEME_HOME}) {
    $badvalue="MEME_HOME";
    $badargs=4;
}

# Obtain defaults from midsvcs.pl
# ($host,$port) = ("localhost","8080");

$max_r_rel_ct = -1;
$rel_opt = 1;
$cxt_rel_opt = 1;
$cxt_attr_notdisplay="false";
%r_map = (
	  "NONE" => 0,
	  "DEFAULT" => 1,
	  "XR" => 2,
	  "SIB" => 2,
	  "ALL" => 3 
	  );

# content_type is "text/plain", "text/html", "text/enscript"
$content_type="text/plain";

# id type is "concept_id", "atom_id", "cui"
$id_type = "concept_id";

# form feed flag, default is no
$ff = 0;

# cluster flag, default is no
$cluster = 0;

#
# Check options
#
&HandleArguments;

#
# Set host/port defaults from MIDSVCS server
#
unless ($host) {
  $host =`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-host`;
  chop($host);
}

unless ($port) {
  $port =`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-port`;
  chop($port);
}

#
# Parse parameters
# There should be zero parameters
if (scalar(@ARGS) != 0) {
    $badargs = 3;
    $badvalue = scalar(@ARGS);
}

%errors = (1 => "Illegal switch: $badvalue",
	   3 => "Bad number of arguments: $badvalue",
	   4 => "$badvalue must be set",
	   5 => "Badly formatted color: $badvalue",
	   6 => qq{Badly formatted style argument.
The allowable parameters are:
   regexp    specify the regular expression  
   section   specify the section of the report
   color     specify a color #RRGGBB
   bold      indicate true/false
   underline indicate true/false
   italics   indicate true/false
   shade     indicate a shade factor 0.0 - 1.0},
	   7 => "Bad bold value (must be true or false): $badvalue",
	   8 => "Bad italics value (must be true or false): $badvalue",
	   9 => "Bad underline value (must be true or false): $badvalue",
	   10 => "Bad shade value (must be 0.0 - 1.0): $badvalue",
	   );

if ($badargs) {
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(1);
}

if ($debug) {
  &PrintDebug;
 }

#
# Connect to server, send ServerShutdown message
#
require 5.003;
use Socket;
$proto = getprotobyname("tcp");

#
# At this point there are several options
# 1. We are using raw ids and @ids will not be null
# 2. We are using a file of ids, read into @ids
# 3. We are using a cluster'd file, read clusters
#    so that we can print the proper separators.
#     $cluster_id => (@ids)
#

if (@ids) {
  if ($cluster eq "true") {
    print "You must supply a cluster id (e.g. -cluster=34)\n";
    exit(1);
  }
  if ($cluster) {
    foreach $id (@ids) {
      $clusters{$cluster}->[scalar(@{$clusters{$cluster}})] = "$id";
    }
  }
} elsif ($file) {
    my ($cid)=1;
    open (F,"$file") || (print "Could not open file $file: $!\n" && exit 255);
    if ($cluster) {
      while (<F>) {
	chop;
	($id,$cluster_id) = split /\|/;
	$cluster_id = $cid++ if ($cluster_id eq "");
	#unless ($_ =~ /\|/) {
	#  print "File must contain two fields, id and cluster_id:\n$_\n";
	#  exit 0;
	#}

	# push onto cluster list
	$clusters{$cluster_id}->[scalar(@{$clusters{$cluster_id}})] = "$id";
      }
    } else {
      @ids = <F>;
      chomp(@ids);
    }
    close(F);
}


#
# If we are dealing with clusters, do it one way
#
if ($cluster) {

  if (%clusters) {
    foreach $cluster_id (sort {$a <=> $b} keys %clusters) {
      @ids = @{$clusters{$cluster_id}};
      $clct = 1;
      $max = $#ids+1;
      foreach $id (@ids) {
	unless (($id_type eq "cui" && $id =~ /^C[L]*\d*$/) ||
		($id_type ne "cui" && $id =~ /^\d*$/)) {
	  print "Badly formatted id ($id) in $file\n." if $file;
	  print "Badly formatted id ($id)\n" unless $file;
	  exit 255;
	}
	if ($form_feed) {
	  print "\f" if $clct > 1;
	}
	print "\n   xxxxxxxxxx (Cluster# $cluster_id: $clct of $max) xxxxxxxxxx\n\n";
	$clct++;
	&PrintReport($id);
      }
    }
   }
}

#
# If not clusters, we just have a bunch of ids
#
else {

  if (@ids) {
    foreach $id (@ids) {
      unless (($id_type eq "cui" && $id =~ /^C\d*$/) ||
	      ($id_type ne "cui" && $id =~ /^\d*$/)) {
	print "Badly formatted id ($id) in $file\n." if $file;
	print "Badly formatted id ($id)\n" unless $file;
	exit 255;
      }
      if ($form_feed) {
	print "\f" if $ct++ > 0;
      } else {
	 print "______________________________________________________________________________\n\n"
	   if $ct++ > 0;
      }
      &PrintReport($id);
      $ct++;
    }
  } else {
    print "You must specify an id with -c, -a, or -i.\n";
    exit 1;
  }

}

exit 0;

######################### LOCAL PROCEDURES #######################

sub PrintReport {
  my ($id) = @_;

  #
  # Change document depending on concept_id/cui
  #
  if ($id_type eq "cui") {
    $param_text = qq{          <Object name="name" id="3">cui</Object>
          <Object name="value" id="4"  class="java.lang.String" >$id</Object>};
  } elsif ($id_type eq "atom_id") {
    $param_text = qq{          <Object name="name" id="3">atom_id</Object>
          <Object name="value" id="4"  class="java.lang.Integer">$id<</Object>};
  } else {
    $param_text = qq{          <Object name="name" id="3">concept_id</Object>
          <Object name="value" id="4"  class="java.lang.Integer">$id</Object>};
  };

  #
  # set param and id counts based on above paramters
  #
  $param_ct = 4;
  $id_max = 11;

  #
  # Add style elements
  #
  $style_ct = 0;
  foreach $style (@styles) {
    $style_params = 
      "$style->{regexp}:$style->{section}:$style->{color}:$style->{shade}:$style->{bold}:$style->{underline}:$style->{italics}";

    # $key must be in: regexp, section, color, shade, bold, underline, italics

    $style_text .= qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="false">
          <Object name="name" id="}.++$id_max.qq{">style$style_ct</Object>
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.String">$style_params</Object>
        </Object>
	};
    $style_ct++;
  }

  #
  # we need to add MEME_HOME 
  # if html mode
  #
  if ($content_type eq "text/html") {
    $html_text = qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="false">
          <Object name="name" id="}.++$id_max.qq{">meme_home</Object>
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.String">$ENV{MEME_HOME}</Object>
	</Object>
	};
  }

  #
  # If max_r_rel_ct is set, pass it
  #
  if ($max_r_rel_ct != -1) {
    $max_text = qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="}.++$id_max.qq{">max_r_rel_count</Object>
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.Integer">$max_r_rel_ct</Object>
	</Object>
	};
  }

  #
  # If url_mid_for_concept_id is set, pass it
  #
  if ($url_mid_for_concept_id) {
    $url_mid_for_concept_id = &UnAndReEncode($url_mid_for_concept_id);
    $url_concept_id_text = qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="}.++$id_max.qq{">url_mid_for_concept_id</Object>
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.String">$url_mid_for_concept_id</Object>
	</Object>
	};
  }

  #
  # If url_mid_for_cui is set, pass it
  #
  if ($url_mid_for_cui eq "true") {
    $url_mid_for_cui = &UnAndReEncode($url_mid_for_cui);
    $url_cui_text = qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="}.++$id_max.qq{">url_mid_for_cui</Object>
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.String">$url_mid_for_cui</Object>
	</Object>
	};
  }

  #
  # If url_mid_for_code is set, pass it
  #
  if ($url_mid_for_code) {
    $url_mid_for_code = &UnAndReEncode($url_mid_for_code);
    $url_code_text = qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="}.++$id_max.qq{">url_mid_for_code</Object>
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.String">$url_mid_for_code</Object>
	</Object>
	};
  }

  #
  # If url_release_for_cui is set, pass it
  #
  if ($url_release_for_cui) {
    $url_release_for_cui = &UnAndReEncode($url_release_for_cui);
    $url_release_cui_text = qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="}.++$id_max.qq{">url_release_for_cui</Object>
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.String">$url_release_for_cui</Object>
	</Object>
	};
  }

  #
  # If url_release_for_sty is set, pass it
  #
  if ($url_release_for_sty) {
    $url_release_for_sty = &UnAndReEncode($url_release_for_sty);
    $url_release_sty_text = qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="}.++$id_max.qq{">url_release_for_sty</Object>
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.String">$url_release_for_sty</Object>
	</Object>
	};
  }

  #
  # If cxt_attr_notdisplay is set, pass it
  #
  if ($cxt_attr_notdisplay eq "true") {
    $cxt_attr_notdisplay = &UnAndReEncode($cxt_attr_notdisplay);
    $cxt_attr_notdisplay_text = qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="false">
          <Object name="name" id="}.++$id_max.qq{">cxt_attr_notdisplay</Object>
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.String">$cxt_attr_notdisplay</Object>
	</Object>
	};
  }

  #
  # Languages
  #
  # e.g.
  #
  #<Object name="2" id="7"  primitive="true">
  #<Var name="name" value="include_or_exclude" />
  #<Object name="value" id="8"  class="java.lang.Boolean">true</Object>
  #</Object>
  #
  #<Object name="4" id="11" >
  #<Var name="name" value="selected_languages" />
  #<Object name="value" id="12"  class="java.lang.String" length="2">
  #<Var name="0" value="ENG" />
  #<Var name="1" value="SPA" />
  #</Object>
  #</Object>
  #
  if ($lat_list) {
    $in_or_out_text = qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{" primitive="true">
          <Var name="name" value="include_or_exclude" />
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.Boolean">true</Object>
	</Object>
	};
    $latlen = scalar(@lats);
    $lats_text = qq{<Object name="}.$param_ct++.qq{" id="}.++$id_max.qq{">
          <Var name="name" value="selected_languages" />
          <Object name="value" id="}.++$id_max.qq{"  class="java.lang.String" length="$latlen">
          };
    $latid = 0;
    foreach $lat (@lats) {
      $lats_text .= qq{<Var name="$latid" value="$lat" />
          };
      $latid++;
    }
    $lats_text .= qq{</Object>
	</Object>
	};
  }
  

  
  socket(SOCK, PF_INET, SOCK_STREAM, $proto);
  $sin = sockaddr_in($port, inet_aton($host));
  $x = connect(SOCK, $sin);
  unless ($x) {
    die qq{
Connection to MEME Application Server refused
The most likely reason is that the server is
not currently running on $host at port $port.
};
  }
  
  binmode(SOCK,":utf8");
  select(SOCK);
  $| = 1;
  select(STDOUT);

  #<!DOCTYPE MASRequest SYSTEM "MASRequest.dtd">

  #
  # Send Request
  #
  print SOCK qq{POST / HTTP/1.1

<MASRequest>
  <ConnectionInformation>
    <Session nosession="true" />
    <DataSource service="$service" />
  </ConnectionInformation>
  <ServiceParameters>
    <Service>ReportsGenerator</Service>
    <Parameter>
      <Object name="" id="1"  class="gov.nih.nlm.meme.common.Parameter" length="$param_ct">
        <Object name="0" id="2"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
	  $param_text
        </Object>
        <Object name="1" id="3"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="4">rel_opt</Object>
          <Object name="value" id="5"  class="java.lang.Integer">$rel_opt</Object>
        </Object>
        <Object name="2" id="6"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="7">cxt_rel_opt</Object>
          <Object name="value" id="8"  class="java.lang.Integer">$cxt_rel_opt</Object>
        </Object>
        <Object name="3" id="9"  class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="false">
          <Object name="name" id="10">content_type</Object>
          <Object name="value" id="11"  class="java.lang.String" >$content_type</Object>
        </Object>
	$style_text$html_text$max_text$url_concept_id_text$url_code_text$url_cui_text$url_release_cui_text$url_release_sty_text$in_or_out_text$lats_text$cxt_attr_notdisplay_text
      </Object>
    </Parameter>
  </ServiceParameters>
  <ClientResponse>
  </ClientResponse>
</MASRequest>
};

  $found=0;
  $message = "";
  $ct=0;
  while (<SOCK>) {
#      print;
    $found = 1 if s/.*(As of.*issues)/$1/;
    $found = 1 if s/.*(<pre>)/$1/;
    $found = 1 if s/.*(<div)/$1/;
    $found = 0 if ($found && /\]\]\>\<\/Object\>/);

    if (/name="details"/) {
      $inhashmap = 1;
     }
    if ($inhashmap) {
      if (/.*name="key".*DATA\[(.*)\]\]\>\<\/Object\>/ ||
	  /.*name="key".*\>(.*)\<\/Object\>/) {
	$key = $1;
      }
      if (s/.*name="value".*DATA\[(.*)\]\]\>\<\/Object\>/$1/ ||
	  s/.*name="value".*\>(.*)\<\/Object\>/$1/) {
	$details{$key}="$1";
      }
    }

    if (s/.*name="message".*DATA\[(.*)\]\]\>\<\/Object\>/$1/ ||
	s/.*name="message".*\>(.*)\<\/Object\>/$1/ ||
	/message="(.*)"/) {
	$message = $1;
    }
    s/&#x0;/\000/g if $content_type eq "text/enscript";
    print if $found;
  }
  close(SOCK);

  if ($message) {
    print "Exception: $message\n";
    print "\t{";
    foreach $key (sort keys %details) {
      print ",\n\t " unless ($ct++ == 0);
      print "$key => $details{$key}";
    }
    print "}\n";
  }
  
}

#
# Debugging purposes, print out options
#
sub PrintDebug {

print "content-type: $content_type\n";
print "file: $file\n";
print "id type: $id_type\n";
print "form feed: $form_feed\n";
print "cluster: $cluster\n";
if (@ids) {
  print "Ids:\n";
  foreach $id (@ids) {
    print "\t$id\n";
  }
}
print "color map:\n";
foreach $key (keys %color_map) {
  print "$key => $color_map{$key}\n";
}
print "enscript params: $e_ptn, $e_sec, $e_shade, $e_red, $e_green, $e_blue\n";
print "host: $host\n";
print "port: $port\n";
print "rel_opt: $rel_opt\n";
print "cxt_rel_opt: $cxt_rel_opt\n";
print "data source: $service\n";
exit 0;

}
sub HandleArguments {

@ARGS=();
while (@ARGV) {
  $arg = shift(@ARGV);
  if ($arg !~ /^-/) {
    push @ARGS, $arg;
    next;
   }
  
  if ($arg eq "-version") {
    print "Version $version, $version_date ($version_authority).\n";
    exit(0);
   }
  elsif ($arg eq "-v") {
    print "$version\n";
    exit(0);
   }
  elsif ($arg eq "-help" || $arg eq "--help") {
    &PrintHelp;
    exit(0);
   }
  elsif ($arg eq "-debug") {
    $debug =1; }
  elsif ($arg =~ /^-d=(.*)$/) {
    $service = $1;  }
  elsif ($arg =~ /^-d$/) {
    $service = shift(@ARGV);  }
  elsif ($arg =~ /^-html$/) {
    $content_type="text/html";  }
  #add cxt_attr_notdisplay for concept report w/o context
  elsif ($arg =~ /^-reporttype=3$/) {
    $cxt_attr_notdisplay="true";  }
  elsif ($arg =~ /^-enscript$/) {
    $content_type="text/enscript";  }
  elsif ($arg =~ /^-a=(.*)$/) {
    $atoms = $1;  $id_type="atom_id";
    @ids = split /,/,$atoms; }
  elsif ($arg =~ /^-a$/) {
    $atoms = shift(@ARGV);  $id_type="atom_id";
    @ids = split /,/,$atoms; }
  elsif ($arg =~ /^-style=(.*)$/) {
    &AddStyle($1); }
  elsif ($arg =~ /^-style$/) {
    &AddStyle(shift(@ARGV));}
  elsif ($arg =~ /^-c=(.*)$/) {
    $concepts = $1;  $id_type="concept_id";
    @ids = split /,/,$concepts; }
  elsif ($arg =~ /^-c$/) {
    $concepts = shift(@ARGV);  $id_type="concept_id";
    @ids = split /,/,$concepts; }
  elsif ($arg =~ /^-i=(.*)$/) {
    $cuis = $1;  $id_type="cui";
    @ids = split /,/,$cui; }
  elsif ($arg =~ /^-i$/) {
    $cuis = shift(@ARGV);  $id_type="cui";
    @ids = split /,/,$cuis; }
  elsif ($arg =~ /^-fa=(.*)$/) {
    $file = $1;  $id_type="atom_id"; }
  elsif ($arg =~ /^-fa$/) {
    $file = shift(@ARGV);  $id_type="atom_id"; }
  elsif ($arg =~ /^-fc=(.*)$/) {
    $file = $1;  $id_type="concept_id"; }
  elsif ($arg =~ /^-fc$/) {
    $file = shift(@ARGV);  $id_type="concept_id"; }
  elsif ($arg =~ /^-fi=(.*)$/) {
    $file = $1;  $id_type="cui"; }
  elsif ($arg =~ /^-fi$/) {
    $file = shift(@ARGV);  $id_type="cui"; }
  elsif ($arg =~ /^-ff$/) {
    $form_feed = 1; 
    $| = 1;   }
  elsif ($arg =~ /^-cluster=(.*)$/) {
    $cluster = $1;  }
  elsif ($arg =~ /^-cluster$/) {
    if ($ARGV[0] !~ /^-/) {
      $cluster = shift(@ARGV);
    } else {
      $cluster = "true";
    }
  }
  elsif ($arg =~ /^-port=(.*)$/) {
    $port = $1;  }
  elsif ($arg =~ /^-r=(NONE|DEFAULT|XR|ALL|0|1|2|3)$/) {
    $rel_opt = $r_map{"$1"} || $1;   }
  elsif ($arg =~ /^-r$/) {
    $rel_opt = shift(@ARGV);
    $rel_opt = $r_map{"$rel_opt"} || $rel_opt;   }
  elsif ($arg =~ /^-x=(NONE|DEFAULT|SIB|ALL|0|1|2|3)$/) {
    $cxt_rel_opt = $r_map{$1} || $1; }
  elsif ($arg =~ /^-x$/) {
    $cxt_rel_opt = shift(@ARGV);
    $cxt_rel_opt = $r_map{"$cxt_rel_opt"} || $cxt_rel_opt;   }
  elsif ($arg =~ /^-max=(.*)$/) {
    $max_r_rel_ct = $1;  }
  elsif ($arg =~ /^-max$/) {
    $max_r_rel_ct = shift(@ARGV);  }
  elsif ($arg =~ /^-lat=(.*)$/) {
    $lat_list = $1;  
    @lats = split /,/, $lat_list; }
  elsif ($arg =~ /^-lat$/) {
    $lat_list = shift(@ARGV); 
    @lats = split /,/, $lat_list; }

  elsif ($arg =~ /^-url_mid_for_concept_id=(.*)$/) {
    $url_mid_for_concept_id = $1;  }
  elsif ($arg =~ /^-url_mid_for_concept_id$/) {
    $url_mid_for_concept_id = shift(@ARGV); }
  elsif ($arg =~ /^-url_mid_for_code=(.*)$/) {
    $url_mid_for_code = $1;  }
  elsif ($arg =~ /^-url_mid_for_code$/) {
    $url_mid_for_code = shift(@ARGV); }
  elsif ($arg =~ /^-url_mid_for_cui=(.*)$/) {
    $url_mid_for_cui = $1;  }
  elsif ($arg =~ /^-url_mid_for_cui$/) {
    $url_mid_for_cui = shift(@ARGV); }
  elsif ($arg =~ /^-url_release_for_cui=(.*)$/) {
    $url_release_for_cui = $1;  }
  elsif ($arg =~ /^-url_release_for_cui$/) {
    $url_release_for_cui = shift(@ARGV); }
  elsif ($arg =~ /^-url_release_for_sty=(.*)$/) {
    $url_release_for_sty = $1;  }
  elsif ($arg =~ /^-url_release_for_sty$/) {
    $url_release_for_sty = shift(@ARGV); }

  elsif ($arg =~ /^-host=(.*)$/) {
    $host = $1;  }
  elsif ($arg =~ /^-host$/) {
    $host = shift(@ARGV);  }
  elsif ($arg =~ /^-port=(.*)$/) {
    $port = $1;  }
  elsif ($arg =~ /^-port$/) {
    $port = shift(@ARGV);  }
  else {
    $badargs = 1;
    $badvalue = $arg;
   }
 }
}

sub UnAndReEncode {
  my($arg) = @_;
  $arg =~ s/&amp;/&/g;
  $arg =~ s/&lt;/</g;
  $arg =~ s/&gt;/>/g;
  $arg =~ s/&/&amp;/g;
  $arg =~ s/</&lt;/g;
  $arg =~ s/>/&gt;/g;
  return $arg;
}

sub AddStyle {
  my($arg) = @_;
  $arg =~ s/ //g;
  @kv = split /;/,$arg;
  my($style);
  # default values
  $style->{"regexp"} = ".*";
  $style->{"bold"} = "false";
  $style->{"italics"} = "false";
  $style->{"underline"} = "false";
  foreach $kv (@kv) {
    ($key,$value) = split /=/, $kv;

    # $key must be in: regexp, section, color, shade, bold, underline, italics
    if ($key eq "regexp") {
      $style->{"regexp"} = $value;
    } elsif ($key eq "section") {
      $style->{"section"} = $value;
    } elsif ($key eq "color") {
      if ($value =~ /^#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})$/) {
	$style->{"color"} = $value;
      } else {
	$badargs = 5; $badvalue =$value;
      };
    } elsif ($key eq "shade") {
      if ($value !~ /[0-1]?\.\d{1,}/) { $badargs = 10; $badvalue="$value"}
      $style->{"shade"} = $value;
    } elsif ($key eq "bold") {
      if ("truefalse" !~ /$value/) { $badargs = 7; $badvalue=$value}
      $style->{"bold"} = $value;
    } elsif ($key eq "italics") {
      if ("truefalse" !~ /$value/) { $badargs = 8; $badvalue=$value}
      $style->{"italics"} = $value;
    } elsif ($key eq "underline") {
      if ("truefalse" !~ /$value/) { $badargs = 9; $badvalue=$value}
      $style->{"underline"} = $value;
    } else {
      $badargs = 6;
    }
  }
  push @styles, $style;
}

sub PrintUsage {

	print qq{ This script has the following usage:
    xreports.pl [-d <database/service>]
		[-html] [-enscript]
 		[-style "[key=value[;key=value]]" ]
		[-a atom_id[,atom_id]]
		[-c concept_id[,concept_id]]
		[-cxt]
		[-i CUI[,CUI]]
		[-ff]
		[-fa <file of atom id>]
		[-fc <file of concept id>]
		[-fi <file of cui id>]
		[-cluster]
		[-cluster <#>]
	        [-r {0|1|2|3}]
	        [-x {0|1|2|3}]
		[-lat <lat,lat>]
		[-max <#>]
	        [-host <host>]
	        [-port <port>]
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 Options:
       -d <db>:       Specify a database (e.g. oa_mid2003) or a
	              MID service (e.g. editing-db)
       -a <id,[id]>:  Specify an atom_id or list of atom_ids to
	              generate reports for.
       -c <id,[id]>:  Specify a Concept_id or list of concept_ids to
	              generate reports for.
       -i <id,[id]>:  Specify a CUI or list of CUI to generate reports for.
       -html:         Generate an HTML-ized report
       -cxt:          Include contexts
       -enscript:     Generate an enscript-ized report
       -style <arg>:  When using html/enscript reports, you can
	              specify style parameters to indicate
		      that a particular regular expression found
		      in a particular section of the report
		      should be given a certain color or should
		      appear in bold/italics or underlined. The 
		      style parameters should be separated by ;
                      and with the format "key=value".  The valid
		      keys are:
		        regexp    specify the regular expression  
		        section   specify the section of the report
			color     specify a color #RRGGBB
			bold      indicate true/false
			underline indicate true/false
			italics   indicate true/false
			shade     indicate a shade factor 0.0 - 1.0

		      For example,

             -style "regexp=.*MSH{\\d*}.*; section=ATOMS,STY; color=#ff0000;"

	              This says to find any line matching MSH{\d*}
                      in the ATOMS or STY sections of the report
		      and color those red.
       -ff:           When multiple reports are being produced,
                      separate each by a form feed "\\f" character.
       -fa <file>:    Pass a file of atom ids
       -fc <file>:    Pass a file of concept ids
       -fi <file>:    Pass a file of CUIs
       -cluster:      If using -fa, -fc, or -fi, indicate that the
		      file contains id|cluster_id instead of just id
       -lat <lat,lat> Restricts the atoms in the report to those from
		      the specified languages.  Must use comma separated list
       -max <#>:      Restricts the number of reviewed relationships
		      shown in the report to the specified number
       -r {0|1|2|3}:  Set relationship display option
                       0 (NONE) - Don't show any relationships
                       1 (DEFAULT) - Show winning relationships
                       2 (XR) - Show winning, XR, and corresponding rels
                       3 (ALL) - Show all relationships
       -x {0|1|2|3}:  Set context relationship display option
                       0 (NONE) - Don't show any context rels
                       1 (DEFAULT) - Show PAR, CHD if no contexts or rels
                       2 (SIB) - Same as 1, but include SIBs
                       3 (ALL) - Show all context relationships
       -url_mid_for_concept_id: Specify concept_id URL base in HTML report
       -url_mid_for_code:       Specify code URL base in HTML report
       -url_mid_for_cui:        Specify cui URL base in HTML report
       -url_release_for_cui:    Specify cui URL base in release HTML report
       -url_release_for_sty:    Specify STY URL base in release HTML report
       -host=<host>:  Machine where the application server is running,
	              Default is localhost
       -port=<port>:  Port on which server is listening, default is 8080
       -v[ersion]:    Print version information.
       -[-]help:      On-line help

 Version $version, $version_date ($version_authority)
};
}



