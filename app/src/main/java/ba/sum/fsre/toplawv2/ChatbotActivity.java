package ba.sum.fsre.toplawv2;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotActivity extends AppCompatActivity  {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "blablablablabla";
    private static final int CONTEXT_LIMIT = 15;

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


        userInput = findViewById(R.id.userInput);
        chatDisplay = findViewById(R.id.chatDisplay);
        conversationTitle = findViewById(R.id.conversationTitle);
        sendButton = findViewById(R.id.sendButton);
        switchConversationsButton = findViewById(R.id.switchConversationsButton);
        chatScrollView = findViewById(R.id.chatScrollView);
        Button generateDocumentButton = findViewById(R.id.generateDocumentButton);


        httpClient = new OkHttpClient();
        firestore = FirebaseFirestore.getInstance();


        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        selectConversation();


        sendButton.setOnClickListener(v -> {
            String input = userInput.getText().toString().trim();
            if (!input.isEmpty()) {
                addMessageToFirestore("user", input); // Save user message
                sendMessage(input);
                userInput.setText("");
            }
        });

        switchConversationsButton.setOnClickListener(v -> selectConversation());


        generateDocumentButton.setOnClickListener(v -> showSignatureDialog());

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

    private void generateLegalDocument() {
        if (currentConversationId == null) {
            Toast.makeText(this, "Nema odabrane konverzacije.", Toast.LENGTH_SHORT).show();
            return;
        }

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
                        // ➕ Izvuci ključne informacije iz poruka
                        String extractedFacts = extractFactsFromMessages(queryDocumentSnapshots);

                        // 🧠 Dodaj naprednu system poruku
                        JSONObject systemMessage = new JSONObject();
                        systemMessage.put("role", "system");
                        systemMessage.put("content",
                                "Generiraj pravni dokument temeljen na razgovoru između korisnika i AI-a. " +
                                        "Izvuci konkretne činjenice iz poruka korisnika kao što su ime osumnjičenog, mjesto događaja, datumi itd. " +
                                        "Ne koristi zamjenske izraze poput [ime], [datum] ili [mjesto], već koristi stvarne vrijednosti ako su dostupne. " +
                                        "Formatiraj dokument u klasičnom pravnom stilu: NASLOV (velikim slovima, centriran), podnaslovi podebljani (npr. Uvod, Činjenični opis, Pravni temelj, Zahtjev), " +
                                        "odlomci trebaju biti čitljivi i bez oznaka poput **zvjezdica**." +
                                        "\n\nDostupne činjenice iz razgovora:\n" + extractedFacts
                        );
                        messagesArray.put(systemMessage);

                        // ➕ Dodaj sve prethodne poruke
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            JSONObject message = new JSONObject();
                            String role = "assistant".equals(document.getString("sender")) ? "assistant" : "user";
                            message.put("role", role);
                            message.put("content", document.getString("message"));
                            messagesArray.put(message);
                        }

                        // 🔄 Nastavi prema API pozivu
                        requestDocumentFromOpenAI(messagesArray);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Greška kod generiranja JSON zahtjeva", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Neuspjelo dohvaćanje poruka iz Firestore-a", Toast.LENGTH_SHORT).show();
                });
    }

    private void requestDocumentFromOpenAI(JSONArray messagesArray) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("model", "ft:gpt-4o-2024-08-06:toplaw:toplaw:AtYaitAD");
            payload.put("messages", messagesArray);
            payload.put("max_tokens", 1000);
            payload.put("temperature", 0.3);
        } catch (JSONException e) {
            runOnUiThread(() -> Toast.makeText(this, "Greška kod kreiranja zahtjeva", Toast.LENGTH_SHORT).show());
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
                runOnUiThread(() -> Toast.makeText(ChatbotActivity.this, "Greška pri komunikaciji s OpenAI", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ChatbotActivity.this, "Neuspješan odgovor AI-a", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String responseText = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseText);
                    String documentContent = jsonResponse
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    createPdfFromText(documentContent);

                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(ChatbotActivity.this, "Greška kod parsiranja AI odgovora", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void createPdfFromText(String content) {
        content = content.replaceAll("\\*\\*(.*?)\\*\\*", "$1");

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        paint.setTextSize(12);
        paint.setColor(Color.BLACK);

        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40;
        int x = margin;
        int y = margin + 20;

        int lineHeight = (int) (paint.descent() - paint.ascent()) + 8;
        int usableWidth = pageWidth - 2 * margin;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        String[] lines = content.split("\n");
        for (String line : lines) {
            List<String> wrappedLines = wrapText(line, paint, usableWidth);
            for (String wrappedLine : wrappedLines) {
                if (y + lineHeight > pageHeight - margin - 40) {
                    pdfDocument.finishPage(page);

                    // Nova stranica
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = margin + 20;
                }

                canvas.drawText(wrappedLine, x, y, paint);
                y += lineHeight;
            }
            y += 6;
        }

        // ✅ Na kraju teksta, dodaj liniju za potpis i potpis sliku (na istoj stranici)
        // ✅ Na kraju teksta, dodaj liniju za potpis i potpis sliku (na istoj stranici ili na novoj)
        if (y + lineHeight + 60 < pageHeight - margin) {
            y += 40;

            // 1. Crtaj tekst "Potpis:"
            canvas.drawText("Potpis:", x, y, paint);

            // 2. Umetni potpis
            File signatureFile = new File(getFilesDir(), "user_signature.png");
            if (signatureFile.exists()) {
                Bitmap signature = BitmapFactory.decodeFile(signatureFile.getAbsolutePath());
                if (signature != null) {
                    int signatureWidth = 150;
                    int signatureHeight = 50;
                    Bitmap scaled = Bitmap.createScaledBitmap(signature, signatureWidth, signatureHeight, false);

                    float labelWidth = paint.measureText("Potpis: ");
                    float sigX = x + labelWidth + 10;
                    float sigY = y - signatureHeight + 12;

                    canvas.drawBitmap(scaled, sigX, sigY, null);

                    // 3. Nacrtaj crtu ispod potpisa
                    canvas.drawLine(sigX, y + 10, sigX + signatureWidth, y + 10, paint);
                }
            }
        } else {
            // Dodaj novu stranicu
            pdfDocument.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            page = pdfDocument.startPage(pageInfo);
            canvas = page.getCanvas();
            y = margin + 20;

            // 1. Crtaj tekst "Potpis:"
            canvas.drawText("Potpis:", x, y, paint);

            // 2. Umetni potpis
            File signatureFile = new File(getFilesDir(), "user_signature.png");
            if (signatureFile.exists()) {
                Bitmap signature = BitmapFactory.decodeFile(signatureFile.getAbsolutePath());
                if (signature != null) {
                    int signatureWidth = 150;
                    int signatureHeight = 50;
                    Bitmap scaled = Bitmap.createScaledBitmap(signature, signatureWidth, signatureHeight, false);

                    float labelWidth = paint.measureText("Potpis: ");
                    float sigX = x + labelWidth + 10;
                    float sigY = y - signatureHeight + 12;

                    canvas.drawBitmap(scaled, sigX, sigY, null);

                    // 3. Nacrtaj crtu ispod potpisa
                    canvas.drawLine(sigX, y + 10, sigX + signatureWidth, y + 10, paint);
                }
            }
        }


        pdfDocument.finishPage(page);

        String fileName = "PravniDokument_" + currentConversationId + ".pdf";
        File file = new File(getExternalFilesDir(null), fileName);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            pdfDocument.writeTo(outputStream);
            runOnUiThread(() -> {
                Toast.makeText(this, "PDF spremljen: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                openPdf(file);
            });
        } catch (IOException e) {
            runOnUiThread(() -> Toast.makeText(this, "Greška pri spremanju PDF-a", Toast.LENGTH_SHORT).show());
        }

        pdfDocument.close();
    }





    private void openPdf(File file) {
        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nema aplikacije za otvaranje PDF-a", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractFactsFromMessages(QuerySnapshot messages) {
        StringBuilder facts = new StringBuilder();

        for (QueryDocumentSnapshot doc : messages) {
            String message = doc.getString("message");
            if (message == null) continue;

            // Prepoznaj ime osumnjičenog
            if (message.toLowerCase().contains("osumnjičeni")) {
                facts.append("→ ").append(message).append("\n");
            }

            // Prepoznaj datum (npr. 20.03.2025)
            if (message.matches(".*\\d{2}\\.\\d{2}\\.\\d{4}.*")) {
                facts.append("→ ").append(message).append("\n");
            }

            // Prepoznaj mjesto
            if (message.toLowerCase().contains("mjesto") || message.toLowerCase().contains("u ")) {
                facts.append("→ ").append(message).append("\n");
            }

            // Prepoznaj vrstu dokumenta
            if (message.toLowerCase().contains("ugovor") || message.toLowerCase().contains("tužba")) {
                facts.append("→ ").append(message).append("\n");
            }
        }

        return facts.toString();
    }
    private List<String> wrapText(String text, Paint paint, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (paint.measureText(currentLine + word + " ") <= maxWidth) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word).append(" ");
            }
        }

        if (!currentLine.toString().isEmpty()) {
            lines.add(currentLine.toString().trim());
        }

        return lines;
    }
    private void showSignatureDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_signature, null);
        SignatureView signatureView = view.findViewById(R.id.signatureView);
        Button clearBtn = view.findViewById(R.id.clearButton);
        Button saveBtn = view.findViewById(R.id.saveButton);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        clearBtn.setOnClickListener(v -> signatureView.clear());

        saveBtn.setOnClickListener(v -> {
            Bitmap signature = signatureView.getSignatureBitmap();
            File file = new File(getFilesDir(), "user_signature.png");
            try (FileOutputStream out = new FileOutputStream(file)) {
                signature.compress(Bitmap.CompressFormat.PNG, 100, out);
                dialog.dismiss();
                generateLegalDocument(); // 👉 Pokreni generiranje nakon što korisnik potpiše
            } catch (Exception e) {
                Toast.makeText(this, "Greška kod spremanja potpisa", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


}
