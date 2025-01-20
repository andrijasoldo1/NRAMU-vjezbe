package ba.sum.fsre.mymath;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatbotActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "tralalalalalalalalalalalalalalalalalalal"; // Replace with your actual API key

    private EditText userInput;
    private TextView chatDisplay;
    private Button sendButton;
    private ScrollView chatScrollView;
    private OkHttpClient httpClient;
    private FirebaseFirestore firestore;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Initialize UI elements
        userInput = findViewById(R.id.userInput);
        chatDisplay = findViewById(R.id.chatDisplay);
        sendButton = findViewById(R.id.sendButton);
        chatScrollView = findViewById(R.id.chatScrollView);

        // Initialize HTTP client and Firestore
        httpClient = new OkHttpClient();
        firestore = FirebaseFirestore.getInstance();

        // Get current user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId != null) {
            loadConversation(); // Load user-specific conversation
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set button click listener
        sendButton.setOnClickListener(v -> {
            String input = userInput.getText().toString().trim();
            if (!input.isEmpty()) {
                addMessageToFirestore("user", input); // Save user message
                sendMessage(input);
                userInput.setText("");
            }
        });
    }

    private void sendMessage(String message) {
        chatDisplay.append("You: " + message + "\n");
        scrollToBottom();

        // Create JSON payload
        JSONObject payload = new JSONObject();
        try {
            payload.put("model", "ft:gpt-4o-2024-08-06:toplaw:toplaw:ArEPEMdK"); // Your fine-tuned model name

            JSONArray messagesArray = new JSONArray();

            // Add system-level instruction
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a legal assistant specializing in answering law-related questions. " +
                    "Do not answer any questions outside of the legal domain.");
            messagesArray.put(systemMessage);

            // Add user message
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messagesArray.put(userMessage);

            payload.put("messages", messagesArray);
        } catch (JSONException e) {
            e.printStackTrace();
            chatDisplay.append("Error creating request payload: " + e.getMessage() + "\n");
            scrollToBottom();
            return;
        }

        // Create request body
        RequestBody body = RequestBody.create(payload.toString(), MediaType.get("application/json; charset=utf-8"));

        // Build request
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        // Execute HTTP request
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
                if (response.isSuccessful()) {
                    String responseText = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseText);

                        // Extract bot message
                        String botMessage = jsonResponse.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        runOnUiThread(() -> {
                            chatDisplay.append("Bot: " + botMessage + "\n");
                            addMessageToFirestore("bot", botMessage); // Save bot response
                            scrollToBottom();
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            chatDisplay.append("Error parsing response: " + e.getMessage() + "\n");
                            scrollToBottom();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        chatDisplay.append("Error: " + response.message() + "\n");
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
                .add(chatMessage)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Message added"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding message", e));
    }

    private void loadConversation() {
        firestore.collection("users")
                .document(currentUserId)
                .collection("conversations")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String sender = document.getString("sender");
                        String message = document.getString("message");
                        chatDisplay.append(sender + ": " + message + "\n");
                    }
                    scrollToBottom();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading conversation", e));
    }

    private void scrollToBottom() {
        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
