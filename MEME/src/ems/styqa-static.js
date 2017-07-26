/* Functions for displaying, sorting STY's */

/* The STYData array is built dynamically from SRDEF and loaded by script */

var stySortMode="alpha";
var stySorted = new Array(STYData.length);
styDisplay(stySortMode);

function styaddfn() {
  var neg = "";
  var sty;

  for (i=0; i<STYData.length; i++) {
    if (document.styframe.stylist.options[i].selected) {
      if (document.styframe.stynegate.checked == true) {
        neg = "!= ";
      } else {
        neg = "";
      }
      sty = STYData[stySorted[i]][0];
      if (document.styframe.styselected.value == "") {
	nl = "";
      } else {
        nl = "\n";
      }
      document.styframe.styselected.value = document.styframe.styselected.value + nl + neg + sty;
    }
  }
  //  alert("styaddfn: " + stySortMode);
  clearsty();
  return true;
}

function textaddfn() {
  var neg = "";
  var word = document.styframe.textfield.value.toLowerCase();
  var whitespace = /[\s]/;
  var result = word.match(whitespace);

  if (result != null) {
    alert("Multiple words specified!");
    return;
  }
  
  if (document.styframe.textnegate.checked == true) {
    neg = "!= ";
  } else {
    neg = "";
  }
  if (document.styframe.textexact.checked == true) {
	    exact = "";
	  } else {
	    exact = "%";
	  }
  if (document.styframe.textselected.value == "") {
    nl = "";
  } else {
    nl = "\n";
  }
  document.styframe.textselected.value = document.styframe.textselected.value + nl + neg + exact + word + exact;
  cleartext();
}

function reinitsty() {
  //  alert(stySortMode);
  styDisplay(stySortMode);
  document.styframe.stysort[0].checked=1;

  for (i=0; i<STYData.length; i++) {
    if (document.styframe.stylist.options[i].selected) {
      document.styframe.stylist.options[i].selected=0;
    }
  }
  document.styframe.stynegate.checked = false;
  document.styframe.styselected.value = "";
}

/* clears the STY elements */
function clearsty() {
  styDisplay(stySortMode);

  for (i=0; i<STYData.length; i++) {
    if (document.styframe.stylist.options[i].selected) {
      document.styframe.stylist.options[i].selected=0;
    }
  }
  document.styframe.stynegate.checked = false;
}

/* clears the SOURCE elements */
function clearsources() {
  for (i=0; i<document.styframe.sourcelist.length; i++) {
    if (document.styframe.sourcelist.options[i].selected) {
      document.styframe.sourcelist.options[i].selected=0;
    }
  }
}

/* clears the TTY elements */
function clearttys() {
  for (i=0; i<document.styframe.ttylist.length; i++) {
    if (document.styframe.ttylist.options[i].selected) {
      document.styframe.ttylist.options[i].selected=0;
    }
  }
}


function reinittext() {
  document.styframe.textfield.value="";
  document.styframe.textnegate.checked = false;
  document.styframe.textexact.checked = true;
  document.styframe.textselected.value = "";
}

function cleartext() {
  document.styframe.textfield.value="";
  document.styframe.textnegate.checked = false;
  document.styframe.textexact.checked = true;
}

function verify() {
  if (document.styframe.styselected.value == "") {
    alert("Need a STY!");
    return(false);
  }
  return(true);
}

function styDisplay(mode) {
  var label;

  if (mode == "" || mode == null) {
    mode = document.styframe.stysort.value;
  }
  stySortMode = mode;
  //  alert("styDisplay: " + stySortMode);

  stySort();

  var o =  document.styframe.stylist.options;
  o.length = 0;
  o.length = stySorted.length;
  for (i=0; i<stySorted.length; i++) {
    if (mode == "tree") {
      label = periods2spaces(STYData[stySorted[i]][2]) + STYData[stySorted[i]][0] + " (" + STYData[stySorted[i]][2] + ")";
    } else if (mode == "tui") {
      label = STYData[stySorted[i]][0] + " (" + STYData[stySorted[i]][1] + ")";
    } else {
      label = STYData[stySorted[i]][0];
    }
    o[i].text = label;
  }
}

function periods2spaces(s) {
  var b;
  var i = 0;
  var n = 0;
  var sp = "";

  while ((i=s.indexOf(".", i)) > 0) {
    n++;
    i++;
  }

  for (i=0; i<n; i++) {
    sp = sp + "..";
  }
  return sp;
}

function stySort() {
  for (i=0; i<stySorted.length; i++) {
    stySorted[i] = i;
  }
  if (stySortMode == "alpha") {
    stySorted.sort(byalpha);
  } else if (stySortMode == "tree") {
    stySorted.sort(bytree);
  } else if (stySortMode == "tui") {
    stySorted.sort(bytui);
  }
}

function byalpha(a,b) {
  if (STYData[a][0] == STYData[b][0]) return(0);
  if (STYData[a][0] < STYData[b][0]) return(-1);
  return 1;
}
function bytui(a,b) {
  if (STYData[a][1] == STYData[b][1]) return(0);
  if (STYData[a][1] < STYData[b][1]) return(-1);
  return 1;
}
function bytree(a,b) {
  if (STYData[a][2] == STYData[b][2]) return(0);
  if (STYData[a][2] < STYData[b][2]) return(-1);
  return 1;
}
