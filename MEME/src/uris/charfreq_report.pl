# The UMLS Release Information System (URIS) version 2.0
# suresh@nlm.nih.gov 10/2003

# Displays character frequencies (Unicode aware)

# CGI params:
# sortby=char|freq
# showchar=any|nonascii|suspicious|...
# showblock=any|...
# showfile=

sub charfreq_report {
  my($html);
  my($releaseconfig) =	UrisUtils->loadReleaseConfig($query->param('configfile'));
  my($outputfile) = join('/', $releaseconfig->{outputdir}, "charfreq.stdout");

  my(@suspicious) = (
		   1..9, 11..31, 127..255
		  );

  my($badcolor) = "#F6F6C4";

  if (-e $outputfile) {
    $metadata = UrisUtils->evalfile($outputfile);
    $html .= &dump_data($metadata);
  } else {
    $html .= "The script to compute character frequency has apparently not been run for this release.";
  }

  print join('',
	     $query->header,
	     $query->start_html($uristitle),
	     $html,
	     $query->end_html);
  return;
}

sub dump_data {
  my($charfreq) = @_;
  my($html);
  my($blocks) = UrisUtils->parse_nameslist;

  my(%b, @blocknames, $x);
  foreach $x (@$blocks) {
    next if $b{$x->{name}};
    $b{$x->{name}}++;
    push @blocknames, $x->{name};
  }

  $form .= $query->start_form;

  @x = ('any', 'extended (8-bit)', 'suspicious', 'Unicode - missing', 'Unicode - not a character', 'Unicode - reserved');
  $form .= "Show characters ranges: " . $query->popup_menu(-name=>'showchar', -value=>\@x) . $query->br;
  $form .= "Show blocknames: " . $query->popup_menu(-name=>'showblock', -value=>[ 'any', @blocknames ]) . $query->br;
#  $form .= "Show blocknames: " . $query->scrolling_list(-name=>'showblock', -value=>[ 'any', @blocknames ], size=>5, multiple=>1) . $query->br;
  $form .= "Show file: " . $query->popup_menu(-name=>'showfile', -value=>['all', sort keys %{ $charfreq } ]) . $query->br;
  $form .= $query->br;
  $form .= " Sort by: " . $query->popup_menu(-name=>'sortby', -value=>['character', 'frequency']);
  $form .= UrisUtils->make_hidden($query, { action=>$action, configfile=>$query->param('configfile') });
  $form .= $query->p;
  $form .= "(Suspicious characters are in the Basic Latin set < 32 (except LINEFEED) and above 127.)";
  $form .= $query->p;
  $form .= $query->submit;
  $form .= $query->end_form;

  $html .= $form;
  $html .= $query->hr;

  my(@rows, @cols, @x);

  return $html unless
    $query->param('showchar') ||
    $query->param('showblock') ||
    $query->param('showfile');

  foreach $file (sort keys %{ $charfreq }) {
    if ($query->param('showfile') && $query->param('showfile') ne "all") {
      next if $file ne $query->param('showfile');
    }

    @rows = ();

    if ($query->param('sortby') =~ /^freq/i) {
      @chars = sort { $charfreq->{$file}->{$a}->{count} <=> $charfreq->{$file}->{$b}->{count} } keys %{ $charfreq->{$file} };
    } else {
      @chars = sort { $a <=> $b } keys %{ $charfreq->{$file} };
    }

    $n=0;
    foreach $c (@chars) {
      if ($query->param('showchar') && $query->param('showchar') ne 'any') {
	if ($query->param('showchar') =~ /extended/i) {
	  next unless $c > 127;
	} elsif ($query->param('showchar') =~ /suspicious/i) {
	  next unless (($c < 10) || ($c > 10 && $c < 32) || $c > 127);
	} elsif ($query->param('showchar') eq "Unicode - missing") {

	  $feature = 0;
	  foreach (@{ $charfreq->{features} }) {
	    $feature = 1 if /missing/i;
	  }
	  next unless $feature;

	} elsif ($query->param('showchar') eq "Unicode - not a character") {

	  $feature = 0;
	  foreach (@{ $charfreq->{features} }) {
	    $feature = 1 if /Not a character/i;
	  }
	  next unless $feature;

	} elsif ($query->param('showchar') eq "Unicode - reserved") {

	  $feature = 0;
	  foreach (@{ $charfreq->{features} }) {
	    $feature = 1 if /reserved/i;
	  }
	  next unless $feature;
	}

      } elsif ($query->param('showblock') && $query->param('showblock') ne 'any') {
	next unless (($charfreq->{$file}->{$c}->{blockname}) eq ($query->param('showblock')));
      }

      @cols = ();
      $r = $charfreq->{$file}->{$c};
      push @cols, $query->td({align=>'right'}, ++$n);
      push @cols, $query->td($file);
      push @cols, $query->td($r->{charname});

      push @cols, $query->td($r->{blockname});

      @x = ();
      push @x, "U+" . $r->{unicode};
      if ($c < 128) {
	push @x, "Dec: $c";
	push @x, sprintf("Oct: %.3o", $c);
	push @x, sprintf("Hex: 0x%x", $c);
      }
      push @cols, $query->td({-align=>'right'}, join($query->br, @x));

      push @cols, $query->td({-align=>'right'}, $r->{count});
      push @rows, join("\n", @cols) . "\n";
    }
    if (@rows) {
      unshift @rows, $query->th(['', 'File', 'Character', 'Blockname', 'Code', 'Freq']);
      $html .= $query->h2($file);
      $html .= $query->table({-border=>1, -cellpadding=>5, -cellspacing=>0, width=>'90%'}, $query->Tr(\@rows));
    }
  }
  return $html;
}

1;

