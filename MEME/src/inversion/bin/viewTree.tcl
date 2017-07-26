#!/share_nfs/usr/ActiveTcl-8.4/bin/wish
# ----------------------------------------------------------------------
#  DEMO: hierarchy in [incr Widgets]
# ----------------------------------------------------------------------
package require Iwidgets 4.0

#image create photo rb -file /umls_dev/NLM/inv/img/confused.gif
#image create photo gb -file /umls_dev/NLM/inv/img/rolleyes.gif
#image create photo happy -file /umls_dev/NLM/inv/img/happy.gif
image create photo rb -file "$env(INV_HOME)/img/confused.gif"
image create photo gb -file "$env(INV_HOME)/img/rolleyes.gif"
image create photo happy -file "$env(INV_HOME)/img/happy.gif"

#top level utilities to report errors
proc popErrMsg { msg { title "Error" }} {
    tk_messageBox -type ok -icon error -message $msg -title $title
}
proc dispMsg { msg } {
    global base
}
proc dispStatus { msg } {
    global lineStatus
    set lineStatus $msg
    return
}

# This demo displays a users file system starting at thier HOME
# directory.  You can change the starting directory by setting the
# environment variable SHOWDIR.
#
if {![info exists env(RootNode)]} {
    set env(RootNode) $env(HOME)
}

global g_data g_defs defs_read g_root env
global g_srcDir g_defsFile g_parChldFile
global g_tempDir g_mode

set g_srcDir "../src"
set g_tempDir "../tmp"
set g_defsFile "../tmp/src_1_defs"
set g_parChldFile "../tmp/src_1_parChld"
set defs_read 0
# set g_mode to 0 for said key; 1 for ptr key
#set g_mode 0 
set g_mode 1

proc reset {} {
    global g_data g_defs
    array unset g_data
}


# ----------------------------------------------------------------------
# PROC: getNodes file
#
# Used as the -querycommand for the hierarchy viewer.  Returns the
# list of files under a particular directory.  If the file is "",
# then the RootNode is used as the directory.  Otherwise, the node itself
# is treated as a directory.  The procedure returns a unique id and
# the text to be displayed for each file.  The unique id is the complete
# path name and the text is the file name.
# ----------------------------------------------------------------------
proc getNodes {node} {
    global g_data g_defs g_root g_uid2said g_mode
    if {$node == ""} {
	set cnodes [list $g_root]
    } else {
	set cnodes ""
	set allNodes [split $node "."]
	set thisNode [lindex $allNodes [expr [llength $allNodes] - 1]]

	if { [info exists g_data($thisNode,children)]} {
	    set cnodes $g_data($thisNode,children)
	}
    }
    
    set ans ""
    foreach cnode $cnodes {
	if {$g_mode == 0} {
	    if {[info exists g_defs($cnode)]} {
		lappend ans [list $node.$cnode $g_defs($cnode)]
	    } else {
		lappend ans [list $node.$cnode "<NoCode> Undefined <$cnode>"]
	    }
	} elseif {$g_mode == 1} {
	    set said $g_uid2said($cnode)
	    if {[info exists g_defs($said)]} {
		lappend ans [list $node.$cnode $g_defs($said)]
	    } else {
		lappend ans [list $node.$cnode $cnode=>NOTDEFINED]
	    }
	}
    }

    return [lsort $ans]
}

# ----------------------------------------------------------------------
# PROC: select_node tags status
#
# Select/Deselect the node given the tags and current selection status.
# The unique id which is the complete file path name is mixed in with 
# all the tags for the node.  So, we'll find it by searching for our 
# RootNode and then doing the selection or deselection.
# ----------------------------------------------------------------------
proc select_node {tags status} {
    global env base
    set uid [lindex $tags [lsearch -regexp $tags $env(RootNode)]]

    if {$status} {
	$base.trfr.h selection remove $uid
    } else {
	$base.trfr.h selection add $uid
    }
}

# ----------------------------------------------------------------------
# PROC: expand_node tags
#
# Expand the node given the tags.  The unique id which is the complete 
# file path name is mixed in with all the tags for the node.  So, we'll 
# find it by searching for our RootNode and then doing the expansion.
# ----------------------------------------------------------------------
proc expand_node {tags} {
    global env base
    set uid [lindex $tags [lsearch -regexp $tags $env(RootNode)]]
    $base.trfr.h expand $uid
}

