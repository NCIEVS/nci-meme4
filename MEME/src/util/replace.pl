#!@PATH_TO_PERL@

if (scalar(@ARGV) < 3 ) {
    print "Usage: $0 <string_to_replace> <replacement_string> <file_list>\n";
    print "\n";
    print "Example: $0 v1.0 v1.1 *.tmp\n";
    print "Example: $0 Frame JFrame file1.a file2.b\n";
    exit -1;
 }

# Remove string args, leaving file list
$replacee=shift(@ARGV);
$replacement=shift(@ARGV);

foreach $infile (@ARGV)  {

    open (INFILE, $infile);
    $infile =~ /^(.*)\.(.*)$/;
    open (TMPFILE, "> $1.$2.tmp");
    while ($line=<INFILE>)  {
        chomp ($line);
        $line =~ s/$replacee/$replacement/g;
        print TMPFILE "$line\n";
    }
    close(INFILE); close(TMPFILE);

}

foreach $infile (@ARGV) {

    $tmpfile =  "$infile.tmp";
    open (TMPFILE, $tmpfile);
    open (OUTFILE, $infile);
    rename "$tmpfile", "$infile";

}





