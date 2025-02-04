package ba.sum.fsre.toplaw.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.toplaw.R;
import ba.sum.fsre.toplaw.adapters.CaseAdapter;
import ba.sum.fsre.toplaw.models.Case;

public class ReceivedCasesFragment extends Fragment {

    private FirebaseFirestore db;
    private List<Case> receivedCases;
    private CaseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_received_cases, container, false);
        ListView listView = view.findViewById(R.id.receivedCasesList);

        db = FirebaseFirestore.getInstance();
        receivedCases = new ArrayList<>();

        // Pass false to hide buttons
        adapter = new CaseAdapter(requireContext(), receivedCases, null, false);
        listView.setAdapter(adapter);

        loadReceivedCases();

        return view;
    }

    private void loadReceivedCases() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collectionGroup("shared_cases")
                .whereEqualTo("sentToUserId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("ReceivedCasesDebug", "Query returned " + querySnapshot.size() + " documents.");
                    receivedCases.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String caseId = document.getString("caseId");
                        if (caseId != null) {
                            fetchCaseDetails(caseId, receivedCases, adapter);
                        } else {
                            Log.e("ReceivedCasesError", "Missing caseId in shared_cases document.");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ReceivedCasesError", e.getMessage());
                    Toast.makeText(requireContext(), "Failed to load received cases: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchCaseDetails(String caseId, List<Case> caseList, CaseAdapter adapter) {
        db.collection("cases")
                .document(caseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Case aCase = documentSnapshot.toObject(Case.class);
                        if (aCase != null) {
                            caseList.add(aCase);
                            adapter.notifyDataSetChanged();
                            Log.d("FetchCaseDetails", "Case added: " + aCase.getName());
                        } else {
                            Log.e("FetchCaseDetailsError", "Failed to parse case: " + caseId);
                        }
                    } else {
                        Log.e("FetchCaseDetailsError", "Case not found: " + caseId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchCaseDetailsError", e.getMessage());
                });
    }
}
