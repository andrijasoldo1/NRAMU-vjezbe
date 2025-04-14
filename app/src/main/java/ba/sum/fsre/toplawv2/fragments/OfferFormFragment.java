package ba.sum.fsre.toplawv2.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ba.sum.fsre.toplawv2.R;

public class OfferFormFragment extends Fragment {

    private EditText messageInput, priceInput;
    private Button submitOfferBtn;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String receiverId;

    public static OfferFormFragment newInstance(String receiverId) {
        OfferFormFragment fragment = new OfferFormFragment();
        Bundle args = new Bundle();
        args.putString("RECEIVER_ID", receiverId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer_form, container, false);

        messageInput = view.findViewById(R.id.offer_message_input);
        priceInput = view.findViewById(R.id.offer_price_input);
        submitOfferBtn = view.findViewById(R.id.submit_offer_button);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            receiverId = getArguments().getString("RECEIVER_ID");
        }

        submitOfferBtn.setOnClickListener(v -> sendOfferMessage());

        return view;
    }

    private void sendOfferMessage() {
        String message = messageInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();

        if (TextUtils.isEmpty(message) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(getContext(), "Message and price are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid price format.", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (senderId == null || receiverId == null) {
            Toast.makeText(getContext(), "User not logged in or receiver missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("receiverId", receiverId);
        messageData.put("participants", List.of(senderId, receiverId));
        messageData.put("text", "Offer: " + message + "\nPrice: " + price + " KM");
        messageData.put("timestamp", System.currentTimeMillis());
        messageData.put("base64Media", null);

        db.collection("messages")
                .add(messageData)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Offer sent successfully!", Toast.LENGTH_SHORT).show();
                    messageInput.setText("");
                    priceInput.setText("");
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to send offer.", Toast.LENGTH_SHORT).show();
                });
    }




}
