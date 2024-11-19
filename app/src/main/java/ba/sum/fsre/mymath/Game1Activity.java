package ba.sum.fsre.mymath;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class Game1Activity extends AppCompatActivity {

    private TextView numberTextView;
    private EditText resultInput;
    private Button submitButton;
    private Button startButton;

    private Handler handler = new Handler();
    private Random random = new Random();

    private int sum = 0;
    private int count = 0;
    private final int TOTAL_NUMBERS = 10;
    private final int DISPLAY_INTERVAL = 800;

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game1);

        numberTextView = findViewById(R.id.number_text);
        resultInput = findViewById(R.id.result_input);
        submitButton = findViewById(R.id.submit_button);
        startButton = findViewById(R.id.start_button);

        numberTextView.setVisibility(View.GONE);
        resultInput.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);

        startButton.setOnClickListener(v -> {
            startButton.setVisibility(View.GONE);
            numberTextView.setVisibility(View.VISIBLE);
            startGame();
        });

        submitButton.setOnClickListener(v -> checkResult());
    }

    private void startGame() {
        count = 0;
        sum = 0;
        startTime = System.currentTimeMillis();

        handler.post(displayNumbersTask);
    }

    private final Runnable displayNumbersTask = new Runnable() {
        @Override
        public void run() {
            if (count < TOTAL_NUMBERS) {
                int randomNum = random.nextInt(9) + 1;
                numberTextView.setText(String.valueOf(randomNum));
                sum += randomNum;
                count++;


                long nextTime = startTime + count * DISPLAY_INTERVAL;
                long delay = nextTime - System.currentTimeMillis();
                handler.postDelayed(this, Math.max(delay, 0));
            } else {

                numberTextView.setVisibility(View.GONE);
                resultInput.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);
            }
        }
    };

    private void checkResult() {
        String userInput = resultInput.getText().toString();
        if (!userInput.isEmpty()) {
            int userResult = Integer.parseInt(userInput);
            if (userResult == sum) {
                Toast.makeText(this, "Correct! Sum is " + sum, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Incorrect! Sum was " + sum, Toast.LENGTH_SHORT).show();
            }
            finish(); // End activity
        } else {
            Toast.makeText(this, "Please enter a number!", Toast.LENGTH_SHORT).show();
        }
    }
}
