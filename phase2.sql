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

    