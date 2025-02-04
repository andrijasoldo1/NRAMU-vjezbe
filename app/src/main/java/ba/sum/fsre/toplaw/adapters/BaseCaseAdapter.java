package ba.sum.fsre.toplaw.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ba.sum.fsre.toplaw.models.Case;

public abstract class BaseCaseAdapter extends ArrayAdapter<Case> {

    public BaseCaseAdapter(@NonNull Context context, int resource, @NonNull List<Case> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public abstract View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent);
}
