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

    private boolean hasAllIn;

    private Blind blind;

    public Player(String name, int money) {
        this.name = name;
        this.cards = new ArrayList<>(2);
        this.money = money;
        this.inPot = 0;
        this.isReady = false;
        this.isPlaying = false;
        this.hasToCall = false;
        this.hasAllIn = false;
        this.blind = Blind.NO_BLIND;
    }

    public String getName() {
        return name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getInPot() {
        return inPot;
    }

    public void setInPot(int inPot) {
        this.inPot = inPot;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isHasToCall() {
        return hasToCall;
    }

    public void setHasToCall(boolean hasToCall) {
        this.hasToCall = hasToCall;
    }

    public boolean isHasAllIn() {
        return hasAllIn;
    }

    public void setHasAllIn(boolean hasAllIn) {
        this.hasAllIn = hasAllIn;
    }

    public Blind getBlind() {
        return blind;
    }

    public void setBlind(Blind blind) {
        this.blind = blind;
    }

    public void newRound() {
        this.inPot = 0;
        this.setPlaying(true);
        this.setHasToCall(false);
        this.setHasAllIn(false);
    }

    public enum Blind {
        NO_BLIND, DEALER, SMALL_BLIND, BIG_BLIND
    }

}
