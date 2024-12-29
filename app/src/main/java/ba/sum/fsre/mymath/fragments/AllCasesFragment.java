package ba.sum.fsre.mymath.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.adapters.CaseAdapter;
import ba.sum.fsre.mymath.models.Case;

public class AllCasesFragment extends Fragment {

    private FirebaseFirestore db;
    private ListView listView;
    private EditText searchBar;
    private Spinner sortSpinner;
    private List<Case> allCases;
    private List<Case> filteredCases;
    private Map<String, String> userEmails; // Maps userId to eMail
    private CaseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_cases, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        listView = view.findViewById(R.id.listView);
        searchBar = view.findViewById(R.id.search_bar);
        sortSpinner = view.findViewById(R.id.sort_spinner);

        // Initialize Case Lists and Adapter
        allCases = new ArrayList<>();
        filteredCases = new ArrayList<>();
        userEmails = new HashMap<>();

        adapter = new CaseAdapter(requireContext(), filteredCases, null) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);

                // Hide Edit and Delete Buttons
                Button editButton = itemView.findViewById(R.id.edit_button);
                Button deleteButton = itemView.findViewById(R.id.delete_button);
                if (editButton != null) editButton.setVisibility(View.GONE);
                if (deleteButton != null) deleteButton.setVisibility(View.GONE);

                return itemView;
            }
        };

        listView.setAdapter(adapter);

        // Load All Non-Anonymous Cases
        loadAllCases();

        // Add Search Functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndSortCases();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup Sorting Functionality
        setupSortSpinner();

        return view;
    }

    private void loadAllCases() {
        db.collection("cases")
                .whereEqualTo("isAnonymous", false) // Only load non-anonymous cases
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allCases.clear();
                    filteredCases.clear();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Case c = document.toObject(Case.class);
                        if (c != null) {
                            allCases.add(c);
                            fetchUserEmail(c.getUserId());
                        }
                    }

                    filteredCases.addAll(allCases);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load cases: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void fetchUserEmail(String userId) {
        if (userId != null && !userEmails.containsKey(userId)) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String eMail = documentSnapshot.getString("eMail");
                            userEmails.put(userId, eMail != null ? eMail.toLowerCase() : "unknown");
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed to fetch email for userId: " + userId, Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void setupSortSpinner() {
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Alphabetical (A-Z)", "Alphabetical (Z-A)", "Newest First", "Oldest First"});
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSortCases();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterAndSortCases() {
        String query = searchBar.getText().toString().toLowerCase();
        String selectedSort = (String) sortSpinner.getSelectedItem();

        filteredCases.clear();

        for (Case c : allCases) {
            // Retrieve "createdBy" email from the userEmails map
            String createdBy = userEmails.getOrDefault(c.getUserId(), "unknown");

            // Check all searchable fields
            if (c.getName().toLowerCase().contains(query) ||
                    c.getStatus().toLowerCase().contains(query) ||
                    c.getTypeOfCase().toLowerCase().contains(query) ||
                    createdBy.contains(query)) {
                filteredCases.add(c);
            }
        }

        // Sort cases based on the selected criteria
        switch (selectedSort) {
            case "Alphabetical (A-Z)":
                Collections.sort(filteredCases, Comparator.comparing(Case::getName, String::compareToIgnoreCase));
                break;
            case "Alphabetical (Z-A)":
                Collections.sort(filteredCases, (case1, case2) -> case2.getName().compareToIgnoreCase(case1.getName()));
                break;
            case "Newest First":
                // Keep the original order as Firestore returns the newest documents first
                break;
            case "Oldest First":
                // Reverse the list to show oldest first
                Collections.reverse(filteredCases);
                break;
        }

        adapter.notifyDataSetChanged();
    }
}
