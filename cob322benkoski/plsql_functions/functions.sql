--------------------------------------------------------
--  File created - Thursday-April-23-2020   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Function CREATE_CHECKING_ACCOUNT
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."CREATE_CHECKING_ACCOUNT" (bal in NUMBER, intr in NUMBER, mb in NUMBER, c_id IN NUMBER)
RETURN number 
IS new_acct_id NUMBER(25,0);
c_date DATE;
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT SYSTIMESTAMP into c_date FROM dual;
INSERT INTO account(balance, interest, creation_date) VALUES(bal, intr, c_date) RETURNING acct_id INTO new_acct_id;
COMMIT;
INSERT INTO customer_accounts(customer_id, acct_id, add_date) VALUES(c_id, new_acct_id, c_date);
INSERT INTO checking_account(acct_id, min_balance) VALUES(new_acct_id, mb);
COMMIT;
RETURN(new_acct_id);
end create_checking_account;

/
--------------------------------------------------------
--  DDL for Function CREATE_CREDIT_CARD
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."CREATE_CREDIT_CARD" (card_num IN VARCHAR2, sec_code in NUMBER, interest IN NUMBER, r_bal in NUMBER, c_lim IN NUMBER, cust_od IN NUMBER)
RETURN number
IS c_id NUMBER(25,0);
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
INSERT INTO card(cvc, card_number) VALUES(sec_code,card_num) RETURNING card_id INTO c_id;
INSERT INTO credit_card(card_id, interest, running_balance, credit_limit) VALUES(c_id, interest, r_bal, c_lim);
COMMIT;
INSERT INTO customer_cards(card_id, customer_id) VALUES(c_id, cust_od);
COMMIT;
RETURN(c_id);
end create_credit_card;

/
--------------------------------------------------------
--  DDL for Function CREATE_DEBIT_CARD
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."CREATE_DEBIT_CARD" (card_num IN VARCHAR2, sec_code in NUMBER, p_code in NUMBER, ac_id IN NUMBER, cust_od IN NUMBER)
RETURN number
IS c_id NUMBER(25,0);
c_date DATE;
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT SYSTIMESTAMP into c_date FROM dual;
INSERT INTO card(cvc, card_number) VALUES(sec_code,card_num) RETURNING card_id INTO c_id;
INSERT INTO debit_card(card_id, pin) VALUES(c_id, p_code);
INSERT INTO card_account(acct_id, card_id, add_date) VALUES(ac_id, c_id, c_date);
COMMIT;
--explicitly commit since we need this data to be present here.
INSERT INTO customer_cards(card_id, customer_id) VALUES(c_id, cust_od);
COMMIT;
RETURN(c_id);
end create_debit_card;

/
--------------------------------------------------------
--  DDL for Function CREATE_NEW_CUSTOMER
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."CREATE_NEW_CUSTOMER" (fname in VARCHAR2, lname in VARCHAR2, d in Date, e in VARCHAR2, a in VARCHAR2)
RETURN number 
IS new_user_id NUMBER(25,0);
c_date DATE;
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT SYSTIMESTAMP into c_date FROM dual; 
INSERT INTO customer(first_name, last_name, creation_date, email, DOB, "address") VALUES(fname,lname, c_date, e, d, a) RETURNING customer_id INTO new_user_id;
COMMIT;
RETURN(new_user_id);
end create_new_customer;

/
--------------------------------------------------------
--  DDL for Function CREATE_SAVINGS_ACCOUNT
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."CREATE_SAVINGS_ACCOUNT" (bal in NUMBER, intr in NUMBER, c_id IN NUMBER)
RETURN number IS new_acct_id NUMBER(25,0);
c_date DATE;
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT SYSTIMESTAMP into c_date FROM dual;
INSERT INTO account(balance, interest, creation_date) VALUES(bal, intr, c_date) RETURNING acct_id INTO new_acct_id;
COMMIT;
INSERT INTO customer_accounts(customer_id, acct_id, add_date) VALUES(c_id, new_acct_id, c_date);
COMMIT;
RETURN(new_acct_id);
end create_savings_account;

