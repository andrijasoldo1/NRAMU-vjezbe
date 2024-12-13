package ba.sum.fsre.mymath.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.models.Case;

public class CaseAdapter extends ArrayAdapter<Case> {

    public interface EditCaseCallback {
        void onEdit(Case caseToEdit);
    }

    private static final String TAG = "CaseAdapter";
    private final Context context;
    private final List<Case> cases;
    private final EditCaseCallback callback;

    public CaseAdapter(Context context, List<Case> cases, @NonNull EditCaseCallback callback) {
        super(context, R.layout.list_item_case, cases);
        this.context = context;
        this.cases = cases;
        this.callback = callback;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_case, parent, false);
        }

        try {
            TextView caseNameView = convertView.findViewById(R.id.case_name);
            TextView caseStatusView = convertView.findViewById(R.id.case_status);
            Button editButton = convertView.findViewById(R.id.edit_button);
            Button deleteButton = convertView.findViewById(R.id.delete_button);

            Case aCase = cases.get(position);

            caseNameView.setText(aCase.getName());
            caseStatusView.setText(aCase.getStatus());

            // Edit Button Listener
            editButton.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onEdit(aCase);
                } else {
                    Log.e(TAG, "Edit callback is null");
                    Toast.makeText(context, "Edit callback is not defined", Toast.LENGTH_SHORT).show();
                }
            });

            // Delete Button Listener
            deleteButton.setOnClickListener(v -> {
                if (aCase.getId() != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("cases")
                            .document(aCase.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                cases.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(context, "Case deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to delete case", e);
                                Toast.makeText(context, "Failed to delete case", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Log.e(TAG, "Case ID is null, cannot delete");
                    Toast.makeText(context, "Case ID is invalid, cannot delete", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in getView: ", e);
        }

        return convertView;
    }
}
