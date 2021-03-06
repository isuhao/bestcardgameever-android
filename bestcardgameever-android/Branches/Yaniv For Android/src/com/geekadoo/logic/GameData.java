package com.geekadoo.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.geekadoo.db.YanivPersistenceAdapter;
import com.geekadoo.exceptions.YanivPersistenceException;
import com.geekadoo.logic.ai.BasicYanivStrategy;
import com.geekadoo.logic.ai.StupidYanivStrategy;
import com.geekadoo.logic.ai.VeryStupidYanivStrategy;

/**
 * a Serializable Object that holds the game data
 * 
 * @author Elad
 */
public class GameData implements Serializable {

	public enum GameDifficultyEnum {
		EASY, NORMAL, HARD
	};

	private static final long serialVersionUID = -4564493907808892053L;

	public static final int YANIV_NUM_CARDS = 5;
	public static final int START_GAME = 1000;
	public static final int RESUME_GAME = 2000;
	public static final int DEFAULT_STARTING_PLAYER = 0;
	public static final String STATE = "state";
	public static final String LOG_TAG = "gameData";
	public static final String[] PLAYER_NAMES = { "Player", "Sivan", "Iddo",
			"Elad" };

	public static final int ASSAF_PENALTY = 30;

	public static final int WIN_SCORE = 0;

	public static final Integer MATCH_LOSING_SCORE = 200;

	public static final String PLAYER_NAME = "playerName";

	public static final String DIFFICULTY_LEVEL = "difficultyLevel";

	/** This flag is used for debugging the game */
	private final boolean disableOpponentsYanivAbility = false;

	public static enum GAME_STATES {
		start, resume, end
	}

	public static enum GAME_INPUT_MODE {
		paused, running
	}

	private static GameData gameData = null;

	// Player 1 - Logic elements
	private PlayerHand p1Hand;
	private String p1SelectedName;
	// Opponent 1 - Logic elements
	private OpponentHand o1Hand;
	// Opponent 2 - Logic elements
	private OpponentHand o2Hand;
	// Opponent 3 - Logic elements
	private OpponentHand o3Hand;
	// Thrown Cards - Logic elements
	private ThrownCards thrownCards;
	// Turn - Logic elements
	private Turn<Hand> turn;
	// Deck - Logic elements
	SingleDeck deck;
	private ArrayList<Hand> playersInOrder;

	private boolean firstDeal;

	private int currentGameNumber;

	private static YanivPersistenceAdapter persistencAdapter;

	private GAME_INPUT_MODE mode;

	private GameDifficultyEnum gameDifficulty ;

	private GameData(GameDifficultyEnum gameDifficulty) {
		if (disableOpponentsYanivAbility) {
			Log.e(LOG_TAG,
					"*************************\n disableOpponentsYanivAbility IS TRUE!!!!!\n****************");
		}

		// Array of order of players in the beginning of the game (p1 is first)
		playersInOrder = new ArrayList<Hand>();
		p1Hand = new PlayerHand();
		playersInOrder.add(p1Hand);
		this.setGameDifficulty(gameDifficulty);
		switch (gameDifficulty) {
		case EASY:
			o1Hand = new OpponentHand(new VeryStupidYanivStrategy());
			o2Hand = new OpponentHand(new VeryStupidYanivStrategy());
			o3Hand = new OpponentHand(new VeryStupidYanivStrategy());
			break;
		case NORMAL:
			o1Hand = new OpponentHand(new StupidYanivStrategy());
			o2Hand = new OpponentHand(new StupidYanivStrategy());
			o3Hand = new OpponentHand(new StupidYanivStrategy());
			break;
		case HARD:
			o1Hand = new OpponentHand(new BasicYanivStrategy());
			o2Hand = new OpponentHand(new BasicYanivStrategy());
			o3Hand = new OpponentHand(new BasicYanivStrategy());
			break;
		default:
			break;
		}
		playersInOrder.add(o1Hand);
		playersInOrder.add(o2Hand);
		playersInOrder.add(o3Hand);
		currentGameNumber = 0;
		startNewGame(p1Hand, true);
	}

	public static GameData getInstance(boolean firstRun,
			GameDifficultyEnum difficulty, Context appCtx) {
		persistencAdapter = new YanivPersistenceAdapter(appCtx);

		if (!firstRun) {
			// Existing game, read it from the persistence provider
			try {
				gameData = persistencAdapter.getSavedGameData(difficulty);
			} catch (YanivPersistenceException e) {
				// TODO: pop up a sorry box and report this problem... - could
				// not load, creating new.
				Log.e(LOG_TAG,
						"GameData could not load state, creating new gamedata");
				gameData = createNewGame(difficulty);
			}
		} else {
			// First run: Override existing game in memory
			gameData = createNewGame(difficulty);
		}
		return gameData;
	}

