package ba.sum.fsre.mymath.adapters;

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

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.models.User;

public class UserAdapter extends ArrayAdapter<User> {

    private final Context context;
    private final List<User> users;

    public UserAdapter(@NonNull Context context, @NonNull List<User> users) {
        super(context, R.layout.list_item_user, users);
        this.context = context;
        this.users = users;
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
        ImageView profileImageView = convertView.findViewById(R.id.profile_image);

        // Set name and email
        userNameView.setText(user.getFirstName() + " " + user.getLastName());
        userEmailView.setText(user.geteMail());

        // Set area of expertise
        if (user.getAreaOfExpertise() != null && !user.getAreaOfExpertise().isEmpty()) {
            userExpertiseView.setText("Područje stručnosti: " + user.getAreaOfExpertise());
            userExpertiseView.setVisibility(View.VISIBLE);
        } else {
            userExpertiseView.setVisibility(View.GONE);
        }

        // Display "Odvjetnik" if user is approved
        if (user.isApproved()) {
            userLawyerStatusView.setVisibility(View.VISIBLE);
            userLawyerStatusView.setText("Odvjetnik");
        } else {
            userLawyerStatusView.setVisibility(View.GONE);
        }

        // Load profile picture
        if (user.getPicture() != null && isBase64(user.getPicture())) {
            try {
                byte[] decodedBytes = Base64.decode(user.getPicture(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                profileImageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                // Log error and set default profile image
                Log.e("UserAdapter", "Failed to decode Base64 for user: " + user.getFirstName(), e);
                profileImageView.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            // Use a default profile picture if the picture is null or invalid
            profileImageView.setImageResource(R.drawable.ic_launcher_background);
        }

        return convertView;
    }

    // Helper method to validate Base64 strings
    private boolean isBase64(String str) {
        try {
            // Decode and re-encode the string to validate Base64 format
            byte[] decoded = Base64.decode(str, Base64.DEFAULT);
            String encoded = Base64.encodeToString(decoded, Base64.DEFAULT);
            return str.trim().equals(encoded.trim());
        } catch (IllegalArgumentException e) {
            return false; // Not valid Base64
        }
    }
}
