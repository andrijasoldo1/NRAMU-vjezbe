package ba.sum.fsre.toplawv2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ba.sum.fsre.toplawv2.ChatActivity;
import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.adapters.UserAdapter;
import ba.sum.fsre.toplawv2.models.Message;
import ba.sum.fsre.toplawv2.models.User;

public class MessagesFragment extends Fragment {

    private ListView userListView;
    private List<User> userList;
    private Map<User, String> userUidMap;
    private Map<String, String> lastMessagesMap;
    private UserAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        userListView = view.findViewById(R.id.user_list_view);
        userList = new ArrayList<>();
        userUidMap = new HashMap<>();
        lastMessagesMap = new HashMap<>();
        db = FirebaseFirestore.getInstance();

        adapter = new UserAdapter(requireContext(), userList, userUidMap, lastMessagesMap);
        userListView.setAdapter(adapter);

        loadUsers();

        userListView.setOnItemClickListener((AdapterView<?> parent, View itemView, int position, long id) -> {
            User selectedUser = userList.get(position);
            String uid = userUidMap.get(selectedUser);
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
                    String uid = document.getId();
                    userList.add(user);
                    userUidMap.put(user, uid);
                }
            }
            adapter.notifyDataSetChanged(); // show names
            loadLastMessagesReceived(); // show previews after
        });
    }

    private void loadLastMessagesReceived() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("messages")
                .whereArrayContains("participants", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING) // obrnut redoslijed
                .limit(100)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Message msg = doc.toObject(Message.class);
                        if (msg == null) continue;

                        // Želimo samo poruke koje je KORISNIK PRIMIO
                        if (!currentUserId.equals(msg.getReceiverId())) continue;

                        String senderId = msg.getSenderId();
                        if (!lastMessagesMap.containsKey(senderId)) {
                            String preview = (msg.getText() != null && !msg.getText().isEmpty())
                                    ? msg.getText() : "[Medijska poruka]";
                            lastMessagesMap.put(senderId, preview);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("LastMessage", "Failed to load: " + e.getMessage()));
    }


}
