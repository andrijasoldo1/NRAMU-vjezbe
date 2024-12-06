package ba.sum.fsre.mymath.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.adapters.CaseAdapter;
import ba.sum.fsre.mymath.models.Case;

public class AllCasesFragment extends Fragment {

    private FirebaseFirestore db;
    private ListView listView;
    private List<Case> allCases;
    private CaseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_cases, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        listView = view.findViewById(R.id.listView);

        // Initialize Case List and Adapter
        allCases = new ArrayList<>();
        adapter = new CaseAdapter(requireContext(), allCases, caseToEdit -> {
            // Editing is not available in All Cases view
            Toast.makeText(requireContext(), "Editing cases is not allowed in All Cases view.", Toast.LENGTH_SHORT).show();
        }) {
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

        return view;
    }

    private void loadAllCases() {
        db.collection("cases")
                .whereEqualTo("isAnonymous", false) // Only load non-anonymous cases
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allCases.clear();
                    allCases.addAll(querySnapshot.toObjects(Case.class));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load cases: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
