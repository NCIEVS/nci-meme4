#!@PATH_TO_PERL@
#
# CHANGES
#  12/16/2007 BAC (1-G0HPX): added pattern for hyphen
#  04/27/2007 BAC (1-E3I6T): Added to repository
#



open (LOG, ">../tmp/charcleanup.txt");

while (<>)
        {
        chomp;
        $orig=$_;
        s/\342\200\223/-/g;     # en dash
        s/\342\200\224/-/g;     # em dash
        s/\342\200\220/-/g;     # hyphen
        s/\342\200\230/'/g;     # left single quote
        s/\342\200\231/'/g;     # right single quote
        s/\342\200\234/"/g;     # left double quote
        s/\342\200\235/"/g;     # right double quote
        #s/\342\211\245/>=/g;   # greater than or equal to
        s/\302\255/-/g;         # soft hyphen
        s/\302\240/ /g;         # no-break space
        print LOG "$orig\n$_\n\n" if ($orig ne $_);
        print "$_\n";
        }

