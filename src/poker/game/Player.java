package poker.game;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String name;

    private final List<Card> cards;

    private int money;

    private int inPot;

    private boolean isReady;

    private boolean isPlaying;

    private boolean hasToCall;

    private Blind blind;

    public Player(String name, int money) {
        this.name = name;
        this.cards = new ArrayList<>(2);
        this.money = money;
        this.inPot = 0;
        this.isReady = false;
        this.isPlaying = false;
        this.hasToCall = false;
        this.blind = Blind.NO_BLIND;
    }

    public String getName() {
        return this.name;
    }

    public List<Card> getCards() {
        return this.cards;
    }

    public Card getCard(int index) {
        return this.cards.get(index);
    }

    public void addCard(Card card) {
        this.cards.add(card);
    }

    public int getMoney() {
        return this.money;
    }

    public void addMoney(int money) {
        this.money += money;
    }

    public int getInPot() {
        return this.inPot;
    }

    public void addInPot(int money) {
        this.inPot += money;
    }

    public boolean getIsReady() {
        return this.isReady;
    }

    public void setIsReady(boolean isReady) {
        this.isReady = isReady;
    }

    public boolean getIsPlaying() {
        return this.isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean getHasToCall() {
        return this.hasToCall;
    }

    public void setHasToCall(boolean bool) {
        this.hasToCall = bool;
    }

    public Blind getBlind() {
        return this.blind;
    }

    public void setBlind(Blind blind) {
        this.blind = blind;
    }

    /*
    First: DEALER
    Second: SMALL_BLIND
    Third: BIG_BLIND
    Others: NO_BLIND
     */
    public enum Blind {
        NO_BLIND, DEALER, SMALL_BLIND, BIG_BLIND
    }

}
