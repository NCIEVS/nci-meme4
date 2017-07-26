#!@PATH_TO_PERL@
#
# validates uis in relationships, attributes and mergefacts to make sure
# they are valid in classes
# also counts rels, atts & mergefacts by tty
# usage:  validate_count.pl <directory of src files>
#
# CHANGES
#  04/27/2007 BAC (1-E3I6T): Added to repository
#

open (RELOUT, ">relsbytty.txt");
open (MERGEOUT, ">mergesbytty.txt");
open (ATTOUT, ">attsbytty.txt");

$srcdir = shift;
open (ATOMS, "$srcdir/classes_atoms.src") || die "can't open atoms\n";



while (<ATOMS>)
        {
        chomp;
        @fields=split(/\|/);
        $shash{$fields[0]}=$fields[0];
        $sabsaid=$fields[1].".".$fields[0];
        $sabsdui=$fields[1].".".$fields[11];
        $sabscui=$fields[1].".".$fields[10];
        $sabcodetg=$fields[2].".".$fields[3];
        $saidhash{$sabsaid}=$fields[0];
        $sduihash{$sabsdui}=$fields[11];
        $scuihash{$sabscui}=$fields[10];
        $stghash{$sabcodetg}=$fields[3];
        $tty{$fields[0]}=$fields[2];
        $tty{$fields[10]}=$fields[2];
        $tty{$fields[11]}=$fields[2];
        }

close (ATOMS);

open (RELS, "$srcdir/relationships.src") || die "can't open rels\n";

while (<RELS>)
        {
        chomp;
        @fields=split(/\|/);
        if ($fields[12] ne "SRC_ATOM_ID")
                {
                $tty1="$fields[13]";
                }
        else {$tty1=$tty{$fields[2]};}
        if ($fields[14] ne "SRC_ATOM_ID")
                {
                $tty2="$fields[15]";
                }
        else {$tty2=$tty{$fields[5]};}
        print RELOUT "$fields[12]|$tty1|$fields[2]|$fields[14]|$tty2|$fields[5]|$fields[3]|$fields[4]\n";

        if ($fields[12] eq "SRC_ATOM_ID")
                {
                $sabsaid=$fields[6].".".$fields[2];
                if (!$saidhash{$sabsaid})
                        {
                        print "BAD ID1: $_\n";
                        }
                }
        elsif ($fields[12] eq "SOURCE_DUI")
                {
                $sabsdui=$fields[13].".".$fields[2];
                if (!$sduihash{$sabsdui})
                        {
                        print "BAD ID1: $_\n";
                        }
                }
        elsif ($fields[12] eq "SOURCE_CUI")
                {
                $sabscui=$fields[13].".".$fields[2];
                if (!$scuihash{$sabscui})
                        {
                        print "BAD ID1: $_\n";
                        }
                }
        elsif ($fields[12] eq "CODE_TERMGROUP")
                {
                $sabcodetg=$fields[13].".".$fields[2];
                if (!$stghash{$sabcodetg})
                        {
                        print "BAD ID1: $_\n";
                        }
                }
                else {print "diff id: $_\n";}
        if ($fields[14] eq "SRC_ATOM_ID")
                {
                $sabsaid=$fields[6].".".$fields[5];
                if (!$saidhash{$sabsaid})
                        {
                        print "BAD ID2: $_\n";
                        }
                }
        elsif ($fields[14] eq "SOURCE_DUI")
                {
                $sabsdui=$fields[15].".".$fields[5];
                if (!$sduihash{$sabsdui})
                        {
                        print "BAD ID2: $_\n";
                        }
                }
        elsif ($fields[14] eq "SOURCE_CUI")
                {
                $sabscui=$fields[15].".".$fields[5];
                if (!$scuihash{$sabscui})
                        {
                        print "BAD ID2: $_\n";
                        }
                }
        elsif ($fields[14] eq "CODE_TERMGROUP")
                {
                $sabcodetg=$fields[15].".".$fields[5];
                if (!$stghash{$sabcodetg})
                        {
                        print "BAD ID2: $_\n";
                        }
                }
                else {print "diff id: $_\n";}
        }

