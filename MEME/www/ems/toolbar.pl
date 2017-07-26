#!/site/bin/perl5

use GD;

$width=95;
$height=30;
$font = GD::gdMediumBoldFont;
#$font = GD::gdSmallFont;
$defaultXOfs = 6;
$yOfs = ($height - $font->height)/2;

@ACTIONS = (
      ["NLM Only", "title"],
      ["Change DB", "nlmmgr"],
      ["Repartition", "nlmmgr"],
      ["Refresh ME", "nlmmgr"],
      ["Change Epoch", "nlmmgr"],
      ["EMS Access", "nlmmgr"],
      ["Clean up", "nlmmgr"]
);

$n = scalar(@ACTIONS);

$im = new GD::Image($width, $n*$height);

$white = $im->colorAllocate(255, 255, 255);
die "Cannot allocate white\n" if $white == -1;
$gray = $im->colorAllocate(204, 204, 204);
die "Cannot allocate gray\n" if $gray == -1;
$black = $im->colorAllocate(0, 0, 0);
die "Cannot allocate black\n" if $black == -1;
$orange = $im->colorAllocate(255, 204, 102);
die "Cannot allocate orange\n" if $orange == -1;
$yellow = $im->colorAllocate(255, 255, 25);
die "Cannot allocate yellow\n" if $yellow == -1;
$blue = $im->colorAllocate(33, 33, 165);
die "Cannot allocate blue\n" if $blue == -1;

$im->filledRectangle(0, 0, $width, $n*$height, $gray);

for ($i=0; $i<$n; $i++) {
    $im->line(0, $i*$height, $width, $i*$height, $white);
    if ($ACTIONS[$i][1] eq "nlmmgr") {
	$xOfs = $defaultXOfs;
	$bgColor = $orange;
	$fgColor = $black;
    } elsif ($ACTIONS[$i][1] eq "editor") {
	$xOfs = $defaultXOfs;
	$bgColor = $gray;
	$fgColor = $black;
    } elsif ($ACTIONS[$i][1] eq "title") {
	$xOfs = ($width - length($ACTIONS[$i][0])*$font->width)/2;
	$bgColor = $blue;
	$fgColor = $yellow;
    } else {
	$xOfs = $defaultXOfs;
	$bgColor = $gray;
	$fgColor = $black;
    }
    $im->filledRectangle(0, $i*$height+1, $width, $i*$height+$height-1, $bgColor);
    $im->string($font, $xOfs, $i*$height+$yOfs, $ACTIONS[$i][0], $fgColor);
}
print $im->gif;
exit 0;
