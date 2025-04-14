package ba.sum.fsre.toplawv2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.List;

import ba.sum.fsre.toplawv2.R;
import ba.sum.fsre.toplawv2.models.Lesson;

public class LessonAdapter extends ArrayAdapter<Lesson> {
    Context context;
    List<Lesson> lessons;
    public LessonAdapter (Context context, List<Lesson> lessons) {
        super(context, R.layout.list_item, lessons);
        this.context = context;
        this.lessons = lessons;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.list_item, parent, false);
        }

        Lesson lesson = this.lessons.get(position);

        ImageView imageImg = convertView.findViewById(R.id.imageImg);
        TextView titleTxt = convertView.findViewById(R.id.titleTxt);

        titleTxt.setText(lesson.getTitle());
        Picasso.get().load(lesson.getImageUrl()).into(imageImg);
        return convertView;
    }
}