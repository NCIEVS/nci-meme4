#!@PATH_TO_PERL@
# Changes:
# 12/22/2005 BAC (1-719SM): use open ":utf8" added

$|=1;
use open ":utf8";
open(CON, "MRCONSO.RRF");
binmode(STDOUT, ":utf8"); # for foreign characters in report, Siebel ticket 1-D7DJW

while(<>) {
  chop;
  ($cui1, $aui1, $st1, $rel, $cui2, $aui2, $st2, $rela, $rui, $sab1, $sab2, $srui, $rg, $suppress, $cvf) = split(/\|/);
  printf("%s|%s|%s|", $cui1, $aui1, $rel, $rela);
  &seekstr(*CON, $cui2);
  while (<CON>) {
    ($cui,$lat,$ts,$lui,$stt,$sui,$pf,$aui,$saui,$scui,$sdui,$sab,$tty,$code,$str,$d) = split /\|/;
    if ($aui eq $aui2) {
      print "$cui|$str";
      last;
    } elsif ($aui2 eq "" && $lat eq "ENG" && 
	     $ts eq "P" && $stt eq "PF" && $pf eq "Y") {
      print "$cui|$str";
      last;
    }
    last unless /^$cui2/;
  }
  printf("|%s|%s|%s|\n", $sab1, $sab2, $suppress);
}

sub seekstr {
    # Seek a given filehandle to the first line starting with a given string.
    # The corresponding file must be sorted on the first |-separated field.
    local(*FILE, $value) = @_;
    local($SIZE, $l, $h, $pos, $_);
    $SIZE = (stat(FILE))[7];
    $l = 0;   $h=$SIZE-1;
    while ($l < $h-16) {
	$pos = int(($l + $h) / 2);
	seek(FILE, $pos, 0)  ||  die "can't seek to $pos\n";
	$_ = <FILE>  if $pos != 0;
	$_ = <FILE>;
	if ($_ eq '') {
	    $h = $pos - 1;
	    next;
	}
	($inval) = /([^|]*)/;
	if ($inval ge $value) {
	    $h = $pos - 1;
	} else {
	    $l = $pos + 1;
	}
    }
    seek(FILE, $l, 0);
    $_ = <FILE> if $l != 0;
    $pos=tell(FILE);
    while (1) {
	$_ = <FILE>;
	return -1 if $_ eq '';
	($inval) = /([^|]*)/;
	if ($inval eq $value) {
	    seek(FILE, $pos, 0);
	    return $pos;
	} elsif ($inval gt $value) {
	    return -1;
	}
	$pos=tell(FILE);
    }
}
