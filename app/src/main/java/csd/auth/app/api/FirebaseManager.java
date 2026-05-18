package csd.auth.app.api;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import csd.auth.app.api.models.UserModel;
import csd.auth.app.api.models.ExchangeModel;


/**
 * @author Nikolaos Bampaliaris
 * @version 1.0
 *
 * This is the firebase manager class.
 * It's a simple-to-use singleton class that provides
 * access to the Firebase Authentication and Firestore
 * services. All firebase operations have been abstracted
 * to make the code more readable and maintainable and most
 * importantly "Dead Simple To Use" across the entire project.
 */
public class FirebaseManager
{
    /**The singleton instance of this class. */
    private static FirebaseManager instance;

    /**The Firebase Authentication service. */
    private final FirebaseAuth auth;

    /**The Firebase Firestore service. */
    private final FirebaseFirestore db;

    /**The logged-in user's UUID. */
    private String user_uid;

    /**
     * The default constructor.
     */
    private FirebaseManager()
    {
        auth    = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();

        // If the app dies or somehow this singleton instances gets destroyed,
        // once the app resumes or re-opens, Google's firebase service "remembers" the
        // login session. Here we make sure to capture it to "log in back" automatically.
        if (auth.getCurrentUser() != null)
        {
            this.user_uid = auth.getCurrentUser().getUid();
        }
    }

    /**
     * @author Nikolaos Bampaliaris
     * Singleton pattern. We create only one instance of this class
     * and use it across the entire application to communicate
     * with Firebase in the easiest way possible!
     *
     * @return The singleton instance of this class.
     */
    public static synchronized FirebaseManager getInstance()
    {
        // Create the instance if it doesn't exist.
        if (instance == null)
        {
            instance = new FirebaseManager();
        }

        // Return the instance.
        return instance;
    }

    /**
     * @author Nikolaos Bampaliaris
     * Registers a new user with the given email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @param callback A callback interface to handle the async result of the operation.
     */
    public void registerUser(
            @NonNull String email,
            @NonNull String password,
            @NonNull ApiResultInterface<String> callback
    )
    {
        // Check if the email and password are not empty.
        if ( email.isEmpty() || password.isEmpty() )
        {
            callback.onFailure(
                    ApiErrorE.REQUIRED_PARAMS_MISSING,
                    "Email and password cannot be empty."
            );
            return;
        }

        // Create a new user.
        this.auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(task ->
        {
            // Success.
            if (task.isSuccessful())
            {
                // Get the UUID of the current user (who just registered).
                String uid = Objects.requireNonNull(this.auth.getCurrentUser()).getUid();

                // Create the new user object.
                UserModel new_user = new UserModel(email);

                // And a new document in the "users" collection using the user's AUTH uuid.
                this.db.collection("users").document(uid)
                .set(new_user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(uid))
                .addOnFailureListener(e -> callback.onFailure(ApiErrorE.FIREBASE_FIRESTORE_ERROR, e.getMessage()));
            }

            // Failure.
            else
            {
                callback.onFailure(
                        ApiErrorE.FIREBASE_AUTH_ERROR,
                        Objects.requireNonNull(task.getException()).getMessage()
                );
            }
        });
    }

    /**
     * @author Nikolaos Bampaliaris
     * Logs in a user with the given email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @param callback A callback interface to handle the async result of the operation.
     */
    public void loginUser(
            @NonNull String email,
            @NonNull String password,
            @NonNull ApiResultInterface<String> callback
    )
    {
        // Check if the email and password are not empty.
        if ( email.isEmpty() || password.isEmpty() )
        {
            callback.onFailure(
                    ApiErrorE.REQUIRED_PARAMS_MISSING,
                    "Email and password cannot be empty."
            );
            return;
        }

        // Attempt to log in the user.
        this.auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                // Get the logged in user's uuid.
                String uid = Objects.requireNonNull(this.auth.getCurrentUser()).getUid();

                // Store it in this instance.
                this.user_uid = uid;

                // Pass it to the caller.
                callback.onSuccess(uid);
            }

