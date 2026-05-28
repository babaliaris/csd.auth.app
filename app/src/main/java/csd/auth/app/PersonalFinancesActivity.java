/**
 * @author Andreas Galanakis
 * @version 1.0
 *
 * Modified by Karapatsias to add the statistics button and the plot graph for the balance
 * @version 2.0 FINAL
 * This is the Personal finances activity class.
 */


package csd.auth.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import csd.auth.app.databinding.ActivityPersonalFinancesBinding;
import csd.auth.app.utils.ExchangeCalculations;


public class PersonalFinancesActivity extends AppCompatActivity {
    private ActivityPersonalFinancesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inflate the layout using View Binding.
        binding = ActivityPersonalFinancesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Add edge to edge support with custom padding.
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
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
        FirebaseManager.getInstance().getMyProfile(new ApiResultInterface<UserModel>() {
            @Override
            public void onSuccess(UserModel user) {
                String email = user.email;
                if (email != null) {
                    String username = email.substring(0, email.indexOf("@"));
                    binding.textView6.setText(username);
                }
            }

            @Override
            public void onFailure(ApiErrorE error, String message) {
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
    private void loadFinancialDashboardData() {
        FirebaseManager manager = FirebaseManager.getInstance();

        // Load Personal Exchanges (Owner scope)
        manager.getMyExchanges(new ApiResultInterface<List<ExchangeModel>>() {
            @Override
            public void onSuccess(List<ExchangeModel> myExchanges) {
                // Run localized computations using static utility logic
                double incomes = ExchangeCalculations.calculateTotalIncomes(myExchanges);
                double expenses = ExchangeCalculations.calculateTotalExpenses(myExchanges);
                double balance = ExchangeCalculations.calculateNetBalance(myExchanges);
                double owedToMe = ExchangeCalculations.calculateTotalOwedToMe(myExchanges);

                // Update UI Nodes safely
                binding.tvTotalIncomes.setText(String.format(Locale.getDefault(), "%.2f€", incomes));
                binding.tvTotalExpenses.setText(String.format(Locale.getDefault(), "%.2f€", expenses));
                binding.tvNetBalance.setText(String.format(Locale.getDefault(), "%.2f€", balance));
                binding.tvOwedToMe.setText(String.format(Locale.getDefault(), "%.2f€", owedToMe));

                // Calculate timestamps for the graph added by Karapatsias
                Calendar cal = Calendar.getInstance();
                long endTimestamp = cal.getTimeInMillis();
                cal.add(Calendar.MONTH, -6);
                long startTimestamp = cal.getTimeInMillis();

                binding.graphContainer.removeAllViews();
                BalanceGraphView balanceGraph = new BalanceGraphView(PersonalFinancesActivity.this, myExchanges, startTimestamp, endTimestamp);
                binding.graphContainer.addView(balanceGraph);
                // Added by Karapatsias
            }

            @Override
            public void onFailure(ApiErrorE error, String error_message) {
                Toast.makeText(
                                PersonalFinancesActivity.this,
                                "Error fetching personal history: " + error_message, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // Load Debt Records (Debtor scope)
        manager.getExchangesIOwe(new ApiResultInterface<List<ExchangeModel>>() {
            @Override
            public void onSuccess(List<ExchangeModel> exchangesIOwe) {
                double iOwe = ExchangeCalculations.calculateTotalIOwe(exchangesIOwe);
                binding.tvTotalIOwe.setText(String.format(Locale.getDefault(), "%.2f€", iOwe));
            }

            @Override
            public void onFailure(ApiErrorE error, String error_message) {
                Toast.makeText(
                                PersonalFinancesActivity.this,
                                "Error fetching debt profiles: " + error_message, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    public void OpenStatisticsMenu(View view) {
        Intent i = new Intent(this, StatisticsActivity.class);
        startActivity(i);
    }

    public void OpenAddIncomeMenu(View view) {
        Intent i = new Intent(this, AddTransactionActivity.class);
        startActivity(i);
    }

    public void OpenAuthMenu(View view) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }


// Made by Nikolaos Karapatsias
    private class BalanceGraphView extends View
    {
        private List<BalancePoint> points; // Points presented in the graph
        private long startTime, endTime; // Timestamps for the graph timeline
        private Paint linePaint, axisPaint, textPaint, pointPaint, gridPaint, zeroLinePaint, tooltipBgPaint, tooltipTextPaint; // Holding the colors of each element in the graph
        private BalancePoint selectedPoint = null; // Helper var to print selected dot

        // Helping class to hold each dot's data
        private class BalancePoint
        {
            ExchangeModel model;
            float balance;
            long timestamp;
            float cx, cy;

            BalancePoint(ExchangeModel m, float b, long t)
            {
                this.model = m;
                this.balance = b;
                this.timestamp = t;
            }
        }

        // Initializing the graph
        public BalanceGraphView(Context context, List<ExchangeModel> data, long st, long et)
        {
            super(context);
            this.startTime = st;
            this.endTime = et;
            this.points = new ArrayList<>();

            List<ExchangeModel> sortedData = new ArrayList<>(data);
            Collections.sort(sortedData, (e1, e2) ->
            {
                long t1 = (e1.date_time != null) ? e1.date_time.toDate().getTime() : 0;
                long t2 = (e2.date_time != null) ? e2.date_time.toDate().getTime() : 0;
                return Long.compare(t1, t2);
            });

            float currentBalance = 0f;
            for (ExchangeModel e : sortedData)
            {
                if (e.date_time != null)
                {
                    float val = 0;
                    try
                    {
                        val = Float.parseFloat(String.valueOf(e.value));
                    }
                    catch (Exception ignored) {}

                    if (e.isIncome())
                    {
                        currentBalance += val;
                    }

                    else
                    {
                        currentBalance -= val;
                    }

                    long t = e.date_time.toDate().getTime();

                    if (t >= this.startTime && t <= this.endTime)
                    {
                        this.points.add(new BalancePoint(e, currentBalance, t));
                    }
                }
            }
            setupPaints();
        }

        // Set up all the colors of the graph
        private void setupPaints() {
            axisPaint = new Paint();
            axisPaint.setColor(Color.DKGRAY);
            axisPaint.setStrokeWidth(4f);

            gridPaint = new Paint();
            gridPaint.setColor(Color.LTGRAY);
            gridPaint.setStrokeWidth(2f);
            gridPaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{10, 10}, 0));

            zeroLinePaint = new Paint();
            zeroLinePaint.setColor(Color.GRAY);
            zeroLinePaint.setStrokeWidth(4f);

            linePaint = new Paint();
            linePaint.setColor(Color.parseColor("#673AB7"));
            linePaint.setStrokeWidth(6f);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setAntiAlias(true);

            pointPaint = new Paint();
            pointPaint.setColor(Color.parseColor("#512DA8"));
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

        // Function to handle the dots when chosen
        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                float touchX = event.getX();
                float touchY = event.getY();
                boolean pointFound = false;

                for (BalancePoint p : points)
                {
                    float dx = touchX - p.cx;
                    float dy = touchY - p.cy;

                    if (Math.sqrt(dx * dx + dy * dy) <= 50f)// 50 pixel radius tolerance of touchdown
                    {
                        selectedPoint = p;
                        pointFound = true;
                        break;
                    }
                }

                if (!pointFound) selectedPoint = null;
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

            textPaint.setTextSize(40f);
            textPaint.setFakeBoldText(true);
            canvas.drawText("Balance - Last 6 Months", padding, padding - 40, textPaint);
            textPaint.setTextSize(30f);
            textPaint.setFakeBoldText(false);

            canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint);
            canvas.drawLine(padding, padding, padding, height - padding, axisPaint);

            if (points.isEmpty())
            {
                canvas.drawText("No data available.", width / 2f - 120, height / 2f, textPaint);
                return;
            }

            float maxVal = Float.NEGATIVE_INFINITY;
            float minVal = Float.POSITIVE_INFINITY;

            for (BalancePoint p : points)
            {
                if (p.balance > maxVal) maxVal = p.balance;
                if (p.balance < minVal) minVal = p.balance;
            }

            if (maxVal == minVal)
            {
                maxVal += 50f;
                minVal -= 50f;
            }

            else
            {
                float rangeMargin = (maxVal - minVal) * 0.15f;
                maxVal += rangeMargin;
                minVal -= rangeMargin;
            }

            if (minVal > 0) minVal = 0;

            float xRange = endTime - startTime;
            float yRange = maxVal - minVal;

            int gridLines = 4;
            for (int i = 0; i <= gridLines; i++)
            {
                float yVal = minVal + (yRange / gridLines) * i;
                float yPos = (height - padding) - ((yVal - minVal) / yRange) * (height - 2 * padding);

                canvas.drawLine(padding, yPos, width - padding, yPos, gridPaint);
                canvas.drawText(String.format(Locale.getDefault(), "%.0f", yVal), 10, yPos + 10, textPaint);
            }

            if (minVal < 0 && maxVal > 0)
            {
                float zeroY = (height - padding) - ((0 - minVal) / yRange) * (height - 2 * padding);
                canvas.drawLine(padding, zeroY, width - padding, zeroY, zeroLinePaint);
            }

            float prevX = -1, prevY = -1;

            for (BalancePoint p : points)
            {
                p.cx = padding + ((p.timestamp - startTime) / xRange) * (width - 2 * padding);
                p.cy = (height - padding) - ((p.balance - minVal) / yRange) * (height - 2 * padding);

                if (prevX != -1 && prevY != -1) {
                    canvas.drawLine(prevX, prevY, p.cx, p.cy, linePaint);
                }

                canvas.drawCircle(p.cx, p.cy, 12f, pointPaint);
                prevX = p.cx;
                prevY = p.cy;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            canvas.drawText(sdf.format(new java.util.Date(startTime)), padding, height - padding + 40, textPaint);
            canvas.drawText(sdf.format(new java.util.Date(endTime)), width - padding - 150, height - padding + 40, textPaint);

            if (selectedPoint != null) {
                canvas.drawCircle(selectedPoint.cx, selectedPoint.cy, 18f, pointPaint);

                String tTitle = selectedPoint.model.title != null ? selectedPoint.model.title : "Unknown";
                String tImpact = (selectedPoint.model.isIncome() ? "+" : "-") + selectedPoint.model.value + " €";
                String tBalance = "Bal: " + String.format(Locale.getDefault(), "%.2f €", selectedPoint.balance);

                String tDate = "";
                if (selectedPoint.model.date_time != null) {
                    SimpleDateFormat tooltipDateSdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                    tDate = tooltipDateSdf.format(selectedPoint.model.date_time.toDate());
                }

                Paint.FontMetrics fm = tooltipTextPaint.getFontMetrics();
                float textHeight = fm.descent - fm.ascent;
                float w1 = tooltipTextPaint.measureText(tTitle + " (" + tDate + ")");
                float w2 = tooltipTextPaint.measureText(tBalance);
                float maxW = Math.max(w1, w2);

                float boxW = maxW + 40f;
                float boxH = (textHeight * 3) + 40f;

                float rectLeft = selectedPoint.cx + 20f;
                float rectTop = selectedPoint.cy - boxH / 2f;

                if (rectLeft + boxW > width - 10)
                {
                    rectLeft = selectedPoint.cx - boxW - 20f;
                }

                RectF tooltipRect = new RectF(rectLeft, rectTop, rectLeft + boxW, rectTop + boxH);
                canvas.drawRoundRect(tooltipRect, 15f, 15f, tooltipBgPaint);

                float textX = rectLeft + 20f;
                float textY = rectTop + 20f - fm.ascent;

                canvas.drawText(tTitle + " (" + tDate + ")", textX, textY, tooltipTextPaint);
                canvas.drawText(tImpact, textX, textY + textHeight, tooltipTextPaint);
                canvas.drawText(tBalance, textX, textY + textHeight * 2, tooltipTextPaint);
            }
        }
    }
}