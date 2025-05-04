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
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.models.User;

public class LawyerListAdapter extends ArrayAdapter<User> {

    private final Context context;
    private final List<User> lawyers;
    private final Map<String, Float> averageRatings;

    public LawyerListAdapter(@NonNull Context context,
                             @NonNull List<User> lawyers,
                             @NonNull Map<String, Float> averageRatings) {
        super(context, R.layout.lv_recommended_job, lawyers);
        this.context = context;
        this.lawyers = lawyers;
        this.averageRatings = averageRatings;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_recommended_job, parent, false);
        }

        User user = lawyers.get(position);

        TextView fullName = convertView.findViewById(R.id.tv_full_name);
        TextView expertise = convertView.findViewById(R.id.tv_expertise);
        ImageView expertiseImage = convertView.findViewById(R.id.expertise_image);
        RatingBar ratingBar = convertView.findViewById(R.id.rating_bar_lawyer);
        ImageView profileImage = convertView.findViewById(R.id.img_profile); // Add this if your layout has it

        fullName.setText(user.getFirstName() + " " + user.getLastName());
        expertise.setText(user.getAreaOfExpertise() != null ? user.getAreaOfExpertise() : "Nepoznato");

        // Load profile picture if Base64
        if (user.getPicture() != null && isBase64(user.getPicture())) {
            try {
                byte[] decodedBytes = Base64.decode(user.getPicture(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                profileImage.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                profileImage.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            profileImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // Set dynamic average rating
        Float avgRating = averageRatings.get(user.geteMail());
        ratingBar.setRating(avgRating != null ? avgRating : 0);

        // Expertise-specific icon
        int iconRes = getExpertiseIcon(user.getAreaOfExpertise());
        expertiseImage.setImageResource(iconRes);

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

    private int getExpertiseIcon(String expertise) {
        if (expertise == null) return R.drawable.law;
        switch (expertise) {
            case "Kazneno pravo": return R.drawable.kazneno_pravo;
            case "Radno pravo": return R.drawable.radno_pravo;
            default: return R.drawable.law;
        }
    }
}



