package ba.sum.fsre.toplawv2;

import android.content.SharedPreferences;
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

    private static final String PREFS_NAME = "HighScorePrefs";
    private static final String HIGH_SCORE_KEY = "HighScore";

    private TextView numberTextView;
    private EditText resultInput;
    private Button submitButton;
    private Button startButton;
    private TextView scoreTextView;
    private TextView highScoreTextView;

    private Handler handler = new Handler();
    private Random random = new Random();

    private int sum = 0;
    private int count = 0;
    private final int TOTAL_NUMBERS = 10; // Total numbers to display
    private final int DISPLAY_INTERVAL = 800; // Milliseconds

    private long startTime; // Start time for the game
    private long endTime;   // End time for the game

    private int score = 0; // To store the user's score
    private int highScore = 0; // To store the high score

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game1);

        numberTextView = findViewById(R.id.number_text);
        resultInput = findViewById(R.id.result_input);
        submitButton = findViewById(R.id.submit_button);
        startButton = findViewById(R.id.start_button);
        scoreTextView = findViewById(R.id.score_text);
        highScoreTextView = findViewById(R.id.high_score_text);

        // Initially hide game elements
        numberTextView.setVisibility(View.GONE);
        resultInput.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        scoreTextView.setVisibility(View.GONE);

        // Load the saved high score
        loadHighScore();

        // Start the game when the "Start" button is pressed
        startButton.setOnClickListener(v -> {
            startButton.setVisibility(View.GONE);
            numberTextView.setVisibility(View.VISIBLE);
            scoreTextView.setVisibility(View.GONE);
            startGame();
        });

        submitButton.setOnClickListener(v -> checkResult());
    }

    private void startGame() {
        count = 0;
        sum = 0;
        score = 0;
        startTime = System.currentTimeMillis(); // Record the start time

        handler.post(displayNumbersTask);
    }

    private final Runnable displayNumbersTask = new Runnable() {
        @Override
        public void run() {
            if (count < TOTAL_NUMBERS) {
                int randomNum = random.nextInt(9) + 1; // Random number between 1 and 9
                numberTextView.setText(String.valueOf(randomNum));
                sum += randomNum;
                count++;

                // Schedule the next display at the exact interval
                long nextTime = startTime + count * DISPLAY_INTERVAL;
                long delay = nextTime - System.currentTimeMillis();
                handler.postDelayed(this, Math.max(delay, 0));
            } else {
                // End of the number display phase
                numberTextView.setVisibility(View.GONE);
                resultInput.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);
                endTime = System.currentTimeMillis(); // Record the end time
            }
        }
    };

    private void checkResult() {
        String userInput = resultInput.getText().toString();
        if (!userInput.isEmpty()) {
            int userResult = Integer.parseInt(userInput);
            if (userResult == sum) {
                long timeTaken = (endTime - startTime) / 1000; // Calculate time in seconds
                calculateScore(timeTaken);
                Toast.makeText(this, "Correct! Your score: " + score, Toast.LENGTH_LONG).show();
                saveHighScore();
            } else {
                Toast.makeText(this, "Incorrect! Sum was " + sum, Toast.LENGTH_SHORT).show();
            }

            // Display the score and high score
            resultInput.setVisibility(View.GONE);
            submitButton.setVisibility(View.GONE);
            scoreTextView.setVisibility(View.VISIBLE);
            scoreTextView.setText("Your Score: " + score);
            highScoreTextView.setVisibility(View.VISIBLE);
            highScoreTextView.setText("High Score: " + highScore);

            // Reset the game after showing the score
            startButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Please enter a number!", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateScore(long timeTaken) {
        // Example scoring: Base score + time-based bonus
        score = 100; // Base score
        if (timeTaken <= 10) {
            score += 50; // Bonus for fast completion
        } else if (timeTaken <= 20) {
            score += 20; // Smaller bonus
        }
    }

    private void saveHighScore() {
        if (score > highScore) {
            highScore = score;

            // Save the new high score to SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(HIGH_SCORE_KEY, highScore);
            editor.apply();
        }
    }

    private void loadHighScore() {
        // Load the high score from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        highScore = prefs.getInt(HIGH_SCORE_KEY, 0);
        highScoreTextView.setText("High Score: " + highScore);
        highScoreTextView.setVisibility(View.VISIBLE);
    }
}
