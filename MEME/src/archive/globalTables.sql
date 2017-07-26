--
-- Oracle .sql script for creating global area tables.
-- To be run from the UMLS_ARCHIVE_EXTRA schema
--

--
-- GLOBAL_MRSAB
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_mrsab'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_mrsab (
 release varchar2(8) NOT NULL,
 vsab varchar2(40) NOT NULL,
 rsab varchar2(40) NOT NULL);

--
-- GLOBAL_MRXNS_ENG
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_mrxns_eng'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_mrxns_eng (
 lat varchar2(3) NOT NULL,
 nstr varchar2(4000) NOT NULL,
 cui varchar2(10) NOT NULL,
 lui varchar2(10) NOT NULL,
 sui varchar2(10) NOT NULL);

 --
-- GLOBAL_MRXNS_ENG
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_mrxnw_eng'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_mrxnw_eng (
 lat varchar2(3) NOT NULL,
 nwd varchar2(100) NOT NULL,
 cui varchar2(10) NOT NULL,
 lui varchar2(10) NOT NULL,
 sui varchar2(10) NOT NULL);

--
-- GLOBAL_MRXW_ENG
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_mrxw_eng'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_mrxw_eng (
 lat varchar2(3) NOT NULL,
 wd varchar2(100) NOT NULL,
 cui varchar2(10) NOT NULL,
 lui varchar2(10) NOT NULL,
 sui varchar2(10) NOT NULL);

--
-- GLOBAL_RELEASE_BITPOS
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_release_bitpos'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_release_bitpos (
 release varchar2(8) NOT NULL,
 bitpos integer NOT NULL);
 

--
-- GLOBAL_CUI
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_cui'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_cui (
 cui varchar2(10) NOT NULL,
 bitmask varchar2(100) NOT NULL);

--
-- GLOBAL_AUI
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_aui'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_aui (
 aui varchar2(10) NOT NULL,
 bitmask varchar2(100) NOT NULL);

--
-- GLOBAL_SUI
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_sui'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_sui (
 sui varchar2(10) NOT NULL,
 bitmask varchar2(100) NOT NULL);

--
-- GLOBAL_STR
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_str'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_str (
 str varchar2(4000) NOT NULL,
 bitmask varchar2(100) NOT NULL);

--
-- GLOBAL_LSTR
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_lstr'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_lstr (
 lstr varchar2(4000) NOT NULL,
 bitmask varchar2(100) NOT NULL);
 

--
-- GLOBAL_SUI_LUI
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE global_sui_lui'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE global_sui_lui (
 sui varchar2(10) NOT NULL,
 lui varchar2(10) NOT NULL);

 
--
-- SEMGROUPS
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE semgroups'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE semgroups (
 abbr varchar2(10) NOT NULL,
 name varchar2(100) NOT NULL,
 ui varchar2(16) NOT NULL,
 sty varchar2(256) NOT NULL);
 
--
-- SEMGROUP_COUNTS_BY_RELEASE
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE semgroup_counts_by_release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE semgroup_counts_by_release (
 release varchar2(10) NOT NULL,
 abbr varchar2(10) NOT NULL,
 ct integer NOT NULL);

--
-- SEMANTIC_TYPES
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE semantic_types'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE semantic_types (
 sty varchar2(256) NOT NULL,
 is_chem char(1) NOT NULL,
 chem_type char(1),
 editing_chem char(1) NOT NULL);


--
-- GroupingData_RRF.txt (GROUPINGDATA_RRF external table)
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE groupingdata_rrf'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE groupingdata_rrf (
  release varchar2(10) NOT NULL,
  file varchar2(50) NOT NULL,
  name varchar2(100) NOT NULL,
  value varchar2(1000),
  sab varchar2(40),
  ct integer NOT NULL);

--
-- GroupingData_ORF.txt (GROUPINGDATA_ORF external table)
--
BEGIN EXECUTE IMMEDIATE 'DROP TABLE groupingdata_orf'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE groupingdata_orf (
  release varchar2(10) NOT NULL,
  file varchar2(50) NOT NULL,
  name varchar2(100) NOT NULL,
  value varchar2(1000),
  sab varchar2(40),
  ct integer NOT NULL);


--
-- NO VIEWS
--