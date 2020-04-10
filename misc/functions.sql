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