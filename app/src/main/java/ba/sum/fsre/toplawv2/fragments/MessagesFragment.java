package ba.sum.fsre.toplawv2.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Collections;
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
    private Map<String, Message> lastMessagesObjects;
    private Map<String, Integer> unreadCountsMap;

    private UserAdapter adapter;
    private FirebaseFirestore db;
    private Map<String, User> allUsersMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        userListView = view.findViewById(R.id.user_list_view);
        userList = new ArrayList<>();
        userUidMap = new HashMap<>();
        lastMessagesObjects = new HashMap<>();
        unreadCountsMap = new HashMap<>();
        allUsersMap = new HashMap<>();
        db = FirebaseFirestore.getInstance();

        adapter = new UserAdapter(requireContext(), userList, userUidMap, lastMessagesObjects, unreadCountsMap);
        userListView.setAdapter(adapter);

        loadUsers();

        userListView.setOnItemClickListener((AdapterView<?> parent, View itemView, int position, long id) -> {
            User selectedUser = userList.get(position);
            String uid = userUidMap.get(selectedUser);

            // Ažuriraj "last seen" vrijeme
            SharedPreferences prefs = requireContext().getSharedPreferences("chat_prefs", Context.MODE_PRIVATE);
            prefs.edit().putLong("last_seen_" + uid, System.currentTimeMillis()).apply();

            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("receiverId", uid);
            intent.putExtra("receiverName", selectedUser.getFirstName() + " " + selectedUser.getLastName());
            startActivity(intent);
        });

        return view;
    }

    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(querySnapshot -> {
            allUsersMap.clear();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                User user = document.toObject(User.class);
                if (user != null) {
                    String uid = document.getId();
                    allUsersMap.put(uid, user);
                }
            }
            loadLastMessagesReceived();
        });
    }

    private void loadLastMessagesReceived() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences prefs = requireContext().getSharedPreferences("chat_prefs", Context.MODE_PRIVATE);

        db.collection("messages")
                .whereArrayContains("participants", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    lastMessagesObjects.clear();
                    userUidMap.clear();
                    userList.clear();
                    unreadCountsMap.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Message msg = doc.toObject(Message.class);
                        if (msg == null || msg.getParticipants() == null) continue;

                        String otherUserId = null;
                        for (String participant : msg.getParticipants()) {
                            if (!participant.equals(currentUserId)) {
                                otherUserId = participant;
                                break;
                            }
                        }

                        if (otherUserId == null) continue;

                        // Zadnja poruka
                        if (!lastMessagesObjects.containsKey(otherUserId)) {
                            lastMessagesObjects.put(otherUserId, msg);

                            User user = allUsersMap.get(otherUserId);
                            if (user != null) {
                                userList.add(user);
                                userUidMap.put(user, otherUserId);
                            }
                        }

                        // Broj nepročitanih poruka (poruke od drugog korisnika koje su novije od "last seen")
                        long lastSeen = prefs.getLong("last_seen_" + otherUserId, 0);
                        if (msg.getSenderId().equals(otherUserId) && msg.getTimestamp() > lastSeen) {
                            int currentCount = unreadCountsMap.getOrDefault(otherUserId, 0);
                            unreadCountsMap.put(otherUserId, currentCount + 1);
                        }
                    }

                    Collections.sort(userList, (u1, u2) -> {
                        String uid1 = userUidMap.get(u1);
                        String uid2 = userUidMap.get(u2);
                        Message m1 = lastMessagesObjects.get(uid1);
                        Message m2 = lastMessagesObjects.get(uid2);
                        long t1 = (m1 != null) ? m1.getTimestamp() : 0;
                        long t2 = (m2 != null) ? m2.getTimestamp() : 0;
                        return Long.compare(t2, t1);
                    });

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("LastMessage", "Failed to load: " + e.getMessage()));
    }
    @Override
    public void onResume() {
        super.onResume();
        loadLastMessagesReceived();
    }
}
