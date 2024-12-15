package ba.sum.fsre.mymath;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.mymath.adapters.MessageRecyclerAdapter;
import ba.sum.fsre.mymath.models.Message;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;

    private FirebaseFirestore db;
    private CollectionReference messagesCollection;
    private String currentUserId;
    private String receiverId;

    private List<Message> messages;
    private MessageRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("receiverId");

        if (receiverId == null) {
            Toast.makeText(this, "Receiver not specified.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messagesCollection = db.collection("messages");

        // Initialize UI
        recyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        messages = new ArrayList<>();
        adapter = new MessageRecyclerAdapter(this, messages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load messages
        loadMessages();

        // Send message
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        messagesCollection
                .whereArrayContains("participants", currentUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (TextUtils.isEmpty(text)) return;

        Message message = new Message(currentUserId, receiverId, text, null, System.currentTimeMillis());
        messagesCollection.add(message).addOnSuccessListener(documentReference -> {
            messageInput.setText(""); // Clear input
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }
}