open (ATTR, "$srcdir/attributes.src") || die "can't open rels\n";

while (<ATTR>)
        {
        chomp;
        @fields=split(/\|/);
        if ($fields[10] ne "SRC_ATOM_ID")
                {
                $tty="$fields[11]";
                }
        else {$tty=$tty{$fields[1]};}
        print ATTOUT "$fields[10]|$fields[3]|$tty|$fields[1]\n";
        next if ($fields[3] eq "SEMANTIC_TYPE");
        next if ($fields[3] eq "CONTEXT");
        if ($fields[10] eq "SRC_ATOM_ID")
                {
                $sabsaid=$fields[5].".".$fields[1];
                if (!$saidhash{$sabsaid})
                        {
                        print "BAD ID: $_\n";
                        }
                }
        if ($fields[10] eq "SOURCE_DUI")
                {
                $sabsdui=$fields[11].".".$fields[1];
                if (!$sduihash{$sabsdui})
                        {
                        print "BAD ID: $_\n";
                        }
                }
        if ($fields[10] eq "SOURCE_CUI")
                {
                $sabscui=$fields[11].".".$fields[1];
                if (!$scuihash{$sabscui})
                        {
                        print "BAD ID: $_\n";
                        }
                }
        if ($fields[10] eq "CODE_TERMGROUP")
                {
                $sabcodetg=$fields[11].".".$fields[1];
                if (!$stghash{$sabcodetg})
                        {
                        print "BAD ID: $_\n";
                        }
                }
        }

open (MERGES, "$srcdir/mergefacts.src") || die "can't open mergefacts\n";

while (<MERGES>)
        {
        chomp;
        @fields=split(/\|/);
        if ($fields[8] ne "SRC_ATOM_ID")
                {
                $tty1="$fields[9]";
                }
        else {$tty1=$tty{$fields[0]};}
        if ($fields[10] ne "SRC_ATOM_ID")
                {
                $tty2="$fields[11]";
                }
        else {$tty2=$tty{$fields[2]};}
        print MERGEOUT "$fields[8]|$tty1|$fields[0]|$fields[10]|$tty2|$fields[2]|$fields[7]\n";
        if ($fields[8] eq "SRC_ATOM_ID")
                {
                # all merges are sab=HCPCS2007, that's ok
                if (!$shash{$fields[0]})
                        {
                        print "BAD ID1: $_\n";
                        }
                }
        if ($fields[10] eq "SRC_ATOM_ID")
                {
                if (!$shash{$fields[2]})
                        {
                        print "BAD ID1: $_\n";
                        }
                }
        if ($fields[8] eq "SOURCE_DUI")
                {
                $sabsdui=$fields[9].".".$fields[0];
                if (!$sduihash{$sabsdui})
                        {
                        print "BAD ID1: $_\n";
                        }
                }
        if ($fields[8] eq "SOURCE_CUI" && $fields[9] ne "CPT2007")
                {
                $sabscui=$fields[9].".".$fields[0];
                if (!$scuihash{$sabscui})
                        {
                        print "BAD ID1: $_\n";
                        }
                }
        if ($fields[10] eq "SOURCE_DUI")
                {
                $sabsdui=$fields[11].".".$fields[2];
                if (!$sduihash{$sabsdui})
                        {
                        print "BAD ID2: $_\n";
                        }
                }
        if ($fields[10] eq "SOURCE_CUI")
                {
                $sabscui=$fields[11].".".$fields[2];
                if (!$scuihash{$sabscui})
                        {
                        print "BAD ID2: $_\n";
                        }
                }
        if ($fields[8] eq "CODE_TERMGROUP")
                {
                $sabcodetg=$fields[9].".".$fields[0];
                if (!$stghash{$sabcodetg})
                        {
                        print "BAD ID1: $_\n";
                        }
                }
        }


system "tallyfield.pl \'\$1\$2\$3\' attsbytty.txt > attcounts.txt";
system "tallyfield.pl \'\$1\$2\$4\$5\$7\$8\' relsbytty.txt > relcounts.txt";
system "tallyfield.pl \'\$1\$2\$4\$5\$7\' mergesbytty.txt > mergecounts.txt";
