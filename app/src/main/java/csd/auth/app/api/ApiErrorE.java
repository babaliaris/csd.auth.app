package csd.auth.app.api;

/**
 * @author Nikolaos Bampaliaris
 * @version 1.0
 *
 * This is the ApiErrorE enum.
 * It's a simple-to-use enumeration class that
 * represents the different types of errors that
 * can occur during Firebase operations.
 * The api caller can switch the error argument
 * provided in the onFailure callback to one of the
 * errors below to identify which error occurred.
 *
 */
public enum ApiErrorE
{
    REQUIRED_PARAMS_MISSING,
    FIREBASE_AUTH_ERROR, FIREBASE_FIRESTORE_ERROR,
    USER_NOT_LOGGED_IN
}
