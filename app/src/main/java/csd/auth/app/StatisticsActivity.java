package csd.auth.app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import csd.auth.app.api.ApiErrorE;
import csd.auth.app.api.ApiResultInterface;
import csd.auth.app.api.FirebaseManager;
import csd.auth.app.api.models.ExchangeModel;
import csd.auth.app.api.models.UserModel;

public class StatisticsActivity extends AppCompatActivity {

    // Variables for each layout element
    private FloatingActionButton backButton;
    private Spinner timePeriodSpinner;
    private TextView textViewSelectedDates;

    // Variables to help calculate the time period set by the spinner
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;
    private SimpleDateFormat dateFormat;
    private String selectedTimePeriod = "";
    private long startingDateTimestamp = 0;
    private long endingDateTimestamp = 0;

    private MaterialButtonToggleGroup toggleGroupType;
    private RecyclerView recyclerViewStats;
    private TextView emptyTextStats;
    private LinearLayout graphsContainer;

    private InternalExchangeAdapter adapter; // Internal adapter hadler independant from ExchangeAdapter activity
    private FirebaseManager firebaseManager; // Firebase handler
    private List<ExchangeModel> allExchanges = new ArrayList<>(); // Array holding the exchanges

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        backButton = findViewById(R.id.Statistics_back_button);
        timePeriodSpinner = findViewById(R.id.spinnerTimePeriod);
        textViewSelectedDates = findViewById(R.id.textViewSelectedDates);
        toggleGroupType = findViewById(R.id.toggleGroupType);
        recyclerViewStats = findViewById(R.id.recyclerViewStats);
        emptyTextStats = findViewById(R.id.emptyTextStats);
        graphsContainer = findViewById(R.id.graphsContainer);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


