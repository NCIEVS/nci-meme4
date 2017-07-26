# Home page for EMS
# suresh@nlm.nih.gov 9/98
# suresh@nlm.nih.gov 8/99 - Modified for Oracle
# suresh@nlm.nih.gov 6/05 - EMS3 mods

sub do_wms_home {
  my($toolbar);
  my($body);
  my(@toolbardata);
  my($level0URL) = $main::EMSCONFIG{LEVEL0WMSURL} . "?$DBget";
  my($level1URL) = $main::EMSCONFIG{LEVEL1WMSURL} . "?$DBget";
  my($level2URL) = $main::EMSCONFIG{LEVEL2WMSURL} . "?$DBget";
  my($toolbarwidth) = $main::EMSCONFIG{WMSTOOLBARWIDTH} || 100;
  my($toolbarbgcolor) = $main::EMSCONFIG{WMSTOOLBARBGCOLOR};

# actions on the toolbar
  my(@actions) = (
		  [$main::EMSCONFIG{LEVEL2NICKNAME}, $level2URL,
		   [
		    ["access", "Access control"],
		    ["cutoff", "Editing cutoff"],
		    ["make_checklist", "Make checklist"],
		    ["wms_deleteworklists", "Old Worklists"],
		   ]
		  ],

		  [$main::EMSCONFIG{LEVEL1NICKNAME}, $level1URL,
		   [
		   ],
		  ],

		  [$main::EMSCONFIG{LEVEL0NICKNAME}, $level0URL,
		   [
		    ["config", "Change config"],
		    ["db", "Change DB"],
		    ["pickcheck", "Checklists"],
		   ]
                  ]
	    );

  my(@d, $r, $r1);

  foreach $r (@actions) {
    @d = ();
    push @d, [ $query->b($r->[0]) . $query->br];
    foreach $r1 (@{ $r->[2] }) {
      push @d, [ $query->a({-href=>$r->[1] . "&action=" . $r1->[0]}, $r1->[1]) ];
    }
    push @toolbardata, [ &toHTMLtable($query, {bgcolor=>$toolbarbgcolor, border=>0, cellpadding=>0, cellspacing=>0, width=>$toolbarwidth}, \@d) ];
    push @toolbardata, [ $query->p ];
  }

  $toolbar = &toHTMLtable($query, {valign=>'top', bgcolor=>$toolbarbgcolor, border=>0, cellpadding=>0, cellspacing=>0, width=>$toolbarwidth}, \@toolbardata);
  $body = <<"EOD";
The WMS is a system for managing the editing workload for the UMLS
Metathesaurus.  NLM manages the worklist creation, workload assignment
and batch approval, while editors themselves manage other aspects of
the worklists they have been assigned.
EOD
  $body .= $query->p;
  @d = ();
  push @d, [
    [{-valign=>'top'}, $query->a({-href=>$level0URL . "&action=wms_query"}, "Query WMS")], 
    [{-valign=>'top'}, "Use a variety of criteria to select the worklists of interest. You can then view their WMS information, constituent concepts, generate reports, etc.  You can also change the metadata for the matching worklists."]
  ];
  push @d, [
    [{-valign=>'top'}, $query->a({-href=>$level0URL . "&action=wms_custom"}, "Custom Queries")],
    [{-valign=>'top'}, "Some canned queries for finding the worklists you want, e.g., all worklists currently being edited."]
  ];
  push @d, [
    [{-valign=>'top'}, $query->a({-href=>$level0URL . "&action=reportqueue"}, "Report Queue")],
    [{-valign=>'top'}, "View the status of requests for generating worklist or checklist reports."]
  ];
  $body .= &toHTMLtable($query, {border=>0, cellpadding=>5, cellspacing=>5, valign=>'top'}, \@d);

  my($htmltablewidth) = $main::EMSCONFIG{HTMLTABLEWIDTH};
  my($t) = &toHTMLtable($query, {border=>0, cellpadding=>5, cellspacing=>0, width=>$htmltablewidth},
    [
     [
      [{valign=>'top', bgcolor=>$toolbarbgcolor}, $toolbar],
      [{valign=>'top'}, $body]
     ]
    ]);
  &printhtml({h1=>$wmstitle, body=>$t});
  return;
}
1;
