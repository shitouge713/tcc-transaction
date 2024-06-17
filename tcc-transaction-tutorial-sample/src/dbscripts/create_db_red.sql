CREATE TABLE `RED_RED_PACKET_ACCOUNT`
(
    `RED_PACKET_ACCOUNT_ID` int(11) NOT NULL,
    `BALANCE_AMOUNT`        decimal(10, 0) DEFAULT NULL,
    `USER_ID`               int(11) DEFAULT NULL,
    PRIMARY KEY (`RED_PACKET_ACCOUNT_ID`)
);

CREATE TABLE `RED_TRADE_ORDER`
(
    `ID`                int(11) NOT NULL AUTO_INCREMENT,
    `SELF_USER_ID`      bigint(11) DEFAULT NULL,
    `OPPOSITE_USER_ID`  bigint(11) DEFAULT NULL,
    `MERCHANT_ORDER_NO` varchar(45) NOT NULL,
    `AMOUNT`            decimal(10, 0) DEFAULT NULL,
    `STATUS`            varchar(45)    DEFAULT NULL,
    `VERSION`           int(11) DEFAULT NULL,
    PRIMARY KEY (`ID`),
    UNIQUE KEY `MERCHANT_ORDER_NO_UNIQUE` (`MERCHANT_ORDER_NO`)
);

INSERT INTO `RED_RED_PACKET_ACCOUNT` (`RED_PACKET_ACCOUNT_ID`, `BALANCE_AMOUNT`, `USER_ID`)
VALUES (1, 950, 1000);
INSERT INTO `RED_RED_PACKET_ACCOUNT` (`RED_PACKET_ACCOUNT_ID`, `BALANCE_AMOUNT`, `USER_ID`)
VALUES (2, 500, 2000);