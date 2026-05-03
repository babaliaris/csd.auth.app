package csd.auth.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
public class EmailSearchDialog extends BottomSheetDialogFragment {

    public interface OnEmailSelectedListener {
        void onEmailSelected(String email);
    }

    private OnEmailSelectedListener listener;

    public void setListener(OnEmailSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_email_search, container, false);

        Button insertButton = view.findViewById(R.id.insertButton);
        EditText emailInput = view.findViewById(R.id.emailInput);

        insertButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (listener != null) {
                listener.onEmailSelected(email);
            }

            dismiss();
        });

        return view;
    }
}
