#!@PATH_TO_PERL
#
# Creates an Oracle .sql script for wrapping a set
# of ORF files with external tables.
#
# input: <Archive release version, e.g 2008AA> <ORF dir>
# output: .sql script written to STDOUT
#
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set Defaults & Environment
#
unless ( $ENV{ARCHIVE_HOME} ) {
 $badvalue = "ARCHIVE_HOME";
 $badargs  = 4;
}

#
# Check options
#
$| = 1;
@ARGS = ();
while (@ARGV) {
 $arg = shift(@ARGV);
 if ( $arg !~ /^-/ ) {
  push @ARGS, $arg;
  next;
 }
 if ( $arg eq "-help" || $arg eq "--help" ) {
  &PrintHelp;
  exit(0);
 } else {
  $badargs = 1;
 }
}

#
# Get command line params
#
if ( scalar(@ARGS) == 2 ) {
 ( $release, $dir ) = @ARGS;
 $release = uc($release);
} else {
 $badargs  = 3;
 $badvalue = scalar(@ARGS);
}

#
# Process errors
#
%errors = (
            1 => "Illegal switch: $badvalue",
            3 => "Bad number of arguments: $badvalue",
            4 => "$badvalue must be set"
);
if ($badargs) {
 &PrintUsage;
 print "\n$errors{$badargs}\n";
 exit(1);
}
@languages = (
               "BAQ", "CZE", "DAN", "DUT", "ENG", "FIN",
               "FRE", "GER", "HEB", "HUN", "ITA", "JPN",
               "NOR", "POR", "RUS", "SPA", "SWE"
);

#
# Write script to STDOUT
#
print qq{

CREATE OR REPLACE DIRECTORY ext_orf$release AS '$dir';

BEGIN EXECUTE IMMEDIATE 'DROP TABLE ambig_lui_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE ambig_lui_orf$release (
    LUI char(8),
    CUI char(8)
)
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf$release
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (lui, cui)
  )
  LOCATION ('AMBIG.LUI')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE ambig_sui_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE ambig_sui_orf$release ( 
    SUI char(8), 
    CUI char(8) 
)
ORGANIZATION EXTERNAL(
  TYPE oracle_loader 
  DEFAULT DIRECTORY ext_orf$release 
  ACCESS PARAMETERS(
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (sui, cui)
  )
  LOCATION ('AMBIG.SUI')
)
REJECT LIMIT 0;


-- CHANGE/ files are not part of comparable ORF


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mratx_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mratx_orf$release (
    CUI varchar(8),
    SAB varchar(40),
    REL varchar(3),
    ATX varchar(300)
)
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf$release
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui, sab, rel, atx CHAR(300))
  )
  LOCATION ('MRATX')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrcoc_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrcoc_orf$release (
    CUI1 varchar(8),
    CUI2 varchar(8),
    SOC varchar(40),
    COT varchar(3),
    COF integer,
    COA varchar(300)
) 
ORGANIZATION EXTERNAL(
  TYPE oracle_loader 
  DEFAULT DIRECTORY ext_orf$release 
  ACCESS PARAMETERS(
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui1, cui2, soc, cot, cof INTEGER EXTERNAL, coa CHAR(300))
  )
  LOCATION ('MRCOC')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrcols_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrcols_orf$release (
    COL varchar2(10),
    DES varchar2(100),
    REF varchar2(20),
    MIN integer,
    AV  numeric(5,2),
    MAX integer,
    FIL varchar2(50),
    DTY varchar2(20))
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf$release
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (col, des, ref, min INTEGER EXTERNAL, av FLOAT EXTERNAL,
      max INTEGER EXTERNAL, fil, dty)
  )
  LOCATION ('MRCOLS')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrcon_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrcon_orf$release (
    CUI char(8),
    LAT char(3),
    TS char(1),
    LUI char(8),
    STT varchar2(3),
    SUI char(8),
    STR varchar2(3000),
    LRL integer
) 
ORGANIZATION EXTERNAL(
  TYPE oracle_loader 
  DEFAULT DIRECTORY ext_orf$release 
  ACCESS PARAMETERS(
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui, lat, ts, lui, stt, sui, str CHAR(3000), lrl INTEGER EXTERNAL)
  )
  LOCATION ('MRCON')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrcui_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrcui_orf$release (
    CUI1  char(8),
    VER varchar2(10),
    CREL  varchar2(4),
    CUI2  char(8),
    MAPIN char(1)
)
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf2007ac
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=10000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui1, ver, crel, cui2, mapin)
  )
  LOCATION ('MRCUI')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrcxt_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrcxt_orf$release (
    CUI char(8),
    SUI char(8),
    SAB varchar2(40),
    CODE varchar2(100),
    CXN integer,
    CXL char(3),
    RNK integer,
    CXS varchar2(3000),
    CUI2 char(8),
    HCD varchar2(100),
    RELA varchar2(100),
    XC varchar2(1)
) 
ORGANIZATION EXTERNAL(
  TYPE oracle_loader 
  DEFAULT DIRECTORY ext_orf$release 
  ACCESS PARAMETERS(
    RECORDS DELIMITED BY 0 X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui, sui, sab, code, cxn INTEGER EXTERNAL,
      cxl, rnk INTEGER EXTERNAL, cxs CHAR(3000), cui2, hcd, rela, xc)
  )
  LOCATION ('MRCXT')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrdef_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrdef_orf$release (
    CUI char(8),
    SAB varchar2(40),
    DEF varchar2(4000)
)
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf$release
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui, sab, def char(4000))
  )
  LOCATION ('MRDEF')
)
REJECT LIMIT 0;


