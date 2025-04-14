package ba.sum.fsre.toplawv2.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.models.Review;
import ba.sum.fsre.toplawv2.models.User;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final List<User> lawyers;
    private final Map<String, List<Review>> reviewsMap;

    public ReviewAdapter(Context context, List<User> lawyers, Map<String, List<Review>> reviewsMap) {
        this.context = context;
        this.lawyers = lawyers;
        this.reviewsMap = reviewsMap;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        User lawyer = lawyers.get(position);

        holder.name.setText(lawyer.getFirstName() + " " + lawyer.getLastName());
        holder.email.setText(lawyer.geteMail());
        holder.expertise.setText(lawyer.getAreaOfExpertise());

        // Load lawyer's picture
        if (lawyer.getPicture() != null && !lawyer.getPicture().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(lawyer.getPicture(), Base64.DEFAULT);
                holder.picture.setImageBitmap(BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length));
            } catch (IllegalArgumentException e) {
                Log.e("ReviewAdapter", "Invalid Base64 for image", e);
                holder.picture.setImageResource(R.drawable.ic_launcher_background); // Placeholder
            }
        } else {
            holder.picture.setImageResource(R.drawable.ic_launcher_background); // Placeholder
        }

        // Show existing reviews
        List<Review> reviews = reviewsMap.get(lawyer.geteMail());
        if (reviews != null && !reviews.isEmpty()) {
            StringBuilder reviewsText = new StringBuilder();
            for (Review review : reviews) {
                reviewsText.append(review.getReviewerEmail()).append(": ")
                        .append(review.getReviewText()).append(" (")
                        .append(review.getRating()).append("⭐)\n");
            }
            holder.reviews.setText(reviewsText.toString().trim());
        } else {
            holder.reviews.setText("No reviews yet.");
        }

        // Handle submit button click
        holder.submitButton.setOnClickListener(v -> {
            String reviewText = holder.reviewInput.getText().toString().trim();
            float rating = holder.ratingBar.getRating();
            String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            if (reviewText.isEmpty() || rating == 0) {
                Toast.makeText(context, "Please provide a review and rating.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new review object
            Review review = new Review(
                    lawyer.geteMail(),
                    currentUserEmail,
                    rating,
                    reviewText
            );

            // Add the review to Firestore
            FirebaseFirestore.getInstance().collection("reviews").add(review)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                        holder.reviewInput.setText("");
                        holder.ratingBar.setRating(0);
                        reviewsMap.get(lawyer.geteMail()).add(review); // Update local reviews
                        notifyDataSetChanged(); // Refresh the view
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to submit review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ReviewAdapter", "Error submitting review", e);
                    });
        });
    }

    @Override
    public int getItemCount() {
        return lawyers.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, expertise, reviews;
        ImageView picture;
        RatingBar ratingBar;
        EditText reviewInput;
        Button submitButton;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.lawyer_name);
            email = itemView.findViewById(R.id.lawyer_email);
            expertise = itemView.findViewById(R.id.lawyer_expertise);
            reviews = itemView.findViewById(R.id.lawyer_reviews);
            picture = itemView.findViewById(R.id.lawyer_picture);
            ratingBar = itemView.findViewById(R.id.lawyer_rating);
            reviewInput = itemView.findViewById(R.id.review_input);
            submitButton = itemView.findViewById(R.id.submit_review_button);
        }
    }
}
