package ba.sum.fsre.toplawv2.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import ba.sum.fsre.toplawv2.PdfViewerActivity;
import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.models.Message;

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
        boolean isSentByCurrentUser = message.getSenderId().equals(currentUserId);

        // Align left or right
        holder.wrapper.setGravity(isSentByCurrentUser ? Gravity.END : Gravity.START);

        // Background bubble
        holder.container.setBackgroundResource(isSentByCurrentUser ?
                R.drawable.bg_message_sent : R.drawable.bg_message_received);

        // Text message
        if (message.getText() != null && !message.getText().isEmpty()) {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(message.getText());
        } else {
            holder.messageText.setVisibility(View.GONE);
        }

        // Media (image or PDF)
        if (message.getBase64Media() != null && !message.getBase64Media().isEmpty()) {
            Bitmap imageBitmap = decodeImageBitmap(message.getBase64Media());

            if (imageBitmap != null) {
                // Show image
                holder.messageImage.setImageBitmap(imageBitmap);
                holder.messageImage.setVisibility(View.VISIBLE);
                holder.messageImage.setOnClickListener(null);
            } else {
                Bitmap pdfThumb = generatePdfThumbnail(message.getBase64Media());
                if (pdfThumb != null) {
                    holder.messageImage.setImageBitmap(pdfThumb);
                    holder.messageImage.setVisibility(View.VISIBLE);
                    holder.messageImage.setOnClickListener(v -> {
                        Intent intent = new Intent(context, PdfViewerActivity.class);
                        intent.putExtra(PdfViewerActivity.EXTRA_PDF_BASE64, message.getBase64Media());
                        context.startActivity(intent);
                    });
                } else {
                    holder.messageImage.setVisibility(View.GONE);
                }
            }
        } else {
            holder.messageImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private Bitmap decodeImageBitmap(String base64) {
        try {
            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap generatePdfThumbnail(String base64) {
        try {
            byte[] pdfData = Base64.decode(base64, Base64.DEFAULT);
            File temp = File.createTempFile("temp_pdf", ".pdf", context.getCacheDir());
            FileOutputStream fos = new FileOutputStream(temp);
            fos.write(pdfData);
            fos.close();

            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(pfd);
            PdfRenderer.Page page = renderer.openPage(0);

            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            page.close();
            renderer.close();
            pfd.close();

            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout wrapper, container;
        TextView messageText;
        ImageView messageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            wrapper = itemView.findViewById(R.id.message_wrapper);
            container = itemView.findViewById(R.id.message_container);
            messageText = itemView.findViewById(R.id.message_text);
            messageImage = itemView.findViewById(R.id.message_image);
        }
    }
}