	public void save(Context applicationContext)
			throws YanivPersistenceException {
		persistencAdapter.setSavedGameData(this);
	}

	public static GameData getInstance() {
		return gameData;
	}

	public static GameData createNewGame(GameDifficultyEnum gameDifficulty) {
		gameData = new GameData(gameDifficulty);
		return gameData;
	}

	public ThrownCards getThrownCards() {
		return thrownCards;
	}

	public Turn<Hand> getTurn() {
		return turn;
	}

	public SingleDeck getDeck() {
		return deck;
	}

	public PlayerHand getP1Hand() {
		return p1Hand;
	}

	public OpponentHand getO1Hand() {
		return o1Hand;
	}

	public OpponentHand getO2Hand() {
		return o2Hand;
	}

	public OpponentHand getO3Hand() {
		return o3Hand;
	}

	public boolean isFirstDeal() {
		return firstDeal;
	}

	public void setFirstDeal(boolean firstDeal) {
		this.firstDeal = firstDeal;
	}

	public ArrayList<Hand> getPlayersInOrder() {
		return playersInOrder;
	}

	public void setPlayersInOrder(ArrayList<Hand> playersInOrder) {
		this.playersInOrder = playersInOrder;
	}

	public List<String> getScoreRepresentation() {
		List<String> retVal = new ArrayList<String>();

		// First row is column titles: game number & player names
		retVal.add("Game");
		for (Hand h : playersInOrder) {
			retVal.add((String) h.getPlayerName());
		}

		// Contents: actual scores
		// For each game in history
		for (int gameNumber = 0; gameNumber < getCurrentGameNumber(); gameNumber++) {
			// Game count is zero based, so when printing we need to add 1
			retVal.add(String.valueOf(gameNumber + 1));
			// Get history for that game
			for (Hand hand : playersInOrder) {
				// For each player
				retVal.add(hand.getScoreHistory().get(gameNumber).toString());
			}
		}
		// Sum
		retVal.add("Totals:");
		for (Hand h : playersInOrder) {
			// Get the sum for each player
			retVal.add(h.getSumScores().toString());
		}

		return retVal;
	}

	public int getCurrentGameNumber() {
		return currentGameNumber;
	}

	public void addRoundScores() {
		for (Hand h : playersInOrder) {
			if (h.isWasAssaffed()) {
				h.setWasAssaffed(false);
				h.addToScoreHistory(ASSAF_PENALTY + h.sumCards());
			} else {
				if (h.isWonRound()) {
					h.setWonRound(false);
					h.addToScoreHistory(WIN_SCORE);
				} else {
					h.addToScoreHistory(h.sumCards());
				}
			}

		}
	}

	public void startNewGame(Hand startingHand, boolean isFirstGameInMatch) {
		if (!isFirstGameInMatch) {
			// advance game number
			currentGameNumber++;
			// reset hands
			p1Hand.reset();
			o1Hand.reset();
			o2Hand.reset();
			o3Hand.reset();
			// Set the starting hand
			turn.newRound(playersInOrder.indexOf(startingHand), false);
		} else {
			firstDeal = true;
			this.turn = new Turn<Hand>(playersInOrder,
					playersInOrder.indexOf(startingHand));
		}
		this.thrownCards = new ThrownCards();
		this.deck = new SingleDeck();
		this.mode = GAME_INPUT_MODE.running;
	}

	public void setGameInputMode(GAME_INPUT_MODE mode) {
		this.mode = mode;
	}

	public GAME_INPUT_MODE getGameInputMode() {
		return mode;
	}

	/**
	 * This method will use the thrown cards to re-create the deck (after
	 * shuffling them)
	 */
	public void refillDeck() {
		// Obtain the cards to be re-used
		List<PlayingCard> usedCards = getThrownCards().popAllButTopFive();

		Collections.shuffle(usedCards);
		getDeck().addCards(usedCards);
	}

	/**
	 * Returns true if the match is one by anyone
	 * 
	 * @return
	 */
	public boolean isMatchWon() {
		boolean retVal = false;
		for (int i = 0; i < playersInOrder.size() && !retVal; i++) {
			Hand hand = playersInOrder.get(i);
			retVal = hand.getSumScores() > MATCH_LOSING_SCORE;
		}
		return retVal;
	}

	public void setP1SelectedName(String p1SelectedName) {
		this.p1SelectedName = p1SelectedName;
	}

	public String getP1SelectedName() {
		return p1SelectedName;
	}

	public void setGameDifficulty(GameDifficultyEnum gameDifficulty) {
		this.gameDifficulty = gameDifficulty;
	}

	public GameDifficultyEnum getGameDifficulty() {
		return gameDifficulty;
	}
}