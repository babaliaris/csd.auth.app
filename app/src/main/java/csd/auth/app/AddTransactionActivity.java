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
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.util.Calendar;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import csd.auth.app.api.ApiErrorE;
import csd.auth.app.api.ApiResultInterface;
import csd.auth.app.api.FirebaseManager;
import csd.auth.app.api.models.ExchangeModel;

/**
 * @author Andreas Galanakis
 * @version 1.0
 *
 * This is the Add Transaction activity class.
 */

public class AddTransactionActivity extends AppCompatActivity {

    private EditText titleInput;
    private EditText amountInput;
    private EditText participant_name_Input;

    private EditText dateInput;

    private MaterialButtonToggleGroup toggleGroup;

    private FirebaseManager firebaseManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);
        CheckBox recurringCheck = findViewById(R.id.is_recurring_checkBox);


        Spinner spinner = findViewById(R.id.repeat_when_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.repeat_when_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        //Make spinner invisible when the activity is created
        spinner.setVisibility(View.GONE);
        //Change the spinner's visibility when the recurring checkbox is selected
        recurringCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                spinner.setVisibility(View.VISIBLE);
            } else {
                spinner.setVisibility(View.GONE);
                spinner.setSelection(0);
            }

        });



        CheckBox sharedCheck = findViewById(R.id.is_shared_checkBox);


        sharedCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //Make a dialog appear for selecting the user email of the shared transaction
            if (isChecked) {

                participant_name_Input.setFocusable(false);
                participant_name_Input.setClickable(true);
                participant_name_Input.setHint(R.string.select_email);
                participant_name_Input.setOnClickListener(v -> {
                    EmailSearchDialog dialog = new EmailSearchDialog();

                    dialog.setListener(email -> {

                    });

                    dialog.show(getSupportFragmentManager(), "EmailSearch");
                });

            //Return the logic to selecting the name of the service participating in the transaction
            } else {

                participant_name_Input.setFocusableInTouchMode(true);
                participant_name_Input.setFocusable(true);
                participant_name_Input.setClickable(false);
                participant_name_Input.setText("");
                participant_name_Input.setHint(R.string.name_of_service);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toggleGroup = findViewById(R.id.toggleGroup);
        titleInput = findViewById(R.id.title_editText);
        participant_name_Input = findViewById(R.id.participant_editTextText);
        TextView from_to = findViewById(R.id.participant_textView);




        //Make some ui changes based of the type of the transaction (Income - Expense)
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {

            if (!isChecked) return;

            if (checkedId == R.id.btnIncome) {
                titleInput.setHint(R.string.add_income);
                from_to.setText(R.string.from);
            } else if (checkedId == R.id.btnExpense) {
                titleInput.setHint(R.string.add_expense);
                from_to.setText(R.string.to);
            }



            //Store the type of the transaction for quicker future use
            String transactionType = "income";
            if (checkedId == R.id.btnIncome) {
                transactionType = "income";
            } else if (checkedId == R.id.btnExpense) {
                transactionType = "expense";
            }

        });


        firebaseManager = FirebaseManager.getInstance();
        amountInput = findViewById(R.id.amount_editTextNumberDecimal);


        dateInput = findViewById(R.id.editTextDate);
        dateInput.setFocusable(false);
        dateInput.setClickable(true);
        dateInput.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    AddTransactionActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        String formattedDate = String.format(
                                Locale.getDefault(),
                                "%02d-%02d-%04d",
                                selectedMonth + 1,
                                selectedDay,
                                selectedYear
                        );

                        dateInput.setText(formattedDate);
                    },
                    year,
                    month,
                    day
            );

            dialog.show();
        });

        FloatingActionButton addBtn = findViewById(R.id.add_exchange_floatingActionButton);
        addBtn.setOnClickListener(v -> createExchange());

    }


    private void createExchange()
    {


        String title = titleInput.getText().toString().trim();
        String from = participant_name_Input.getText().toString().trim();
        String dateStr = dateInput.getText().toString().trim();
        String amountStr = amountInput.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountStr);
        }
        catch (NumberFormatException e)
        {
            Toast.makeText(this,
                    R.string.invalid_amount,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //Determine INCOME / EXPENSE from toggle
        int selectedId = toggleGroup.getCheckedButtonId();

        ExchangeModel.SideE side;

        if (selectedId == R.id.btnIncome)
        {
            side = ExchangeModel.SideE.INCOME;
        }
        else
        {
            side = ExchangeModel.SideE.EXPENSE;
        }

        //Convert date string → Timestamp
        Timestamp timestamp;
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            if (date != null)
            {
                timestamp = new Timestamp(date);
            }
            else
            {
                timestamp = Timestamp.now();
            }
        }
        catch (Exception e)
        {
            timestamp = Timestamp.now();
        }

        //Read shared / recurring controls

        CheckBox sharedCheck = findViewById(R.id.is_shared_checkBox);
        CheckBox recurringCheck = findViewById(R.id.is_recurring_checkBox);
        Spinner spinner = findViewById(R.id.repeat_when_spinner);

        boolean isShared = sharedCheck.isChecked();
        boolean isRecurring = recurringCheck.isChecked();

        String recurringType = null;
        if (isRecurring)
        {
            recurringType = spinner.getSelectedItem().toString();
        }

        //Determine shared user OR service name
        String debtUserUuid = null;
        String serviceName = null;

        if (isShared)
        {
            // TODO:
            // Replace this later with selected user's UUID
            debtUserUuid = from;
        }
        else
        {
            serviceName = from;
        }

        //Build exchange model
        FirebaseUser currentUser = FirebaseAuth
                .getInstance()
                .getCurrentUser();

        if (currentUser == null)
        {
            Toast.makeText(this,
                    R.string.user_not_logged_in,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = currentUser.getUid();


        ExchangeModel exchange = new ExchangeModel(
                title,
                side,
                amount,
                timestamp,
                ownerId,
                isShared,
                debtUserUuid,
                isRecurring,
                recurringType,
                serviceName
        );

        //Send to Firebase
        firebaseManager.addExchange(exchange, new ApiResultInterface<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(AddTransactionActivity.this,
                        R.string.save_success,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(ApiErrorE error, String message) {
                Toast.makeText(AddTransactionActivity.this,
                        R.string.error + message,
                        Toast.LENGTH_LONG).show();
            }

        });
    }

    public void OpenFinancesMenu(View view) {
        Intent i = new Intent(this, PersonalFinancesActivity.class);
        startActivity(i);
    }




}