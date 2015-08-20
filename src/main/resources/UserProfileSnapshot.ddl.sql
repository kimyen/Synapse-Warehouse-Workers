CREATE TABLE IF NOT EXISTS `USER_PROFILE_SNAPSHOT` (
  `TIMESTAMP` bigint NOT NULL,
  `ID` bigint(20) NOT NULL,
  `USER_NAME` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `FIRST_NAME` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `LAST_NAME` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `EMAIL` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `LOCATION` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `COMPANY` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `POSITION` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  PRIMARY KEY (`ID`, `TIMESTAMP`),
  INDEX (USER_NAME),
  INDEX (LOCATION),
  INDEX (COMPANY),
  INDEX (POSITION)
)
