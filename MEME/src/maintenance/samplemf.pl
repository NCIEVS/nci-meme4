#!@PATH_TO_PERL@
#
# Changes:
#  03/27/2007 BAC (1-DU4EP): created simple script to get merge facts sample for recipe writing
#

while (<>) {
    ($id,$type,$id2,$sab,$d,$d,$d,$merge_set,$type,$qual,$typew,$qual2) = split /\|/;
    if (! $hash{$merge_set} ){
        print;
        }
    $hash{$merge_set} =1;
}
