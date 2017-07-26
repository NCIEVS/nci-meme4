# The UMLS Release Information System (URIS) version 2.0
# suresh@nlm.nih.gov 10/2003

# CGI params:
# file=

sub cat {
  my($preformat) = $query->param('preformat');
  my($file) = $query->param('file');
  my($html);

  $html .= $query->header;
  $html .= $query->start_html($uristitle);

  if ($file && -e $file) {
    open(F, $file);
    @_ = <F>;
    close(F);
    $html .= ($preformat ? $query->pre(join('', @_)) : join('', @_));
  } else {
    $html .= "File: $file not found";
  }

  $html .= $query->end_html;
  print $html;
  return;
}
1;

