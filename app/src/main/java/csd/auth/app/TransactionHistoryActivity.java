package csd.auth.app;


import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.List;
import csd.auth.app.api.ApiErrorE;
import csd.auth.app.api.ApiResultInterface;
import csd.auth.app.api.FirebaseManager;
import csd.auth.app.api.models.ExchangeModel;
import androidx.recyclerview.widget.RecyclerView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExchangeAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        firebaseManager = FirebaseManager.getInstance();

        loadExchanges();
    }

    private void loadExchanges() {

        firebaseManager.getMyExchanges(new ApiResultInterface<>() {

            @Override
            public void onSuccess(List<ExchangeModel> result) {

                Toast.makeText(
                        TransactionHistoryActivity.this,
                        R.string.loaded + result.size() + R.string.s_exchanges,
                        Toast.LENGTH_LONG
                ).show();

                adapter.updateList(result);
            }

            @Override
            public void onFailure(ApiErrorE error, String message) {

                Toast.makeText(
                        TransactionHistoryActivity.this,
                        R.string.error + message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

}