/
--------------------------------------------------------
--  DDL for Function DO_ACCOUNT_ACTION
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."DO_ACCOUNT_ACTION" (amt in NUMBER, loc in NUMBER, a_id in NUMBER, c_id in NUMBER)
RETURN number
IS new_bal_ret number(17,3);
PRAGMA AUTONOMOUS_TRANSACTION;
old_bal NUMBER;
new_bal NUMBER;
min_bal NUMBER;
ac_id NUMBER;
BEGIN
SELeCT nvl(min_balance, 0) INTO min_bal FROM account NATURal LEFT JOIN checking_account WHERE acct_id = a_id;
SELECT balance into old_bal FROM account where acct_id = a_id;
INSERT INTO account_actions(amount, action_time) VALUES(amt, (SELECT TO_CHAR(CURRENT_DATE, 'DD-MON-YYYY') FROM dual)) RETURNING action_id into ac_id;
INSERT INTO account_actions_location(action_id, location_id) VALUES(ac_id, loc);
commit;
--UPDATE account SET balance = balance + amount WHERE acct_id = a_id;
IF old_bal + amt < min_bal THEN 
    RETURN(-1 * old_bal);
END IF;
INSERT INTO account_performs(customer_id, action_id, acct_id) VALUES(c_id, ac_id, a_id);
UPDATE account SET balance = balance + amt WHERE acct_id = a_id;
SELECT balance into new_bal FROM account where acct_id = a_id;
commit;
RETURN(new_bal);
END do_account_action;

/
--------------------------------------------------------
--  DDL for Function MAKE_PURCHASE_CREDIT
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."MAKE_PURCHASE_CREDIT" (amt in NUMBER, p_name in VARCHAR2, c_id in NUMBER)
RETURN number
IS new_purchase_id number(25,0);
-- make these intentionally larger than the needed amount so no weird errors happen.
c_lim NUMBER(16,2);
r_bal NUMBER(16,2);
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT credit_limit, running_balance into c_lim, r_bal FROM credit_card WHERE card_id = c_id;
-- error out if insufficient funds
IF r_bal + amt > c_lim THEN RETURN(-1); END IF;
UPDATE CREDIT_CARD SET RUNNING_BALANCE = r_bal + amt WHERE card_id = c_id;
INSERT INTO purchases(purchase_name, purchase_amount, purchase_time) VALUES(p_name, amt, (SELECT SYSDATE from dual)) RETURNING purchase_id INTO new_purchase_id;
INSERT INTO BUYS(purchase_id, card_id) VALUES(new_purchase_id, c_id);
COMMIT;
RETURN(new_purchase_id);
end make_purchase_credit;

/
--------------------------------------------------------
--  DDL for Function MAKE_PURCHASE_DEBIT
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."MAKE_PURCHASE_DEBIT" (amt in NUMBER, p_name in VARCHAR2, c_id IN NUMBER)
RETURN number
IS new_purchase_id number(25,0);
acct_lim NUMBER(16,2);
ac_id NUMBER;
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT balance, acct_id into acct_lim, ac_id FROM debit_card NATURAL JOIN account NATURAL JOIN card_account WHERE card_id = c_id;
--DBMS_OUTPUT.put_line(acct_lim);
--DBMS_OUTPUT.put_line(amt);
IF amt > acct_lim THEN RETURN(-1); END IF;

UPDATE account SET balance = balance - amt WHERE acct_id = ac_id;
commit;
INSERT INTO purchases(purchase_name, purchase_amount) VALUES(p_name, amt) RETURNING purchase_id INTO new_purchase_id;
INSERT INTO BUYS(purchase_id, card_id) VALUES(new_purchase_id, c_id);
COMMIT;
RETURN(new_purchase_id);
end make_purchase_debit;

