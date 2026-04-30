package csd.auth.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

import csd.auth.app.api.ApiErrorE;
import csd.auth.app.api.ApiResultInterface;
import csd.auth.app.api.FirebaseManager;
import csd.auth.app.databinding.ActivityLoginBinding;
import csd.auth.app.databinding.ActivityRegisterBinding;


/**
 * @author Nikolaos Bampaliaris
 * @version 1.0
 *
 * This is the login activity class.
 */
public class LoginActivity extends AppCompatActivity
{
    private final int global_padding = 24;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        // Inflate the layout using View Binding.
        binding = ActivityLoginBinding.inflate(this.getLayoutInflater());
        setContentView(binding.getRoot());

        // Add edge to edge support with custom padding.
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginActivity, (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Convert to actual pixels
            int customPadding = (int) (this.global_padding * getResources().getDisplayMetrics().density);

            // Add padding to all sides of the view without
            // losing the benefits of the edge-to-edge effect.
            v.setPadding(
                    systemBars.left + customPadding,
                    systemBars.top + customPadding,
                    systemBars.right + customPadding,
                    systemBars.bottom + customPadding
            );

            return insets;
        });

        // Set up the login button logic.
        this.setupLoginButton();

        // Set up the registration button logic.
        this.setupOpenRegistrationFormButton();
    }


    private void setupLoginButton()
    {
        binding.loginLoginBtn.setOnClickListener(v ->
        {
            // Reset errors at the start of each click.
            binding.loginEmailInput.setError(null);
            binding.loginPassInput.setError(null);

            String email        = Objects.requireNonNull(binding.loginEmailInput.getText()).toString().trim();
            String password     = Objects.requireNonNull(binding.loginPassInput.getText()).toString().trim();

            // Email validation.
            if (email.isEmpty())
            {
                binding.loginEmailInput.setError(this.getString(R.string.login_email_required));
                return;
            }

            // Password validation (required).
            if (password.isEmpty())
            {
                binding.loginPassInput.setError(this.getString(R.string.login_pass_required));
                return;
            }

            // Disable button to prevent multiple clicks
            binding.loginLoginBtn.setEnabled(false);

            // Register the user (API call).
            FirebaseManager.getInstance().loginUser(email, password, new ApiResultInterface<String>()
            {
                @Override
                public void onSuccess(String uid)
                {
                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.login_success_msg), Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to HOME PAGE.
                }

                @Override
                public void onFailure(ApiErrorE error, String message)
                {
                    // Re-enable button so user can try again
                    binding.loginLoginBtn.setEnabled(true);
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }


    private void setupOpenRegistrationFormButton()
    {
        binding.loginGoToRegisterBtn.setOnClickListener(v->
        {
            // Create an Intent to go from this Activity to RegisterActivity
            android.content.Intent intent = new android.content.Intent(LoginActivity.this, RegisterActivity.class);

            // Start the activity
            startActivity(intent);
            finish();
        });
    }
}