/******************************************************************************
 * Copyright (c) 2005 Actuate Corporation.
 * All rights reserved. This file and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial implementation
 *
 * Classic Models Inc. sample database developed as part of the
 * Eclipse BIRT Project. For more information, see http:\\www.eclipse.org\birt
 *
 *******************************************************************************/

/* Recommended DATABASE name is classicmodels. */

/* CREATE DATABASE classicmodels; */
/* USE classicmodels; */

/* DROP the existing tables. Comment this out if it is not needed. */

DROP TABLE PAYMENT
IF EXISTS;
DROP TABLE ORDER_DETAIL
IF EXISTS;
DROP TABLE "ORDER"
IF EXISTS;
DROP TABLE PRODUCT
IF EXISTS;
DROP TABLE CUSTOMER
IF EXISTS;
DROP TABLE EMPLOYEE
IF EXISTS;
DROP TABLE PERSON
IF EXISTS;
DROP TABLE OFFICE
IF EXISTS;

/* Create the full set of Classic Models Tables */

CREATE TABLE OFFICE (
  OFFICE_CODE   VARCHAR(50) NOT NULL,
  CITY          VARCHAR(50) NOT NULL,
  PHONE         VARCHAR(50) NOT NULL,
  ADDRESS_LINE1 VARCHAR(50) NOT NULL,
  ADDRESS_LINE2 VARCHAR(50) NULL,
  STATE         VARCHAR(50) NULL,
  COUNTRY       VARCHAR(50) NOT NULL,
  POSTAL_CODE   VARCHAR(10) NOT NULL,
  TERRITORY     VARCHAR(10) NOT NULL,
  CONSTRAINT OFFICE_PK PRIMARY KEY (OFFICE_CODE)
);


CREATE TABLE PERSON (
  PERSON_NUMBER INTEGER,
  FIRST_NAME    VARCHAR(50) NOT NULL,
  LAST_NAME     VARCHAR(50) NOT NULL,
  CONSTRAINT PERSON_PK PRIMARY KEY (PERSON_NUMBER)
);

CREATE TABLE EMPLOYEE (
  EMPLOYEE_NUMBER INTEGER      NOT NULL,
  PERSON_NUMBER   INTEGER,
  EXTENSION       VARCHAR(10)  NOT NULL,
  EMAIL           VARCHAR(100) NOT NULL,
  OFFICE_CODE     VARCHAR(20)  NOT NULL,
  REPORTS_TO      INTEGER      NULL,
  JOB_TITLE       VARCHAR(50)  NOT NULL,
  CONSTRAINT EMPLOYEE_PK PRIMARY KEY (EMPLOYEE_NUMBER),
  CONSTRAINT EMPLOYEE_PERSON_NUMBER_FK FOREIGN KEY (PERSON_NUMBER) REFERENCES PERSON (PERSON_NUMBER),
  CONSTRAINT EMPLOYEE_REPORTS_TO_FK FOREIGN KEY (REPORTS_TO) REFERENCES EMPLOYEE (EMPLOYEE_NUMBER),
  CONSTRAINT EMPLOYEE_OFFICE_CODE_FK FOREIGN KEY (OFFICE_CODE) REFERENCES OFFICE (OFFICE_CODE)
);

CREATE TABLE CUSTOMER (
  CUSTOMER_NUMBER           INTEGER     NOT NULL,
  PERSON_NUMBER             INTEGER,
  PHONE                     VARCHAR(50) NOT NULL,
  ADDRESS_LINE_1            VARCHAR(50) NOT NULL,
  ADDRESS_LINE_2            VARCHAR(50) NULL,
  CITY                      VARCHAR(50) NOT NULL,
  STATE                     VARCHAR(50) NULL,
  POSTAL_CODE               VARCHAR(15) NULL,
  COUNTRY                   VARCHAR(50) NOT NULL,
  SALES_REP_EMPLOYEE_NUMBER INTEGER     NULL,
  CREDITLIMIT               DOUBLE      NULL,
  CONSTRAINT CUSTOMER_PK PRIMARY KEY (CUSTOMER_NUMBER),
  CONSTRAINT CUSTOMER_PERSON_NUMBER_FK FOREIGN KEY (PERSON_NUMBER) REFERENCES PERSON (PERSON_NUMBER),
  CONSTRAINT CUSTOMER_SALES_REP_EMPLOYEE_NUMBER_FK FOREIGN KEY (SALES_REP_EMPLOYEE_NUMBER) REFERENCES EMPLOYEE (EMPLOYEE_NUMBER)
);

CREATE TABLE PRODUCT (
  PRODUCT_CODE        VARCHAR(50) NOT NULL,
  PRODUCT_NAME        VARCHAR(70) NOT NULL,
  PRODUCT_LINE        VARCHAR(50) NOT NULL,
  PRODUCT_SCALE       VARCHAR(10) NOT NULL,
  PRODUCT_VENDOR      VARCHAR(50) NOT NULL,
  PRODUCT_DESCRIPTION CLOB        NOT NULL,
  QUANTITY_IN_STOCK   SMALLINT    NOT NULL,
  BUY_PRICE           DOUBLE      NOT NULL,
  MSRP                DOUBLE      NOT NULL,
  PRIMARY KEY (PRODUCT_CODE)
);

CREATE TABLE "ORDER" (
  ORDER_NUMBER    INTEGER     NOT NULL,
  ORDER_DATE      DATETIME    NOT NULL,
  REQUIRED_DATE   DATETIME    NOT NULL,
  SHIPPED_DATE    DATETIME    NULL,
  STATUS          VARCHAR(15) NOT NULL,
  COMMENTS        CLOB        NULL,
  CUSTOMER_NUMBER INTEGER     NOT NULL,
  CONSTRAINT ORDER_PK PRIMARY KEY (ORDER_NUMBER),
  CONSTRAINT ORDER_CUSTOMER_NUMBER_FK FOREIGN KEY (CUSTOMER_NUMBER) REFERENCES CUSTOMER (CUSTOMER_NUMBER)
);

CREATE TABLE ORDER_DETAIL (
  ORDER_NUMBER      INTEGER     NOT NULL,
  PRODUCT_CODE      VARCHAR(50) NOT NULL,
  QUANTITY_ORDERED  INTEGER     NOT NULL,
  PRICE_EACH        DOUBLE      NOT NULL,
  ORDER_LINE_NUMBER SMALLINT    NOT NULL,
  CONSTRAINT ORDER_DETAIL_PK PRIMARY KEY (ORDER_NUMBER, PRODUCT_CODE),
  CONSTRAINT ORDER_DETAIL_ORDER_NUMBER_FK FOREIGN KEY (ORDER_NUMBER) REFERENCES "ORDER" (ORDER_NUMBER),
  CONSTRAINT ORDER_DETAIL_PRODUCT_CODE_FK FOREIGN KEY (PRODUCT_CODE) REFERENCES PRODUCT (PRODUCT_CODE)
);

CREATE TABLE PAYMENT (
  CUSTOMER_NUMBER INTEGER     NOT NULL,
  CHECK_NUMBER    VARCHAR(50) NOT NULL,
  PAYMENT_DATE    DATETIME    NOT NULL,
  AMOUNT          DOUBLE      NOT NULL,
  CONSTRAINT PAYMENT_PK PRIMARY KEY (CUSTOMER_NUMBER, CHECK_NUMBER),
  CONSTRAINT PAYMENT_CUSTOMER_NUMBER_FK FOREIGN KEY (CUSTOMER_NUMBER) REFERENCES CUSTOMER (CUSTOMER_NUMBER)
);