# ----------------------------------------------------------------------
# PROC: collapse_node tags
#
# Collapse the node given the tags.  The unique id which is the complete 
# file path name is mixed in with all the tags for the node.  So, we'll 
# find it by searching for our RootNode and then doing the collapse.
# ----------------------------------------------------------------------
proc collapse_node {tags} {
    global env base
    set uid [lindex $tags [lsearch -regexp $tags $env(RootNode)]]
    $base.trfr.h collapse $uid
}

# ----------------------------------------------------------------------
# PROC: expand_recursive
#
# Recursively expand all the file nodes in the hierarchy.  
# ----------------------------------------------------------------------
proc expand_recursive {node} {
    global base
    set files [getNodes $node]

    foreach tagset $files {
	set uid [lindex $tagset 0]
	$base.trfr.h expand $uid
	if {[getNodes $uid] != {}} {
	    expand_recursive $uid
	}
    }
}

# ----------------------------------------------------------------------
# PROC: expand_all
#
# Expand all the file nodes in the hierarchy.  
# ----------------------------------------------------------------------
proc expand_all {} {
    expand_recursive ""
}

# ----------------------------------------------------------------------
# PROC: collapse_all
#
# Collapse all the nodes in the hierarchy.
# ----------------------------------------------------------------------
proc collapse_all {} {
    global base
    $base.trfr.h configure -querycommand "getNodes %n"
}

proc print_all {} {
}


# 
# Create the hierarchy mega-widget, adding commands to both the item
# and background popup menus.
#

#---------------------------
# gui start
global base lineStatus
set base .top
catch {destroy $base} junk
toplevel $base
wm focusmodel $base passive
wm geometry $base 644x517+208+138; update
wm maxsize $base 1280 1024
wm minsize $base 256 192
wm overrideredirect $base 0
wm resizable $base 1 1
wm deiconify $base
wm title $base "UMLS Tree viewer"


proc clockIt {{end 0}} {
    global g_startTime g_tmHrs g_tmMins g_tmSecs
    if {$end == 1} {
	set etime [clock seconds]
	set ptime [expr $etime - $g_startTime]

	set g_tmHrs [expr $ptime / 3600]
	set rem [expr $ptime % 3600]
	set g_tmMins [expr $rem / 60]
	set g_tmSecs [expr $rem % 60]
	if {$g_tmHrs > 0} {
	    return "${g_tmHrs}h:${g_tmMins}m:${g_tmSecs}s"
	} elseif {$g_tmMins > 0} {
	    return "${g_tmMins}m:${g_tmSecs}s"
	} else {
	    return "${g_tmSecs}s"
	}
    } else {
	set g_startTime [clock seconds]
    }
}

proc cb_displayData { } {
    global g_data g_defs defs_read
    global g_srcDir g_defsFile g_parChldFile
    set srcDate 0
    clockIt
    set btime [clock seconds]
    if {![file exists $g_srcDir/contexts.src]} {
	puts "$g_srcDir/contexts.src doesn't exist. Bailing out."
	return
    } else {
	set srcDate [file mtime $g_srcDir/contexts.src]
    }
    if {![file exists $g_srcDir/classes_atoms.src]} {
	puts "$g_srcDir/classes_atoms.src doesn't exist. Bailing out."
	return
    } else {
	set temp [file mtime $g_srcDir/classes_atoms.src]
	if {$temp > $srcDate} { set srcDate $temp }
    }

    if {[file exists $g_parChldFile] && [file exists $g_defsFile]
	&& [file mtime $g_parChldFile] >= $srcDate
	&& [file mtime $g_defsFile] >= $srcDate} {
	loadData
    } else {
	prepareData
    }
    showData
    set ptime [clockIt 1]
    puts "Execution took $ptime"
}

proc saveNode { nd1 type nd2 } {
    global g_data
    if {![info exists g_data($nd1,$type)]} {
	lappend g_data($nd1,$type) $nd2
    } elseif {[lsearch -exact $g_data($nd1,$type) $nd2] < 0} {
	lappend g_data($nd1,$type) $nd2
    }
}
proc saveSaid2Uid { said uid } {
    global g_said2uid
    if {![info exists g_said2uid($said)]} {
	lappend g_said2uid($said) $uid
    } elseif {[lsearch -exact $g_said2uid($said) $uid] < 0} {
	lappend g_said2uid($said) $uid
    }
}
proc getParHcd { lst par } {
    set ln [llength $lst]
    if {$ln > 1} {
	set temp [lrange $lst 0 end-1]
	set par [join $temp "_"]
	return $par
    } elseif {$ln == 1} {
	set ele [lindex $lst 0]
	set ln [string length $ele]
	if {$ln > 1} {
	    set temp [string range $ele 0 0]
	    return $temp
	}
	if {$ln == 1} { return $par }
    }
    return {}
}
    

