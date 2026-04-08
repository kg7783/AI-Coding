package de.einmaleins.trainer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final long ANIMATION_DELAY_MS = 600;

    private Trainer trainer;
    private ConfigManager configManager;
    private SessionManager sessionManager;
    private ProblemView problemView1;
    private ProblemView problemView2;
    private NumberPadView numberPad;
    private Button btnSetup;
    private Button btnNewSession;
    private Button btnHistory;
    private StatsView statsView;

    private ProblemView activeProblem;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        handler = new Handler(Looper.getMainLooper());
        sessionManager = new SessionManager(this);

        configManager = new ConfigManager(this);
        trainer = new Trainer();

        EinmaleinsConfig config = configManager.loadConfig();
        trainer.setConfig(config);

        if (sessionManager.hasActiveSession()) {
            java.util.List<ProgressSession> sessions = sessionManager.loadSessions();
            if (!sessions.isEmpty()) {
                ProgressSession current = sessions.get(0);
                trainer.setStats(
                        current.getCorrectAnswers(),
                        current.getWrongAnswers(),
                        current.getTotalAttempts()
                );
                trainer.setSeriesStats(current.getMultCorrect(), current.getMultWrong(),
                                      current.getDivCorrect(), current.getDivWrong());
            }
        }

        problemView1 = findViewById(R.id.problemView1);
        problemView2 = findViewById(R.id.problemView2);
        numberPad = findViewById(R.id.numberPad);
        btnSetup = findViewById(R.id.btnSetup);
        btnNewSession = findViewById(R.id.btnNewSession);
        btnHistory = findViewById(R.id.btnHistory);
        statsView = findViewById(R.id.statsView);

        problemView1.setActive(true);
        problemView2.setActive(false);
        activeProblem = problemView1;

        problemView1.setProblem(trainer.generateMultiplicationProblem());
        problemView2.setProblem(trainer.generateDivisionProblem());

        updateStats();

        setupListeners();
    }

    private void setupListeners() {
        problemView1.setOnProblemClickListener(view -> {
            activeProblem = problemView1;
            problemView1.setActive(true);
            problemView2.setActive(false);
        });

        problemView2.setOnProblemClickListener(view -> {
            activeProblem = problemView2;
            problemView1.setActive(false);
            problemView2.setActive(true);
        });

        problemView1.setOnTimerExpiredListener(view -> {
            checkProblem(view);
        });

        problemView2.setOnTimerExpiredListener(view -> {
            checkProblem(view);
        });

        numberPad.setOnNumberPadListener(new NumberPadView.OnNumberPadListener() {
            @Override
            public void onNumberPressed(int number) {
                if (activeProblem != null && !activeProblem.isAnimating()) {
                    activeProblem.appendDigit(number);
                }
            }

            @Override
            public void onDeletePressed() {
                if (activeProblem != null && !activeProblem.isAnimating()) {
                    activeProblem.deleteDigit();
                }
            }

            @Override
            public void onOkPressed() {
                if (activeProblem != null && !activeProblem.isAnimating()) {
                    checkProblem(activeProblem);
                }
            }
        });

        btnSetup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SetupActivity.class);
            startActivity(intent);
        });

        btnNewSession.setOnClickListener(v -> startNewSession());

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SessionListActivity.class);
            startActivity(intent);
        });
    }

    private void startNewSession() {
        sessionManager.startNewSession();
        trainer.resetStats();
        problemView1.cancelTimer();
        problemView2.cancelTimer();
        problemView1.setProblem(trainer.generateMultiplicationProblem());
        problemView2.setProblem(trainer.generateDivisionProblem());
        problemView1.clearAnswer();
        problemView2.clearAnswer();
        updateStats();
    }

    private void saveCurrentProgress() {
        sessionManager.updateCurrentSession(
                trainer.getCorrectAnswers(),
                trainer.getWrongAnswers(),
                trainer.getTotalAttempts(),
                trainer.getMultCorrect(),
                trainer.getMultWrong(),
                trainer.getDivCorrect(),
                trainer.getDivWrong()
        );
    }

    private void checkProblem(ProblemView problemView) {
        if (problemView == null || problemView.isAnimating()) return;
        
        if (!sessionManager.hasActiveSession()) {
            sessionManager.startNewSession();
        }
        
        boolean wasCorrect = problemView.checkAnswerAndGetResult();
        MathProblem problem = problemView.getProblem();
        
        if (problem == null) {
            return;
        }
        
        int series = problem.num2;
        boolean isDivision = (problemView == problemView2);
        
        if (wasCorrect) {
            trainer.recordCorrectAnswer(series, isDivision);
            updateStats();
            saveCurrentProgress();
            
            boolean autoSwitch = trainer.getConfig().autoSwitchMode;
            if (autoSwitch) {
                if (problemView == problemView1) {
                    activeProblem = problemView2;
                    problemView1.setActive(false);
                    problemView2.setActive(true);
                } else {
                    activeProblem = problemView1;
                    problemView1.setActive(true);
                    problemView2.setActive(false);
                }
            }
            
            handler.postDelayed(() -> {
                problemView.cancelTimer();
                problemView.setProblem(isDivision ? 
                    trainer.generateDivisionProblem() : 
                    trainer.generateMultiplicationProblem());
            }, ANIMATION_DELAY_MS);
        } else {
            if (!problemView.getAnswerText().isEmpty()) {
                trainer.recordWrongAnswer(series, isDivision);
                updateStats();
                saveCurrentProgress();
            }
            
            handler.postDelayed(() -> {
                problemView.clearAnswer();
                problemView.startTimer();
            }, ANIMATION_DELAY_MS);
        }
    }

    private void updateStats() {
        statsView.setStats(trainer.getCorrectAnswers(), trainer.getWrongAnswers());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (problemView1 != null) problemView1.cancelTimer();
        if (problemView2 != null) problemView2.cancelTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (problemView1 != null) problemView1.cancelTimer();
        if (problemView2 != null) problemView2.cancelTimer();
        saveCurrentProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EinmaleinsConfig config = configManager.loadConfig();
        trainer.setConfig(config);
        updateStats();
        
        if (config.autoSwitchMode) {
            activeProblem = problemView1;
            problemView1.setActive(true);
            problemView2.setActive(false);
        }
    }
}
