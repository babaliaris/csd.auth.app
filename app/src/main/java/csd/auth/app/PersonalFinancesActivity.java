package csd.auth.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



/**
 * @author Andreas Galanakis
 * @version 1.0
 *
 * This is the Personal finances activity class.
 */
public class PersonalFinancesActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_finances);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });
    }
    public void OpenAddIncomeMenu(View view) {
        Intent i = new Intent(this, AddTransactionActivity.class);
        startActivity(i);
    }

    public void OpenTransactionHistoryMenu(View view) {
        Intent i = new Intent(this, TransactionHistoryActivity.class);
        startActivity(i);
    }
}