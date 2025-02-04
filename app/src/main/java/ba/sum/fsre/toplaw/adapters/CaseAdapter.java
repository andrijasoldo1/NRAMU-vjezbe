package ba.sum.fsre.toplaw.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ba.sum.fsre.toplaw.R;
import ba.sum.fsre.toplaw.models.Case;

public class CaseAdapter extends BaseCaseAdapter {

    public interface EditCaseCallback {
        void onEdit(Case caseToEdit);
    }

    private static final String TAG = "CaseAdapter";
    private final Context context;
    private final List<Case> cases;
    private final EditCaseCallback callback;
    private final FirebaseFirestore db;
    private final boolean showButtons;

    public CaseAdapter(Context context, List<Case> cases, @Nullable EditCaseCallback callback, boolean showButtons) {
        super(context, R.layout.list_item_case, cases);
        this.context = context;
        this.cases = cases;
        this.callback = callback;
        this.db = FirebaseFirestore.getInstance();
        this.showButtons = showButtons;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_case, parent, false);
        }

        try {
            // Initialize views
            TextView caseNameView = convertView.findViewById(R.id.case_name);
            TextView caseStatusView = convertView.findViewById(R.id.case_status);
            TextView caseTypeView = convertView.findViewById(R.id.case_type);
            TextView casePriceView = convertView.findViewById(R.id.case_price);
            TextView caseDescriptionView = convertView.findViewById(R.id.case_description);
            TextView userEmailView = convertView.findViewById(R.id.user_email);
            Button editButton = convertView.findViewById(R.id.edit_button);
            Button deleteButton = convertView.findViewById(R.id.delete_button);
            Button sendButton = convertView.findViewById(R.id.send_button);

            Case aCase = cases.get(position);

            // Set case details
            caseNameView.setText(aCase.getName() != null ? aCase.getName() : "Unnamed Case");
            caseStatusView.setText(aCase.getStatus() != null ? aCase.getStatus() : "Status Unknown");
            caseTypeView.setText(aCase.getTypeOfCase() != null ? "Type: " + aCase.getTypeOfCase() : "Type: Unknown");
            casePriceView.setText(aCase.getPrice() > 0 ? String.format("Price: %.2f KM", aCase.getPrice()) : "Price: Not set");
            caseDescriptionView.setText(aCase.getDescription() != null && !aCase.getDescription().isEmpty()
                    ? aCase.getDescription()
                    : "No description available");

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

            // Show or hide buttons based on the context
            if (showButtons) {
                editButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
                sendButton.setVisibility(View.VISIBLE);

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
                                    Toast.makeText(context, "Case deleted successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to delete case", e);
                                    Toast.makeText(context, "Failed to delete case.", Toast.LENGTH_SHORT).show();
                                });
                    }
                });

                // Send Button Listener
                sendButton.setOnClickListener(v -> {
                    showRecipientDialog(aCase.getId());
                });
            } else {
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
                sendButton.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in getView: ", e);
        }

        return convertView;
    }

    private void showRecipientDialog(String caseId) {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(context, "No users available to send the case to.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> userIds = new ArrayList<>();
                    List<String> userEmails = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String userId = document.getId();
                        String email = document.getString("eMail");

                        // Log each user's details
                        Log.d(TAG, "UserID: " + userId + ", Email: " + email);

                        if (email != null && !email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            userIds.add(userId);
                            userEmails.add(email);
                        }
                    }

                    Log.d(TAG, "Total users fetched: " + userEmails.size());

                    if (!userEmails.isEmpty()) {
                        showRecipientSelectionDialog(caseId, userIds, userEmails);
                    } else {
                        Toast.makeText(context, "No other users found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch users", e);
                    Toast.makeText(context, "Failed to fetch users.", Toast.LENGTH_SHORT).show();
                });
    }


    private void showRecipientSelectionDialog(String caseId, List<String> userIds, List<String> userEmails) {
        String[] emailArray = userEmails.toArray(new String[0]);

        new AlertDialog.Builder(context)
                .setTitle("Select Recipient")
                .setItems(emailArray, (dialog, which) -> {
                    String recipientUserId = userIds.get(which);
                    sendCaseToUser(caseId, recipientUserId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendCaseToUser(String caseId, String recipientUserId) {
        Map<String, Object> sharedData = new HashMap<>();
        sharedData.put("caseId", caseId);
        sharedData.put("sentByUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        sharedData.put("sentToUserId", recipientUserId);
        sharedData.put("sentAt", FieldValue.serverTimestamp());

        db.collection("cases")
                .document(caseId)
                .collection("shared_cases")
                .add(sharedData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Case sent successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send case", e);
                    Toast.makeText(context, "Failed to send case.", Toast.LENGTH_SHORT).show();
                });
    }
}