proc prepareData {} {
    puts "selected PrepareDat"
    global g_data g_defs defs_read g_cd2said g_root g_uid2said g_said2uid
    global g_srcDir g_defsFile g_parChldFile env g_mode
    # read context file and gather par chld rels
    #set temp $g_srcDir/context.src
    puts "srcdir $g_srcDir"
    puts "defs   $g_defsFile"
    puts "parch  $g_parChldFile"

    reset
    set defs_read 0
    set irf [open $g_srcDir/contexts.src "r"]
    set orf [open $g_parChldFile "w"]
    while {![eof $irf]} {
	gets $irf line
	set lst [split $line "|"]
	set rel [lindex $lst 1]
	if {$rel == "PAR"} {
	    set chld [lindex $lst 0]
	    set par [lindex $lst 3]
	    set g_defs($chld) "NOCODE|UNKNOWN"
	    if {$g_mode == 0} {
		puts $orf "$par|$chld"
		saveNode $chld parents $par
		saveNode $par children $chld
		set g_defs($par) "NOCODE|UNKNOWN"
	    } elseif {$g_mode == 1} {
		set rela [lindex $lst 2]
		#set temp1 [split [lindex $lst 7] "."]
		#set temp2 [join $temp1 "_"]
		set temp [regsub -all {\.} [lindex $lst 7] "_"]
		set phcd "${rela}_$temp"
		set hcd "${phcd}_$chld"

		puts $orf "$phcd|$hcd|$par|$chld"
		saveNode $hcd parents $phcd
		saveNode $phcd children $hcd
		set g_uid2said($phcd) $par
		set g_uid2said($hcd) $chld
		saveSaid2Uid $par $phcd
		saveSaid2Uid $chld $hcd
		set g_defs($par) "NOCODE|UNKNOWN"
	    }
	}
    }
    close $irf
    close $orf

    # now gather definitions from atoms file.
    set irf [open "$g_srcDir/classes_atoms.src" "r"]
    set orf [open $g_defsFile "w"]
    while {![eof $irf]} {
	gets $irf line
	set lst [split $line "|"]
	set num [lindex $lst 0]
	set tty [lindex $lst 2]
	set tty [lindex [split $tty "/"] 1]
	set code [lindex $lst 3]
	set str [lindex $lst 7]
	
	if {[info exists g_defs($num)]} {
	    set g_defs($num) "$code|($tty) $str <$num>"
	}
    }
    foreach ele [array names g_defs] {
	puts $orf "$ele|$g_defs($ele)"
	set tmp [split $g_defs($ele) "|"]
	set code [lindex $tmp 0]
	set str [lindex $tmp 1]
	set g_defs($ele) "<$code> <$str>"
	set g_cd2said($code) $ele
    }
    set defs_read 1

    # now find root node
    set g_root ""
    foreach ele [array names g_data "*,children"] {
	set par [lindex [split $ele ","] 0]
	if {! [info exists g_data($par,parents)]} {
	    set g_root $par
	    puts "Root nodes: $g_root"
	    set env(RootNode) $g_root
	    set g_defs($g_root) "<NoCode> RootNode <$g_root> "
	    return
	}
    }
}

proc loadData {} {
    puts "selected LoadData"
    global g_data g_defs defs_read g_root g_cd2said g_uid2said g_said2uid
    global g_srcDir g_defsFile g_parChldFile env g_mode

    if {$defs_read == 1} { return }

    set defs_read 1
    reset
    # read par/chld rels
    puts "Loading par/chld relationships"
    set irf [open $g_parChldFile "r"]
    while {![eof $irf]} {
	gets $irf line 
	foreach {par chld psaid csaid} [split $line "|"] {
	    saveNode $chld parents $par
	    saveNode $par children $chld
	    if {$g_mode == 1} {
		set g_uid2said($par) $psaid
		set g_uid2said($chld) $csaid
		saveSaid2Uid $psaid $par
		saveSaid2Uid $csaid $chld
	    }
	}
    }
    close $irf
    #read defs
    puts "Loading definitions"
    set irf [open $g_defsFile "r"]
    while {![eof $irf]} {
	gets $irf line
	foreach {said code definition} [split $line "|"] {
	    set g_defs($said) "<$code> $definition"
	    set g_cd2said($code) $said
	    
	}
    }
    close $irf
    # find root node
    set g_root ""
    foreach ele [array names g_data "*,children"] {
	set par [lindex [split $ele ","] 0]
	if {! [info exists g_data($par,parents)]} {
	    set g_root $par
	    puts "Root nodes: $g_root"
	    set env(RootNode) $g_root
	    set g_defs($g_root) "<NoCode> RootNode <$g_root>"
	    return
	}
    }
}

