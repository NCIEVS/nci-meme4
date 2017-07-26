#!/site/bin/perl5

use GD;

$width=600;
$height=60;
$font = GD::gdLargeFont;
$string = "E D I T I N G  M A N A G E M E N T  S Y S T E M  (EMS)";
$xOfs = ($width - length($string)*$font->width)/2;
$yOfs = ($height - $font->height)/2;

$im = new GD::Image($width, $height);

$white = $im->colorAllocate(255, 255, 255);
die "Cannot allocate white\n" if $white == -1;
$gray = $im->colorAllocate(204, 204, 204);
die "Cannot allocate gray\n" if $gray == -1;
$black = $im->colorAllocate(0, 0, 0);
die "Cannot allocate black\n" if $black == -1;
#$yellow = $im->colorAllocate(255, 204, 102);
$yellow = $im->colorAllocate(255, 204, 102);
die "Cannot allocate yellow\n" if $yellow == -1;
#$blue = $im->colorAllocate(120, 120, 225);
$blue = $im->colorAllocate(33, 33, 165);
die "Cannot allocate blue\n" if $blue == -1;

$im->filledRectangle(0, 0, $width, $height, $blue);
$im->string($font, $xOfs, $yOfs, $string, $yellow);
print $im->gif;
exit 0;
