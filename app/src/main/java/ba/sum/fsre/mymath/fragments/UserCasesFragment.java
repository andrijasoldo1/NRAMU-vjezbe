package ba.sum.fsre.mymath.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.adapters.CaseAdapter;
import ba.sum.fsre.mymath.models.Case;

public class UserCasesFragment extends Fragment {

    private FirebaseFirestore db;
    private ListView listView;
    private List<Case> userCases;
    private CaseAdapter adapter;

    // Input fields
    private EditText caseNameInput, caseDescriptionInput, casePriceInput, caseTypeInput, caseStatusInput, caseAnonymousInput;
    private Button saveCaseButton, clearFormButton;

    private Case currentEditingCase = null; // Holds the case being edited

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_cases, container, false);

        db = FirebaseFirestore.getInstance();
        listView = view.findViewById(R.id.listView);

        // Input fields
        caseNameInput = view.findViewById(R.id.caseNameInput);
        caseDescriptionInput = view.findViewById(R.id.caseDescriptionInput);
        casePriceInput = view.findViewById(R.id.casePriceInput);
        caseTypeInput = view.findViewById(R.id.caseTypeInput);
        caseStatusInput = view.findViewById(R.id.caseStatusInput);
        caseAnonymousInput = view.findViewById(R.id.caseAnonymousInput);
        saveCaseButton = view.findViewById(R.id.saveCaseButton);
        clearFormButton = view.findViewById(R.id.clearCaseFormButton);

        userCases = new ArrayList<>();
        adapter = new CaseAdapter(requireContext(), userCases, this::populateFormForEditing);
        listView.setAdapter(adapter);

        // Load user's cases
        loadUserCases();

        saveCaseButton.setOnClickListener(v -> saveOrUpdateCase());
        clearFormButton.setOnClickListener(v -> clearForm());

        return view;
    }

    private void loadUserCases() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("cases")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userCases.clear();
                    userCases.addAll(querySnapshot.toObjects(Case.class));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to load cases", Toast.LENGTH_SHORT).show());
    }

    private void saveOrUpdateCase() {
        String caseName = caseNameInput.getText().toString().trim();
        String caseDescription = caseDescriptionInput.getText().toString().trim();
        String casePrice = casePriceInput.getText().toString().trim();
        String caseType = caseTypeInput.getText().toString().trim();
        String caseStatus = caseStatusInput.getText().toString().trim();
        String caseAnonymous = caseAnonymousInput.getText().toString().trim();

        if (TextUtils.isEmpty(caseName) || TextUtils.isEmpty(casePrice) || TextUtils.isEmpty(caseType)) {
            Toast.makeText(requireContext(), "Please fill all mandatory fields", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isAnonymous = Boolean.parseBoolean(caseAnonymous);
        double price = Double.parseDouble(casePrice);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (currentEditingCase == null) {
            Case newCase = new Case(caseName, userId, null, price, caseDescription, null, caseStatus, isAnonymous, caseType);
            db.collection("cases")
                    .add(newCase)
                    .addOnSuccessListener(documentReference -> {
                        newCase.setId(documentReference.getId());
                        userCases.add(newCase);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Case added successfully", Toast.LENGTH_SHORT).show();
                        clearForm();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to add case", Toast.LENGTH_SHORT).show());
        } else {
            currentEditingCase.setName(caseName);
            currentEditingCase.setDescription(caseDescription);
            currentEditingCase.setPrice(price);
            currentEditingCase.setTypeOfCase(caseType);
            currentEditingCase.setStatus(caseStatus);
            currentEditingCase.setAnonymous(isAnonymous);

            db.collection("cases")
                    .document(currentEditingCase.getId())
                    .set(currentEditingCase)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Case updated successfully", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        clearForm();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update case", Toast.LENGTH_SHORT).show());
        }
    }

    private void clearForm() {
        caseNameInput.setText("");
        caseDescriptionInput.setText("");
        casePriceInput.setText("");
        caseTypeInput.setText("");
        caseStatusInput.setText("");
        caseAnonymousInput.setText("");
        currentEditingCase = null;
    }

    private void populateFormForEditing(Case existingCase) {
        currentEditingCase = existingCase;
        caseNameInput.setText(existingCase.getName());
        caseDescriptionInput.setText(existingCase.getDescription());
        casePriceInput.setText(String.valueOf(existingCase.getPrice()));
        caseTypeInput.setText(existingCase.getTypeOfCase());
        caseStatusInput.setText(existingCase.getStatus());
        caseAnonymousInput.setText(String.valueOf(existingCase.isAnonymous()));
    }
}
