#!@PATH_TO_PERL@  
# pretty-print a concept report from a set of meme3-format .src files
# modified 4/4/2001 by SSL to work with MEME3 format.  

use DB_File;
use Getopt::Std;

$| =1;
getopts('b:hp');

if(($#ARGV>2)||($opt_h)){ 
  print "USAGE: src.conreport.pl [-h][-b [y/n]]\n"; 
  print "option -h prints this message\n";
  print "option -b [y/n] controls whether databases are built\n";
  print "Default is that databases will be rebuilt if the files they are
    based on have changed\n"; 
  exit(0); 
 }

$a="ATOM.db";
$m="MERGE1.db";
$l="LIST.db";
$r="REL.db";
$a2="ATTS.db";

$class_file = "classes_atoms.src";
$merge_file = "mergefacts.src";
$att_file = "attributes.src";
$rel_file = "relationships.src";

(dbmopen %ATOM, $a, 0666 || die "Can't open file: $!") if (-e $class_file);
(dbmopen %LIST, $l, 0666 || die "Can't open file: $!") if (-e $merge_file);
(dbmopen %REL, $r, 0666 || die "Can't open file: $!") if (-e $rel_file);
(dbmopen %ATTS, $a2, 0666 || die "Can't open file: $!") if (-e $att_file);

if($opt_b){
  if ($opt_b eq "y"){$build_all=1}       #option set to yes, rebuild databases
  elsif ($opt_b eq "n"){$build_all=0}    #option set to no, don't rebuild dbs
 }

else{                #otherwise, rebuild only if files have changed
  $build_all=0;
  @class_stat = stat $class_file;
  ($class_time = $ATOM{TIME}) || ($ATOM{TIME}= $class_stat[9]);
  unless ($class_stat[9] == $class_time){
    $build_atom=1; 
    print "\nNeed to rebuild ATOMS.db\n";
   }
  @merge_stat = stat $merge_file;
  ($merge_time = $LIST{TIME}) || ($LIST{TIME}=$merge_stat[9]);
  unless ($merge_stat[9]==$merge_time){
    $build_atom=1; 
    print "Need to rebuild LIST.db\n";
   }
  @rel_stat = stat $rel_file;
  ($rel_time = $REL{TIME}) || ($REL{TIME}=$rel_stat[9]);
  unless ($rel_stat[9]==$rel_time){
    $build_rel=1; 
    print "Need to rebuild REL.db\n";
   }
  @att_stat = stat $att_file;
  ($att_time = $ATTS{TIME}) || ($ATTS{TIME}=$att_stat[9]);
  unless ($att_stat[9]==$att_time){
    $build_att=1; 
    print "Need to rebuild ATTS.db\n";
   }
 }

if ($build_atom){%ATOM=();%LIST=();}
if ($build_rel){%REL=()}
if ($build_att){%ATTS=()};


print "Enter Source Term Id Or Return To Exit:\n";
$term_id = <STDIN>;
chomp $term_id;
$done = $term_id;

while($term_id =~ /[0-9]/){
  if(($build_all)||($build_atom)){ #unless this has been done, get info:
    $atoms = "./classes_atoms.src";
    @class_stat = stat $atoms;
    $ATOM{TIME}=$class_stat[9];
    @merge_stat = stat $merge_file;
    $LIST{TIME}=$merge_stat[9];
    open(ATOMS, "<$atoms")|| die "Can't open $atoms: $!";
    print "Starting ATOMS Database...\n";
    while (<ATOMS>){
      @atoms = split(/\|/);
      $ID = $atoms[0];
      $code = $atoms[3];
      $text= $atoms[7];
      $ATOM{$ID} = ($text . "\[$code\]"); 
      #match all ID nums to their text atom name, 
      #once with code and once without
     }
    print "Done Making Atoms Database...\n" if $opt_p;
    open (MERGE, "<./mergefacts.src");
    while(<MERGE>){ 
      @merge_stat = stat $merge_file;
      $LIST{TIME}=$merge_stat[9];
      @fields = split(/\|/);
      $ID1 = $fields[0];
      $ID2 = $fields[2];
      unless ($LIST{$ID1} =~/$ID2/){
	$LIST{$ID1} .= ("+" . $ID2);   #and concept string
       }
      unless ($LIST{$ID2} =~/$ID1/){
	$LIST{$ID2} .= ("+" . $ID1);   #and concept string
       }
     }
    print "Done Making List Database...\n" if $opt_p;
    close(MERGE);
   } # end first run-only stuff
  
  $done = '';
  $list = '';
  
#The above should get all SYs of an atom-- ie the concept
  print  "***********Atoms In Concept: $term_id $ATOM{$term_id}*********\n";
  unless ($LIST{$term_id}){$LIST{$term_id}=$term_id}
  @list = split(/\+/, $LIST{$term_id});
  foreach $item_id(@list){
    $done .= ("+" . $item_id);
    print "$item_id\t$ATOM{$item_id}\n"; 
    @sub_list= split(/\+/, $LIST{$item_id});
    foreach $at(@sub_list){
      unless ($done =~ /$at/){
	print "$at\t$ATOM{$at}\n";
	$done .= ("+" . $at);
       }
     }
   }
  
  print "\n+++++Relationships In Concept:\n\n";
  if(($build_all)||($build_rel)){
    @rel_stat = stat $rel_file;
    $REL{TIME}=$rel_stat[9];
    open (RELS, "<relationships.src")|| die "Can't open rels";
    while(<RELS>){
      @fields = split(/\|/);
      $ID1 = $fields[2];
      $ID2 = $fields[5];
      $rel_type = $fields[3];
      $rel_type =~ s/\//\\\//;
      $rel_at = $fields[4];
      
      $REL{$ID1}.= "+" . $ATOM{$ID1}."\|".$rel_type."\|".$rel_at."\|".$ATOM{$ID2};
      $REL{$ID2}.= "+". $ATOM{$ID1}."\|". $rel_type."\|".$rel_at."\|".$ATOM{$ID2};
     }
    close(RELS);
   }# end first run-only stuff
     @r_list = split(/\+/,$done);
  foreach $term_id(@r_list){
    unless ($term_id) {next}
    $rel_done='';
    if ($REL{$term_id}){
      print "$ATOM{$term_id}:\n";
      @rels = split(/\+/,$REL{$term_id});
      foreach $rel(@rels){
	if($rel){
	  #unless(($rel_done)&&($rel_done=~/$rel/))
	  #{
	  $rel=~s/\\//;
	  print "$rel\n";
	  $rel_done .= ("+".$rel);
	 }
       }
     }
   }
   
  if (-e "attributes.src"){
    print "\n+++++Attributes In Concept:\n\n";
    if(($build_all)||($build_att)){
      @att_stat = stat $att_file;
      $ATTS{TIME}=$att_stat[9];
      open (ATTR, "<attributes.src");
      while(<ATTR>){ 
	chomp;
	@fields = split(/\|/);
	$ID=$fields[1];
	$att_type = $fields[3];
	$att_value = $fields[4];
	$att_line=($att_type ."|".$att_value);
	$ATTS{$ID}.= ("+" . $att_line);
       }
      close(ATTR);
     }
    @a_list = split(/\+/,$done);
    foreach $term_id(@a_list){
      if($term_id){
	if($ATTS{$term_id}){print "$ATOM{$term_id}:\n"}
	foreach $attribute(split(/\+/,$ATTS{$term_id})){
	  if ($attribute=~/CONTEXT/)
	   {
	    print "\tCONTEXT|\n"; 
	    context($attribute);
	   }
	  else{
	    if($attribute){print "\t$attribute\n"}
	   } 
	 }
       }
     }
   }
  print "*************************************\n";   
  print "\nEnter Source Id Or Return To Exit:\n";
  $term_id = <STDIN>;
  chomp $term_id;
  $done = $term_id;
  $build_all = 0; #next time round loop, don't build anything
  $build_atom =0;
  $build_att =0;
  $build_rel =0;
 }		       



#################### BRIAN'S CONTEXT PARSER
sub context{
  $_=$_[0];
  $\ = "\n";              # set output record separator
  chomp;
  s/\\t/\t/g;
  ($num, $code, $anc, $self, $chd, $sib) = split(/\t/);
  $CXind = 3;
  foreach (split(/~/, $anc)) {
    s/\^$//;    # bug in input: ANC's shouldn't have chflag's
    print ' ' x $CXind, $_;
    $CXind += 2;
   }
  $self =~ s/\^$//;  # bug in input: self lines shouldn't have chflag's
  @sibs = split(/~/, $sib);
  push(@sibs, $self);
  @sibs = sort caseins @sibs;
  foreach (@sibs) {
    if ($_ eq $self) {
      print ' ' x ($CXind-1), "<$self>";
      $CXind += 2;
      foreach $ch (split(/~/, $chd)) {
	$chflag = ($ch =~ s/\^$//) ? ' +' : '';
	print ' ' x $CXind, $ch . $chflag;
       }
      $CXind -= 2;
     } else {
       $chflag = (s/\^$//) ? ' +' : '';
       print ' ' x $CXind, $_ . $chflag;
      }
   }
 }
sub caseins {
  ($t1 = $a) =~ tr/a-z/A-Z/;
  ($t2 = $b) =~ tr/a-z/A-Z/;
  return ($t1 lt $t2) ? -1 : ($t1 eq $t2) ? 0 : 1;
 }


END{
  dbmclose %ATOM;
  dbmclose %LIST;
  dbmclose %REL;
  dbmclose %ATTS;
 }
