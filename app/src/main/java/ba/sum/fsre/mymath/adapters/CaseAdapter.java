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

public class CaseAdapter extends BaseCaseAdapter {

    public interface EditCaseCallback {
        void onEdit(Case caseToEdit);
    }

    private static final String TAG = "CaseAdapter";
    private final Context context;
    private final List<Case> cases;
    private final EditCaseCallback callback;
    private final FirebaseFirestore db;

    public CaseAdapter(Context context, List<Case> cases, @NonNull EditCaseCallback callback) {
        super(context, R.layout.list_item_case, cases);
        this.context = context;
        this.cases = cases;
        this.callback = callback;
        this.db = FirebaseFirestore.getInstance();
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
            TextView caseTypeView = convertView.findViewById(R.id.case_type);
            TextView userEmailView = convertView.findViewById(R.id.user_email);
            Button editButton = convertView.findViewById(R.id.edit_button);
            Button deleteButton = convertView.findViewById(R.id.delete_button);

            Case aCase = cases.get(position);

            // Set case details
            caseNameView.setText(aCase.getName());
            caseStatusView.setText(aCase.getStatus());
            caseTypeView.setText("Type: " + aCase.getTypeOfCase()); // Display type of case

            // Fetch user email from Firestore using userId
            if (aCase.getUserId() != null) {
                db.collection("users")
                        .document(aCase.getUserId())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String email = documentSnapshot.getString("eMail");
                                userEmailView.setText(email != null ? "Created by: " + email : "Created by: Unknown");
                            } else {
                                userEmailView.setText("Created by: Unknown");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to fetch user email", e);
                            userEmailView.setText("Created by: Unknown");
                        });
            } else {
                userEmailView.setText("Created by: Unknown");
            }

            // Edit Button Listener
            editButton.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onEdit(aCase);
                } else {
                    Toast.makeText(context, "Edit callback is not defined", Toast.LENGTH_SHORT).show();
                }
            });

            // Delete Button Listener
            deleteButton.setOnClickListener(v -> {
                if (aCase.getId() != null) {
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
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in getView: ", e);
        }

        return convertView;
    }
}
