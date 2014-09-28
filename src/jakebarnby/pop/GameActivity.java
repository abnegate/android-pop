package jakebarnby.pop;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

	private static final int[] BALLOONS_BY_LEVEL = {12, 10, 8,  6,  5,  4,  3,  2,  2,  1,  100};
	private static final int[] SCORE_BY_LEVEL =    {60, 60, 70, 70, 70, 60, 50, 40, 45, 25, 300};
	private static final int[] IMAGES = {R.drawable.balloon_blue, R.drawable.balloon_red, R.drawable.balloon_green};
	private static final long COUNTDOWN_TIME = 10900;
	private static final float width = 320.0f;
	private static final float height = 320.0f;
	
	private int currentBalloons;
	private int score = 0;
	private int currentLevel = 0;
	
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
		currentBalloons = GameActivity.BALLOONS_BY_LEVEL[currentLevel];
		createBalloons();
	}
	
	/**
	 * Create, initialize and display the balloons to be popped on screen
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
		score++;
		((TextView) findViewById(R.id.textView_clickcount)).setText("Level " + (currentLevel+1) + "   Pops: " + score + "/" + GameActivity.SCORE_BY_LEVEL[currentLevel]);
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
					//Update timer text
					((TextView) findViewById(R.id.textView_timer)).setText(Long.toString(millisUntilFinished/1000));	
				}

				@Override
				public void onFinish() {
					// Show the dialog if the user is still in this activity, otherwise reset the game
					((TextView) findViewById(R.id.textView_timer)).setText(String.valueOf(0));
					if (hasWindowFocus()) { showGameOverDialog(); } 
					else { resetGame(); }
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
		final Dialog dialog = new FadeDialog(new Dialog(this), R.layout.dialog_game_over).getDialog();

		if (!dialog.isShowing()) {
			setDialogButtons(dialog);
			checkHighScore();
			// Set values to containers
			TextView title = (TextView) dialog.findViewById(R.id.textView_dialogGOTitle);
			title.setText("Level " + (currentLevel+1));
			
			TextView info = (TextView) dialog.findViewById(R.id.textView_dialogGOInfo);
			info.setText("Your score: "
					+ score + "/" + GameActivity.SCORE_BY_LEVEL[currentLevel]
					+ "\nHigh score: "
					+ getSharedPreferences("highScores", Context.MODE_PRIVATE).getInt("highScore", 0)
					+ "\nPops per second: " + (float) score / 10);
			dialog.show();
		}
		
		//Implement a short timer which enables the buttons on completion so users don;t accidentally close the dialog
		new CountDownTimer(2000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {}

			@Override
			public void onFinish() {setDialogButtonListeners(dialog);}
		}.start();
	}
	
	/**
	 * Sets the dialog buttons based on the players score
	 * @param d The dialog to set buttons for
	 */
	private void setDialogButtons(Dialog d) {
		//Find button layout and initialize button 
		LinearLayout layout = (LinearLayout) d.findViewById(R.id.dialog_buttonBar);
		Button b = new Button(d.getContext());
		b.setBackgroundResource(R.layout.menu_button);
		b.setBackgroundColor(Color.parseColor("#000000"));
		b.setTextColor(Color.parseColor("#FFFFFF"));
		b.setTypeface(null, Typeface.BOLD);
		b.setId(100);
		b.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.3f));
		//Check if users score was high enough for this level
		if (score >= GameActivity.SCORE_BY_LEVEL[currentLevel]) {
			b.setText("Next Level");
		} else {
			b.setText("Try Again");
		}
		layout.addView(b);
	}
	
	/**
	 * Resets the game screen
	 */
	private void resetGame() {
		stopTimer();
		// Remove and recreate balloons
		FrameLayout layout = (FrameLayout)findViewById(R.id.frameLayout);
		layout.removeAllViews();
		balloons = null;
		createBalloons();
		// Reset text
		TextView startClicking = (TextView) findViewById(R.id.textView_timer);
		startClicking.setText(R.string.start_clicking);
		TextView clickCount = (TextView) findViewById(R.id.textView_clickcount);
		clickCount.setText("Level " + (currentLevel+1) + "   Pops: 0" + "/" + GameActivity.SCORE_BY_LEVEL[currentLevel]);	
		setScore(0);
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
		
		final Button b = ((Button) dialog.findViewById(100));
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//User made it to the next level, increase the level and adjust balloons
				if (b.getText().equals("Next Level")) {
					currentLevel++;
					currentBalloons = GameActivity.BALLOONS_BY_LEVEL[currentLevel];	
				}
				resetGame();
				dialog.dismiss();
			}
		});
		
		//If user presses back button while the dialog has focus, implement same functionality as new game button
		dialog.setOnKeyListener(new Dialog.OnKeyListener() {
			@Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                	resetGame();
    				dialog.dismiss();
                }
                return true;
            }
        });
	}
}
