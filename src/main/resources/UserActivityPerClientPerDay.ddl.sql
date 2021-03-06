CREATE TABLE IF NOT EXISTS `USER_ACTIVITY_PER_CLIENT_PER_DAY` (
  `USER_ID` bigint NOT NULL,
  `DATE` date NOT NULL,
  `CLIENT` ENUM('R','PYTHON','WEB', 'JAVA', 'COMMAND_LINE', 'ELB_HEALTHCHECKER', 'UNKNOWN', 'SYNAPSER') NOT NULL,
  PRIMARY KEY (`USER_ID`, `DATE`, `CLIENT`),
  INDEX (CLIENT),
  INDEX (USER_ID),
  INDEX (DATE)
)
