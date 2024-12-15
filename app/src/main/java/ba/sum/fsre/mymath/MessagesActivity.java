package ba.sum.fsre.mymath;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.mymath.adapters.ConversationAdapter;
import ba.sum.fsre.mymath.models.Conversation;

public class MessagesActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListView conversationListView;
    private List<Conversation> conversations;
    private ConversationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        conversationListView = findViewById(R.id.conversationListView);
        conversations = new ArrayList<>();
        adapter = new ConversationAdapter(this, conversations);
        conversationListView.setAdapter(adapter);

        // Load conversations for the current user
        loadConversations();

        // Navigate to chat when a conversation is clicked
        conversationListView.setOnItemClickListener((parent, view, position, id) -> {
            Conversation selectedConversation = conversations.get(position);
            Intent chatIntent = new Intent(MessagesActivity.this, ChatActivity.class);
            chatIntent.putExtra("receiverId", selectedConversation.getReceiverId());
            chatIntent.putExtra("receiverName", selectedConversation.getReceiverName());
            startActivity(chatIntent);
        });
    }

    private void loadConversations() {
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("messages")
                .whereEqualTo("senderId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    conversations.clear();
                    conversations.addAll(queryDocumentSnapshots.toObjects(Conversation.class));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle errors here
                });
    }
}