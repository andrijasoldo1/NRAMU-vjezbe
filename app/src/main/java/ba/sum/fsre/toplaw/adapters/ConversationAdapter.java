package ba.sum.fsre.toplaw.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ba.sum.fsre.toplaw.R;
import ba.sum.fsre.toplaw.models.Conversation;

public class ConversationAdapter extends ArrayAdapter<Conversation> {

    private final Context context;
    private final List<Conversation> conversations;

    public ConversationAdapter(Context context, List<Conversation> conversations) {
        super(context, R.layout.list_item_conversation, conversations);
        this.context = context;
        this.conversations = conversations;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_conversation, parent, false);
        }

        Conversation conversation = conversations.get(position);

        TextView nameView = convertView.findViewById(R.id.conversation_name);
        TextView lastMessageView = convertView.findViewById(R.id.last_message);

        nameView.setText(conversation.getReceiverName());
        lastMessageView.setText(conversation.getLastMessage());

        return convertView;
    }
}
