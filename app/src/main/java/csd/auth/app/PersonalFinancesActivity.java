package csd.auth.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.util.List;
import java.util.Locale;

import csd.auth.app.api.ApiErrorE;
import csd.auth.app.api.ApiResultInterface;
import csd.auth.app.api.FirebaseManager;
import csd.auth.app.api.models.ExchangeModel;
import csd.auth.app.api.models.UserModel;
import csd.auth.app.databinding.ActivityPersonalFinancesBinding;
import csd.auth.app.utils.ExchangeCalculations;


/**
 * @author Andreas Galanakis
 * @version 1.0
 *
 * This is the Personal finances activity class.
 */
public class PersonalFinancesActivity extends AppCompatActivity
{
    private ActivityPersonalFinancesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inflate the layout using View Binding.
        binding = ActivityPersonalFinancesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Add edge to edge support with custom padding.
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );


            return insets;
        });

        // Call method to calculate and display statistics of the user's past exchanges
        this.loadFinancialDashboardData();

        // Gain access to user's profile information
        FirebaseManager.getInstance().getMyProfile(new ApiResultInterface<>()
        {
            @Override
            public void onSuccess(UserModel user)
            {

                // Create a username based on the user's email address
                // and display it as a welcome greeting in the TextView
                String email = user.email;
                if (email != null)
                {
                    String username = email.substring(0, email.indexOf("@"));
                    binding.textView6.setText(username);
                }

            }

            @Override
            public void onFailure(ApiErrorE error, String message)
            {
                // Assign a fallback username in the TextView
                // for being denied access to profile information
                binding.textView6.setText(R.string.user);
                Toast.makeText(
                        PersonalFinancesActivity.this,
                        "Could not load profile",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadFinancialDashboardData();
    }


    /**
     * @author Nikolaos Bampaliaris
     * Handles pulling data streams out of Firestore collections asynchronously
     * to perform local dashboard metric aggregation.
     */
    private void loadFinancialDashboardData()
    {
        FirebaseManager manager = FirebaseManager.getInstance();

        // Load Personal Exchanges (Owner scope)
        manager.getMyExchanges(new ApiResultInterface<List<ExchangeModel>>()
        {
            @Override
            public void onSuccess(List<ExchangeModel> myExchanges)
            {
                // Run localized computations using static utility logic
                double incomes   = ExchangeCalculations.calculateTotalIncomes(myExchanges);
                double expenses  = ExchangeCalculations.calculateTotalExpenses(myExchanges);
                double balance   = ExchangeCalculations.calculateNetBalance(myExchanges);
                double owedToMe  = ExchangeCalculations.calculateTotalOwedToMe(myExchanges);

                // Update UI Nodes safely
                binding.tvTotalIncomes.setText(String.format(Locale.getDefault(), "%.2f€", incomes));
                binding.tvTotalExpenses.setText(String.format(Locale.getDefault(), "%.2f€", expenses));
                binding.tvNetBalance.setText(String.format(Locale.getDefault(), "%.2f€", balance));
                binding.tvOwedToMe.setText(String.format(Locale.getDefault(), "%.2f€", owedToMe));
            }

            @Override
            public void onFailure(ApiErrorE error, String error_message)
            {
                Toast.makeText(
                        PersonalFinancesActivity.this,
                        "Error fetching personal history: " + error_message, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // Load Debt Records (Debtor scope)
        manager.getExchangesIOwe(new ApiResultInterface<List<ExchangeModel>>()
        {
            @Override
            public void onSuccess(List<ExchangeModel> exchangesIOwe)
            {
                double iOwe = ExchangeCalculations.calculateTotalIOwe(exchangesIOwe);
                binding.tvTotalIOwe.setText(String.format(Locale.getDefault(), "%.2f€", iOwe));
            }

            @Override
            public void onFailure(ApiErrorE error, String error_message)
            {
                Toast.makeText(
                        PersonalFinancesActivity.this,
                        "Error fetching debt profiles: " + error_message, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }


    public void OpenAddIncomeMenu(View view)
    {
        Intent i = new Intent(this, AddTransactionActivity.class);
        startActivity(i);
    }

    public void OpenTransactionHistoryMenu(View view)
    {
        Intent i = new Intent(this, TransactionHistoryActivity.class);
        startActivity(i);
    }
}