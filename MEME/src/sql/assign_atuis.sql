    EXEC MEME_UTILITY.drop_it('table','new_stys_$$');
    CREATE TABLE new_stys_$$ AS
    SELECT DISTINCT cs.cui,a.hashcode
    FROM concept_status cs, attributes a
    WHERE cs.tobereleased in ('Y','y')
      AND a.tobereleased in ('Y','y')
      AND a.attribute_name in ('SEMANTIC_TYPE','NON_HUMAN')
      AND a.concept_id = cs.concept_id
    MINUS
    SELECT sg_id, hashcode
    FROM attributes_ui
    WHERE sg_type = 'CUI'
      AND attribute_name in ('SEMANTIC_TYPE','NON_HUMAN')
      AND root_source = 'MTH';

    exec MEME_SYSTEM.drop_indexes('source_attributes');
    exec MEME_SYSTEM.truncate('source_attributes');
    INSERT INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
           sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
           attribute_value, generated_status, source, status, released,
           tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R', 0, 0, 0, concept_id,
           cui, 'CUI', '', 'C', attribute_name, attribute_value, 'Y','MTH',
           'R','N','Y',0,'N', '', '', hashcode from
    (SELECT cs.cui, cs.concept_id, a.attribute_name, a.attribute_value, a.hashcode
    FROM concept_status cs, attributes a
    WHERE cs.tobereleased in ('Y','y')
          AND a.tobereleased in ('Y','y')
          AND a.attribute_name in ('SEMANTIC_TYPE','NON_HUMAN')
          AND a.concept_id = cs.concept_id
          AND (cs.cui, a.hashcode) IN
              (SELECT b.cui, b.hashcode FROM new_stys_$$ b)
    );
    COMMIT;

    UPDATE source_attributes
    SET attribute_id = rownum;
    COMMIT;

    exec MEME_SYSTEM.reindex('source_attributes');
    exec meme_source_processing.assign_atuis(table_name => 'SA',authority => 'MTH',work_id => 0);
    drop table new_stys_$$;
    exit;