            else
            {
                callback.onFailure(
                        ApiErrorE.FIREBASE_AUTH_ERROR,
                        Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }


    /**
     * @author Nikolaos Bampaliaris
     * Logs out the current user.
     *
     * @param callback A callback interface to handle the async result of the operation.
     */
    public void logoutUser(
            @NonNull ApiResultInterface<Void> callback
    )
    {
        // Sign out the current user and set the cached user uuid to null
        // to prevent developers from accidentally using it at any
        // api call.
        this.auth.signOut();
        this.user_uid = null;

        // Pass null to the caller, so they know that the operation was finished.
        callback.onSuccess(null);
    }


    /**
     * @author Nikolaos Bampaliaris
     * Get the current user's information.
     *
     * @param callback A callback interface to handle the async result of the operation.
     */
    public void getMyProfile(
            @NonNull ApiResultInterface<UserModel> callback
    )
    {
        // Check that the user is logged in.
        if (this.user_uid == null)
        {
            callback.onFailure(
                    ApiErrorE.USER_NOT_LOGGED_IN,
                    "User is not logged in."
            );
            return;
        }

        // Get the user's document by his UUID.
        this.db.collection("users").document(this.user_uid).get()
        .addOnSuccessListener(documentSnapshot ->
        {
            UserModel user = documentSnapshot.toObject(UserModel.class);
            callback.onSuccess(user);
        })
        .addOnFailureListener(e -> callback.onFailure(ApiErrorE.FIREBASE_FIRESTORE_ERROR, e.getMessage()));
    }

    /**
     * @author Nikolaos Bampaliaris
     * Get all my exchanges.
     *
     * @param callback A callback interface to handle the async result of the operation.
     */
    public void getMyExchanges(
            @NonNull ApiResultInterface<List<ExchangeModel>> callback
    )
    {
        // Check that the user is logged in.
        if (this.user_uid == null)
        {
            callback.onFailure(
                    ApiErrorE.USER_NOT_LOGGED_IN,
                    "User is not logged in."
            );
            return;
        }

        // Get all the user's exchanges using his UUID against the "owner_user_uuid" field.
        this.db.collection("exchanges")
        .whereEqualTo("owner_user_uuid", this.user_uid)
        .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<ExchangeModel> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        ExchangeModel model = doc.toObject(ExchangeModel.class);

                        if (model != null) {
                            model.id = doc.getId(); // IMPORTANT: store Firestore document ID
                            list.add(model);
                        }
                    }

                    callback.onSuccess(list);
                })
        .addOnFailureListener(e -> callback.onFailure(ApiErrorE.FIREBASE_FIRESTORE_ERROR, e.getMessage()));
    }



    /**
     * @author Nikolaos Bampaliaris
     * Get all the exchanges I owe.
     *
     * @param callback A callback interface to handle the async result of the operation.
     */
    public void getExchangesIOwe(
            @NonNull ApiResultInterface<List<ExchangeModel>> callback
    )
    {
        // Check that the user is logged in.
        if (this.user_uid == null)
        {
            callback.onFailure(
                    ApiErrorE.USER_NOT_LOGGED_IN,
                    "User is not logged in."
            );
            return;
        }

        // Get all the exchanges the user owes using his UUID against the "debt_user_uuid" field.
        this.db.collection("exchanges")
        .whereEqualTo("debt_user_uuid", this.user_uid)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
            List<ExchangeModel> list = queryDocumentSnapshots.toObjects(ExchangeModel.class);
            callback.onSuccess(list);
        })
        .addOnFailureListener(e -> callback.onFailure(ApiErrorE.FIREBASE_FIRESTORE_ERROR, e.getMessage()));
    }


    /**
     * @author Nikolaos Bampaliaris
     * Fetches all user emails from the database, excluding the currently logged-in user.
     *
     * @param callback A callback interface to handle the async result containing a list of emails.
     */
    public void getAllOtherUserEmails(@NonNull ApiResultInterface<List<String>> callback)
    {
        // Check that the user is logged in.
        String currentUid = this.getCurrentUserId();
        if (currentUid == null)
        {
            callback.onFailure(
                ApiErrorE.USER_NOT_LOGGED_IN,
                "User is not logged in."
            );
            return;
        }

        // Query the entire 'users' collection
        this.db.collection("users")
        .get()
        .addOnSuccessListener(queryDocumentSnapshots ->
        {
            // Create a list to store the emails.
            List<String> emailList = new ArrayList<>();

            // For each document in the query result.
            for (DocumentSnapshot doc : queryDocumentSnapshots)
            {
                // Exclude the currently logged-in user by checking document ID (UID)
                if ( !doc.getId().equals(currentUid) )
                {
                    // Convert the document to a UserModel object.
                    // and add it to the list.
                    UserModel user = doc.toObject(UserModel.class);
                    if (user != null && user.email != null && !user.email.isEmpty())
                    {
                        emailList.add(user.email);
                    }

                    // Notify the developer. This should never happen in production!!!
                    assert user != null && user.email != null && !user.email.isEmpty();
                }
            }

            // Return the list of emails to the caller
            callback.onSuccess(emailList);
        })
        .addOnFailureListener(e ->
                callback.onFailure(ApiErrorE.FIREBASE_FIRESTORE_ERROR, e.getMessage())
        );
    }


    /**
     * Add a new exchange to the database.
     * @author Andreas Galanakis
     * @param exchange The exchange to add.
     * @param callback A callback interface to handle the async result of the operation.
     */
    public void addExchange(
            @NonNull ExchangeModel exchange,
            @NonNull ApiResultInterface<String> callback
    )
    {
        // Check if the user is logged in.
        if (auth.getCurrentUser() == null)
        {
            callback.onFailure(
                ApiErrorE.USER_NOT_LOGGED_IN,
                "User is not logged in."
            );
            return;
        }

        // Add the exchange to the database.
        db.collection("exchanges")
        .add(exchange)
        .addOnSuccessListener(documentReference ->
        {
            exchange.id = documentReference.getId();
            callback.onSuccess(documentReference.getId());
        })
        .addOnFailureListener(e ->
        {
            callback.onFailure(
                ApiErrorE.FIREBASE_FIRESTORE_ERROR,
                e.getMessage()
            );
        });
    }

    /**
     * Get the current user's UUID.
     * @author Andreas Galanakis
     * @return The current user's UUID.
     */
    public String getCurrentUserId()
    {
        // Check if the user is logged in.
        if (auth.getCurrentUser() == null)
        {
            return null;
        }

        // Return the user's UUID.
        return auth.getCurrentUser().getUid();
    }
}
