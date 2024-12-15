package ba.sum.fsre.mymath.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

        userNameView.setText(user.getFirstName() + " " + user.getLastName());
        userEmailView.setText(user.geteMail());

        return convertView;
    }
}