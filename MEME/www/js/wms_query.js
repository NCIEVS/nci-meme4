<!-- Javascript for WMS query page -->
<!-- suresh@nlm - 12/2005 -->

<SCRIPT LANGUAGE="JavaScript">
function syncTOP() {
  for (var i=0; i<document.forms[0].sortbybottom.length; i++) {
    if (!document.forms[0].sortbybottom.options[i].selected)
      continue;
    document.forms[0].sortbytop.options[i].selected = true;
  }
  for (var i=0; i<document.forms[0].ascdescbottom.length; i++) {
    if (!document.forms[0].ascdescbottom.options[i].selected)
      continue;
    document.forms[0].ascdesctop.options[i].selected = true;
  }
  return true;
}

function syncBOTTOM() {
  for (var i=0; i<document.forms[0].sortbytop.length; i++) {
    if (!document.forms[0].sortbytop.options[i].selected)
      continue;
    document.forms[0].sortbybottom.options[i].selected = true;
  }
  for (var i=0; i<document.forms[0].ascdesctop.length; i++) {
    if (!document.forms[0].ascdesctop.options[i].selected)
      continue;
    document.forms[0].ascdescbottom.options[i].selected = true;
  }
  return true;
}
</SCRIPT>
