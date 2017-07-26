#!/site/bin/perl5

use GD;

$width=900;
$height=10;
$ofs = 98;

$im = new GD::Image($width, $height);

$white = $im->colorAllocate(255, 255, 255);
$gray = $im->colorAllocate(187, 187, 187);

$im->filledRectangle(0, 0, $width, $height, $white);
$im->line($ofs, 0, $ofs+$thick, $height, $gray);
print $im->gif;
exit 0;
