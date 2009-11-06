#---------------------------------------------------------------------------
# TEST_TABLE
#---------------------------------------------------------------------------

DROP TABLE IF EXISTS TEST_TABLE;
CREATE TABLE TEST_TABLE
  (COLUMN0 VARCHAR(32),
   COLUMN1 VARCHAR(32),
   COLUMN2 VARCHAR(32),
   COLUMN3 VARCHAR(32)) TYPE = InnoDB;

#---------------------------------------------------------------------------
# SECOND_TABLE
#---------------------------------------------------------------------------

DROP TABLE IF EXISTS SECOND_TABLE;
CREATE TABLE SECOND_TABLE
  (COLUMN0 VARCHAR(32),
   COLUMN1 VARCHAR(32),
   COLUMN2 VARCHAR(32),
   COLUMN3 VARCHAR(32)) TYPE = InnoDB;

#---------------------------------------------------------------------------
# EMPTY_TABLE
#---------------------------------------------------------------------------

DROP TABLE IF EXISTS EMPTY_TABLE;
CREATE TABLE EMPTY_TABLE
  (COLUMN0 VARCHAR(32),
   COLUMN1 VARCHAR(32),
   COLUMN2 VARCHAR(32),
   COLUMN3 VARCHAR(32)) TYPE = InnoDB;

#---------------------------------------------------------------------------
# PK_TABLE
#---------------------------------------------------------------------------

DROP TABLE IF EXISTS PK_TABLE;
CREATE TABLE PK_TABLE
  (PK0 NUMERIC(38, 0) NOT NULL,
   PK1 NUMERIC(38, 0) NOT NULL,
   PK2 NUMERIC(38, 0) NOT NULL,
   NORMAL0 VARCHAR(32),
   NORMAL1 VARCHAR(32), PRIMARY KEY (PK0, PK1, PK2)) TYPE = InnoDB;

#---------------------------------------------------------------------------
# ONLY_PK_TABLE
#---------------------------------------------------------------------------

DROP TABLE IF EXISTS ONLY_PK_TABLE;
CREATE TABLE ONLY_PK_TABLE
  (PK0 NUMERIC(38, 0) NOT NULL PRIMARY KEY) TYPE = InnoDB;

#---------------------------------------------------------------------------
# EMPTY_MULTITYPE_TABLE
#---------------------------------------------------------------------------

DROP TABLE IF EXISTS EMPTY_MULTITYPE_TABLE;
CREATE TABLE EMPTY_MULTITYPE_TABLE
  (VARCHAR_COL VARCHAR(32),
   NUMERIC_COL NUMERIC(38, 0),
   TIMESTAMP_COL TIMESTAMP NULL,
   VARBINARY_COL VARBINARY(254)) TYPE = InnoDB;
