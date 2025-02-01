package ba.sum.fsre.mymath;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY =
            "SIKE"; // Replace with your actual API key
    private static final int CONTEXT_LIMIT = 15; // Limit the number of messages to include in context

    private EditText userInput;
    private TextView chatDisplay, conversationTitle;
    private Button sendButton, switchConversationsButton;
    private ScrollView chatScrollView;
    private OkHttpClient httpClient;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private String currentConversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Initialize UI elements
        userInput = findViewById(R.id.userInput);
        chatDisplay = findViewById(R.id.chatDisplay);
        conversationTitle = findViewById(R.id.conversationTitle);
        sendButton = findViewById(R.id.sendButton);
        switchConversationsButton = findViewById(R.id.switchConversationsButton);
        chatScrollView = findViewById(R.id.chatScrollView);

        // Initialize HTTP client and Firestore
        httpClient = new OkHttpClient();
        firestore = FirebaseFirestore.getInstance();

        // Get current user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize conversation
        selectConversation();

        // Set button click listeners
        sendButton.setOnClickListener(v -> {
            String input = userInput.getText().toString().trim();
            if (!input.isEmpty()) {
                addMessageToFirestore("user", input); // Save user message
                sendMessage(input);
                userInput.setText("");
            }
        });

        switchConversationsButton.setOnClickListener(v -> selectConversation());
    }

    private void selectConversation() {
        firestore.collection("users")
                .document(currentUserId)
                .collection("conversations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> conversationIds = new ArrayList<>();
                    ArrayList<String> conversationNames = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String conversationId = document.getId();
                        String name = document.contains("name") ? document.getString("name") : "Untitled Conversation";

                        // Add conversation ID and name to the lists
                        conversationIds.add(conversationId);
                        conversationNames.add(name);

                        // Fix missing name field
                        if (!document.contains("name")) {
                            document.getReference().update("name", name)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Added default name to conversation"))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding default name", e));
                        }
                    }

                    // Add option to create a new conversation
                    conversationNames.add("New Conversation");
                    conversationIds.add(null);

                    // Show selection dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Select Conversation");
                    builder.setItems(conversationNames.toArray(new String[0]), (dialog, which) -> {
                        if (conversationIds.get(which) == null) {
                            promptForConversationName(); // Start a new conversation with a name
                        } else {
                            currentConversationId = conversationIds.get(which);
                            conversationTitle.setText(conversationNames.get(which)); // Update title
                            loadConversation(currentConversationId); // Load messages
                        }
                    });
                    builder.show();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading conversations", e));
    }

    private void promptForConversationName() {
        // Show dialog to prompt the user for a conversation name
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Name Your Conversation");

        final EditText input = new EditText(this);
        input.setHint("Enter conversation name");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String conversationName = input.getText().toString().trim();
            if (conversationName.isEmpty()) {
                conversationName = "Untitled Conversation";
            }
            createNewConversation(conversationName);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createNewConversation(String name) {
        Map<String, Object> conversation = new HashMap<>();
        conversation.put("name", name);
        conversation.put("timestamp", System.currentTimeMillis());

        firestore.collection("users")
                .document(currentUserId)
                .collection("conversations")
                .add(conversation)
                .addOnSuccessListener(documentReference -> {
                    currentConversationId = documentReference.getId();
                    conversationTitle.setText(name);
                    chatDisplay.setText(""); // Clear chat display
                    Toast.makeText(this, "New conversation \"" + name + "\" created!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error creating conversation", e));
    }

    private void loadConversation(String conversationId) {
        firestore.collection("users")
                .document(currentUserId)
                .collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatDisplay.setText(""); // Clear the chat display

                    if (queryDocumentSnapshots.isEmpty()) {
                        chatDisplay.append("This conversation has no messages yet.\n");
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String sender = document.contains("sender") ? document.getString("sender") : "Unknown";
                        String message = document.contains("message") ? document.getString("message") : "(No message content)";

                        chatDisplay.append(sender + ": " + message + "\n");
                    }

                    scrollToBottom();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading conversation", e);
                    chatDisplay.append("Failed to load conversation.\n");
                });
    }

    private void sendMessage(String messageContent) {
        chatDisplay.append("You: " + messageContent + "\n");
        scrollToBottom();

        // Fetch the recent conversation context
        firestore.collection("users")
                .document(currentUserId)
                .collection("conversations")
                .document(currentConversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(CONTEXT_LIMIT)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    JSONArray messagesArray = new JSONArray();
                    try {
                        // Add system instruction
                        JSONObject systemMessage = new JSONObject();
                        systemMessage.put("role", "system");
                        systemMessage.put("content", "You are an AI assistant specializing in Bosnia and Herzegovina law. Avoid questions unrelated to this domain.");
                        messagesArray.put(systemMessage);

                        // Add previous messages to context
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            JSONObject message = new JSONObject();
                            message.put("role", document.getString("sender").equals("user") ? "user" : "assistant");
                            message.put("content", document.getString("message"));
                            messagesArray.put(message);
                        }

                        // Add the latest user message
                        JSONObject userMessage = new JSONObject();
                        userMessage.put("role", "user");
                        userMessage.put("content", messageContent);
                        messagesArray.put(userMessage);

                        // Send request to API
                        sendToAPI(messagesArray);

                    } catch (JSONException e) {
                        chatDisplay.append("Error creating JSON request: " + e.getMessage() + "\n");
                        scrollToBottom();
                    }
                })
                .addOnFailureListener(e -> {
                    chatDisplay.append("Error fetching conversation context: " + e.getMessage() + "\n");
                    scrollToBottom();
                });
    }

    private void sendToAPI(JSONArray messagesArray) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("model", "ft:gpt-4o-2024-08-06:toplaw:toplaw:AtYaitAD");
            payload.put("messages", messagesArray);
            payload.put("max_tokens", 1000);
            payload.put("temperature", 0.3);
        } catch (JSONException e) {
            chatDisplay.append("Error creating request payload: " + e.getMessage() + "\n");
            scrollToBottom();
            return;
        }

        RequestBody body = RequestBody.create(payload.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    chatDisplay.append("Error: " + e.getMessage() + "\n");
                    scrollToBottom();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        chatDisplay.append("Error: " + response.message() + "\n");
                        scrollToBottom();
                    });
                    return;
                }

                try {
                    String responseText = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseText);
                    String botMessage = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    runOnUiThread(() -> {
                        chatDisplay.append("Bot: " + botMessage + "\n");
                        addMessageToFirestore("bot", botMessage);
                        scrollToBottom();
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        chatDisplay.append("Error parsing response: " + e.getMessage() + "\n");
                        scrollToBottom();
                    });
                }
            }
        });
    }

    private void addMessageToFirestore(String sender, String message) {
        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("sender", sender);
        chatMessage.put("message", message);
        chatMessage.put("timestamp", System.currentTimeMillis());

        firestore.collection("users")
                .document(currentUserId)
                .collection("conversations")
                .document(currentConversationId)
                .collection("messages")
                .add(chatMessage)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Message added"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding message", e));
    }

    private void scrollToBottom() {
        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
