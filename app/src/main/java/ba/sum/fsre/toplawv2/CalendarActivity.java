package ba.sum.fsre.toplawv2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import ba.sum.fsre.toplawv2.models.DayModel;

public class CalendarActivity extends AppCompatActivity {

    private RecyclerView calendarRecyclerView;
    private TextView monthText, selectedMeetingsView;
    private LinearLayout meetingsListLayout;

    private Button prevMonthBtn, nextMonthBtn, createMeetingBtn;
    private Spinner prioritySpinner;
    private TextInputEditText titleEditText, noteEditText, startTimeEditText, endTimeEditText;
    private SwitchMaterial remindSwitch;

    private final Calendar currentCalendar = Calendar.getInstance();
    private final Calendar selectedDate = Calendar.getInstance();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("LLLL yyyy", new Locale("hr"));

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthText = findViewById(R.id.monthText);
        prevMonthBtn = findViewById(R.id.prevMonthBtn);
        nextMonthBtn = findViewById(R.id.nextMonthBtn);
        selectedMeetingsView = findViewById(R.id.selectedMeetingsView);
        meetingsListLayout = findViewById(R.id.meetingsListLayout);

        titleEditText = findViewById(R.id.meetingTitle);
        noteEditText = findViewById(R.id.meetingNote);
        startTimeEditText = findViewById(R.id.startTimeEditText);
        endTimeEditText = findViewById(R.id.endTimeEditText);
        remindSwitch = findViewById(R.id.remindSwitch);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        createMeetingBtn = findViewById(R.id.createMeetingBtn);

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Hitno", "Važno", "Dugotrajno"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        prevMonthBtn.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextMonthBtn.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        startTimeEditText.setOnClickListener(v -> showTimePicker(startTimeEditText));
        endTimeEditText.setOnClickListener(v -> showTimePicker(endTimeEditText));
        createMeetingBtn.setOnClickListener(v -> createMeeting());

        updateCalendar(); // inicializacija prikaza
    }

    private void updateCalendar() {
        monthText.setText(monthFormat.format(currentCalendar.getTime()));
        List<DayModel> days = new ArrayList<>();

        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Ubaci prazne dane (offset)
        for (int i = 0; i < firstDayOfWeek; i++) {
            days.add(new DayModel(0, false, null));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            days.add(new DayModel(day, true, new ArrayList<>()));
        }

        ba.sum.fsre.toplawv2.CalendarAdapter adapter = new ba.sum.fsre.toplawv2.CalendarAdapter(days, day -> {
            selectedDate.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), day);
            fetchMeetingsForDate(dateFormat.format(selectedDate.getTime()));
        });

        calendarRecyclerView.setAdapter(adapter);

        // 🔥 Označi dane koji imaju sastanke
        fetchEventDaysForMonth(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), adapter, days);
    }


    private int getColorForPriority(String priority) {
        switch (priority) {
            case "Hitno": return getResources().getColor(android.R.color.holo_red_dark);
            case "Važno": return getResources().getColor(android.R.color.holo_orange_dark);
            case "Dugotrajno": return getResources().getColor(android.R.color.holo_blue_dark);
            default: return getResources().getColor(android.R.color.darker_gray);
        }
    }

    private void showTimePicker(TextInputEditText editText) {
        Calendar now = Calendar.getInstance();
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(now.get(Calendar.HOUR_OF_DAY))
                .setMinute(now.get(Calendar.MINUTE))
                .setTitleText("Odaberi vrijeme")
                .build();

        picker.addOnPositiveButtonClickListener(view -> {
            String formatted = String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute());
            editText.setText(formatted);
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void createMeeting() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Niste prijavljeni!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = titleEditText.getText().toString().trim();
        String note = noteEditText.getText().toString().trim();
        String date = dateFormat.format(selectedDate.getTime());
        String startTime = startTimeEditText.getText().toString().trim();
        String endTime = endTimeEditText.getText().toString().trim();
        String priority = prioritySpinner.getSelectedItem().toString();
        boolean remind = remindSwitch.isChecked();

        if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Ispunite sva obavezna polja!", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> meeting = new HashMap<>();
        meeting.put("title", title);
        meeting.put("note", note);
        meeting.put("date", date);
        meeting.put("startTime", startTime);
        meeting.put("endTime", endTime);
        meeting.put("priority", priority);
        meeting.put("remind", remind);
        meeting.put("userId", userId);

        db.collection("meetings").add(meeting)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Sastanak kreiran!", Toast.LENGTH_SHORT).show();
                    fetchMeetingsForDate(date);
                    clearForm();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Greška pri spremanju.", Toast.LENGTH_SHORT).show());
    }

    private void clearForm() {
        titleEditText.setText("");
        noteEditText.setText("");
        startTimeEditText.setText("");
        endTimeEditText.setText("");
        remindSwitch.setChecked(false);
        prioritySpinner.setSelection(0);
    }

    private void fetchEventDaysForMonth(int year, int month, ba.sum.fsre.toplawv2.CalendarAdapter adapter, List<DayModel> dayModels) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("meetings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    HashMap<String, List<String>> dayToPriorities = new HashMap<>();

                    for (QueryDocumentSnapshot doc : query) {
                        String dateStr = doc.getString("date");
                        String priority = doc.getString("priority");

                        try {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(dateFormat.parse(dateStr));
                            int d = cal.get(Calendar.DAY_OF_MONTH);
                            int m = cal.get(Calendar.MONTH);
                            int y = cal.get(Calendar.YEAR);

                            if (y == year && m == month) {
                                String key = String.valueOf(d);
                                if (!dayToPriorities.containsKey(key)) {
                                    dayToPriorities.put(key, new ArrayList<>());
                                }
                                dayToPriorities.get(key).add(priority);
                            }

                        } catch (Exception ignored) {}
                    }

                    // sad postavi točkice za svaki dan
                    for (int i = 0; i < dayModels.size(); i++) {
                        DayModel model = dayModels.get(i);
                        if (model.day == 0) continue;
                        List<String> priorities = dayToPriorities.get(String.valueOf(model.day));
                        if (priorities != null) {
                            List<Integer> dots = new ArrayList<>();
                            for (String prio : priorities) {
                                dots.add(getColorForPriority(prio));
                            }
                            model.dots = dots;
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
    private void fetchMeetingsForDate(String date) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("meetings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(query -> {
                    meetingsListLayout.removeAllViews();

                    if (query.isEmpty()) {
                        selectedMeetingsView.setText("Nema sastanaka za odabrani datum.");
                        return;
                    }

                    selectedMeetingsView.setText("Sastanci:");

                    for (QueryDocumentSnapshot doc : query) {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        String note = doc.getString("note");
                        String start = doc.getString("startTime");
                        String end = doc.getString("endTime");
                        String priority = doc.getString("priority");
                        boolean remind = doc.getBoolean("remind") != null && doc.getBoolean("remind");

                        TextView clickable = new TextView(this);
                        clickable.setText("- " + title + " (" + start + " - " + end + ")");
                        clickable.setTextSize(16f);
                        clickable.setPadding(16, 8, 16, 8);
                        clickable.setClickable(true);
                        clickable.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

                        clickable.setOnClickListener(v -> {
                            Intent i = new Intent(this, MeetingDetailActivity.class);
                            i.putExtra("id", id);
                            i.putExtra("title", title);
                            i.putExtra("note", note);
                            i.putExtra("time", start + " - " + end);
                            i.putExtra("priority", priority);
                            i.putExtra("remind", remind);
                            startActivity(i);
                        });

                        meetingsListLayout.addView(clickable);
                    }
                });
    }

}
