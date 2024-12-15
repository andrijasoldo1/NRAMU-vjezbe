package ba.sum.fsre.mymath.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.adapters.MessageRecyclerAdapter;
import ba.sum.fsre.mymath.models.Message;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;

    private FirebaseFirestore db;
    private CollectionReference messagesCollection;
    private String currentUserId;
    private String receiverId;

    private List<Message> messages;
    private MessageRecyclerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Assume `receiverId` is passed as an argument to the fragment
        if (getArguments() != null) {
            receiverId = getArguments().getString("receiverId");
        } else {
            Toast.makeText(getContext(), "Receiver not specified.", Toast.LENGTH_SHORT).show();
            return view;
        }

        messagesCollection = db.collection("messages");

        // Initialize UI
        recyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);

        messages = new ArrayList<>();
        adapter = new MessageRecyclerAdapter(getContext(), messages, currentUserId);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Load messages
        loadMessages();

        // Send message
        sendButton.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void loadMessages() {
        messagesCollection
                .whereArrayContains("participants", currentUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        messages.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            if (message != null && message.getParticipants().contains(receiverId)) {
                                messages.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();

        if (TextUtils.isEmpty(text)) {
            return;
        }

        Message message = new Message(currentUserId, receiverId, text, null, System.currentTimeMillis());
        messagesCollection.add(message).addOnSuccessListener(documentReference -> {
            messageInput.setText(""); // Clear input field
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to send message.", Toast.LENGTH_SHORT).show();
        });
    }
}