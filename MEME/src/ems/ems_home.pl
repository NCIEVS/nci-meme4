# Home page for EMS
# suresh@nlm.nih.gov 9/98
# suresh@nlm.nih.gov 8/99 - Modified for Oracle
# suresh@nlm.nih.gov 6/05 - EMS3 mods

sub do_ems_home {
  my($toolbar);
  my($body);
  my(@toolbardata);
  my($level0URL) = $main::EMSCONFIG{LEVEL0EMSURL} . "?$DBget";
  my($level1URL) = $main::EMSCONFIG{LEVEL1EMSURL} . "?$DBget";
  my($level2URL) = $main::EMSCONFIG{LEVEL2EMSURL} . "?$DBget";
  my($toolbarwidth) = $main::EMSCONFIG{EMSTOOLBARWIDTH} || 100;
  my($toolbarbgcolor) = $main::EMSCONFIG{EMSTOOLBARBGCOLOR};
  my($conceptreporturl) = $main::EMSCONFIG{MIDCONCEPTREPORTURL};

# actions on the toolbar
  my(@actions) = (
		  [$main::EMSCONFIG{LEVEL2NICKNAME}, $level2URL,
		   [
		    ["access", "Access control"],
		    ["cutoff", "Editing cutoff"],
		    ["batch_cutoff", "Batch cutoff"],
		    ["epoch", "Change epoch"],
		    ["me_partition", "ME Partition"],
		    ["locks", "Locks"],
		    ["ems_info", "EMS Config"],
		    ["bin_config", "Bin config"],
		    ["midsvcs", "Midsvcs info"],
		    ["atom_ordering", "Atom Ordering"],
		    ["ah_canonical", "AH canonical"],
		    ["lvgif", "LVGIF"],
		    ["stycooc", "STY Co-oc"],
		    ["styqa", "STY Q/A"],
		   ]
		  ],

		  [$main::EMSCONFIG{LEVEL1NICKNAME}, $level1URL,
		   [
		   ]
		  ],

		  [$main::EMSCONFIG{LEVEL0NICKNAME}, $level0URL,
		   [
		    ["config", "Change config"],
		    ["db", "Change DB"],
		    ["db_refresh", "DB refresh"],
		    ["conceptreport", "Concept report"],
		    ["termgroup_rank", "Termgroup rank"],
		    ["stylist", "STY"],
		   ]
                  ]
	    );

  my(@d, $r, $r1);

  foreach $r (@actions) {
    @d = ();
    next unless @{ $r->[2] } > 0;
    push @d, [ $query->b($r->[0]) . $query->br];
    foreach $r1 (@{ $r->[2] }) {
      push @d, [ $query->a({-href=>$r->[1] . "&action=" . $r1->[0]}, $r1->[1]) ];
    }
    push @toolbardata, [ &toHTMLtable($query, {bgcolor=>$toolbarbgcolor, border=>0, cellpadding=>0, cellspacing=>0, width=>$toolbarwidth}, \@d) ];
    push @toolbardata, [ $query->p ];
  }

  $toolbar = &toHTMLtable($query, {bgcolor=>$toolbarbgcolor, border=>0, cellpadding=>0, cellspacing=>0, width=>$toolbarwidth}, \@toolbardata);
  $body = <<"EOD";
The Editing Management System is used to create and manage the editing process
for the UMLS Metathesaurus.
Workload is generated from
three kinds of editing bins: Mutually Exclusive (ME) bins,
ad-hoc (AH) bins and QA bins.
EOD

  $body .= $query->h2("Editing Counts and Worklists");
  $body .= <<"EOD";
Select from the following links to make worklists or checklists,
or view the current bin counts.
EOD
  $body .= $query->p;
  $body .= $query->ul($query->li(
  [
   $query->a({-href=>$level0URL . "&action=me_bins"}, "Currently defined ME bins"),
   $query->a({-href=>$level0URL . "&action=qa_bins"}, "Currently defined QA bins"),
   $query->a({-href=>$level0URL . "&action=ah_bins"}, "Currently defined AH bins"),
  ]));
  $body .= $query->p;

  $body .= $query->h2("Status and Progress");

  @d = ();
  push @d,
    [$query->a({-href=>$level0URL . "&action=daily_report"}, "Daily Editing Report"), "Archive of all previous editing reports.",
    ];
  push @d,
    [$query->a({-href=>$level0URL . "&action=sourceinsertion"}, "Source insertion metadata"), "Data on when sources were inserted into this database, along with links to the SRC concepts.",
    ];
  push @d,
    [$query->a({-href=>$level0URL . "&action=sourcestats"}, "Source statistics"), "Statistical data for individual sources, such as atom, relationship, attribute counts, data on overlap with other sources, counts grouped by STY, TTY, etc.",
    ];
  $body .= &toHTMLtable($query, {border=>1, cellpadding=>5, cellspacing=>0}, \@d);
  $body .= $query->p;

  my($htmltablewidth) = $main::EMSCONFIG{HTMLTABLEWIDTH};
  my($t) = &toHTMLtable($query, {border=>0, cellpadding=>5, cellspacing=>0, width=>$htmltablewidth},
    [
     [
      [{valign=>'top', bgcolor=>$toolbarbgcolor}, $toolbar],
      [{valign=>'top'}, $body]
     ]
    ]);
  &printhtml({h1=>"Editing Management System", body=>$t});
  return;
}
1;
