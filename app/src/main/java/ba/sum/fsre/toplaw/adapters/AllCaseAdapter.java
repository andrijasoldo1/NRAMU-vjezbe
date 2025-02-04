package ba.sum.fsre.toplaw.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import ba.sum.fsre.toplaw.R;
import ba.sum.fsre.toplaw.models.Case;

public class AllCaseAdapter extends BaseCaseAdapter {

    private static final String TAG = "AllCaseAdapter";
    private final Context context;
    private final List<Case> cases;
    private final FirebaseFirestore db;

    public AllCaseAdapter(Context context, List<Case> cases) {
        super(context, R.layout.list_item_all_cases, cases);
        this.context = context;
        this.cases = cases;
        this.db = FirebaseFirestore.getInstance();
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_all_cases, parent, false);
        }

        try {
            // Get views from the layout
            ImageView iconView = convertView.findViewById(R.id.icon);
            TextView titleView = convertView.findViewById(R.id.title);
            TextView typeOfCaseView = convertView.findViewById(R.id.type_of_case);
            TextView descriptionView = convertView.findViewById(R.id.description);
            TextView createdByView = convertView.findViewById(R.id.created_by);
            TextView priceView = convertView.findViewById(R.id.price);

            // Get the current case
            Case aCase = cases.get(position);

            // Populate the views with data
            titleView.setText(aCase.getName());
            typeOfCaseView.setText(aCase.getTypeOfCase());
            descriptionView.setText(aCase.getDescription());
            priceView.setText(String.format("%,.2f KM", aCase.getPrice()));

            // Fetch user email from Firestore
            db.collection("users")
                    .document(aCase.getUserId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String email = documentSnapshot.getString("eMail");
                            createdByView.setText(email != null ? "Created by: " + email : "Created by: Unknown");
                        } else {
                            createdByView.setText("Created by: Unknown");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch user email", e);
                        createdByView.setText("Created by: Unknown");
                    });

            // Optionally set an icon
            iconView.setImageResource(R.drawable.ic_law);

        } catch (Exception e) {
            Log.e(TAG, "Error in getView: ", e);
            Toast.makeText(context, "Error displaying case details", Toast.LENGTH_SHORT).show();
        }





        return convertView;
    }
}