/
--------------------------------------------------------
--  DDL for Function NUM_ACCOUNTS
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."NUM_ACCOUNTS" (c_id in NUMBER)
RETURN number
IS c NUMBER;
BEGIN
SELECT count(acct_id) INTO c FROM customer_accounts WHERE customer_id = c_id;
RETURN(c);
END num_accounts;

/
--------------------------------------------------------
--  DDL for Function NUM_CARDS
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."NUM_CARDS" (c_id in NUMBER)
RETURN number
IS c NUMBER;
BEGIN
SELECT count(customer_id) INTO c FROM customer_cards WHERE customer_id = c_id;
RETURN(c);
END num_cards;

/
--------------------------------------------------------
--  DDL for Function NUM_CHECKING_ACCOUNTS
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."NUM_CHECKING_ACCOUNTS" (c_id IN NUMBER)
RETURN number
IS num_chck NUMBER(25);
BEGIN
SELECT count(*) into num_chck FROM customer_accounts NATURAL JOIN checking_account WHERE customer_id = c_id;
RETURN(num_chck);
end num_checking_accounts;

/
--------------------------------------------------------
--  DDL for Function NUM_CREDIT
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."NUM_CREDIT" (c_id in NUMBER)
RETURN number
IS c number(5);
BEGIN
SELECT count(customer_id) INTO c FROM customer_cards NATURAL JOIN credit_card WHERE customer_id = c_id;
RETURN(c);
END num_credit;

/
--------------------------------------------------------
--  DDL for Function NUM_DEBIT
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."NUM_DEBIT" (c_id in NUMBER)
RETURN number
IS c number(5);
BEGIN
SELECT count(customer_id) INTO c FROM customer_cards NATURAL JOIN debit_card WHERE customer_id = c_id;
RETURN(c);
END num_debit;

/
--------------------------------------------------------
--  DDL for Function NUM_LOANS
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."NUM_LOANS" (c_id in NUMBER)
RETURN number
IS c NUMBER;
BEGIN
SELECT count(customer_id) INTO c FROM loan NATURAL JOIN customer_loans WHERE customer_id = c_id;
RETURN(c);
END num_loans;

/
--------------------------------------------------------
--  DDL for Function REPLACE_CREDIT_CARD
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."REPLACE_CREDIT_CARD" (card_num IN NUMBER, sec_code in NUMBER, card_i in NUMBER )
RETURN VARCHAR2
IS c_id VARCHAR2(40);
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
UPDATE CARD SET card_number = card_num,  cvc = sec_code WHERE card_id = card_i RETURNING card_number INTO c_id;
COMMIT;
RETURN(c_id);
end replace_credit_card;

/
--------------------------------------------------------
--  DDL for Function REPLACE_DEBIT_CARD
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."REPLACE_DEBIT_CARD" (card_num IN NUMBER, sec_code in NUMBER, n_pin in NUMBER, card_i in NUMBER )
RETURN VARCHAR2
IS c_id VARCHAR2(40);
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
UPDATE CARD SET card_number = card_num,  cvc = sec_code WHERE card_id = card_i RETURNING card_number INTO c_id;
UPDATE debit_card SET pin = n_pin WHERE card_id = card_i;
COMMIT;
RETURN(c_id);
end replace_debit_card;

/
--------------------------------------------------------
--  DDL for Function UPDATE_CUSTOMER
--------------------------------------------------------

  CREATE OR REPLACE EDITIONABLE FUNCTION "COB322"."UPDATE_CUSTOMER" (c_id in NUMBER, fname in VARCHAR2, lname in VARCHAR2, e in VARCHAR2, a in VARCHAR2)
RETURN number 
IS new_user_id NUMBER(25,0);
--https://stackoverflow.com/questions/8729236/solution-to-cannot-perform-a-dml-operation-inside-a-query
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
UPDATE customer SET email = e, first_name = fname, last_name = lname, "address" = a WHERE customer_id = c_id;
COMMIT;
RETURN(c_id);
end update_customer;

/