-- NO MRDOC in comparable ORF


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrfiles_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrfiles_orf$release (
    FIL varchar2(50),
    DES varchar2(100),
    FMT varchar2(150),
    CLS integer,
    RWS integer,
    BTS integer
)
ORGANIZATION EXTERNAL(
  TYPE oracle_loader 
  DEFAULT DIRECTORY ext_orf$release 
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (fil, des, fmt, cls INTEGER EXTERNAL, rws INTEGER EXTERNAL, bts INTEGER EXTERNAL)
  )
  LOCATION ('MRFILES')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrjoin_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrjoin_orf$release (
    CUI char(8),
    LUI char(8),
    SUI char(8),
    SAB varchar2(40),
    TTY varchar2(20),
    CODE varchar2(100),
    SRL integer,
    LAT char(3),
    TS char(1),
    STT varchar(3),
    STR varchar2(3000)
)
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf$release
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui, lui, sui, sab, tty, code, srl INTEGER EXTERNAL,
      lat, ts, stt, str CHAR(3000))
  )
  LOCATION ('MRJOIN')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrrank_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrrank_orf$release (
    RANK integer,
    SAB varchar2(40),
    TTY varchar2(20), 
    SUPRES char(1)
) 
ORGANIZATION EXTERNAL(
  TYPE oracle_loader 
  DEFAULT DIRECTORY ext_orf$release 
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (rank INTEGER EXTERNAL, sab, tty, supres)
  )
  LOCATION ('MRRANK')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrrel_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrrel_orf$release (
    CUI1  char(8),
    REL varchar2(3),
    CUI2  char(8),
    RELA  varchar2(100),
    SAB varchar2(40),
    SL  varchar2(40),
    MG  varchar2(1)
)
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf$release
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui1, rel, cui2, rela, sab, sl, mg)
  )
  LOCATION ('MRREL')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrsab_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrsab_orf$release (
    VCUI char(8),
    RCUI char(8),
    VSAB varchar2(40),
    RSAB varchar2(40),
    SON varchar2(3000),
    SF varchar2(40),
    SVER varchar2(20),
    MSTART char(10),
    MEND char(8),
    IMETA varchar2(10),
    RMETA varchar2(10),
    SLC varchar2(1000),
    SCC varchar2(1000),
    SRL integer,
    TFR integer,
    CFR integer,
    CXTY varchar2(50),
    TTYL varchar2(200),
    ATNL varchar2(4000),
    LAT char(3),
    CENC varchar2(20),
    CURVER char(1),
    SABIN char(1)
)
ORGANIZATION EXTERNAL(
  TYPE oracle_loader 
  DEFAULT DIRECTORY ext_orf$release 
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (vcui, rcui, vsab, rsab, son CHAR(3000), sf, sver, mstart, mend, imeta, rmeta,
      slc CHAR(1000), scc CHAR(1000), srl INTEGER EXTERNAL, tfr INTEGER EXTERNAL,
      cfr INTEGER EXTERNAL, cxty, ttyl, atnl CHAR(4000), lat, cenc, curver, sabin)
  )
  LOCATION ('MRSAB')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrsat_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrsat_orf$release (
    CUI char(8),
    LUI char(8),
    SUI char(8),
    CODE  varchar2(100),
    ATN varchar2(100),
    SAB varchar2(40),
    ATV varchar2(4000)
)
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf$release
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui, lui, sui, code, atn, sab, atv CHAR(4000))
  )
  LOCATION ('MRSAT')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrso_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrso_orf$release (
    CUI char(8),
    LUI char(8),
    SUI char(8),
    SAB varchar2(40),
    TTY varchar2(20),
    CODE varchar2(100),
    SRL integer
) 
ORGANIZATION EXTERNAL(
  TYPE oracle_loader 
  DEFAULT DIRECTORY ext_orf$release 
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui, lui, sui, sab, tty, code, srl INTEGER EXTERNAL)
  )
  LOCATION ('MRSO')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrsty_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrsty_orf$release (
    CUI  char(8),
    TUI char(4),
    STY varchar2(50)
)
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf$release
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (cui, tui, sty)
  )
  LOCATION ('MRSTY')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrxns_eng_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrxns_eng_orf$release (
    LAT char(3),
    NSTR varchar2(3000),
    CUI char(8),
    LUI char(8),
    SUI char(8)
) 
ORGANIZATION EXTERNAL(
  TYPE oracle_loader 
  DEFAULT DIRECTORY ext_orf$release 
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (lat, nstr CHAR(3000), cui, lui, sui)
  )
  LOCATION ('MRXNS.ENG')
)
REJECT LIMIT 0;


