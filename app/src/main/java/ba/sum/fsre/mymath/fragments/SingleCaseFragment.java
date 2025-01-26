package ba.sum.fsre.mymath.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.models.Case;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SingleCaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SingleCaseFragment extends Fragment {
    private Case currentCase;
    private FirebaseFirestore db;
    private TextView nameTextView, priceTextView, caseTypeTextView, descriptionTextView, statusTextView, userTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_case, container, false);

        // Initialize Views
        nameTextView = view.findViewById(R.id.case_title);
        userTextView = view.findViewById(R.id.name);
        priceTextView = view.findViewById(R.id.price);
        caseTypeTextView = view.findViewById(R.id.case_expertise);
        descriptionTextView = view.findViewById(R.id.description);
        statusTextView = view.findViewById(R.id.case_status);


        // Retrieve Case ID from Arguments
        String caseId = getArguments() != null ? getArguments().getString("CASE_ID") : null;
        if (caseId != null) {
            loadCaseDetails(caseId);
        } else {
            Log.d("SingleCaseFragment", "CASE_ID is null, no case to display.");
        }

        // Setup Back Navigation
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void loadCaseDetails(String caseId) {
        db = FirebaseFirestore.getInstance();
        db.collection("cases").document(caseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentCase = documentSnapshot.toObject(Case.class);
                        if (currentCase != null) {
                            updateUI();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Case not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load case details", Toast.LENGTH_SHORT).show()
                );
    }

    private void updateUI() {
        if (currentCase != null) {
            nameTextView.setText(currentCase.getName() != null ? currentCase.getName() : "No name provided");
            priceTextView.setText(String.format("%.2f KM", currentCase.getPrice()));
            caseTypeTextView.setText(currentCase.getTypeOfCase() != null ? currentCase.getTypeOfCase() : "Unknown type");
            descriptionTextView.setText(currentCase.getDescription() != null ? currentCase.getDescription() : "No description provided");
            statusTextView.setText(currentCase.getStatus() != null ? currentCase.getStatus() : "No status available");

            if (currentCase.getUserId() != null) {
                db.collection("users").document(currentCase.getUserId())
                        .get()
                        .addOnSuccessListener(userSnapshot -> {
                            if (userSnapshot.exists()) {
                                String userName = userSnapshot.getString("eMail"); // Adjust to match your Firestore schema
                                userTextView.setText(userName != null ? userName : "Unknown User");
                            } else {
                                userTextView.setText("User not found");
                            }
                        })
                        .addOnFailureListener(e ->
                                userTextView.setText("Error loading user details")
                        );
            } else {
                userTextView.setText("Anonymous");
            }
        }
    }

    public static SingleCaseFragment newInstance(String caseId) {
        SingleCaseFragment fragment = new SingleCaseFragment();
        Bundle args = new Bundle();
        args.putString("CASE_ID", caseId);
        fragment.setArguments(args);
        return fragment;
    }
}