package ba.sum.fsre.toplawv2.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.models.User;

public class UserAdapter extends ArrayAdapter<User> {

    private final Context context;
    private final List<User> users;
    private final Map<User, String> userUidMap;
    private final Map<String, String> lastMessagesMap;

    public UserAdapter(@NonNull Context context,
                       @NonNull List<User> users,
                       @NonNull Map<User, String> userUidMap,
                       @NonNull Map<String, String> lastMessagesMap) {
        super(context, R.layout.list_item_user, users);
        this.context = context;
        this.users = users;
        this.userUidMap = userUidMap;
        this.lastMessagesMap = lastMessagesMap;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_user, parent, false);
        }

        User user = users.get(position);

        TextView userNameView = convertView.findViewById(R.id.user_name);
        TextView userEmailView = convertView.findViewById(R.id.user_email);
        TextView userExpertiseView = convertView.findViewById(R.id.user_expertise);
        TextView userLawyerStatusView = convertView.findViewById(R.id.user_lawyer_status);
        TextView lastMessageView = convertView.findViewById(R.id.user_last_message);
        ImageView profileImageView = convertView.findViewById(R.id.profile_image);

        userNameView.setText(user.getFirstName() + " " + user.getLastName());
        userEmailView.setText(user.geteMail());

        if (user.getAreaOfExpertise() != null && !user.getAreaOfExpertise().isEmpty()) {
            userExpertiseView.setText("Područje stručnosti: " + user.getAreaOfExpertise());
            userExpertiseView.setVisibility(View.VISIBLE);
        } else {
            userExpertiseView.setVisibility(View.GONE);
        }

        if (user.isApproved()) {
            userLawyerStatusView.setVisibility(View.VISIBLE);
            userLawyerStatusView.setText("Odvjetnik");
        } else {
            userLawyerStatusView.setVisibility(View.GONE);
        }

        // Last message logic
        String uid = userUidMap.get(user);
        if (uid != null) {
            String lastMsg = lastMessagesMap.get(uid);
            if (lastMsg != null && !lastMsg.isEmpty()) {
                lastMessageView.setText(lastMsg);
                lastMessageView.setVisibility(View.VISIBLE);
            } else {
                lastMessageView.setVisibility(View.GONE);
            }
            Log.d("UserAdapter", "User: " + user.getFirstName() + ", UID: " + uid + ", LastMsg: " + lastMsg);
        } else {
            lastMessageView.setVisibility(View.GONE);
            Log.w("UserAdapter", "UID missing for user: " + user.getFirstName());
        }

        // Load profile picture
        if (user.getPicture() != null && isBase64(user.getPicture())) {
            try {
                byte[] decodedBytes = Base64.decode(user.getPicture(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                profileImageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Log.e("UserAdapter", "Base64 decode failed for: " + user.getFirstName(), e);
                profileImageView.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            profileImageView.setImageResource(R.drawable.ic_launcher_background);
        }

        return convertView;
    }

    private boolean isBase64(String str) {
        try {
            byte[] decoded = Base64.decode(str, Base64.DEFAULT);
            String encoded = Base64.encodeToString(decoded, Base64.DEFAULT);
            return str.trim().equals(encoded.trim());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
