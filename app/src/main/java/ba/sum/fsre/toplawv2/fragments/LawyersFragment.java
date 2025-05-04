package ba.sum.fsre.toplawv2.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.adapters.LawyerListAdapter;
import ba.sum.fsre.toplawv2.models.User;

public class LawyersFragment extends Fragment {

    private FirebaseFirestore db;
    private ListView listView;
    private List<User> allLawyers;
    private List<User> filteredLawyers;
    private EditText searchBar;
    private Spinner sortSpinner, expertiseSpinner;
    private LinearLayout filtersLayout;
    private LawyerListAdapter adapter;
    private final Map<String, Float> ratingMap = new HashMap<>(); // New rating map

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lawyers, container, false);

        db = FirebaseFirestore.getInstance();
        listView = view.findViewById(R.id.lv_recommended_jobs);
        searchBar = view.findViewById(R.id.et_job_search);
        sortSpinner = view.findViewById(R.id.spinner_job_sort);
        expertiseSpinner = view.findViewById(R.id.spinner_job_field);
        filtersLayout = view.findViewById(R.id.ll_job_filters);

        ImageButton filterButton = view.findViewById(R.id.btn_filter_jobs);
        filterButton.setOnClickListener(v -> toggleFilters());

        allLawyers = new ArrayList<>();
        filteredLawyers = new ArrayList<>();

        adapter = new LawyerListAdapter(requireContext(), filteredLawyers, ratingMap);
        listView.setAdapter(adapter);

        loadLawyers();
        setupExpertiseSpinner();
        setupSortSpinner();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndSortLawyers();
            }
        });

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            // Get the lawyer email from the clicked item
            User selectedLawyer = filteredLawyers.get(position);
            String lawyerEmail = selectedLawyer.geteMail();

            // Create the LawyerPageFragment and pass the email as an argument
            LawyerPageFragment lawyerPageFragment = LawyerPageFragment.newInstance(lawyerEmail);

            // Replace current fragment with the LawyerPageFragment
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, lawyerPageFragment);
            transaction.addToBackStack(null);  // Optional: to add this transaction to the back stack
            transaction.commit();
        });


        return view;
    }

    private void toggleFilters() {
        if (filtersLayout.getVisibility() == View.GONE) {
            filtersLayout.setVisibility(View.VISIBLE);
            filtersLayout.animate().alpha(1.0f).setDuration(300).start();
        } else {
            filtersLayout.animate().alpha(0.0f).setDuration(200).withEndAction(() -> filtersLayout.setVisibility(View.GONE)).start();
        }
    }

    private void loadLawyers() {
        db.collection("users")
                .whereEqualTo("isApproved", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allLawyers.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            allLawyers.add(user);
                        }
                    }
                    loadReviewsAndApply(); // Fetch reviews after loading users
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load lawyers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadReviewsAndApply() {
        db.collection("reviews")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, List<Float>> temp = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String email = doc.getString("lawyerEmail");
                        Number ratingNum = doc.getDouble("rating");
                        if (email != null && ratingNum != null) {
                            temp.computeIfAbsent(email, k -> new ArrayList<>()).add(ratingNum.floatValue());
                        }
                    }

                    ratingMap.clear();
                    for (Map.Entry<String, List<Float>> entry : temp.entrySet()) {
                        List<Float> ratings = entry.getValue();
                        float average = 0f;
                        if (!ratings.isEmpty()) {
                            float sum = 0f;
                            for (float r : ratings) sum += r;
                            average = sum / ratings.size();
                        }
                        ratingMap.put(entry.getKey(), average);
                    }

                    filteredLawyers.clear();
                    filteredLawyers.addAll(allLawyers);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("LawyersFragment", "Failed to load reviews", e));
    }

    private void setupExpertiseSpinner() {
        List<String> expertiseList = Arrays.asList("All", "Kazneno pravo", "Građansko pravo", "Trgovačko pravo",
                "Upravno pravo", "Radno pravo", "Obiteljsko pravo", "Nekretninsko pravo", "Intelektualno vlasništvo",
                "Međunarodno privatno pravo", "Ovršno pravo");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, expertiseList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expertiseSpinner.setAdapter(adapter);
        expertiseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSortLawyers();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"Alphabetical (A-Z)", "Alphabetical (Z-A)"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSortLawyers();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterAndSortLawyers() {
        String query = searchBar.getText().toString().toLowerCase();
        String expertise = (String) expertiseSpinner.getSelectedItem();
        String sort = (String) sortSpinner.getSelectedItem();

        filteredLawyers.clear();

        for (User user : allLawyers) {
            String userExpertise = user.getAreaOfExpertise();

            boolean matchesQuery = user.getFirstName().toLowerCase().contains(query)
                    || user.getLastName().toLowerCase().contains(query)
                    || (userExpertise != null && userExpertise.toLowerCase().contains(query));

            boolean matchesExpertise = expertise.equals("All") ||
                    (userExpertise != null && userExpertise.equalsIgnoreCase(expertise));

            if (matchesQuery && matchesExpertise) {
                filteredLawyers.add(user);
            }
        }

        if (sort.equals("Alphabetical (A-Z)")) {
            filteredLawyers.sort(Comparator.comparing(u -> u.getFirstName() + " " + u.getLastName()));
        } else if (sort.equals("Alphabetical (Z-A)")) {
            filteredLawyers.sort((u1, u2) -> (u2.getFirstName() + " " + u2.getLastName())
                    .compareToIgnoreCase(u1.getFirstName() + " " + u1.getLastName()));
        }

        adapter.notifyDataSetChanged();
    }
}

