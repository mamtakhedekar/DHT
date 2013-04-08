CREATE TABLE SKey (ID INTEGER NOT NULL, NKey INTEGER, SKey VARCHAR(255), PRIMARY KEY (ID))
CREATE TABLE ITEM (ID INTEGER NOT NULL, VALUE VARCHAR(255), KEY_ID INTEGER, PRIMARY KEY (ID));
ALTER TABLE ITEM ADD CONSTRAINT FK_ITEM_KEY_ID FOREIGN KEY (KEY_ID) REFERENCES SKey (ID);
CREATE TABLE SEQUENCE (SEQ_NAME VARCHAR(50) NOT NULL, SEQ_COUNT DECIMAL(38), PRIMARY KEY (SEQ_NAME));
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('SEQ_GEN', 0);
