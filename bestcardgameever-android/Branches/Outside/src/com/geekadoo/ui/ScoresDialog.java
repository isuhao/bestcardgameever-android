package com.geekadoo.ui;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.geekadoo.R;
import com.geekadoo.R.id;
import com.geekadoo.logic.Hand;
import com.geekadoo.utils.MutableMediaPlayer;

public class ScoresDialog extends Dialog implements
		android.view.View.OnClickListener {
	public interface OkButtonHandler {

		void afterScoreShown(final Hand hand);
	}

	private static final double cDialogHeightSizeFactor = 0.6;
	private static final double cDialogWidthSizeFactor = 0.85;
	private static final double cGridViewHeightSizeFactor = 0.83;
	private int numColumns = 5;
	private Button okButton;
	private OkButtonHandler handler;
	private Hand hand;
	private Context context;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public ScoresDialog(Context context) {
		super(context);

		this.setTitle(R.string.scoresDialogHeading);
		setContentView(R.layout.scores_view);

		okButton = (Button) findViewById(id.scoresDialogOkButton);
		okButton.setOnClickListener(this);
		// Have the system blur any windows behind this one.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		// Set the number of columns
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setNumColumns(numColumns);
		this.context = context;
	}

	public void showScores(List<String> scores, Hand winningHand,
			OkButtonHandler handler, boolean matchOver) {
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(new TextAdapter(this.getContext(), scores,
				numColumns));
		fixSize(gridView);
		if (handler != null) {
			this.handler = handler;
			this.hand = winningHand;
			if (matchOver) {
				this.setTitle("Match Over, "
						+ (winningHand.isHumanPlayer() ? "You" : winningHand
								.getPlayerName()) + " won!");
				okButton.setText("Start a New Match");

				if (winningHand.isHumanPlayer()) {
					// match won by player
					findViewById(R.id.scoresDialogMainView)
							.setBackgroundResource(
									R.drawable.gamewon);
					MutableMediaPlayer.play(context, R.raw.applause);
				} else {
					// match lost
					findViewById(R.id.scoresDialogMainView)
							.setBackgroundResource(
									R.drawable.gameover);
					MutableMediaPlayer.play(context, R.raw.ooooh);
				}
			} else {
				okButton.setText("Proceed to Next Game");

				if (winningHand.isHumanPlayer()) {
					// game won by player
					findViewById(R.id.scoresDialogMainView)
							.setBackgroundResource(
									R.drawable.whitebubblesback480x800);
				} else {
					// game lost
					findViewById(R.id.scoresDialogMainView)
							.setBackgroundResource(
									R.drawable.whitebubblesback480x800);
				}
			}
		} else {
			// player just looking at scores
			findViewById(R.id.scoresDialogMainView).setBackgroundResource(
					R.drawable.whitebubblesback480x800);
			this.handler = null;
			this.hand = null;
			okButton.setText("Got it!");
		}
		show();
	}

	/**
	 * Set the dialog size to be x% from the screen
	 * 
	 * @param gridview
	 */
	private void fixSize(GridView gridview) {
		// Get the screen dimensions
		Display display = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		LinearLayout dialogMainView = (LinearLayout) findViewById(R.id.scoresDialogMainView);

		// Prepare the dimensions for the dialog box
		width *= cDialogWidthSizeFactor;
		height *= cDialogHeightSizeFactor;

		// Set the dialog dimensions
		dialogMainView.getLayoutParams().height = height;
		dialogMainView.getLayoutParams().width = width;

		// Set the grid view to be smaller than dialog box - and leave space for
		// the button
		gridview.getLayoutParams().height = (int) (height * cGridViewHeightSizeFactor);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.scoresDialogOkButton:
			if (handler != null) {
				handler.afterScoreShown(hand);
			}
			dismiss();
			break;

		default:
			break;
		}
	}
}