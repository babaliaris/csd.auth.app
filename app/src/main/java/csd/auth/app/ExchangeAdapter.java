package csd.auth.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import csd.auth.app.api.models.ExchangeModel;


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
            h.participant.setText(
                    h.participant.getContext().getString(R.string.shared_with, e.debt_user_uuid)
            );
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