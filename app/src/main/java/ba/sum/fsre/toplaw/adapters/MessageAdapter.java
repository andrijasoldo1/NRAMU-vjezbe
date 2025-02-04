
package ba.sum.fsre.toplaw.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ba.sum.fsre.toplaw.R;
import ba.sum.fsre.toplaw.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final Context context;
    private final List<Message> messages;
    private final String currentUserId;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (message.getSenderId().equals(currentUserId)) {
            holder.messageText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        } else {
            holder.messageText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }

        if (message.getBase64Media() != null) {
            holder.messageImage.setVisibility(View.VISIBLE);
            byte[] decodedBytes = Base64.decode(message.getBase64Media(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.messageImage.setImageBitmap(bitmap);
        } else {
            holder.messageImage.setVisibility(View.GONE);
        }

        holder.messageText.setText(message.getText());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageImage = itemView.findViewById(R.id.message_image);
        }
    }
}