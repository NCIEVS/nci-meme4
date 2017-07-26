#!@PATH_TO_PERL@

while (<>)
{
	if (length($_) > $maxlen)
	{
		$maxline = $_;
		$maxlen = length($_);
	}
}
print "Length of longest line:\t$maxlen\n";
print $maxline;
