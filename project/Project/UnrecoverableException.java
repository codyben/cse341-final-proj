/** 
 * we assume that if a function is executing a "safe" query 
 * i.e, no user input, and it fails, that it is unrecoverable.
 * Why? This may be due to a connection timeout and at that point, I consider
 * the program to be in an unstable state. At that point, the safest thing to do 
 * is kill the session and alert the user.
 */

public class UnrecoverableException extends Exception {}