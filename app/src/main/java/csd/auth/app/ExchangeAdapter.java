package csd.auth.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import csd.auth.app.api.ApiErrorE;
import csd.auth.app.api.ApiResultInterface;
import csd.auth.app.api.FirebaseManager;
import csd.auth.app.api.models.ExchangeModel;
import csd.auth.app.api.models.UserModel;


/**
 * @author Andreas Galanakis
 * @version 1.0
 *
 * Adapter class responsible for binding ExchangeModel data
 * to RecyclerView items.
 */
public class ExchangeAdapter extends RecyclerView.Adapter<ExchangeAdapter.ViewHolder> {

    // List of all the exchanges displayed in the RecyclerView
    private List<ExchangeModel> list;

    // Listener for handling interaction with the RecyclerView items
    private OnExchangeClickListener listener;

    public void setOnExchangeClickListener(OnExchangeClickListener listener) {
        this.listener = listener;
    }

    /**
     * Interface used to handle click events on a single exchange item.
     */
    public interface OnExchangeClickListener {
        void onItemClick(ExchangeModel exchange);
    }

    /**
     * Constructor that receives the dataset to be displayed.
     *
     * @param list List of ExchangeModel objects
     */
    public ExchangeAdapter(List<ExchangeModel> list) {
        this.list = list;
    }

    // 2
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, amount, date, participant, recurring;

        /**
         * ViewHolder class that holds references to all UI components
         * inside a single RecyclerView item.
         * This improves performance by avoiding repeated view lookups.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
            participant = itemView.findViewById(R.id.participant);
            recurring = itemView.findViewById(R.id.recurring);
        }
    }

    /**
     * Inflates the layout for each RecyclerView item.
     *
     * @param parent The parent ViewGroup
     * @param viewType The type of view (unused here)
     * @return A new ViewHolder instance
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exchange_item, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Binds ExchangeModel data to the UI elements of a list item.
     * This method updates:
     * - title
     * - amount (with + or - sign depending on income/expense)
     * - date
     * - participant (shared user or service name)
     * - recurring status (if enabled)
     * Also handles click events for each item.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        ExchangeModel e = list.get(pos);

        h.title.setText(e.title);

        String sign = e.isIncome() ? "+" : "-";
        h.amount.setText(
                h.amount.getContext().getString(R.string.amount_format, sign, e.value)
        );
        h.date.setText(e.date_time.toDate().toString());

        if (e.is_shared)
        {
            if (e.debt_user_uuid != null && !e.debt_user_uuid.isEmpty())
            {
                // Tag the view holder's root layout with the expected UUID for this specific row position.
                h.itemView.setTag(e.debt_user_uuid);

                // Clear old recycled text out immediately with a temporary loading indicator.
                h.participant.setText("Shared with: Loading...");

                // Request the data from Firebase Manager.
                FirebaseManager.getInstance().getUserProfileByUUID(e.debt_user_uuid, new ApiResultInterface<UserModel>()
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
            h.participant.setText(e.service_name);
        }

        if (e.is_recurring && e.recurring_type != null)
        {
            h.recurring.setVisibility(View.VISIBLE);
            h.recurring.setText(h.recurring
                    .getContext()
                    .getString(R.string.repeats_format,e.recurring_type)
            );
        }
        else
        {
            h.recurring.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {

            if (listener != null)
            {
                listener.onItemClick(e);
            }
        });
    }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return size of the exchange list
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Updates the dataset of the adapter and refreshes the RecyclerView.
     *
     * @param newList New list of ExchangeModel objects
     */
    public void updateList(List<ExchangeModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }
}