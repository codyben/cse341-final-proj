CREATE or replace function num_accounts (c_id in NUMBER)
RETURN number
IS c number(5,2);
BEGIN
SELECT count(acct_id) INTO c FROM holds WHERE customer_id = c_id;
RETURN(c);
END num_accounts;

create or REPLACE function do_account_action(amt in NUMBER, location in NUMBER, a_id in NUMBER)
RETURN number
IS new_bal_ret number(10,4);
PRAGMA AUTONOMOUS_TRANSACTION;
old_bal NUMBER;
new_bal NUMBER;
min_bal NUMBER;
BEGIN
SELeCT nvl(min_balance, 0) INTO min_bal FROM account NATURal LEFT JOIN checking_account WHERE acct_id = a_id;
SELECT balance into old_bal FROM account where acct_id = a_id;
INSERT INTO account_actions(amount, action_time, location_id) VALUES(amt, (SELECT TO_CHAR(CURRENT_DATE, 'DD-MON-YYYY') FROM dual),location);
commit;
--UPDATE account SET balance = balance + amount WHERE acct_id = a_id;
IF old_bal + amt < min_bal THEN 
    RETURN(-1 * old_bal);
END IF;
UPDATE account SET balance = balance + amt WHERE acct_id = a_id;
SELECT balance into new_bal FROM account where acct_id = a_id;
commit;
RETURN(new_bal);
END do_account_action;

create or replace function make_purchase_credit(amt in NUMBER, p_name in VARCHAR2, c_id in NUMBER)
RETURN number
IS new_purchase_id number(25,0);
c_lim NUMBER(10,2);
r_bal NUMBER(10,2);
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT credit_limit, running_balance into c_lim, r_bal FROM credit_card WHERE card_id = c_id;
-- error out if insufficient funds
IF r_bal + amt > c_lim THEN RETURN(-1); END IF;
UPDATE CREDIT_CARD SET RUNNING_BALANCE = r_bal + amt WHERE card_id = c_id;
INSERT INTO purchases(purchase_name, purchase_amount) VALUES(p_name, amt) RETURNING purchase_id INTO new_purchase_id;
INSERT INTO BUYS(purchase_id, card_id) VALUES(new_purchase_id, c_id);
COMMIT;
RETURN(new_purchase_id);
end make_purchase_credit;

create or replace function make_purchase_debit(amt in NUMBER, p_name in VARCHAR2, c_id IN NUMBER)
RETURN number
IS new_purchase_id number(25,0);
acct_lim NUMBER(15,2);
ac_id NUMBER;
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT balance, acct_id into acct_lim, ac_id FROM debit_card NATURAL JOIN account WHERE card_id = c_id;
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

create or replace function create_credit_card(card_num IN VARCHAR2, sec_code in NUMBER, interest IN NUMBER, r_bal in NUMBER, c_lim IN NUMBER, cust_od IN NUMBER)
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

create or replace function create_debit_card(card_num IN VARCHAR2, sec_code in NUMBER, p_code in NUMBER, ac_id IN NUMBER, cust_od IN NUMBER)
RETURN number
IS c_id NUMBER(25,0);
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
INSERT INTO card(cvc, card_number) VALUES(sec_code,card_num) RETURNING card_id INTO c_id;
INSERT INTO debit_card(card_id, pin, acct_id) VALUES(c_id, p_code, ac_id);
COMMIT;
--explicitly commit since we need this data to be present here.
INSERT INTO customer_cards(card_id, customer_id) VALUES(c_id, cust_od);
COMMIT;
RETURN(c_id);
end create_debit_card;

create or replace function create_checking_account(bal in NUMBER, intr in NUMBER, mb in NUMBER, c_id IN NUMBER)
RETURN number 
IS new_acct_id NUMBER(25,0);
c_date DATE;
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT SYSTIMESTAMP into c_date FROM dual;
INSERT INTO account(balance, interest, creation_date) VALUES(bal, intr, c_date) RETURNING acct_id INTO new_acct_id;
COMMIT;
INSERT INTO holds(customer_id, acct_id, add_date) VALUES(c_id, new_acct_id, c_date);
INSERT INTO checking_account(acct_id, min_balance) VALUES(new_acct_id, mb);
COMMIT;
RETURN(new_acct_id);
end create_checking_account;

create or replace function create_savings_account(bal in NUMBER, intr in NUMBER, c_id IN NUMBER)
RETURN number IS new_acct_id NUMBER(25,0);
c_date DATE;
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT SYSTIMESTAMP into c_date FROM dual;
INSERT INTO account(balance, interest, creation_date) VALUES(bal, intr, c_date) RETURNING acct_id INTO new_acct_id;
COMMIT;
INSERT INTO holds(customer_id, acct_id, add_date) VALUES(c_id, new_acct_id, c_date);
COMMIT;
RETURN(new_acct_id);
end create_savings_account;

create or replace function num_checking_accounts(c_id IN NUMBER)
RETURN number
IS num_chck NUMBER(25);
BEGIN
SELECT count(*) into num_chck FROM HOLDS NATURAL JOIN checking_account WHERE customer_id = 62;
RETURN(num_chck);
end num_checking_accounts;

create or replace function create_new_customer(fname in VARCHAR2, lname in VARCHAR2, d in Date, e in VARCHAR2)
RETURN number 
IS new_user_id NUMBER(25,0);
c_date DATE;
PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
SELECT SYSTIMESTAMP into c_date FROM dual; 
INSERT INTO customer(first_name, last_name, creation_date, email, DOB) VALUES(fname,lname, c_date, e, d) RETURNING customer_id INTO new_user_id;
RETURN(new_user_id);
end create_new_customer;

create or replace function update_customer(c_id in NUMBER, fname in VARCHAR2, lname in VARCHAR2, e in VARCHAR2)
RETURN number 
IS new_user_id NUMBER(25,0);
BEGIN
UPDATE customer SET email = e, first_name = fname, last_name = lname WHERE customer_id = c_id;
RETURN(c_id);
end update_customer;