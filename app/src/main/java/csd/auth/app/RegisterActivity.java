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
import csd.auth.app.databinding.ActivityRegisterBinding;


/**
 * @author Nikolaos Bampaliaris
 * @version 1.0
 *
 * This is the register activity class.
 */
public class RegisterActivity extends AppCompatActivity
{
    private final int global_padding = 24;
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge support.
        EdgeToEdge.enable(this);

        // Inflate the layout using View Binding.
        binding = ActivityRegisterBinding.inflate(this.getLayoutInflater());
        setContentView(binding.getRoot());

        // Add edge to edge support with custom padding.
        ViewCompat.setOnApplyWindowInsetsListener(binding.registerActivity, (v, insets) ->
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

        // Prepare the registration UI logic.
        this.setupRegisterButton();

        // Prepare the button to go to the login activity.
        this.setUpBackToLoginButton();
    }



    private void setupRegisterButton()
    {
        binding.registerRegisterBtn.setOnClickListener(v ->
        {
            // Reset errors at the start of each click.
            binding.registerEmailInput.setError(null);
            binding.registerPassInput.setError(null);
            binding.registerRepassInput.setError(null);

            String email        = Objects.requireNonNull(binding.registerEmailInput.getText()).toString().trim();
            String password     = Objects.requireNonNull(binding.registerPassInput.getText()).toString().trim();
            String rePassword   = Objects.requireNonNull(binding.registerRepassInput.getText()).toString().trim();

            // Email validation.
            if (email.isEmpty())
            {
                binding.registerEmailInput.setError(this.getString(R.string.register_email_required));
                return;
            }

            // Password validation (required).
            if (password.isEmpty())
            {
                binding.registerPassInput.setError(this.getString(R.string.register_pass_required));
                return;
            }

            // Password validation (length).
            if (password.length() < 6)
            {
                binding.registerPassInput.setError(this.getString(R.string.register_pass_min_length));
                return;
            }

            // Password validation (match).
            if (!password.equals(rePassword))
            {
                binding.registerRepassInput.setError(this.getString(R.string.register_pass_mismatch));
                return;
            }

            // Disable button to prevent multiple clicks
            binding.registerRegisterBtn.setEnabled(false);

            // Register the user (API call).
            FirebaseManager.getInstance().registerUser(email, password, new ApiResultInterface<String>()
            {
                @Override
                public void onSuccess(String uid)
                {
                    // So a messag and move on to the Login Activity.
                    Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.register_success_msg), Toast.LENGTH_SHORT).show();
                    android.content.Intent intent = new android.content.Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(ApiErrorE error, String message)
                {
                    // Re-enable button so user can try again
                    binding.registerRegisterBtn.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }



    private void setUpBackToLoginButton()
    {
        binding.registerGoToLoginBtn.setOnClickListener(v ->
        {
            // Create an Intent to go from this Activity to LoginActivity
            android.content.Intent intent = new android.content.Intent(RegisterActivity.this, LoginActivity.class);

            // Start the activity
            startActivity(intent);
            finish();
        });
    }
}