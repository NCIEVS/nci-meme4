#!@PATH_TO_PERL@
#
# This script is used to set TTYL based on MRCON/SO for MRSAB
# Should be run from the ORF directory itself only when QA
# checks report a problem.
#
open ($IN,"MRSO") || die "could not open MRSO: $! $?\n";
while (<$IN>) {
    ($a,$b,$c,$sab,$tty,$d) = split /\|/;
    ${$ttyl{$sab}}{$tty} = 1;
}
close($IN);
print "done with MRSO\n";
open ($IN,"MRSAB") || die "could not open MRSAB: $! $?\n";
open ($OUT,">MRSAB.fix") || die "could not open MRSAB.fix: $! $?\n";
while (<$IN>) {
    @f = split /\|/;
    $ttyl = "";
    foreach $tty (sort keys %{$ttyl{$f[3]}}) {
        $ttyl .= "$tty,";
    }
    $ttyl =~ s/,$//;
$f[17] = $ttyl;
print $OUT join "|", @f;
}
close($IN);
close($OUT);
