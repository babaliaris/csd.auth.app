package csd.auth.app;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.List;
import csd.auth.app.api.ApiErrorE;
import csd.auth.app.api.ApiResultInterface;
import csd.auth.app.api.FirebaseManager;
import csd.auth.app.api.models.ExchangeModel;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


/**
 * @author Andreas Galanakis
 * @version 1.0
 *
 * This is the Transaction history activity class.
 */
public class TransactionHistoryActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    ExchangeAdapter adapter;
    FirebaseManager firebaseManager;

    TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExchangeAdapter(new ArrayList<>());

        adapter.setOnExchangeClickListener(exchange -> {
            showExchangeDialog(exchange);
        });
        recyclerView.setAdapter(adapter);

        firebaseManager = FirebaseManager.getInstance();

        loadExchanges();

        emptyText = findViewById(R.id.emptyText);


    }

    private void loadExchanges() {

        firebaseManager.getMyExchanges(new ApiResultInterface<>() {

            @Override
            public void onSuccess(List<ExchangeModel> result) {

                Toast.makeText(
                        TransactionHistoryActivity.this,
                        "Loaded: " + result.size(),
                        Toast.LENGTH_SHORT
                ).show();

                adapter.updateList(result);

                if (result.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(ApiErrorE error, String message) {

                Toast.makeText(
                        TransactionHistoryActivity.this,
                        R.string.error + message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void performDelete(ExchangeModel e) {

        FirebaseFirestore.getInstance()
                .collection("exchanges")
                .document(e.id)
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();

                    // refresh list
                    loadExchanges();
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(this, "Delete failed: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteExchange(ExchangeModel e) {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete transaction?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    performDelete(e);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showExchangeDialog(ExchangeModel e) {

        String message =
                "Title: " + e.title + "\n" +
                        "Amount: " + e.value + "\n" +
                        "Date: " + e.date_time.toDate().toString() + "\n" +
                        "Shared: " + e.is_shared + "\n" +
                        "Recurring: " + e.is_recurring;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Transaction Details")
                .setMessage(message)
                .setPositiveButton("Delete", (d, w) -> {
                    deleteExchange(e);
                })
                .setNegativeButton("Close", null)
                .show();
    }


    public void OpenFinancesMenu(View view) {
        Intent i = new Intent(this, PersonalFinancesActivity.class);
        startActivity(i);
    }

}