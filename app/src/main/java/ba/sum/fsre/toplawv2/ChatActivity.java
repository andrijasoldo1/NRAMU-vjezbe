package ba.sum.fsre.toplawv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.toplawv2.adapters.MessageRecyclerAdapter;
import ba.sum.fsre.toplawv2.models.Message;

public class ChatActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton, attachButton;

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

        // Firebase
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("receiverId");

        if (receiverId == null) {
            Toast.makeText(this, "Receiver not specified.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messagesCollection = db.collection("messages");

        // UI
        recyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        attachButton = findViewById(R.id.attachButton);

        messages = new ArrayList<>();
        adapter = new MessageRecyclerAdapter(this, messages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load existing messages
        loadMessages();

        // Send text message
        sendButton.setOnClickListener(v -> sendMessage());

        // Attach file
        attachButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");

            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_FILE_REQUEST);
        });
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
            messageInput.setText("");
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }

    private void sendBase64File(String base64Data) {
        Message message = new Message(currentUserId, receiverId, null, base64Data, System.currentTimeMillis());
        messagesCollection.add(message)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "File sent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send file", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                sendBase64File(base64);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