proc showData {} {
    global base
    puts "selected ShowDat"
    catch {destroy $base.trfr.h} msg
    iwidgets::hierarchy $base.trfr.h \
	-querycommand "getNodes %n" \
	-visibleitems 30x15 \
	-markbackground yellow \
	-labeltext "Tree"  \
	-selectcommand "select_node %n %s" \
	-closedicon rb \
	-openicon gb \
	-nodeicon happy
    # -textbackground white
    pack $base.trfr.h -side left -expand yes -fill both

    $base.trfr.h component itemMenu add command -label "Select" \
	-command {select_node [$base.trfr.h current] 0}
    $base.trfr.h component itemMenu add command -label "Deselect" \
	-command {select_node [$base.trfr.h current] 1}
    $base.trfr.h component itemMenu add separator
    $base.trfr.h component itemMenu add command -label "Expand" \
	-command {expand_node [$base.trfr.h current]}
    $base.trfr.h component itemMenu add command -label "Collapse" \
	-command {collapse_node [$base.trfr.h current]}

    $base.trfr.h component bgMenu add command -label "Expand All" \
	-command expand_all
    $base.trfr.h component bgMenu add command -label "Collapse All" \
	-command collapse_all
    $base.trfr.h component bgMenu add command -label "Print All" \
	-command {$base.trfr.h print_all}
    $base.trfr.h component bgMenu add command -label "Clear Selections" \
	-command {$base.trfr.h selection clear}

}

proc cb_printData {} { puts "selected PrintDat"}
proc cb_findNode {} { puts "selected FindNode"}
proc cb_expandAll {} {
    puts "selected ExpandAll"
    expand_all
 }
proc cb_collapseAll {} {
    puts "selected CollapseAll"
    collapse_all
}

proc cb_help {} { puts "selected Help" }

proc cb_getInDir { } {
    global base g_srcDir g_tempDir g_defsFile g_parChldFile defs_read g_mode
    puts "selected getInDir"
    set temp [tk_getOpenFile -initialdir "." -parent $base]
    if { $temp != "" } {
	set g_srcDir [file dirname $temp]
	dispStatus "Input Dir : $g_srcDir"
	set srcDirTail [lrange [file split $g_srcDir] end end]
	set g_defsFile "$g_tempDir/${srcDirTail}_${g_mode}_defs"
	set g_parChldFile "$g_tempDir/${srcDirTail}_${g_mode}_parChld"
	set defs_read 0
	dispStatus "Temp Dir : $g_tempDir"
	puts "defs file is $g_defsFile"
	puts "pcf  file is $g_parChldFile"
    }
}

proc cb_getTempDir { } {
    global base g_srcDir g_tempDir g_defsFile g_parChldFile defs_read g_mode
    puts "selected getTempDir"
    set temp [tk_getSaveFile -initialdir "." -parent $base]
    if { $temp != "" } {
	set g_tempDir [file dirname $temp]
	set srcDirTail [lrange [file split $g_srcDir] end end]
	set g_defsFile "$g_tempDir/${srcDirTail}_${g_mode}_defs"
	set g_parChldFile "$g_tempDir/${srcDirTail}_${g_mode}_parChld"
	set defs_read 0
	dispStatus "Temp Dir : $g_tempDir"
	puts "defs file is $g_defsFile"
	puts "pcf  file is $g_parChldFile"
    }
}
proc cb_changeMode { } {
    global g_mode base defs_read g_srcDir g_tempDir g_defsFile g_parChldFile
    set oldVal $g_mode
    set newVal 0
    if {[$base.plfr.mode getcurselection] != "SAID"} { set newVal 1 }
    if {$oldVal == $newVal} { return }
    set g_mode $newVal
    set srcDirTail [lrange [file split $g_srcDir] end end]
    set g_defsFile "$g_tempDir/${srcDirTail}_${g_mode}_defs"
    set g_parChldFile "$g_tempDir/${srcDirTail}_${g_mode}_parChld"
    set defs_read 0
    dispStatus "Temp Dir : $g_tempDir"
    puts "defs file is $g_defsFile"
    puts "pcf  file is $g_parChldFile"

}


