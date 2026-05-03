package csd.auth.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class AddIncomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_income);

        CheckBox recurringCheck = findViewById(R.id.checkBox);
        Spinner spinner = findViewById(R.id.repeat_when_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.repeat_when_array,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setVisibility(View.GONE);

        recurringCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                spinner.setVisibility(View.VISIBLE);
            } else {
                spinner.setVisibility(View.GONE);
                spinner.setSelection(0);
            }

        });

        EditText fromField = findViewById(R.id.editTextText2);
        CheckBox sharedCheck = findViewById(R.id.checkBox2);

        sharedCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                fromField.setFocusable(false);
                fromField.setClickable(true);
                fromField.setHint("Select User (Email)");

                fromField.setOnClickListener(v -> {
                    EmailSearchDialog dialog = new EmailSearchDialog();

                    dialog.setListener(email -> {

                    });

                    dialog.show(getSupportFragmentManager(), "EmailSearch");
                });

            } else {

                fromField.setFocusableInTouchMode(true);
                fromField.setFocusable(true);
                fromField.setClickable(false);
                fromField.setText("");
                fromField.setHint("Name of Person/Service");
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

    public void OpenFinancesMenu(View view) {
        Intent i = new Intent(this, PersonalFinancesActivity.class);
        startActivity(i);
    }




}