        // Setup back button
        backButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(StatisticsActivity.this, PersonalFinancesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        recyclerViewStats.setLayoutManager(new LinearLayoutManager(this));

        adapter = new InternalExchangeAdapter();
        recyclerViewStats.setAdapter(adapter);
        firebaseManager = FirebaseManager.getInstance();
        loadExchanges();

        toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) ->
        {
            if (isChecked)
            {
                filterAndDisplayExchanges();
            }
        });

        // Setup the date spinner
        timePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selectedTimePeriod = parent.getItemAtPosition(position).toString();
                Log.d("StatisticsActivity", "The selected period is: " + selectedTimePeriod);

                if (selectedTimePeriod.equalsIgnoreCase("Custom Dates"))
                {
                    showStartDatePicker();
                }

                else if (selectedTimePeriod.equalsIgnoreCase("Last Month"))
                {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 999);
                    endingDateTimestamp = calendar.getTimeInMillis();
                    calendar.add(Calendar.DAY_OF_YEAR, -30);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    startingDateTimestamp = calendar.getTimeInMillis();
                    textViewSelectedDates.setVisibility(View.GONE);
                    filterAndDisplayExchanges();
                }

                else if (selectedTimePeriod.equalsIgnoreCase("Last 3 Months"))
                {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 999);
                    endingDateTimestamp = calendar.getTimeInMillis();
                    calendar.add(Calendar.DAY_OF_YEAR, -90);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    startingDateTimestamp = calendar.getTimeInMillis();
                    textViewSelectedDates.setVisibility(View.GONE);
                    filterAndDisplayExchanges();
                }

                else
                {
                    startingDateTimestamp = 0;
                    endingDateTimestamp = 0;
                    textViewSelectedDates.setVisibility(View.GONE);
                    filterAndDisplayExchanges();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
    }

    // Load all the exchanges from the database into allExchanges array
    private void loadExchanges()
    {
        firebaseManager.getMyExchanges(new ApiResultInterface<List<ExchangeModel>>()
        {
            @Override
            public void onSuccess(List<ExchangeModel> result)
            {
                allExchanges = result;
                filterAndDisplayExchanges();
            }

            @Override
            public void onFailure(ApiErrorE error, String message)
            {
                Toast.makeText(
                        StatisticsActivity.this,
                        R.string.error + message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // Filtering out all the data of ArrayList based on the time period set by the user and passing to the graph adapter
    private void filterAndDisplayExchanges()
    {
        int checkedId = toggleGroupType.getCheckedButtonId();

        if (checkedId == R.id.btnGraphs)
        {
            recyclerViewStats.setVisibility(View.GONE);
            emptyTextStats.setVisibility(View.GONE);
            graphsContainer.setVisibility(View.VISIBLE);
            drawGraphs();
            return;
        }

        graphsContainer.setVisibility(View.GONE);
        boolean showShared = (checkedId == R.id.btnShared);
        List<ExchangeModel> filteredList = new ArrayList<>();

        for (ExchangeModel e : allExchanges)
        {
            boolean matchesType = (e.is_shared == showShared);
            boolean matchesDate = true;

            if (startingDateTimestamp > 0 && endingDateTimestamp > 0)
            {
                if (e.date_time != null)
                {
                    long transactionTime = e.date_time.toDate().getTime();

                    if (transactionTime < startingDateTimestamp || transactionTime > endingDateTimestamp)
                    {
                        matchesDate = false;
                    }
                }
            }

            if (matchesType && matchesDate)
            {
                filteredList.add(e);
            }
        }

        Collections.sort(filteredList, (e1, e2) ->
        {
            long time1 = (e1.date_time != null) ? e1.date_time.toDate().getTime() : 0;
            long time2 = (e2.date_time != null) ? e2.date_time.toDate().getTime() : 0;
            return Long.compare(time2, time1);
        });

        adapter.updateList(filteredList); // Pass the filtered data to the graph adapter

        if (filteredList.isEmpty()) // Handler in case no data were found
        {
            emptyTextStats.setVisibility(View.VISIBLE);
            recyclerViewStats.setVisibility(View.GONE);
        }

        else
        {
            emptyTextStats.setVisibility(View.GONE);
            recyclerViewStats.setVisibility(View.VISIBLE);
        }
    }

    // Function to show the calendar for the custom start date picker
    private void showStartDatePicker()
    {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog startDateDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) ->
                {
                    startDateCalendar = Calendar.getInstance();
                    startDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                    showEndDatePicker();
                }, year, month, day);

        startDateDialog.setTitle("Select Start Date");
        startDateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        startDateDialog.show();
    }

    // Function to show the calendar for the custom end date picker
    private void showEndDatePicker()
    {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog endDateDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) ->
                {
                    endDateCalendar = Calendar.getInstance();
                    endDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                    processSelectedDates();
                }, year, month, day);

        endDateDialog.setTitle("Select End Date");
        endDateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        if (startDateCalendar != null)
        {
            endDateDialog.getDatePicker().setMinDate(startDateCalendar.getTimeInMillis());
        }

        endDateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Today", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                endDateCalendar = Calendar.getInstance();
                processSelectedDates();
            }
        });

        endDateDialog.show();
    }


    private void processSelectedDates()
    {
        if (startDateCalendar != null && endDateCalendar != null)
        {
            startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startDateCalendar.set(Calendar.MINUTE, 0);
            startDateCalendar.set(Calendar.SECOND, 0);
            startDateCalendar.set(Calendar.MILLISECOND, 0);
            endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endDateCalendar.set(Calendar.MINUTE, 59);
            endDateCalendar.set(Calendar.SECOND, 59);
            endDateCalendar.set(Calendar.MILLISECOND, 999);
            startingDateTimestamp = startDateCalendar.getTimeInMillis();
            endingDateTimestamp = endDateCalendar.getTimeInMillis();

            String startString = dateFormat.format(startDateCalendar.getTime());
            String endString = dateFormat.format(endDateCalendar.getTime());
            String displayRange = "Selected: " + startString + " - " + endString;
            textViewSelectedDates.setText(displayRange);
            textViewSelectedDates.setVisibility(View.VISIBLE);
            filterAndDisplayExchanges();
        }
    }

    // Similar to ExchangeAdapter activity maintaining integrity
    // Setup the list of the transactions
    private class InternalExchangeAdapter extends RecyclerView.Adapter<InternalExchangeAdapter.ViewHolder>
    {
        private List<ExchangeModel> dataSet = new ArrayList<>();

        public void updateList(List<ExchangeModel> newList)
        {
            this.dataSet = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_statistics_exchange, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position)
        {
            ExchangeModel e = dataSet.get(position);

            h.title.setText(e.title != null ? e.title : "Unknown");

            String sign = e.isIncome() ? "+" : "-";
            h.amount.setText(sign + " " + e.value);

            if (e.date_time != null)
            {
                h.date.setText(e.date_time.toDate().toString());
            }

            else
            {
                h.date.setText("Date: N/A");
            }

            if (e.is_shared)
            {
                if (e.debt_user_uuid != null && !e.debt_user_uuid.isEmpty())
                {
                    // Tag the view holder's root layout with the expected UUID for this specific row position.
                    h.itemView.setTag(e.debt_user_uuid);

                    // Clear old recycled text out immediately with a temporary loading indicator.
                    h.participant.setText("Shared with: Loading...");

                    // Request the data from Firebase Manager.
                    firebaseManager.getUserProfileByUUID(e.debt_user_uuid, new ApiResultInterface<UserModel>()
                    {
                        @Override
                        public void onSuccess(UserModel user)
                        {
                            // Check that the UUID is still the same (using the tag help identifier).
                            if (e.debt_user_uuid.equals(h.itemView.getTag()))
                            {
                                // User email found.
                                if (user != null && user.email != null)
                                {
                                    h.participant.setText("Shared with: " + user.email);
                                }

                                // Just a fallback for old shared entries.
                                // (A bug where dept_user_uuid contained the email instead of the uuid).
                                else if (e.debt_user_uuid.contains("@"))
                                {
                                    // Fallback for your legacy test data bug
                                    h.participant.setText("Shared with: " + e.debt_user_uuid);
                                }

                                //Email not found.
                                else
                                {
                                    h.participant.setText("Shared with: Unknown User");
                                }
                            }
                        }

                        @Override
                        public void onFailure(ApiErrorE error, String error_message)
                        {
                            // For the tagged position.
                            if (e.debt_user_uuid.equals(h.itemView.getTag()))
                            {
                                // Even if Firestore fails/errs out, check if it's a legacy email string
                                if (e.debt_user_uuid.contains("@"))
                                {
                                    h.participant.setText("Shared with: " + e.debt_user_uuid);
                                }

                                // Else show an error.
                                else
                                {
                                    h.participant.setText("Shared with: Error fetching email");
                                }
                            }
                        }
                    });
                }

                else
                {
                    h.participant.setText("Shared with: N/A");
                }
            }

            else
            {
                h.participant.setText(e.service_name != null ? e.service_name : "N/A");
            }

            if (e.is_recurring && e.recurring_type != null)
            {
                h.recurring.setVisibility(View.VISIBLE);
                h.recurring.setText("Repeats: " + e.recurring_type);
            }
            else
            {
                h.recurring.setVisibility(View.GONE);
            }
        }


        @Override
        public int getItemCount()
        {
            return dataSet.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView title, amount, date, participant, recurring;

            ViewHolder(View itemView)
            {
                super(itemView);
                title = itemView.findViewById(R.id.stat_title);
                amount = itemView.findViewById(R.id.stat_amount);
                date = itemView.findViewById(R.id.stat_date);
                participant = itemView.findViewById(R.id.stat_participant);
                recurring = itemView.findViewById(R.id.stat_recurring);
            }
        }
    }


    // Manage button
    public void OpenTransactionHistoryMenu(View view)
    {
        Intent i = new Intent(this, TransactionHistoryActivity.class);
        startActivity(i);
    }



    private void drawGraphs()
    {
        graphsContainer.removeAllViews();

        List<ExchangeModel> graphData = new ArrayList<>();
        for (ExchangeModel e : allExchanges)
        {
            boolean matchesDate = true;

            if (startingDateTimestamp > 0 && endingDateTimestamp > 0)
            {
                if (e.date_time != null)
                {
                    long transactionTime = e.date_time.toDate().getTime();

                    if (transactionTime < startingDateTimestamp || transactionTime > endingDateTimestamp)
                    {
                        matchesDate = false;
                    }
                }
            }

            if (matchesDate)
            {
                graphData.add(e);
            }
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);

        params.setMargins(16, 16, 16, 16);

        Graph incomesGraph = new Graph(this, graphData, true, startingDateTimestamp, endingDateTimestamp);
        Graph expensesGraph = new Graph(this, graphData, false, startingDateTimestamp, endingDateTimestamp);

        graphsContainer.addView(incomesGraph, params);
        graphsContainer.addView(expensesGraph, params);
    }

    // Function to set each point of the graph in case the user chooses one
    private class DrawnPoint
    {
        ExchangeModel model;
        float cx, cy;
        DrawnPoint(ExchangeModel model, float cx, float cy)
        {
            this.model = model;
            this.cx = cx;
            this.cy = cy;
        }
    }

    // Graphs class
    private class Graph extends View
    {
        private List<ExchangeModel> items;
        private boolean isIncome;
        private long startTime, endTime;
        private Paint linePaint, axisPaint, textPaint, pointPaint, gridPaint;
        private Paint tooltipBgPaint, tooltipTextPaint;

        private List<DrawnPoint> drawnPoints = new ArrayList<>();
        private DrawnPoint selectedPoint = null;

        public Graph(Context context, List<ExchangeModel> data, boolean isIncome, long st, long et)
        {
            super(context);
            this.isIncome = isIncome;
            this.items = new ArrayList<>();

            for(ExchangeModel e : data) {
                if(e.isIncome() == isIncome)
                {
                    this.items.add(e);
                }
            }

            // Sort chronologically for drawing continuous lines
            Collections.sort(this.items, (e1, e2) ->
            {
                long t1 = (e1.date_time != null) ? e1.date_time.toDate().getTime() : 0;
                long t2 = (e2.date_time != null) ? e2.date_time.toDate().getTime() : 0;
                return Long.compare(t1, t2);
            });

            this.endTime = et;

            if (!this.items.isEmpty())
            {
                this.startTime = this.items.get(0).date_time.toDate().getTime();

                if (this.endTime == 0)
                {
                    this.endTime = this.items.get(this.items.size() - 1).date_time.toDate().getTime();
                }
            }

            else
            {
                this.startTime = st;
                if (this.startTime == 0 || this.endTime == 0)
                {
                    this.startTime = System.currentTimeMillis() - 86400000L;
                    this.endTime = System.currentTimeMillis();
                }
            }

            if (this.endTime <= this.startTime)
            {
                this.endTime = this.startTime + 86400000L; // Adds 1 day buffer
            }

            setupPaints();
        }

        // Setup the colors for each graph element
        private void setupPaints()
        {
            axisPaint = new Paint();
            axisPaint.setColor(Color.DKGRAY);
            axisPaint.setStrokeWidth(4f);

            gridPaint = new Paint();
            gridPaint.setColor(Color.LTGRAY);
            gridPaint.setStrokeWidth(2f);
            gridPaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{10, 10}, 0));

            linePaint = new Paint();
            linePaint.setColor(isIncome ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
            linePaint.setStrokeWidth(6f);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setAntiAlias(true);

            pointPaint = new Paint();
            pointPaint.setColor(isIncome ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
            pointPaint.setStyle(Paint.Style.FILL);
            pointPaint.setAntiAlias(true);

            textPaint = new Paint();
            textPaint.setColor(Color.DKGRAY);
            textPaint.setTextSize(32f);
            textPaint.setAntiAlias(true);

            tooltipBgPaint = new Paint();
            tooltipBgPaint.setColor(Color.argb(230, 40, 40, 40));
            tooltipBgPaint.setStyle(Paint.Style.FILL);
            tooltipBgPaint.setAntiAlias(true);

            tooltipTextPaint = new Paint();
            tooltipTextPaint.setColor(Color.WHITE);
            tooltipTextPaint.setTextSize(28f);
            tooltipTextPaint.setAntiAlias(true);
        }

        // Print the dots when touched
        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                float touchX = event.getX();
                float touchY = event.getY();
                boolean pointFound = false;

                for (DrawnPoint p : drawnPoints)
                {
                    float dx = touchX - p.cx;
                    float dy = touchY - p.cy;

                    if (Math.sqrt(dx * dx + dy * dy) <= 50f)// Touch margin of 50 pixls
                    {
                        selectedPoint = p;
                        pointFound = true;
                        break;
                    }
                }

                // Diselect if user touch a blank space
                if (!pointFound)
                {
                    selectedPoint = null;
                }

                invalidate();
                return true;
            }
            return super.onTouchEvent(event);
        }


        // Printing the graph
        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);
            int padding = 100;
            int width = getWidth();
            int height = getHeight();

            drawnPoints.clear();

            textPaint.setTextSize(40f);
            textPaint.setFakeBoldText(true);
            canvas.drawText(isIncome ? "Incomes (€)" : "Expenses (€)", padding, padding - 40, textPaint);
            textPaint.setTextSize(30f);
            textPaint.setFakeBoldText(false);

            canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint);
            canvas.drawLine(padding, padding, padding, height - padding, axisPaint);

            if (items.isEmpty()) {
                canvas.drawText("No data available.", width / 2f - 100, height / 2f, textPaint);
                return;
            }

            float maxVal = 0;
            for(ExchangeModel e : items) {
                try {
                    float v = Float.parseFloat(String.valueOf(e.value));
                    if(v > maxVal) maxVal = v;
                } catch (Exception ignored) {}
            }
            if (maxVal == 0) maxVal = 100;

            float xRange = endTime - startTime;
            float yRange = maxVal;

            int gridLines = 4;
            for (int i = 0; i <= gridLines; i++) {
                float yVal = (maxVal / gridLines) * i;
                float yPos = (height - padding) - ((yVal / yRange) * (height - 2 * padding));

                canvas.drawLine(padding, yPos, width - padding, yPos, gridPaint);
                canvas.drawText(String.format(Locale.getDefault(), "%.1f", yVal), 10, yPos + 10, textPaint);
            }

            float prevX = -1, prevY = -1;
            for(ExchangeModel e : items) {
                long t = (e.date_time != null) ? e.date_time.toDate().getTime() : startTime;
                float val = 0;
                try {
                    val = Float.parseFloat(String.valueOf(e.value));
                } catch (Exception ignored) {}

                float cx = padding + ((t - startTime) / xRange) * (width - 2 * padding);
                float cy = (height - padding) - ((val / yRange) * (height - 2 * padding));

                if (prevX != -1 && prevY != -1) {
                    canvas.drawLine(prevX, prevY, cx, cy, linePaint);
                }

                canvas.drawCircle(cx, cy, 12f, pointPaint);
                drawnPoints.add(new DrawnPoint(e, cx, cy)); // Save screen coordinates for touch detection

                prevX = cx;
                prevY = cy;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            canvas.drawText(sdf.format(new java.util.Date(startTime)), padding, height - padding + 40, textPaint);
            canvas.drawText(sdf.format(new java.util.Date(endTime)), width - padding - 120, height - padding + 40, textPaint);

            if (selectedPoint != null) {
                canvas.drawCircle(selectedPoint.cx, selectedPoint.cy, 18f, pointPaint);

                String tName = selectedPoint.model.title != null ? selectedPoint.model.title : "Unknown";
                String tAmount = (isIncome ? "+" : "-") + selectedPoint.model.value + " €";
                String tDate = "";
                if (selectedPoint.model.date_time != null) {
                    SimpleDateFormat tooltipDateSdf = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());
                    tDate = tooltipDateSdf.format(selectedPoint.model.date_time.toDate());
                }

                Paint.FontMetrics fm = tooltipTextPaint.getFontMetrics();
                float textHeight = fm.descent - fm.ascent;
                float w1 = tooltipTextPaint.measureText(tName);
                float w2 = tooltipTextPaint.measureText(tAmount);
                float w3 = tooltipTextPaint.measureText(tDate);
                float maxW = Math.max(w1, Math.max(w2, w3));

                float boxW = maxW + 40f;
                float boxH = (textHeight * 3) + 40f;

                float rectLeft = selectedPoint.cx + 20f;
                float rectTop = selectedPoint.cy - boxH / 2f;

                if (rectLeft + boxW > width - 10) {
                    rectLeft = selectedPoint.cx - boxW - 20f;
                }

                RectF tooltipRect = new RectF(rectLeft, rectTop, rectLeft + boxW, rectTop + boxH);
                canvas.drawRoundRect(tooltipRect, 15f, 15f, tooltipBgPaint);

                float textX = rectLeft + 20f;
                float textY = rectTop + 20f - fm.ascent;

                canvas.drawText(tName, textX, textY, tooltipTextPaint);
                canvas.drawText(tAmount, textX, textY + textHeight, tooltipTextPaint);
                canvas.drawText(tDate, textX, textY + textHeight * 2, tooltipTextPaint);
            }
        }
    }
}