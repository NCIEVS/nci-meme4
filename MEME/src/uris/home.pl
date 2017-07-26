# Home page
# suresh@nlm.nih.gov 10/2003

sub home {
  my($html);
  my(@rows);
  my(@releaseconfig) = UrisUtils->loadUrisConfig($urisconfig);

  my($releaseconfig) = (
			$query->param('configfile') ?
		UrisUtils->loadReleaseConfig($query->param('configfile')) :
			$releaseconfig[0]
		       );
#   my($releaseconfig) = UrisUtils->loadReleaseConfig('2006AA.config');
  my($configfile) = $query->param('configfile') || $releaseconfig->{configfile};

  push @rows, $query->th(['Name', 'Description', 'Information']);
  foreach $a (@{ $urisactions->{menuitem} }) {
    my($u) = UrisUtils->make_url($query, {configfile=>$configfile,
					  action=>$a->{action}
					 });
    if ($a->{metadata}) {
      my(@rows2);
      my($prefix) = join("/", $releaseconfig->{outputdir}, $a->{metadata} . ".");

      $metadata = UrisUtils->evalfile(join("/", $prefix . "metadata"));
      if (-e join("/", $prefix . "running")) {
	$d = GeneralUtils->file2str(join("/", $prefix . "running"));
	push @rows2, $query->td("<FONT COLOR=red>Running</FONT>" . ($d ? " since $d" : "") . $query->br);
      } else {
	push @rows2, $query->td('Last run: ' . $metadata->{enddate} . " (" . $metadata->{elapsed} . ")");
      }
      if (-e ($prefix . "stderr") && ! -z ($prefix . "stderr")) {
	my($u) = $query->url() . "?" . join("&", "action=cat", "file=${prefix}stderr", "preformat=1");
	push @rows2, $query->td("<FONT COLOR=red>" . $query->a({href=>$u}, "Errors") . "</FONT>");
      } else {
	push @rows2, $query->td("No Errors");
      }
      if ($a->{dumpstdout} && -e ($prefix . "stdout") && ! -z ($prefix . "stdout")) {
	my($u) = $query->url() . "?" . join("&", "action=cat", "file=${prefix}stdout", "preformat=1");
	push @rows2, $query->td("<FONT COLOR=red>" . $query->a({href=>$u}, "Output") . "</FONT>");
      }
      $info = $query->table($query->Tr(\@rows2));
    } else {
      $info = "n/a" . $query->br;
    }
    push @rows, $query->td([
			    ($a->{action} ? $query->a({-href=>$u}, $a->{name}) : $a->{name}),
			    $a->{description}, $info
			   ]);
  }

  $html .= $query->header;
  $html .= $query->start_html($uristitle);

  $html .= join("\n",
		$fs,
		"<FONT COLOR=red>UMLS version: $releaseconfig->{releaseversion}</FONT>",
		": " . $releaseconfig->{description},
		UrisUtils->switch_version($query, \@releaseconfig)
	       );
  $html .= $query->hr . $query->p . "\n";

  $html .= $query->h1("UMLS Release Information System");
  $html .= <<"EOD";
URIS provides quality assurance and summary data for UMLS releases.
Select the link in the first column, if any, to analyze the data for the action.
Links in the last column extract the errors or raw output from the generator script.
EOD
  $html .= $query->p;
  $html .= $query->table({-border=>1, -cellpadding=>5, -cellspacing=>0, -width=>"90%"}, $query->Tr(\@rows));
  $html .= $query->end_html;
  print $html;
}
1;


