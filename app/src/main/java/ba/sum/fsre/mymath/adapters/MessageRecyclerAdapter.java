package ba.sum.fsre.mymath.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.models.Message;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.MessageViewHolder> {

    private final Context context;
    private final List<Message> messages;
    private final String currentUserId;

    public MessageRecyclerAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        if (message.getSenderId().equals(currentUserId)) {
            // Sent message
            holder.container.setGravity(Gravity.END);
            holder.messageText.setBackgroundResource(R.drawable.bg_message_sent);
        } else {
            // Received message
            holder.container.setGravity(Gravity.START);
            holder.messageText.setBackgroundResource(R.drawable.bg_message_received);
        }

        // Text messages
        if (message.getText() != null && !message.getText().isEmpty()) {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setTextColor(Color.parseColor("#FFFFFF"));
            holder.messageText.setText(message.getText());
        } else {
            holder.messageText.setVisibility(View.GONE);
        }

        // Image messages
        if (message.getBase64Media() != null) {
            holder.messageImage.setVisibility(View.VISIBLE);
            byte[] decodedBytes = Base64.decode(message.getBase64Media(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.messageImage.setImageBitmap(bitmap);
        } else {
            holder.messageImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView messageText;
        ImageView messageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.message_container);
            messageText = itemView.findViewById(R.id.message_text);
            messageImage = itemView.findViewById(R.id.message_image);
        }
    }
}
