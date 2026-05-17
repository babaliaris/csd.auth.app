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
 * This is the Exchange Adapter class.
 */
public class ExchangeAdapter extends RecyclerView.Adapter<ExchangeAdapter.ViewHolder> {

    private List<ExchangeModel> list;

    public interface OnExchangeClickListener {
        void onItemClick(ExchangeModel exchange);
    }

    private OnExchangeClickListener listener;

    public void setOnExchangeClickListener(OnExchangeClickListener listener) {
        this.listener = listener;
    }

    //Pass data into the adapter
    public ExchangeAdapter(List<ExchangeModel> list) {
        this.list = list;
    }

    // 2
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, amount, date, participant, recurring;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
            participant = itemView.findViewById(R.id.participant);
            recurring = itemView.findViewById(R.id.recurring);
        }
    }

    //Create view
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exchange_item, parent, false);

        return new ViewHolder(view);
    }

    // 4
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        ExchangeModel e = list.get(pos);

        h.title.setText(e.title);

        String sign = e.isIncome() ? "+" : "-";
        h.amount.setText(
                h.amount.getContext().getString(R.string.amount_format, sign, e.value)
        );
        h.date.setText(e.date_time.toDate().toString());

        if (e.is_shared) {
            h.participant.setText(
                    h.participant.getContext().getString(R.string.shared_with, e.debt_user_uuid)
            );
        } else {
            h.participant.setText(e.service_name);
        }

        if (e.is_recurring && e.recurring_type != null)
        {
            h.recurring.setVisibility(View.VISIBLE);
            h.recurring.setText(
                    h.recurring.getContext().getString(
                            R.string.repeats_format,
                            e.recurring_type
                    )
            );
        }
        else
        {
            h.recurring.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {

            if (listener != null) {
                listener.onItemClick(e);
            }
        });
    }

    // 5
    @Override
    public int getItemCount() {
        return list.size();
    }

    // 6
    public void updateList(List<ExchangeModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }
}