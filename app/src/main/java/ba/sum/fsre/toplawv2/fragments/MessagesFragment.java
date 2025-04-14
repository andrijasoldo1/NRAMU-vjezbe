package ba.sum.fsre.toplawv2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ba.sum.fsre.toplawv2.ChatActivity;
import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.adapters.UserAdapter;
import ba.sum.fsre.toplawv2.models.User;

public class MessagesFragment extends Fragment {

    private ListView userListView;
    private List<User> userList;
    private Map<User, String> userUidMap; // Maps User to their UID
    private UserAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        userListView = view.findViewById(R.id.user_list_view);
        userList = new ArrayList<>();
        userUidMap = new HashMap<>();
        adapter = new UserAdapter(requireContext(), userList);
        userListView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Load users from Firestore
        loadUsers();

        // Handle user selection
        userListView.setOnItemClickListener((AdapterView<?> parent, View itemView, int position, long id) -> {
            User selectedUser = userList.get(position);

            // Get the UID for the selected user
            String uid = userUidMap.get(selectedUser);

            // Start ChatActivity with the selected user's ID
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("receiverId", uid);
            intent.putExtra("receiverName", selectedUser.getFirstName() + " " + selectedUser.getLastName());
            startActivity(intent);
        });

        return view;
    }

    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(querySnapshot -> {
            userList.clear();
            userUidMap.clear();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                User user = document.toObject(User.class);
                if (user != null) {
                    userList.add(user);
                    userUidMap.put(user, document.getId()); // Store the document ID as the UID
                }
            }
            adapter.notifyDataSetChanged();
        });
    }
}