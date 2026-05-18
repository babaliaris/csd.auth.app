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

        binding = ActivityPersonalFinancesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.loadFinancialDashboardData();
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