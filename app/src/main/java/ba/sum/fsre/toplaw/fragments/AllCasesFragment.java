package ba.sum.fsre.toplaw.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import ba.sum.fsre.toplaw.R;
import ba.sum.fsre.toplaw.adapters.AllCaseAdapter;
import ba.sum.fsre.toplaw.adapters.BaseCaseAdapter;
import ba.sum.fsre.toplaw.models.Case;

public class AllCasesFragment extends Fragment {

    private FirebaseFirestore db;
    private ListView listView;

    private LinearLayout filtersLayout;
    private EditText searchBar;
    private Spinner sortSpinner;
    private Spinner statusSpinner;
    private Spinner expertiseSpinner;
    private List<Case> allCases;
    private List<Case> filteredCases;
    private Map<String, String> userEmails; // Maps userId to eMail
    private BaseCaseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_cases, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        listView = view.findViewById(R.id.list_item_all_cases);
        searchBar = view.findViewById(R.id.search_bar);
        sortSpinner = view.findViewById(R.id.sort_spinner);
        statusSpinner = view.findViewById(R.id.status_spinner);
        expertiseSpinner = view.findViewById(R.id.expertise_spinner);
        filtersLayout = view.findViewById(R.id.filters_layout);

        // Setup Filters Toggle Button
        ImageButton filterButton = view.findViewById(R.id.filter_button);
        filterButton.setOnClickListener(v -> toggleFilters());
        // Initialize Case Lists and Adapter
        allCases = new ArrayList<>();
        filteredCases = new ArrayList<>();
        userEmails = new HashMap<>();

        adapter = new AllCaseAdapter(requireContext(), filteredCases);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            Case selectedCase = filteredCases.get(position);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, SingleCaseFragment.newInstance(selectedCase.getId()))
                    .addToBackStack(null)
                    .commit();
        });


        // Load All Non-Anonymous Cases
        loadAllCases();

        // Setup Filters and Sorting
        setupStatusSpinner();
        setupExpertiseSpinner();
        setupSortSpinner();

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

        return view;
    }



    private void toggleFilters() {
        if (filtersLayout.getVisibility() == View.GONE) {
            // Show filters with animation
            filtersLayout.setVisibility(View.VISIBLE);
            filtersLayout.animate()
                    .alpha(1.0f)
                    .setDuration(300)
                    .start();
        } else {
            // Hide filters with animation
            filtersLayout.animate()
                    .alpha(0.0f)
                    .setDuration(200)
                    .withEndAction(() -> filtersLayout.setVisibility(View.GONE))
                    .start();
        }
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

    private void setupStatusSpinner() {
        List<String> statusList = new ArrayList<>();
        statusList.add("All");
        statusList.add("Open");
        statusList.add("In Progress");
        statusList.add("Closed");
        statusList.add("Archived");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, statusList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSortCases();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupExpertiseSpinner() {
        List<String> expertiseList = new ArrayList<>();
        expertiseList.add("All");
        expertiseList.add("Kazneno pravo");
        expertiseList.add("Građansko pravo");
        expertiseList.add("Trgovačko pravo");
        expertiseList.add("Upravno pravo");
        expertiseList.add("Radno pravo");
        expertiseList.add("Obiteljsko pravo");
        expertiseList.add("Nekretninsko pravo");
        expertiseList.add("Intelektualno vlasništvo");
        expertiseList.add("Međunarodno privatno pravo");
        expertiseList.add("Ovršno pravo");

        ArrayAdapter<String> expertiseAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, expertiseList);
        expertiseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expertiseSpinner.setAdapter(expertiseAdapter);

        expertiseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSortCases();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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
        String selectedStatus = (String) statusSpinner.getSelectedItem();
        String selectedExpertise = (String) expertiseSpinner.getSelectedItem();
        String selectedSort = (String) sortSpinner.getSelectedItem();

        filteredCases.clear();

        for (Case c : allCases) {
            String createdBy = userEmails.getOrDefault(c.getUserId(), "unknown");

            boolean matchesQuery = c.getName().toLowerCase().contains(query) ||
                    c.getStatus().toLowerCase().contains(query) ||
                    c.getTypeOfCase().toLowerCase().contains(query) ||
                    createdBy.contains(query);

            boolean matchesStatus = selectedStatus.equals("All") || c.getStatus().equalsIgnoreCase(selectedStatus);
            boolean matchesExpertise = selectedExpertise.equals("All") || c.getTypeOfCase().equalsIgnoreCase(selectedExpertise);

            if (matchesQuery && matchesStatus && matchesExpertise) {
                filteredCases.add(c);
            }
        }

        switch (selectedSort) {
            case "Alphabetical (A-Z)":
                Collections.sort(filteredCases, Comparator.comparing(Case::getName, String::compareToIgnoreCase));
                break;
            case "Alphabetical (Z-A)":
                Collections.sort(filteredCases, (case1, case2) -> case2.getName().compareToIgnoreCase(case1.getName()));
                break;
            case "Newest First":
                break;
            case "Oldest First":
                Collections.reverse(filteredCases);
                break;
        }

        adapter.notifyDataSetChanged();
    }
}