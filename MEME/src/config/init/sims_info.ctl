options (direct=true)
unrecoverable
load data
infile 'sims_info' "str X'7c0a'"
badfile 'sims_info.bad'
discardfile 'sims_info.dsc'
truncate
into table sims_info
fields terminated by '|'
trailing nullcols
(
source                                  CHAR,
date_created                            DATE "DD-mon-YYYY HH24:MI:SS",
meta_year                               CHAR,
init_rcpt_date                          DATE "DD-mon-YYYY HH24:MI:SS",
clean_rcpt_date                         DATE "DD-mon-YYYY HH24:MI:SS",
test_insert_date                        DATE "DD-mon-YYYY HH24:MI:SS",
real_insert_date                        DATE "DD-mon-YYYY HH24:MI:SS",
source_contact                          CHAR(4000),
inverter_contact                        CHAR,
nlm_path                                CHAR,
apelon_path                             CHAR,
inversion_script                        CHAR,
inverter_notes_file                     CHAR,
conserve_file                           CHAR,
sab_list                                CHAR,
meow_display_name                       CHAR(3000),
source_desc                             CHAR(4000),
status                                  CHAR,
worklist_sortkey_loc                    CHAR,
termgroup_list                          CHAR(4000),
attribute_list                          CHAR(4000),
inversion_notes                         CHAR(4000),
notes                                   CHAR(4000),
inv_recipe_loc                          CHAR,
suppress_edit_rec                       CHAR,
versioned_cui                           CHAR,
root_cui                                CHAR,
source_official_name                    CHAR(3000),
source_short_name                       CHAR(3000),
attribute_name_list                     CHAR(2000),
term_type_list                          CHAR(1000),
term_frequency                          DECIMAL EXTERNAL,
cui_frequency                           DECIMAL EXTERNAL,
citation                                CHAR(4000),
last_contacted                          DATE "DD-mon-YYYY HH24:MI:SS",
license_info                            CHAR(4000),
character_set                           CHAR,
valid_start_date                        DATE "DD-mon-YYYY HH24:MI:SS",
valid_end_date                          DATE "DD-mon-YYYY HH24:MI:SS",
insert_meta_version                     CHAR,
remove_meta_version                     CHAR,
nlm_contact                             CHAR,
acquisition_contact                     CHAR(1000),
content_contact                         CHAR(1000),
license_contact                         CHAR(1000),
context_type                            CHAR,
language                                CHAR,
test_insertion_start                    DATE "DD-mon-YYYY HH24:MI:SS",
test_insertion_end                      DATE "DD-mon-YYYY HH24:MI:SS",
real_insertion_start                    DATE "DD-mon-YYYY HH24:MI:SS",
real_insertion_end                      DATE "DD-mon-YYYY HH24:MI:SS",
editing_start                           DATE "DD-mon-YYYY HH24:MI:SS",
editing_end                             DATE "DD-mon-YYYY HH24:MI:SS",
latest_available                        CHAR,
release_url_list                        CHAR(1000),
internal_url_list                       CHAR(1000),
rel_directionality_flag                 CHAR
)
