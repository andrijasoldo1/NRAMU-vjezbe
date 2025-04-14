package ba.sum.fsre.toplawv2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class MeetingDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String meetingId;

    private TextInputEditText editTitle, editNote;
    private TextView editTime;
    private Spinner prioritySpinner;
    private Switch remindSwitch;
    private Button editBtn, saveBtn, deleteBtn;

    private String startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_detail);

        db = FirebaseFirestore.getInstance();

        editTitle = findViewById(R.id.editMeetingTitle);
        editNote = findViewById(R.id.editMeetingNote);
        editTime = findViewById(R.id.editMeetingTime);
        prioritySpinner = findViewById(R.id.editPrioritySpinner);
        remindSwitch = findViewById(R.id.editRemindSwitch);
        editBtn = findViewById(R.id.editMeetingBtn);
        saveBtn = findViewById(R.id.saveMeetingBtn);
        deleteBtn = findViewById(R.id.deleteMeetingBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Hitno", "Važno", "Dugotrajno"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        // Get data
        meetingId = getIntent().getStringExtra("id");
        String title = getIntent().getStringExtra("title");
        String note = getIntent().getStringExtra("note");
        String time = getIntent().getStringExtra("time");
        String priority = getIntent().getStringExtra("priority");
        boolean remind = getIntent().getBooleanExtra("remind", false);

        String[] timeParts = time.split(" - ");
        startTime = timeParts.length > 0 ? timeParts[0] : "";
        endTime = timeParts.length > 1 ? timeParts[1] : "";

        // Set data
        editTitle.setText(title);
        editNote.setText(note);
        editTime.setText("Vrijeme: " + time);
        remindSwitch.setChecked(remind);
        int index = adapter.getPosition(priority);
        if (index >= 0) prioritySpinner.setSelection(index);

        setEditable(false);

        editBtn.setOnClickListener(v -> setEditable(true));

        saveBtn.setOnClickListener(v -> {
            String newTitle = editTitle.getText().toString().trim();
            String newNote = editNote.getText().toString().trim();
            String newPriority = prioritySpinner.getSelectedItem().toString();
            boolean newRemind = remindSwitch.isChecked();

            HashMap<String, Object> update = new HashMap<>();
            update.put("title", newTitle);
            update.put("note", newNote);
            update.put("priority", newPriority);
            update.put("remind", newRemind);

            db.collection("meetings").document(meetingId)
                    .update(update)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Sastanak ažuriran.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Greška pri ažuriranju.", Toast.LENGTH_SHORT).show());
        });

        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Brisanje sastanka")
                    .setMessage("Jeste li sigurni da želite obrisati sastanak?")
                    .setPositiveButton("Da", (dialog, which) -> {
                        db.collection("meetings").document(meetingId)
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Sastanak obrisan.", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Greška pri brisanju.", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Ne", null)
                    .show();
        });
    }

    private void setEditable(boolean enable) {
        editTitle.setEnabled(enable);
        editNote.setEnabled(enable);
        prioritySpinner.setEnabled(enable);
        remindSwitch.setEnabled(enable);
        saveBtn.setEnabled(enable);
        saveBtn.setVisibility(enable ? Button.VISIBLE : Button.GONE);
    }
}