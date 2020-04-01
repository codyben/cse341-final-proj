CREATE or replace function num_accounts (c_id in NUMBER)
RETURN number
IS c number(5,2);
BEGIN
SELECT count(acct_id) INTO c FROM holds WHERE customer_id = c_id;
RETURN(c);
END num_accounts;