proc cb_findData {} {
    puts "selected FindData"
    findDialog
}

proc cb_releaseData {} { highlight }


proc highlight {{tags ""}} {
    global base
    if {$tags != ""} {
	eval [list $base.trfr.h mark add] $tags
    } else {
	$base.trfr.h mark clear
    }
}

proc markPaths { str { by String} } {
    global g_data g_defs defs_read g_cd2said g_root env g_said2uid g_mode
    set ans ""
    if {$by == "String"} {
	foreach ele [array names g_defs] {
	    if {[regexp -nocase $str $g_defs($ele)]} {
		if {$g_mode == 0} {
		    lappend ans $ele
		} elseif {$g_mode == 1} {
		    set ans [concat $ans $g_said2uid($ele)]
		}
	    }
	}
    } elseif {$by == "Code"} {
	if {$g_mode == 0} {
	    lappend ans $g_cd2said($str)
	} elseif {$g_mode == 1} {
	    puts "Code is $str"
	    set tmp $g_cd2said($str)
	    puts "said is $tmp"
	    set ans $g_said2uid($tmp)
	    puts "uid is $ans"
	}
    } elseif {$by == "SAID"} {
	if {$g_mode == 0} {
	    lappend ans $str
	} elseif {$g_mode == 1} {
	    set ans $g_said2uid($str)
	}
    }
    set cxtPaths ""
    foreach node $ans {
	foreach path [findPaths $node] {
	    set soFar ""
	    foreach ele [split $path "."] {
		set soFar "$soFar.$ele"
		lappend cxtPaths $soFar
	    }
	}
    }
    highlight $cxtPaths
}

proc findPaths { nd } {
    global g_root g_data
    set ans ""
    if {[info exists g_data($nd,parents)]} {
	foreach par $g_data($nd,parents) {
	    foreach parPath [findPaths $par] {
		lappend ans "$parPath.$nd"
	    }
	}
	return $ans
    } else {
	return [list "$g_root"]
    }
}

proc findDialog { } {
    global base
    set fbase $base.dfind
    catch {destroy $base.dfind} junk
    toplevel $fbase -class Toplevel
    wm focusmodel $fbase passive
    wm title $fbase "Find and highlight paths"
    
    iwidgets::optionmenu $fbase.om_findBy -labeltext "Find By: " -labelpos w
    pack $fbase.om_findBy
    $fbase.om_findBy insert end Code SAID String
    
    iwidgets::entryfield $fbase.str -labeltext "Search" -labelpos nw
    pack $fbase.str
    
    iwidgets::buttonbox $fbase.bb
    $fbase.bb add Find -text Find -command {cb_findIt}
    $fbase.bb add Close -text Close -command {destroy $base.dfind}
    pack $fbase.bb
}

proc cb_findIt {} {
    global base
    set by [$base.dfind.om_findBy get]
    set what [$base.dfind.str get]
    puts "by is $by"
    puts "what is $what"
    markPaths $what $by
}

# toplevel menu bar

frame $base.mbfr -borderwidth 1 -height 30 -relief raised -width 30

#add fiel buttons and the corresponding menu items
menubutton $base.mbfr.file -text "File" \
    -menu "$base.mbfr.file.menu" -anchor w -padx 4 -pady 3 -width 4

menu $base.mbfr.file.menu \
        -activeborderwidth 1 -borderwidth 1 -tearoff 0 

$base.mbfr.file.menu add command -label "DisplayData" \
        -accelerator Ctrl+L -command {cb_displayData}

$base.mbfr.file.menu add separator 

$base.mbfr.file.menu add command -label "Print" \
        -accelerator CTRL+P -command {cb_printData}

$base.mbfr.file.menu add separator 
$base.mbfr.file.menu add command -label "Exit" \
        -accelerator CTRL+E -command { destroy . }

# add edit button and the corresponding menu
menubutton $base.mbfr.nvgt -text "Navigate" \
        -anchor w -menu "$base.mbfr.nvgt.menu" -padx 4 -pady 3 -width 4 

