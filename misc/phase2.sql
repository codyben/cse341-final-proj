drop table branch;
drop table performs;
drop table branch;
drop table atm;
drop table atm_locations;
drop table account_actions;
drop table performs;
drop table checking_account;
drop table account;
drop table secured_loan;
drop table collateral;
drop table credit_card;
drop table debit_card;
drop table card;
drop table purchases;
drop table buys;
drop table holds;
drop table loan_action;
drop table loan_payment;
drop table loan;
drop table location;
drop table customer;

create table customer
(
    customer_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    DOB date not null,
    constraint customer_pk PRIMARY KEY (customer_id)
);

create table location
(
    location_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    address varchar(50) not null,
    constraint location_pk PRIMARY KEY (location_id)
);

create table branch 
(
    branch_id NUMBER(10) not null REFERENCES location(location_id),
    hours_of_operation varchar(1) not null,
    constraint branch_pk PRIMARY KEY (branch_id)
);

create table atm
(
    operator_name varchar(50),
    hours_of_operation varchar(1) not null,
    atm_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    constraint atm_pk PRIMARY KEY (atm_id)
);

create table atm_locations
(
    location_id NUMBER(10) not null REFERENCES location(location_id),
    atm_id NUMBER(10) not null REFERENCES atm(atm_id),
    constraint atm_locations_pk PRIMARY KEY (atm_id, location_id) 
);


create table account
(
    acct_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    balance NUMBER(15) DEFAULT 0 not null,
    interest DECIMAL(5,5) not null,
    creation_date date,
    constraint acc_pk PRIMARY KEY (acct_id)
);


create table checking_account 
(
     acct_id NUMBER(10) not null REFERENCES account(acct_id),
     min_balance NUMBER(2) default 5 not null,
     constraint ck_acct_pk PRIMARY KEY (acct_id)
);

create table loan
(
    
    loan_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    interest DECIMAL(6,5) not null,
    payment DECIMAL(8,8) not null,
    amount number(15) not null,
    constraint loan_pk PRIMARY KEY (loan_id)
);

create table collateral
(
    collateral_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    value DECIMAL(8,8) not null,
    constraint collateral_pk PRIMARY KEY (collateral_id)
);

create table secured_loan
(
    loan_id NUMBER(10) REFERENCES loan(loan_id) not null,
    collateral_id NUMBER(10) REFERENCES collateral(collateral_id),
    constraint secured_loan_pk PRIMARY KEY (loan_id)
);

create table card
(
    card_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    cvc NUMBER(3) not null,
    card_number NUMBER(16) not null,
    constraint card_pk PRIMARY KEY (card_id)
);

create table purchases
(
    purchase_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    purchase_name varchar(50) not null,
    purchase_time date,
    purchase_amount number(6) not null,
    constraint purchases_pk PRIMARY KEY (purchase_id)
);

create table buys 
(
    purchase_id NUMBER(10) REFERENCES purchases(purchase_id),
    card_id NUMBER(10) REFERENCES card(card_id),
    constraint buys_pk PRIMARY KEY (purchase_id)
);

create table credit_card
(
    card_id NUMBER(10) REFERENCES card(card_id) not null,
    interest DECIMAL(6,5) not null,
    balance_due AS (running_balance * (1+interest)),
    running_balance FLOAT(63) default 0 not null,
    constraint credit_pk PRIMARY KEY (card_id)
);

create table debit_card
(
    card_id NUMBER(10) REFERENCES card(card_id),
    pin NUMBER(5) default 12345 not null,
    acct_id NUMBER(10) REFERENCES account(acct_id) not null,
    constraint debit_pk PRIMARY KEY (card_id)
);


create table holds 
(
    customer_id NUMBER(10) REFERENCES customer(customer_id),
    acct_id NUMBER(10) REFERENCES account(acct_id),
    add_date date,
    constraint holds_pk PRIMARY KEY (customer_id, acct_id)
);

create table loan_action 
(
    payment_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    amount number(10) not null,
    time date,
    loan_id REFERENCES loan(loan_id),
    location_id REFERENCES location(location_id),
    constraint loan_action_pk PRIMARY KEY(payment_id)
);

create table loan_payment 
(
    customer_id REFERENCES customer(customer_id),
    payment_id REFERENCES loan_action(payment_id),
    constraint loan_payment_pk PRIMARY KEY (payment_id)
);

create table account_actions 
(
    action_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    amount NUMBER(15) not null,
    action_time date,
    location_id NUMBER(10) REFERENCES location(location_id),
    constraint acct_actions_pk PRIMARY KEY (action_id)
);
create table performs
(
    customer_id NUMBER(10) not null REFERENCES customer(customer_id),
    action_id NUMBER(10) not null REFERENCES account_actions(action_id),
    acct_id NUMBER(10) not null REFERENCES account(acct_id),
    constraint acct_performs_pk PRIMARY KEY (action_id)
    
);
ALTER TABLE location drop column address;
ALTER TABLE location add ( address VARCHAR2(300) GENERATED ALWAYS as ( to_char(street_num) || ' ' || street || ' ' || city || ' ' || state || ' ' || to_char(zip)) VIRTUAL);

CREATE TABLE customer_cards
(
    card_id NUMBER(10) not null REFERENCES card(card_id),
    customer_id NUMBER(10) not null REFERENCES customer(customer_id),
    constraint customer_cards_pk PRIMARY KEY (card_id, customer_id)
);

create table card_account
(
    acct_id NUMBER REFERENCES checking_account(acct_id),
    card_id NUMBER REFERENCES debit_card(card_id),
    add_date date,
    constraint card_acc_pk PRIMARY KEY (acct_id, card_id)
);


create table card_actions 
(
    action_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    amount NUMBER(15) not null,
    action_time date,
    constraint card_actions_pk PRIMARY KEY (action_id)
);

create table loan_payment_location
(
    payment_id NUMBER not null REFERENCES loan_action(payment_id),
    location_id NUMBER not null REFERENCES branch(branch_id),
    CONSTRAINT loan_payment_location_pk PRIMARY KEY(payment_id)
);

create table card_payments
(
    action_id REFERENCES CARD_ACTIONS(action_id),
    card_id REFERENCES CREDIT_CARD(card_id),
    constraint card_payments_pk PRIMARY KEY (action_id)
);

create table account_actions_location
(
    action_id REFERENCES ACCOUNT_ACTIONS(action_id),
    location_if REFERENCES location(location_id),
    constraint account_actions_location_pk PRIMARY KEY (action_id)
);

CREATE TABLE customer_loans
(
    loan_id NUMBER(10) not null REFERENCES loan(loan_id),
    customer_id NUMBER(10) not null REFERENCES customer(customer_id),
    constraint customer_loans_pk PRIMARY KEY (loan_id, customer_id)
);

CREATE TABLE loan_collateral
(
    loan_id NUMBER(10) not null REFERENCES secured_loan(loan_id),
    collateral_id NUMBER(10) not null REFERENCES collateral(collateral_id),
    constraint loan_collateral_pk PRIMARY KEY (loan_id, collateral_id)
);