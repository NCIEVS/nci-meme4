var STYData = [
	["CHEM", "_", "_" ],
	["NONCHEM", "_", "_" ],
	[ "Element, Ion, or Isotope", "T196", "A1.4.1.2.3"],
	[ "Inorganic Chemical", "T197", "A1.4.1.2.2"],
	[ "Clinical Drug", "T200", "A1.3.3"],
	[ "Clinical Attribute", "T201", "A2.3.1"],
	[ "Drug Delivery Device", "T203", "A1.3.1.1"],
	[ "Immunologic Factor", "T129", "A1.4.1.1.3.5"],
	[ "Indicator, Reagent, or Diagnostic Aid", "T130", "A1.4.1.1.4"],
	[ "Hazardous or Poisonous Substance", "T131", "A1.4.1.1.5"],
	[ "Organism", "T001", "A1.1"],
	[ "Plant", "T002", "A1.1.1"],
	[ "Alga", "T003", "A1.1.1.1"],
	[ "Social Behavior", "T054", "B1.1.1"],
	[ "Individual Behavior", "T055", "B1.1.2"],
	[ "Daily or Recreational Activity", "T056", "B1.2"],
	[ "Occupational Activity", "T057", "B1.3"],
	[ "Health Care Activity", "T058", "B1.3.1"],
	[ "Laboratory Procedure", "T059", "B1.3.1.1"],
	[ "Diagnostic Procedure", "T060", "B1.3.1.2"],
	[ "Therapeutic or Preventive Procedure", "T061", "B1.3.1.3"],
	[ "Research Activity", "T062", "B1.3.2"],
	[ "Molecular Biology Research Technique", "T063", "B1.3.2.1"],
	[ "Governmental or Regulatory Activity", "T064", "B1.3.3"],
	[ "Educational Activity", "T065", "B1.3.4"],
	[ "Machine Activity", "T066", "B1.4"],
	[ "Phenomenon or Process", "T067", "B2"],
	[ "Human-caused Phenomenon or Process", "T068", "B2.1"],
	[ "Environmental Effect of Humans", "T069", "B2.1.1"],
	[ "Natural Phenomenon or Process", "T070", "B2.2"],
	[ "Entity", "T071", "A"],
	[ "Physical Object", "T072", "A1"],
	[ "Manufactured Object", "T073", "A1.3"],
	[ "Medical Device", "T074", "A1.3.1"],
	[ "Research Device", "T075", "A1.3.2"],
	[ "Conceptual Entity", "T077", "A2"],
	[ "Idea or Concept", "T078", "A2.1"],
	[ "Temporal Concept", "T079", "A2.1.1"],
	[ "Qualitative Concept", "T080", "A2.1.2"],
	[ "Quantitative Concept", "T081", "A2.1.3"],
	[ "Spatial Concept", "T082", "A2.1.5"],
	[ "Geographic Area", "T083", "A2.1.5.4"],
	[ "Molecular Sequence", "T085", "A2.1.5.3"],
	[ "Nucleotide Sequence", "T086", "A2.1.5.3.1"],
	[ "Amino Acid Sequence", "T087", "A2.1.5.3.2"],
	[ "Carbohydrate Sequence", "T088", "A2.1.5.3.3"],
	[ "Regulation or Law", "T089", "A2.4.2"],
	[ "Occupation or Discipline", "T090", "A2.6"],
	[ "Biomedical Occupation or Discipline", "T091", "A2.6.1"],
	[ "Organization", "T092", "A2.7"],
	[ "Health Care Related Organization", "T093", "A2.7.1"],
	[ "Professional Society", "T094", "A2.7.2"],
	[ "Self-help or Relief Organization", "T095", "A2.7.3"],
	[ "Group", "T096", "A2.9"],
	[ "Professional or Occupational Group", "T097", "A2.9.1"],
	[ "Population Group", "T098", "A2.9.2"],
	[ "Family Group", "T099", "A2.9.3"],
	[ "Age Group", "T100", "A2.9.4"],
	[ "Patient or Disabled Group", "T101", "A2.9.5"],
	[ "Body Part, Organ, or Organ Component", "T023", "A1.2.3.1"],
	[ "Tissue", "T024", "A1.2.3.2"],
	[ "Cell", "T025", "A1.2.3.3"],
	[ "Cell Component", "T026", "A1.2.3.4"],
	[ "Gene or Genome", "T028", "A1.2.3.5"],
	[ "Body Location or Region", "T029", "A2.1.5.2"],
	[ "Body Space or Junction", "T030", "A2.1.5.1"],
	[ "Substance", "T167", "A1.4"],
	[ "Food", "T168", "A1.4.3"],
	[ "Functional Concept", "T169", "A2.1.4"],
	[ "Intellectual Product", "T170", "A2.4"],
	[ "Language", "T171", "A2.5"],
	[ "Sign or Symptom", "T184", "A2.2.2"],
	[ "Classification", "T185", "A2.4.1"],
	[ "Anatomical Abnormality", "T190", "A1.2.2"],
	[ "Group Attribute", "T102", "A2.8"],
	[ "Chemical", "T103", "A1.4.1"],
	[ "Chemical Viewed Structurally", "T104", "A1.4.1.2"],
	[ "Organic Chemical", "T109", "A1.4.1.2.1"],
	[ "Steroid", "T110", "A1.4.1.2.1.9.1"],
	[ "Fungus", "T004", "A1.1.2"],
	[ "Virus", "T005", "A1.1.3"],
	[ "Rickettsia or Chlamydia", "T006", "A1.1.4"],
	[ "Bacterium", "T007", "A1.1.5"],
	[ "Animal", "T008", "A1.1.7"],
	[ "Invertebrate", "T009", "A1.1.7.1"],
	[ "Vertebrate", "T010", "A1.1.7.2"],
	[ "Amphibian", "T011", "A1.1.7.2.1"],
	[ "Bird", "T012", "A1.1.7.2.2"],
	[ "Fish", "T013", "A1.1.7.2.3"],
	[ "Reptile", "T014", "A1.1.7.2.4"],
	[ "Mammal", "T015", "A1.1.7.2.5"],
	[ "Human", "T016", "A1.1.7.2.5.1"],
	[ "Anatomical Structure", "T017", "A1.2"],
	[ "Embryonic Structure", "T018", "A1.2.1"],
	[ "Congenital Abnormality", "T019", "A1.2.2.1"],
	[ "Acquired Abnormality", "T020", "A1.2.2.2"],
	[ "Fully Formed Anatomical Structure", "T021", "A1.2.3"],
	[ "Body System", "T022", "A2.1.4.1"],
	[ "Neoplastic Process", "T191", "B2.2.1.2.1.2"],
	[ "Receptor", "T192", "A1.4.1.1.3.6"],
	[ "Archaeon", "T194", "A1.1.6"],
	[ "Antibiotic", "T195", "A1.4.1.1.1.1"],
	[ "Body Substance", "T031", "A1.4.2"],
	[ "Organism Attribute", "T032", "A2.3"],
	[ "Finding", "T033", "A2.2"],
	[ "Laboratory or Test Result", "T034", "A2.2.1"],
	[ "Injury or Poisoning", "T037", "B2.3"],
	[ "Biologic Function", "T038", "B2.2.1"],
	[ "Physiologic Function", "T039", "B2.2.1.1"],
	[ "Organism Function", "T040", "B2.2.1.1.1"],
	[ "Mental Process", "T041", "B2.2.1.1.1.1"],
	[ "Organ or Tissue Function", "T042", "B2.2.1.1.2"],
	[ "Cell Function", "T043", "B2.2.1.1.3"],
	[ "Molecular Function", "T044", "B2.2.1.1.4"],
	[ "Genetic Function", "T045", "B2.2.1.1.4.1"],
	[ "Pathologic Function", "T046", "B2.2.1.2"],
	[ "Disease or Syndrome", "T047", "B2.2.1.2.1"],
	[ "Mental or Behavioral Dysfunction", "T048", "B2.2.1.2.1.1"],
	[ "Cell or Molecular Dysfunction", "T049", "B2.2.1.2.2"],
	[ "Experimental Model of Disease", "T050", "B2.2.1.2.3"],
	[ "Event", "T051", "B"],
	[ "Activity", "T052", "B1"],
	[ "Behavior", "T053", "B1.1"],
	[ "Vitamin", "T127", "A1.4.1.1.3.4"],
	[ "Eicosanoid", "T111", "A1.4.1.2.1.9.2"],
	[ "Nucleic Acid, Nucleoside, or Nucleotide", "T114", "A1.4.1.2.1.5"],
	[ "Organophosphorus Compound", "T115", "A1.4.1.2.1.6"],
	[ "Amino Acid, Peptide, or Protein", "T116", "A1.4.1.2.1.7"],
	[ "Carbohydrate", "T118", "A1.4.1.2.1.8"],
	[ "Lipid", "T119", "A1.4.1.2.1.9"],
	[ "Chemical Viewed Functionally", "T120", "A1.4.1.1"],
	[ "Pharmacologic Substance", "T121", "A1.4.1.1.1"],
	[ "Biomedical or Dental Material", "T122", "A1.4.1.1.2"],
	[ "Biologically Active Substance", "T123", "A1.4.1.1.3"],
	[ "Neuroreactive Substance or Biogenic Amine", "T124", "A1.4.1.1.3.1"],
	[ "Hormone", "T125", "A1.4.1.1.3.2"],
	[ "Enzyme", "T126", "A1.4.1.1.3.3"]
];
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
  var word = document.styframe.textfield.value;
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
  if (document.styframe.textselected.value == "") {
    nl = "";
  } else {
    nl = "\n";
  }
  document.styframe.textselected.value = document.styframe.textselected.value + nl + neg + word;
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

function reinittext() {
  document.styframe.textfield.value="";
  document.styframe.textnegate.checked = false;
  document.styframe.textselected.value = "";
}

function cleartext() {
  document.styframe.textfield.value="";
  document.styframe.textnegate.checked = false;
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