BEGIN EXECUTE IMMEDIATE 'DROP TABLE mrxnw_eng_orf$release'; EXCEPTION WHEN OTHERS THEN NULL; END;
/

CREATE TABLE mrxnw_eng_orf$release (
    LAT char(3),
    NWD varchar2(100),
    CUI char(8),
    LUI char(8),
    SUI char(8)
)
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf$release
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (lat, nwd, cui, lui, sui)
  )
  LOCATION ('MRXNW.ENG')
)
REJECT LIMIT 0;
};

#
# Language MRXW tables
#
foreach $lat (@languages) {
 if ( -e "$dir/MRXW.$lat" ) {
  $table = "mrxw{${lat}_orf$release";
  print qq{
BEGIN EXECUTE IMMEDIATE 'DROP TABLE $table'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
CREATE TABLE mrxw_${lat}_orf$release (
    LAT char(3),
    WD  varchar2(100),
    CUI char(8),
    LUI char(8),
    SUI char(8)
)
ORGANIZATION EXTERNAL (
  TYPE oracle_loader
  DEFAULT DIRECTORY ext_orf$release
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY 0X'7c0a'
    CHARACTERSET UTF8
    STRING SIZES ARE IN CHARACTERS
    DATE_CACHE=50000
    FIELDS TERMINATED BY '|'
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS
     (lat, wd, cui, lui, sui)
  )
  LOCATION ('MRXW.${lat}')
)
REJECT LIMIT 0;

}
 }
}

#
# Write views and report counts
#
print qq{

-- Views
CREATE OR REPLACE VIEW mrcon_orf${release}_lstr AS
select CUI,LUI,SUI,LAT,lower(STR) as LSTR from mrcon_orf$release;


-- Report load counts
exec DBMS_OUTPUT.PUT_LINE('***** Counting Rows ... Please Wait *****');
select 'AMBIG.LUI'filename, count(*) from ambig_lui_orf${release}
union all
select 'AMBIG.SUI'filename, count(*) from ambig_sui_orf${release}
union all
select 'MRATX'filename, count(*) from mratx_orf${release}
union all
select 'MRCOC'filename, count(*) from mrcoc_orf${release}
union all
select 'MRCOLS'filename, count(*) from mrcols_orf${release}
union all
select 'MRCON'filename, count(*) from mrcon_orf${release}
union all
select 'MRCUI'filename, count(*) from mrcui_orf${release}
union all
select 'MRCXT'filename, count(*) from mrcxt_orf${release}
union all
select 'MRDEF'filename, count(*) from mrdef_orf${release}
union all
select 'MRFILES'filename, count(*) from mrfiles_orf${release}
union all
select 'MRJOIN'filename, count(*) from mrjoin_orf${release}
union all
select 'MRRANK'filename, count(*) from mrrank_orf${release}
union all
select 'MRREL'filename, count(*) from mrrel_orf${release}
union all
select 'MRSAB'filename, count(*) from mrsab_orf${release}
union all
select 'MRSAT'filename, count(*) from mrsat_orf${release}
union all
select 'MRSO'filename, count(*) from mrso_orf${release}
union all
select 'MRSTY'filename, count(*) from mrsty_orf${release}
union all
select 'MRXNS.ENG'filename, count(*) from mrxns_eng_orf${release}
union all
select 'MRXNW.ENG'filename, count(*) from mrxnw_eng_orf${release}
 };

foreach $lat (@languages) {
  if ( -e "$dir/MRXW.$lat" ) {
   print qq{union all 
select 'MRXW.$lat'filename, count(*) from mrxw_ ${lat} _orf ${release}
 };
  }
}
print ";\n";
######################### LOCAL PROCEDURES #######################
sub PrintUsage {
 print qq{ This script has the following usage:
    $0: <release> <dir>
};
}

sub PrintHelp {
 &PrintUsage;
 print qq{
 This script writes an SQL script for creating a set of external table
 wrappers around the comparable ORF files in the specified directory.
 The release name is used in building the table names

 Options:
       -[-]help:            On-line help

 Arguments:
       release:             The UMLS release
       dir:                 The dir containing RRF files

};
}
