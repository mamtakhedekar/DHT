ALTER TABLE ITEM DROP CONSTRAINT FK_ITEM_KEY_ID
DROP TABLE SKey
DROP TABLE ITEM
DELETE FROM SEQUENCE WHERE SEQ_NAME = 'SEQ_GEN'
