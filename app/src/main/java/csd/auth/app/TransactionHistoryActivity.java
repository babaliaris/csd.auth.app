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

        // Initialize the RecyclerView component used for
        // displaying the user's transaction history as a scrollable list
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create an adapter for managing and binding
        // exchange data into RecyclerView item views
        adapter = new ExchangeAdapter(new ArrayList<>());

        // Enable listener for expanding dialog with exchange information
        // every time a user taps on a RecyclerView item
        adapter.setOnExchangeClickListener(exchange -> {
            showExchangeDialog(exchange);
        });

        // Attach the adapter to the RecyclerView
        recyclerView.setAdapter(adapter);

        // Request instance of Firebase Manager for handling Firestore communication
        // and call the loadExchanges method for loading all exchanges from Firestore
        firebaseManager = FirebaseManager.getInstance();
        loadExchanges();

        emptyText = findViewById(R.id.emptyText);


    }

    /**
     * Load all exchanges belonging to the currently logged-in user
     * from Firestore and display them inside the RecyclerView.
     */
    private void loadExchanges() {

        // Request all the exchanges owned by the current user
        firebaseManager.getMyExchanges(new ApiResultInterface<>() {

            @Override
            public void onSuccess(List<ExchangeModel> result) {

                // Display information regarding how many exchanges were loaded successfully
                Toast.makeText(
                        TransactionHistoryActivity.this,
                        "Loaded: " + result.size(),
                        Toast.LENGTH_SHORT
                ).show();

                // Update the RecyclerView with the newly retrieved results
                adapter.updateList(result);

                // If no transaction exist display a message to the user
                // and make the RecyclerView hidden
                if (result.isEmpty())
                {
                    emptyText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                // Otherwise display the RecyclerView normally
                else
                {
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

    /**
     * Permanently delete an exchange document from Firestore.
     *
     * @param e The exchange to delete.
     */
    private void performDelete(ExchangeModel e) {

        // Access the exchanges collection, then locate and remove
        // the document using its unique Firestore ID
        FirebaseFirestore.getInstance()
                .collection("exchanges")
                .document(e.id)
                .delete()
                .addOnSuccessListener(unused -> {

                    // Inform the user that the document was deleted successfully
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();

                    // Reload the exchange list, so that
                    // the deletion is made apparent to the user
                    loadExchanges();
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(this, "Delete failed: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Display a confirmation dialog before deleting a transaction.
     *
     * @param e The exchange selected for deletion.
     */
    private void deleteExchange(ExchangeModel e) {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete transaction?")
                .setMessage("This action cannot be undone.")
                // If the user confirms deletion permanently delete the exchange
                .setPositiveButton("Delete", (dialog, which) -> {
                    performDelete(e);
                })
                // Close the dialog without performing any action
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Display detailed information about a selected transaction.
     *
     * @param e The selected exchange object.
     */
    private void showExchangeDialog(ExchangeModel e) {

        // Construct the string that contains the transaction's information
        String message = "Title: " + e.title + "\n" +
                         "Amount: " + e.value + "\n" +
                         "Date: " + e.date_time.toDate().toString() + "\n" +
                         "Shared: " + e.is_shared + "\n" +
                         "Recurring: " + e.is_recurring;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Transaction Details")
                .setMessage(message)
                // Allow direct deletion from the details dialog
                .setPositiveButton("Delete", (d, w) -> {
                    deleteExchange(e);
                })
                // Close the dialog without performing any action
                .setNegativeButton("Close", null)
                .show();
    }


    public void OpenStatisticsMenu(View view) {
        Intent i = new Intent(this, StatisticsActivity.class);
        startActivity(i);
    }

}