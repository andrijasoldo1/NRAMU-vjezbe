package ba.sum.fsre.toplaw.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.toplaw.R;
import ba.sum.fsre.toplaw.adapters.LessonAdapter;
import ba.sum.fsre.toplaw.models.Lesson;

public class ListViewFragment extends Fragment {
    private FirebaseFirestore db;
    private ListView listView;
    private LessonAdapter lessonAdapter;
    private List<Lesson> lessons;

    public ListViewFragment() {
        super();
    }

    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_view, container, false);
        listView = v.findViewById(R.id.listView);
        db = FirebaseFirestore.getInstance();
        lessons = new ArrayList<>();
        lessonAdapter = new LessonAdapter(v.getContext(), lessons);
        listView.setAdapter(lessonAdapter);
        getLessons();
        return v;
    }

    private void getLessons () {
        db.collection("lessons").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                lessons.clear();
                QuerySnapshot documents = task.getResult();
                for (DocumentSnapshot document : documents) {
                    String imageUrl = document.getString("imageUrl");
                    String title = document.getString("title");

                    Lesson lesson = new Lesson(title, imageUrl);
                    lessons.add(lesson);
                }
                lessonAdapter.notifyDataSetChanged();
            }
        });
    }
}