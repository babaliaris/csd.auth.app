package csd.auth.app.api;

/**
 * @author Nikolaos Bampaliaris
 * @version 1.0
 *
 * A generalized callback interface for Firebase operations.
 * The user (FirebaseManager methods caller) will implement
 * this as a lambda callback function while calling
 * API methods.

 * @param <T> The type of data expected on success.
 */
public interface ApiResultInterface<T>
{
    /**
     * Called when the operation is successful.
     *
     * @param data The data returned by the operation.
     */
    void onSuccess(T data);


    /**
     * Called when the operation fails.
     *
     * @param error_message The error message returned by the operation.
     */
    void onFailure(ApiErrorE error, String error_message);
}
