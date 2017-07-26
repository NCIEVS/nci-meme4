#!/bin/csh -f

############################################################
# Script: prep_insertion.csh
# Author: Joanne Wong (jwong@msdinc.com)
# Notes:  Creates file structure and copies files for
#         real insertions.  Edit VARIABLES section if
#         paths change.
############################################################

#### CHECK USAGE ####

#default to real
set mode="real"
set sab="NOSAB"

if ($#argv < 1 || $#argv > 2) then
    echo "Usage: $0 [-t] <VSAB>"
    exit 1
else if ("$argv[1]" == "-t") then
    if ($#argv == 2) then
        set mode="test"
        set sab=$2
    else
        echo "Usage: $0 [-t] <VSAB>"
        exit 1
    endif
else if ($#argv == 1) then
    set mode="real"
    set sab=$1
else
    echo "Usage: $0 [-t] <VSAB>"
    exit 1
endif


#### VARIABLES ####

# parameters passed by script
set lc_sab=`perl -e "print lc($sab)"`

# location of src_root
set host = `hostname`
set src_root_dir = "/meme_work/inv/sources"

# subdirectory names
set src_dir="src"
set bin_dir="bin"
set insert_dir="insert"
set test_dir="test"

#### DETERMINE USAGE, SET TARGET ####

if ($mode == "test") then
  set target_dir = "$test_dir"
else
  set target_dir = "$insert_dir"
endif

#### MAIN ####

echo "---------------------------------------------------------------------------------"
echo "Starting ... `/bin/date`"
echo "---------------------------------------------------------------------------------"
echo "mode         = $mode"
echo "sab          = $sab"
echo "lc_sab       = $lc_sab"
echo "target_dir   = $target_dir"
echo "host         = $host"
echo "src_root_dir = $src_root_dir"

if (-e "$src_root_dir/$sab") then

    if ( "$sab" !~ MTH* ) then

        echo "  Processing non-MTH source: $sab"
        # make directories for insertion
        echo "    make directories"
        cd $src_root_dir/$sab
        if (! -e $target_dir) then
            mkdir  $target_dir
        endif
        cd $target_dir

        # copy .src, .RRF files
        echo "    copy .src and .RRF files"
        cp $src_root_dir/$sab/$src_dir/*.src .
        cp $src_root_dir/$sab/$src_dir/*.RRF .

        # copy .csh recipe script and make it user,group executable/writeable
        echo "    copy script and make executable"
        cp $src_root_dir/$sab/$bin_dir/$lc_sab.csh .
        chmod 774 $lc_sab.csh

    else
        echo "  Processing MTH source: $sab"
        # make directories for insertion
        cd $src_root_dir/$sab
        mkdir $target_dir
        cd $target_dir

        # make directories for parts 1 and 3.
        # there is no part 2 (SNOMEDCT_US is now a separate insertion).
        foreach part (1 3)
            echo "    make directories - part $part"
            mkdir part$part
            echo "    copy files - part $part"
            cp $src_root_dir/$sab/part$part/*.src part$part
            cp $src_root_dir/$sab/part$part/MRDOC.RRF part$part

            # copy .csh recipe script and make it user,group executable/writeable
            echo "    copy script and make executable - part $part"
            cp $src_root_dir/$sab/$bin_dir/$lc_sab.csh part$part
            chmod 774 part$part/$lc_sab.csh

            if ($part == 3) then
                echo "    prep special files - part $part"
                # Make sure to include part3/umlscui.txt for part3
                cp $src_root_dir/$sab/part$part/umlscui.txt part$part

                # Make sure to include ../vsabs.deleted for part3
                cp $src_root_dir/$sab/vsabs.deleted part$part
            endif
        end

        # make directory for bequeathal rels
        echo "  prep part 4"
        mkdir part4
        cp $src_root_dir/$sab/part3/bequeathal.relationships.src part4/relationships.src
    endif
else
   echo "ERROR: required directory $src_root_dir/$sab does not exist."
   exit 1
endif

echo "---------------------------------------------------------------------------------"
echo "Done.  Please double-check directories created in $src_root_dir/$sab/$target_dir."
echo "---------------------------------------------------------------------------------"