menu $base.mbfr.nvgt.menu \
        -activeborderwidth 1 -borderwidth 1 -tearoff 0 

$base.mbfr.nvgt.menu add command -label "Find" \
        -accelerator Ctrl+C -command {cb_findData}

$base.mbfr.nvgt.menu add command -label "Release" \
        -accelerator Ctrl+C -command {cb_releaseData}

$base.mbfr.nvgt.menu add command -label "Expand All" \
        -accelerator Ctrl+C -command {cb_expandAll}

$base.mbfr.nvgt.menu add command -label "Collapse All" \
    -accelerator Ctrl+C -command {cb_collapseAll}


# add help button and the corresponding menu
menubutton $base.mbfr.help -text "Help" \
        -anchor w -menu "$base.mbfr.help.menu" -padx 4 -pady 3 -width 4

menu $base.mbfr.help.menu \
        -activeborderwidth 1 -borderwidth 1 -tearoff 0 

$base.mbfr.help.menu add command -label "About" -command {cb_help}

pack $base.mbfr \
        -in $base -anchor center -expand 0 -fill x -side top 
pack $base.mbfr.file \
        -in $base.mbfr -anchor center -expand 0 -fill none -side left 
pack $base.mbfr.nvgt \
        -in $base.mbfr -anchor center -expand 0 -fill none -side left
pack $base.mbfr.help \
        -in $base.mbfr -anchor center -expand 0 -fill none -side right 



# now crete the pallet frame.
frame $base.plfr -borderwidth 1 -relief raised
button $base.plfr.inDir -text "InDir" -relief raised -command { cb_getInDir }
label $base.plfr.inDirLbl -textvariable g_srcDir -relief raised

button $base.plfr.tempDir -text "TempDir" -relief raised \
    -command { cb_getTempDir }
label $base.plfr.tempDirLbl -textvariable g_tempDir -relief raised

iwidgets::combobox $base.plfr.mode -labeltext "Key:" -labelpos w \
    -selectioncommand { cb_changeMode }
$base.plfr.mode insert list end SAID PTR
$base.plfr.mode selection set PTR

button $base.plfr.display -text "Display" -borderwidth 1 -relief raised \
    -command {cb_displayData }
button $base.plfr.find -text "Find" -borderwidth 1 -relief raised \
    -command {cb_findData }
button $base.plfr.release -text "Release" -borderwidth 1 -relief raised \
    -command {cb_releaseData }
button $base.plfr.exit -text "Exit" -borderwidth 1 -relief raised \
    -command { destroy . }



pack $base.plfr -in $base -anchor w -expand 0 -fill x -side top

grid $base.plfr.inDir -in $base.plfr -row 0 -column 0 -sticky news
grid $base.plfr.inDirLbl -in $base.plfr -row 0 -column 1 -columnspan 3 \
    -sticky news

grid $base.plfr.tempDir -in $base.plfr -row 0 -column 4 -sticky news
grid $base.plfr.tempDirLbl -in $base.plfr -row 0 -column 5 \
    -columnspan 3 -sticky news

grid $base.plfr.mode -in $base.plfr -row 1 -column 0 -sticky news \
    -columnspan 2
grid $base.plfr.display -in $base.plfr -row 1 -column 2 -sticky news
grid $base.plfr.find -in $base.plfr -row 1 -column 3 -sticky news
grid $base.plfr.release -in $base.plfr -row 1 -column 4 -sticky news
grid $base.plfr.exit -in $base.plfr -row 1 -column 6 -sticky news

# now crete the tree frame.
frame $base.trfr \
    -borderwidth 2 -cursor fleur -height 75 -relief raised -width 84 

frame $base.lsfr \
    -borderwidth 2 -cursor fleur -relief raised
pack $base.lsfr -in $base -anchor w -expand 0 -fill x -side top
pack $base.trfr -in $base -anchor w -expand 1 -fill both -side left 



 
label $base.lsfr.linestatus -textvariable lineStatus -relief raised
pack $base.lsfr.linestatus -in $base.lsfr  -side top -anchor center \
	-expand 0 -fill x



proc DEBUGdumpRanges {} {
    global g_data g_defs
    set irf [open "ranges.src" "w"]
    foreach key [array names g_data "*,children"] {
	set tmp [lindex [split $key ","] 0]
	puts $irf "$tmp|$g_defs($tmp)"
    }
    close $irf
}
