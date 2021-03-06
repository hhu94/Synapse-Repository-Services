CREATE TABLE IF NOT EXISTS `SUBSCRIPTION` (
  `ID` bigint(20) NOT NULL,
  `SUBSCRIBER_ID` bigint(20) NOT NULL,
  `OBJECT_ID` bigint(20) NOT NULL,
  `OBJECT_TYPE` ENUM('FORUM', 'THREAD', 'DATA_ACCESS_SUBMISSION', 'DATA_ACCESS_SUBMISSION_STATUS') NOT NULL,
  `CREATED_ON` bigint(20) NOT NULL,
  PRIMARY KEY (`SUBSCRIBER_ID`, `OBJECT_ID`, `OBJECT_TYPE`),
  UNIQUE (`ID`),
  CONSTRAINT `DISCUSSION_THREAD_SUBSCRIBER_ID_FK` FOREIGN KEY (`SUBSCRIBER_ID`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE CASCADE,
  INDEX `SUBSCRIPTION_TOPIC_INDEX` (`OBJECT_ID`, `OBJECT_TYPE`)
)
