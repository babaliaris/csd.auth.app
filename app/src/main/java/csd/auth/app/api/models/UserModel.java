package csd.auth.app.api.models;
import androidx.annotation.NonNull;

import com.google.firebase.firestore.PropertyName;

/**
 * @author Nikolaos Bampaliaris
 * @version 1.0
 *
 * This is the User model class.
 */
public class UserModel
{
    /**The user's email address.*/
    @PropertyName("email")
    public String email;

    /**
     * Required default constructor for Firestore.
     */
    public UserModel() {}

    /**
     * Constructor for a new user.
     *
     * @param email The user's email address.
     */
    public UserModel(
            @NonNull String email
    )
    {
        this.email = email;
    }
}
