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

create table account_actions 
(
    action_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
    amount NUMBER(15) not null,
    action_time date,
    constraint acct_actions_pk PRIMARY KEY (action_id)
);

create table performs
(
    customer_id NUMBER(10) not null REFERENCES customer(customer_id),
    action_id NUMBER(10) not null REFERENCES account_actions(action_id),
    constraint acct_performs_pk PRIMARY KEY (action_id)
    
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
    loan_id NUMBER(10) REFERENCES loan(loan_id),
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

    