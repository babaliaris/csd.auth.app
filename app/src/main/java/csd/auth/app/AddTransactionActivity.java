package csd.auth.app;

import android.app.TimePickerDialog;
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


import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

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
    private MaterialAutoCompleteTextView participant_name_Input;
    private EditText dateInput;
    private Timestamp selectedTimestamp;
    private MaterialButtonToggleGroup toggleGroup;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);

        // Get a reference to the layout's input fields for allowing the activity to interact with those
        toggleGroup = findViewById(R.id.toggleGroup);
        titleInput = findViewById(R.id.title_editText);
        participant_name_Input = findViewById(R.id.participant_editTextText);
        participant_name_Input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        participant_name_Input.setFocusable(true);
        participant_name_Input.setFocusableInTouchMode(true);
        firebaseManager = FirebaseManager.getInstance();
        amountInput = findViewById(R.id.amount_editTextNumberDecimal);


        CheckBox recurringCheck = findViewById(R.id.is_recurring_checkBox);
        Spinner spinner = findViewById(R.id.repeat_when_spinner);

        // Create spinner for the option to decide one's recurring exchange's repeat rate
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.repeat_when_array, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Make spinner invisible when the activity is created
        spinner.setVisibility(View.GONE);

        // Change the spinner's visibility when the recurring checkbox is selected (or unselected)
        recurringCheck.setOnCheckedChangeListener((buttonView, isChecked) ->
        {

            if (isChecked)
            {
                spinner.setVisibility(View.VISIBLE);
            }
            else
            {
                spinner.setVisibility(View.GONE);

                // Disable normal keyboard text input
                spinner.setSelection(0);
            }

        });




        CheckBox sharedCheck = findViewById(R.id.is_shared_checkBox);

        // Revise input method of the participant's name that takes part in the exchange
        // when the shared checkbox is selected (or unselected)
        sharedCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {

            // When shared mode is ON, let user freely select the participant's name
            // from a dropdown list with emails of all currently registered users in the app
            if (isChecked)
            {
                // Call method that returns a list with all the registered user email addresses
                FirebaseManager.getInstance().getAllOtherUserEmails(new ApiResultInterface<>() {
                    @Override
                    public void onSuccess(List<String> emails) {

                        // Create adapter for converting the email list into visual dropdown items
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                AddTransactionActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                emails
                        );

                        // Connect the adapter to the dropdown
                        participant_name_Input.setAdapter(adapter);

                        // Open the dropdown when the user taps into the input field
                        participant_name_Input.setOnClickListener(v -> {
                            participant_name_Input.showDropDown();
                        });

                        // Automatically display the dropdown suggestions
                        // when the input field gains focus
                        participant_name_Input.setOnFocusChangeListener((v, hasFocus) -> {
                            if (hasFocus)
                            {
                                participant_name_Input.showDropDown();
                            }
                        });
                    }

                    @Override
                    public void onFailure(ApiErrorE error, String message) {
                        Toast.makeText(AddTransactionActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

                // Remove any unwanted text alluding to the checkbox previous state
                participant_name_Input.setText("");
                participant_name_Input.setHint(R.string.select_email);

                // Disable normal keyboard text input
                participant_name_Input.setInputType(0);

                // When the user taps the field open the registered user's email dropdown list
                participant_name_Input.setOnClickListener(v ->
                        participant_name_Input.showDropDown()
                );
            }

            // When shared mode is OFF, let user type the name of the participant service
            else
            {
                // Remove any unwanted text alluding to the checkbox previous state
                participant_name_Input.setText("");
                participant_name_Input.setHint(R.string.name_of_service);

                // Enable normal typing mode
                participant_name_Input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

                // Remove dropdown behavior
                participant_name_Input.setOnClickListener(null);
                participant_name_Input.setOnFocusChangeListener(null);
                participant_name_Input.setAdapter(null);
            }
        });


        // Add edge to edge support with custom padding.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Add padding to all sides of the view without
            // losing the benefits of the edge-to-edge effect.
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });




        TextView from_to = findViewById(R.id.participant_textView);

        //Make necessary UI changes based of the type of the transaction (Income - Expense)
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {

            if (!isChecked) return;

            if (checkedId == R.id.btnIncome) {
                titleInput.setHint(R.string.income_name);
                from_to.setText(R.string.from);
            } else if (checkedId == R.id.btnExpense) {
                titleInput.setHint(R.string.expense_name);
                from_to.setText(R.string.to);
            }

        });


        // Get a reference to the date input field for allowing interaction with the layout
        dateInput = findViewById(R.id.editTextDate);

        // Disable free typing access to the user
        // ensuring that only valid date/time are being selected
        dateInput.setFocusable(false);
        dateInput.setClickable(true);

        // Upon user interaction with the date input field
        // Allow the user to select the date and time of the exchange
        dateInput.setOnClickListener(v -> {

            // Create a calendar object initialized with the device's current date and time
            Calendar calendar = Calendar.getInstance();

            // Extract current year, month, and day from the calendar
            // and store their values
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create a dialog that allows the user to freely select a date
            DatePickerDialog dateDialog = new DatePickerDialog(
                    AddTransactionActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        // After selecting the date open a second dialog
                        // for allowing the user to select a specific time
                        TimePickerDialog timeDialog = new TimePickerDialog(
                                AddTransactionActivity.this,
                                (timeView, hourOfDay, minute) -> {

                                    // Create another calendar object that stores the selected date and time
                                    Calendar finalCal = Calendar.getInstance();
                                    finalCal.set(
                                            selectedYear,
                                            selectedMonth,
                                            selectedDay,
                                            hourOfDay,
                                            minute,
                                            0
                                    );

                                    // Convert the selected date and time into a Firebase Timestamp
                                    // that is later stored in Firestore as part of the transaction
                                    selectedTimestamp = new Timestamp(finalCal.getTime());

                                    // Create a user-friendly formatted string
                                    // representation of the selected date and time
                                    String formatted = String.format(
                                            Locale.getDefault(),
                                            "%02d-%02d-%04d %02d:%02d",
                                            selectedDay,
                                            selectedMonth + 1,
                                            selectedYear,
                                            hourOfDay,
                                            minute
                                    );

                                    // Display the formatted date and time inside the input field
                                    dateInput.setText(formatted);
                                },
                                12, 0, true
                        );

                        // Display the time picker dialog to the user
                        timeDialog.show();
                    },
                    year,
                    month,
                    day
            );

            // Display the date picker dialog to the user
            dateDialog.show();
        });


        // Get a reference to the floating action button
        // and set it to call the createExchange method upon tapping
        FloatingActionButton addBtn = findViewById(R.id.add_exchange_floatingActionButton);
        addBtn.setOnClickListener(v -> createExchange());

    }



    // Method used for collecting the values assigned by the user
    // then combining them as data for one exchange and sending it to the database
    private void createExchange()
    {

        // Extract the text from the input fields and store them in some respective string variables
        String title = titleInput.getText().toString().trim();
        String from = participant_name_Input.getText().toString().trim();
        String amountStr = amountInput.getText().toString().trim();

        // Check for missing entry fields before creating the exchange
        // and have an error message displayed in that instance
        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }


        // Store the accepted (type double) amount value
        // and display if it does not meet the requirements
        double amount;
        try
        {
            amount = Double.parseDouble(amountStr);
        }
        catch (NumberFormatException e)
        {
            Toast.makeText(this,
                    R.string.invalid_amount,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine INCOME / EXPENSE from toggle
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

        // Convert date string to timestamp
        Timestamp timestamp = selectedTimestamp;

        if (timestamp == null)
        {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read the status of shared and recurring checks

        CheckBox sharedCheck = findViewById(R.id.is_shared_checkBox);
        CheckBox recurringCheck = findViewById(R.id.is_recurring_checkBox);
        Spinner spinner = findViewById(R.id.repeat_when_spinner);

        boolean isShared = sharedCheck.isChecked();
        boolean isRecurring = recurringCheck.isChecked();

        String recurringType;

        if (isRecurring)
        {
            recurringType = spinner.getSelectedItem().toString();
        }

        else
        {
            recurringType = null;
        }

        // Determine the name of the shared user or service
        String serviceName = null;

        // If is shared, get the UUID of the user and store it in debtUserUuid.
        CompletableFuture<String> uuidFuture = new CompletableFuture<>();
        if (isShared)
        {
            // Execute the API call.
            FirebaseManager.getInstance().getUserUUIDByEmail(from, new ApiResultInterface<>()
            {
                @Override
                public void onSuccess(String uuid)
                {
                    uuidFuture.complete(uuid);
                }

                @Override
                public void onFailure(ApiErrorE error, String error_message)
                {
                    uuidFuture.completeExceptionally(new Exception(error_message));
                }
            });
        }

        else
        {
            uuidFuture.complete(null);
            serviceName = from;
        }

        final String finalServiceName = serviceName;

        // This code will wait until uuidFuture is completed (asynchronously).
        uuidFuture.thenAccept(resolvedUuid ->
        {
            // Build the exchange model.
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null)
            {
                runOnUiThread(() -> Toast.makeText(
                        AddTransactionActivity.this,
                        R.string.user_not_logged_in,
                        Toast.LENGTH_SHORT).show());
                return;
            }

            // Get the current user's UUID.
            String ownerId = currentUser.getUid();

            // Create the exchange model.
            ExchangeModel exchange = new ExchangeModel(
                    title,
                    side,
                    amount,
                    timestamp,
                    ownerId,
                    isShared,
                    resolvedUuid,
                    isRecurring,
                    recurringType,
                    finalServiceName
            );

            // Send it to Firebase
            firebaseManager.addExchange(exchange, new ApiResultInterface<String>()
            {
                @Override
                public void onSuccess(String result)
                {
                    Toast.makeText(AddTransactionActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                    finish(); // Smoothly returns the user to the main screen
                }

                @Override
                public void onFailure(ApiErrorE error, String message)
                {
                    Toast.makeText(AddTransactionActivity.this, R.string.error + message, Toast.LENGTH_SHORT).show();
                }
            });

        }).exceptionally(throwable ->
        {
            runOnUiThread(() -> Toast.makeText(
                    AddTransactionActivity.this,
                    getString(R.string.error) + " " + throwable.getMessage(),
                    Toast.LENGTH_SHORT).show());
            return null;
        });
    }

    public void OpenFinancesMenu(View view) {
        Intent i = new Intent(this, PersonalFinancesActivity.class);
        startActivity(i);
    }




}