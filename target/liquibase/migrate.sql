--  *********************************************************************
--  Update Database Script
--  *********************************************************************
--  Change Log: src/main/resources/database_changelog.xml
--  Ran at: 25.04.2020, 18:53
--  Against: root@localhost@jdbc:mysql://localhost:3306/myproject?serverTimezone=UTC&allowMultiQueries=true
--  Liquibase version: 3.5.4
--  *********************************************************************

--  Lock Database
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 1, LOCKEDBY = 'DESKTOP-81TLO5H (192.168.0.102)', LOCKGRANTED = '2020-04-25 18:53:02.330' WHERE ID = 1 AND LOCKED = 0;

--  Changeset src/main/resources/db.changelog/testdata/2020-02-04-insert-test-data.xml::2020-02-04-insert-test-data::esv
BEGIN;

INSERT INTO users(id, is_moderator, reg_time, name, email, password, code, photo) VALUES
(1, 1,  '2016-01-06','Bonita Boyer',  'tarakan@mail.ru', '68350669', '1234', '1234'),
(2, 1,'2014-09-30', 'Ernie Adamson',  'WaltraudK.Pearson96@example.com', '44968843', '1234', '1234'),
(3, -1, '2000-02-16','Hosea Tolliver',  'dydqi814@example.com', '982937485', '1234', '1234'),
(4, 1, '1996-04-22','Maryanna Boykin',   'ElliottSkaggs@example.com', '989219271', '1234', '1234'),
(5, -1, '1970-01-04','Hank Mullin',   'HerschelKendall72@example.com', '80403776', '1234', '1234'),
(6, 1, '2015-07-21','Alpha Figueroa',   'SheriseLogue@example.com', '330900623', '1234', '1234'),
(7, -1, '1988-03-03','Elisha Schmitz',   'Melda.Mercier27@nowhere.com', '9433394', '1234', '1234'),
(8, 1,'1972-06-22','Clifford Tomlin',   'FossD3@nowhere.com', '8272165', '1234', '1234'),
(9, -1,'2009-03-09','Kenneth Rayburn',   'Copeland@example.com', '283044892', '1234', '1234'),
(10, 1, '1987-07-09','Dominique Schneider',  'Meacham33@nowhere.com', '8223815', '1234', '1234');

COMMIT;

INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('2020-02-04-insert-test-data', 'esv', 'src/main/resources/db.changelog/testdata/2020-02-04-insert-test-data.xml', NOW(), 1, '7:fce16330d9bf8293b36f0fdaafbf6032', 'sqlFile', '', 'EXECUTED', NULL, NULL, '3.5.4', '7829988040');

--  Release Database Lock
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 0, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1;

