package jakebarnby.pop;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.jakebarnby.pop.R;
import com.startapp.android.publish.StartAppAd;

/**
 * Game screen of Click app, provides the user with interface for playing the game.
 * @author Jake Barnby
 *
 */
public class GameActivity extends Activity {
	
	private StartAppAd startAppAd = new StartAppAd(this);
	
	//private static final int LEVELS = 8;
	//private static final int[] BALLOONS_BY_LEVEL = {12, 10, 8 , 6, 4, 3, 2, 2};
	//private static final int[] SCORE_BY_LEVEL = {20, 23, 26, 30, 32};
	private static int[] IMAGES = {R.drawable.balloon_blue, R.drawable.balloon_red, R.drawable.balloon_green};
	private static final long COUNTDOWN_TIME = 10900;
	private static final float width = 320.0f;
	private static final float height = 320.0f;
	
	private int currentBalloons = 8;
	private int score = 0;
	//private int currentLevel;
	
	private ImageButton[] balloons;
	
	
	private CountDownTimer timer;

	// Getters and setters----------
	public int getScore() {
		return score;
	}

	public void setScore(int newScore) {
		score = newScore;
	}

	// -------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(R.layout.activity_new_game);
		
		createBalloons();
	}
	
	/**
	 * Create and display the balloons to be popped on screen
	 */
	private void createBalloons() {
		balloons = new ImageButton[currentBalloons];
		FrameLayout layout = (FrameLayout)findViewById(R.id.frameLayout);
		final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.FILL);
		layout.setLayoutParams(params);
		
		for (int i = 0; i < currentBalloons; i++) {
			int imageId = (int)(Math.random() * IMAGES.length);
			
			balloons[i] = new ImageButton(this);
			balloons[i].setImageResource(IMAGES[imageId]);
			balloons[i].setBackgroundColor(Color.TRANSPARENT);
			balloons[i].setScaleX(0.4f);
			balloons[i].setScaleY(0.4f);

			balloons[i].setX((float) ((-width) + (Math.random() * (width*2))));
			balloons[i].setY((float) ((-height) + (Math.random() * (height*2))));
			balloons[i].setVisibility(View.VISIBLE);
			balloons[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					pop(v);
				}
			});
			
			layout.addView(balloons[i]);
		}
	}
	
	private void setBalloonPosition(ImageButton b) {
		float x =  (float) ((-width) + (Math.random() * (width*2)));
		float y = (float) ((-height) + (Math.random() * (height*2)));
		
		for(int j = 0; j < currentBalloons; j++) {
			if (x >= balloons[j].getX() + 60 || x <= balloons[j].getX() - 60 &&
				y >= balloons[j].getY() + 80 || y <= balloons[j].getY() - 80) {
				b.setX(x);
				b.setY(y);
				return;
			}
			else {
				setBalloonPosition(b);
			}
		}
	}

	/**
	 * Response to popping a balloon, updates score and attached text view.
	 * @param view - The view this method was called from
	 */
	protected void pop(View view) {
		ImageButton b = (ImageButton)view;
		setBalloonPosition(b);
		
		if (score == 0) {
			// First click, start a new timer
			startTimer();
		}
		if (!((TextView)  findViewById(R.id.textView_timer)).getText().equals("0")) {
		score++;
		((TextView) findViewById(R.id.textView_clickcount)).setText("Pops: " + score);
		}
	}
	

	/*
	 * ACTIVITY RESPONSE METHODS--------------
	 */

	@Override
	public void onResume() {
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		super.onResume();
		startAppAd.onResume();
		score = 0;
	}

	@Override
	public void onPause() {
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		//Stop CountDownTimer thread
		if (timer!=null) { timer.cancel(); timer = null; }
		super.onPause();
		startAppAd.onPause();

	}

	@Override
	public void onDestroy() {
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		stopTimer();
		super.onDestroy();
	}

	/**
	 * Create a new <code>CountDownTimer</code> to update textViews at 1 second intervals
	 * and display a <code>GameOverDialog</code> when finished
	 */
	private void startTimer() {
		if (timer == null) {
			timer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
		
				@Override
				public void onTick(long millisUntilFinished) {
					TextView timer = (TextView) findViewById(R.id.textView_timer);
					//Update the score down textView with the new time remaining
					timer.setText(Long.toString(millisUntilFinished/1000));
					
				}

				@Override
				public void onFinish() {
					//Set the score down textView to display 0
					((TextView) findViewById(R.id.textView_timer)).
					setText(String.valueOf(0));
					if (hasWindowFocus()) {
						showGameOverDialog();
					} else {
						stopTimer();
						resetGame();
						setScore(0); 
					}
				}
			};
		timer.start(); 
		}
	}
	
	/**
	 * Kill CountDownTimer thread
	 */
	protected void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	/**
	 * Check if the current score is better than the current high score
	 */
	private void checkHighScore() {
		// Get the current high score
		SharedPreferences prefs = getSharedPreferences("highScores", Context.MODE_PRIVATE);
		int highScore = prefs.getInt("highScore", 0);
		// If current score is greater than high score commit the new score
		if (score > highScore) {
			Editor editor = prefs.edit();
			editor.putInt("highScore", score);
			editor.commit();
		}
	}
		
	/**
	 * Create and show fade dialog with game over information
	 */
	protected void showGameOverDialog() {
		// Create custom dialog object
		Dialog dialog = new FadeDialog(new Dialog(this), R.layout.dialog_game_over).getDialog();

		if (!dialog.isShowing()) {
			checkHighScore();
			// Set values to containers
			TextView info = (TextView) dialog.findViewById(R.id.textView_dialogGOInfo);
			info.setText("Your score: "
					+ score
					+ "\nHigh score: "
					+ getSharedPreferences("highScores", Context.MODE_PRIVATE).getInt("highScore", 0)
					+ "\nClicks per second: " + (float) score / 5);
			setDialogButtonListeners(dialog);
			dialog.show();
		}
	}
	
	/**
	 * Resets <code>TextView's</code> to their initial states for a new game
	 */
	private void resetGame() {
		FrameLayout layout = (FrameLayout)findViewById(R.id.frameLayout);
		layout.removeAllViews();
		balloons = null;
		createBalloons();
		TextView startClicking = (TextView) findViewById(R.id.textView_timer);
		startClicking.setText(R.string.start_clicking);
		((TextView) findViewById(R.id.textView_clickcount)).setText(R.string.initial_clicks);	
	}
	
	/**
	 * Sets listeners for the buttons of the game over dialog
	 * @param dialog - The dialog parent of the buttons
	 */
	private void setDialogButtonListeners(final Dialog dialog) {
		((Button) dialog.findViewById(R.id.button_dialogGOClose)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Close button pressed, return user to main activity
				Intent intent = new Intent(GameActivity.this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				dialog.dismiss();
				finish();
			}
		});

		((Button) dialog.findViewById(R.id.button_dialogGONewgame)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//New game button pressed, reset game activity
				stopTimer();
				resetGame();
				setScore(0);
				dialog.dismiss();
			}
		});
		
		//If user presses back button while the dialog has focus, implement same functionality as new game button
		dialog.setOnKeyListener(new Dialog.OnKeyListener() {
			@Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                	stopTimer();
                	resetGame();
    				setScore(0);
    				dialog.dismiss();
                }
                return true;
            }
        });
	}
}
