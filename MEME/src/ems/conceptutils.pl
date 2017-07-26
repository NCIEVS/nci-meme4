# Some utility functions for concepts

# suresh@nlm.nih.gov 2/98
# suresh@nlm.nih.gov 2/00 - ported to Oracle

package conceptutils;

#unshift @INC, "/site/umls/oracle/utils";
#require "oracleIF.pl";
use OracleIF;

# converts a CUI to a concept-id
sub cui2conceptid {
    my($oracleDBH, $cui) = @_;
    my($SQL);

    $SQL = <<"EOD";
SELECT DISTINCT(concept_id) FROM attributes WHERE
    attribute_name='CUI' AND
    attribute_value=\'$cui\'
EOD
    $SQL = <<"EOD";
SELECT DISTINCT(concept_id) FROM concept_status WHERE cui=\'$cui\'
EOD
    return $oracleDBH->selectFirstAsScalar($SQL);
}

# code to concept_ids
sub code2conceptids {
    my($oracleDBH, $code) = @_;
    my($SQL, @rows);

    $SQL = <<"EOD";
SELECT DISTINCT(concept_id) FROM classes WHERE
    code=\'$code\'
EOD
    @rows = $oracleDBH->selectAllAsArray( $SQL);
    return(@rows);
}

# code to atom_ids
sub code2atomids {
    my($oracleDBH, $code) = @_;
    my($SQL, @rows);

    $SQL = <<"EOD";
SELECT atom_id FROM classes WHERE code=\'$code\'
EOD
    @rows = $oracleDBH->selectAllAsArray( $SQL);
    return(@rows);
}

# source_cui to atom_ids
sub scui2atomids {
    my($oracleDBH, $scui) = @_;
    my($SQL, @rows);

    $SQL = <<"EOD";
SELECT atom_id FROM classes WHERE source_cui=\'$scui\'
EOD
    @rows = $oracleDBH->selectAllAsArray( $SQL);
    return(@rows);
}

sub ndccode2atomids {
   my ($oracleDBH,$code) = @_;
   my ($SQL, @rows);
   my $whereSQL = " WHERE attribute_name = \'NDC\' AND MEME_OPERATIONS.Get_Norm_Ndc(attribute_value) = MEME_OPERATIONS.Get_Norm_Ndc(\'" . $code ."\') AND (";
   if (substr($code,0,1) ne "0") {
     $whereSQL = $whereSQL . "attribute_value like \'0" . substr($code,0,2) . "%\' OR ";
   }
   $whereSQL = $whereSQL . " attribute_value like \'" . substr($code,0,2) . "%\')";
   $SQL = <<"EOD";
SELECT atom_id from attributes $whereSQL
EOD
   print STDERR $SQL;
   @rows = $oracleDBH->selectAllAsArray($SQL);
  return (@rows);
}
# Converts a concept_id to CUIs
sub conceptid2cuis {
    my($oracleDBH, $conceptid) = @_;
    my($SQL);

    $SQL = <<"EOD";
SELECT DISTINCT(attribute_value) FROM attributes WHERE
    attribute_name='CUI' AND
    concept_id=$conceptid
EOD
    $SQL = <<"EOD";
SELECT DISTINCT(cui) FROM concept_status WHERE concept_id=$conceptid
EOD
    return $oracleDBH->selectAllAsArray($SQL);
}

# Extracts all STYs for a concept given a concept_id
sub conceptid2stys {
    my($oracleDBH, $conceptid) = @_;
    my($SQL);

    $SQL = <<"EOD";
SELECT DISTINCT attribute_value FROM attributes WHERE
    attribute_name='SEMANTIC_TYPE' AND
    concept_id=$conceptid
EOD
    return $oracleDBH->selectAllAsArray($SQL);
}

# Returns the concept-id for an atom-id
sub atomid2conceptid {
    my($oracleDBH, $atomid) = @_;
    my($conceptid);
    my($SQL);

    $SQL = <<"EOD";
SELECT concept_id FROM classes WHERE atom_id = $atomid
EOD
    return $oracleDBH->selectFirstAsScalar($SQL);
}

# All the atom_ids for a source_row_id
sub sourcerowid2atomids {
    my($oracleDBH, $source_row_id) = @_;
    my($SQL);

    $SQL = <<"EOD";
SELECT local_row_id FROM source_id_map
WHERE  source_row_id=$source_row_id
AND    table_name='C'
EOD
    return $oracleDBH->selectAllAsArray($SQL);
}

# concept_id to preferred string
sub conceptid2str {
    my($oracleDBH, $conceptid) = @_;
    my($SQL);
    my($term);

# in cache?
    return $conceptid2str{$conceptid} if $conceptid2str{$conceptid};

    $SQL = <<"EOD";
SELECT a.atom_name FROM atoms a, concept_status cs WHERE
    cs.concept_id = $conceptid AND
    a.atom_id = cs.preferred_atom_id
EOD
    $term = $oracleDBH->selectFirstAsScalar( $SQL);

# cache this
    $conceptid2str{$conceptid} = $term;
    return($term);
}
sub attrbutesfromconceptid {
    my($oracleDBH, $conceptid) = @_;
    my($SQL);
    my($term);

# in cache?
    return $conceptid2str{$conceptid} if $conceptid2str{$conceptid};

    $SQL = <<"EOD";
     SELECT a.attribute_id, attribute_level, a.atom_id,a.attribute_name,a.attribute_value, a.status, a.tobereleased,source 
     FROM attributes a
   WHERE
    a.concept_id = $conceptid AND
    attribute_value NOT LIKE '<>Long_Attribute<>:%'
UNION
   SELECT a.attribute_id, attribute_level,a.atom_id, a.attribute_name,b.text_value, a.status, a.tobereleased,source 
   FROM attributes a, stringtab b
   WHERE
    a.concept_id = $conceptid AND
    attribute_value LIKE '<>Long_Attribute<>:%' AND
    b.string_id = to_number(substr(attribute_value,20))
EOD
    return $oracleDBH->selectAllAsRef($SQL);
}
1;
