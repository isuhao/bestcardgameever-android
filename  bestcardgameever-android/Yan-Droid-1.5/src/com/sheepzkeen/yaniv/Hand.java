package com.sheepzkeen.yaniv;

import java.util.ArrayList;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Represents a hand of cards
 * This abstract class is an implementation of a hand of cards that holds the cards and the UI elements
 * and allows for basic hand actions such as pick up and drop.
 * it also dictates the visibility and other UI properties
 * @author Elad
 *
 */
public abstract class Hand implements Comparable<Hand> {
	protected static final int YANIV_AMOUNT = 7;
	protected PlayingCard[] cards;
	int firstFreeLocation;
	private PlayingCard[] compactedArr;
	//private boolean[] cardsSelected = {false,false,false,false,false};
	private ImageView[] cardsViews;
	public ImageView[] getCardsViews() {
		return cardsViews;
	}
	public View getContainer() {
		return container;
	}

	private View container;
	private String name;
	private boolean canDrop;
	private boolean canPickup;
	private boolean cardVisibility;
	
	protected YanivStrategy strategy;
	
	public Hand(View container, ImageView[] cardsViews, TextView name) {

		this.cards = new PlayingCard[Yaniv.YANIV_NUM_CARDS];
		firstFreeLocation = 0;
		this.container = container;
		this.cardsViews = cardsViews;
		this.name = (String) name.getText();
		this.canDrop = true;
		this.canPickup = false;
	}
	
	/**
	 * Hand picks up a card
	 * first decide if should pick up from deck or thrown cards
	 * then do it
	 * AI players will pick up according to AI
	 * human player will pick up on his own
	 * @param deck
	 */
	public void pickup(PlayingCardsCollection thrownCards, PlayingCardsCollection deck, PickupMethod method){
		switch (method) {
			case fromDeck:
				//pickup from deck
				addCard(deck.popTopCard());
				break;
			case fromThrown:
				//pickup from thrown
				addCard(thrownCards.popTopCard());
				break;
			case decidePickup:
				//decide and add
				if (strategy!=null){
					addCard(strategy.decidePickUp(thrownCards,deck));
				}else{
					addCard(deck.popTopCard());
				}
				break;
		}
	}
	
	public PlayingCard getCardByLocation(int location) {
		return cards[location];
	}

	/**
	 * Select which cards, and drops them, in case it is a human player, it simply drops the cards he selected
	 * (after verifying they can be dropped with rules) in case of an opponent, AI decides it
	 * @return an array of type {@link PlayingCard} which contains the cards to drop 
	 * @throws InvalidYanivException when cards selected cannot be dropped
	 */
	public PlayingCard[] drop() throws InvalidYanivException{
		selectCardsToDrop();
		return dropSelected();
	}

	/**
	 * Selects which cards will be dropped and moves them to the 'cardsSelected' array 
	 * @throws InvalidYanivException 
	 */
	protected abstract void selectCardsToDrop() throws InvalidYanivException;
	
	/**
	 * returns the selected cards so that they can be thrown
	 * @return The cards to be thrown
	 */
	private PlayingCard[] dropSelected() {
		ArrayList<PlayingCard> cardsToDrop = new ArrayList<PlayingCard>();
		for (int cardIndex = 0; cardIndex < cards.length; cardIndex++) {
			if(cards[cardIndex] != null && cards[cardIndex].isSelected()){
				cardsToDrop.add(cards[cardIndex]);
				cards[cardIndex]=null;
			}
		}
		compactHand();
		resetSelectedCards();
		PlayingCard[] retval = new PlayingCard[cardsToDrop.size()];
		cardsToDrop.toArray(retval);
		return retval;
	}


	public boolean canYaniv(){
		
		if (countCards()<=YANIV_AMOUNT) {
			return shouldYaniv();
		}else{
			return false;
		}
		
	}
	
	protected abstract boolean shouldYaniv();
	
	public void doYaniv(){
		if(shouldYaniv()){
			//do yaniv...
			Log.e("Yaniv", "This is where the yaniv would have been performed by the AI");
		}
		//Finish game:
		//TODO: something here...
		
	}
	
//	public interface onYanivPerformedListener{
//		void onYanivPerformed();
//	}
	
//	/**
//	 * plays a turn
//	 * @param currentDeck the deck in its current state (missing cards and all)
//	 * @return true iff the player has declared Yaniv and the game has ended
//	 */
//	public boolean playTurn(PlayingCardsCollection currentDeck) {
//		if(canYaniv())
//		{
//			doYaniv();
//			return true;
//		}
//		else
//		{
//			pickup();
//			drop();
//			return false;
//		}
//		
//	}

	/**
	 * returns true for player and false for opponent	
	 * @return true if this is an instance of player, false otherwise
	 */
	public boolean shouldCardsBeShown(){
		return cardVisibility;
	}

	public void setShouldCardsBeShown(boolean cardVisibility){
		this.cardVisibility = cardVisibility;
	}
	
	
	/**
	 * adds a card to the hand based on the first free location (@see firstFreeLocation)
	 * returns the slot it was added in
	 * @param card
	 * @return
	 */
	protected int addCard(PlayingCard card) {
		cards[firstFreeLocation] = card;
		return firstFreeLocation++;
		
	}
	
	/**
	 * compacting-filling the first cards and leaving the blanks towards the end of the hand
	 * for instance if the hand was: {3h,null,null,joker,9h}
	 * after compacting it will be: {3h,joker,9h,null,null}
	 */
	protected void compactHand(){
		compactedArr = new PlayingCard[Yaniv.YANIV_NUM_CARDS];
		int idxInComapctedArr=0;
		for (int idxInCards = 0; idxInCards < cards.length; idxInCards++) {
			if (cards[idxInCards]!=null) {
				compactedArr[idxInComapctedArr++] = cards[idxInCards];
			}
		}
		this.cards = compactedArr;
		this.firstFreeLocation = idxInComapctedArr;
	}
	/**
	 * resets all cards selected status to false
	 */
	protected void resetSelectedCards(){
		for (PlayingCard card : cards) {
			if(card != null){
				card.setSelected(false);
			}
		}
	}
	
	public boolean isCardSelected(int cardIndex){
		return cards[cardIndex].isSelected();
	}
	
	public boolean hasSelectedCard(){
		for (PlayingCard card : cards) {
			if (!(card == null) && card.isSelected())
				return true;
		}
		return false;
	}

	public void changeSelectionStateOnCard(int cardIndex) {
		cards[cardIndex].setSelected(!cards[cardIndex].isSelected());
	}
	
	/**
	 * will return true for a human player
	 * @return True iff the player is human
	 */
	abstract public boolean isAwaitingInput();
	
	public String getPlayerName() {
		
		return this.name;
	}
	public boolean getCanDrop(){
		return canDrop;
	}
	public void setCanDrop(boolean canDrop) {
		this.canDrop = canDrop;
	}
	public boolean getCanPickup() {
		return this.canPickup;
	}
	public void setCanPickup(boolean canPickup) {
		this.canPickup = canPickup;
	}
	
	public int countCards() {
		int retVal = 0;
		for (PlayingCard card : cards) {
			if (card != null)
				retVal+=card.getCountValue();
		}
		return retVal;

	}
	
	@Override
	public int compareTo(Hand another) {
		return this.countCards() - another.countCards();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Hand " + name + ". [");
		for (PlayingCard card : cards) {
			if(card!=null){
				sb.append(card+" ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

}
