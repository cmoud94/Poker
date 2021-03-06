/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int ID;

    private final String name;

    private final List<Card> cards;

    private int money;

    private int inPot;

    private int lastBet;

    private boolean isReady;

    private boolean isPlaying;

    private boolean hasToCall;

    private boolean hasAllIn;

    private Blind blind;

    public Player(int ID, String name, int money) {
        this.ID = ID;
        this.name = name;
        this.cards = new ArrayList<>(2);
        this.money = money;
        this.inPot = 0;
        this.lastBet = 0;
        this.isReady = false;
        this.isPlaying = false;
        this.hasToCall = false;
        this.hasAllIn = false;
        this.blind = Blind.NO_BLIND;
    }

    public int getID() {
        return ID;
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

    public int getLastBet() {
        return lastBet;
    }

    public void setLastBet(int lastBet) {
        this.lastBet = lastBet;
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
        this.setInPot(0);
        this.setLastBet(0);
        this.setPlaying(true);
        this.setHasToCall(false);
        this.setHasAllIn(false);
    }

    public void printCards() {
        for (Card card : this.getCards()) {
            System.out.println("\t" + card.toString());
        }
    }

    public enum Blind {
        NO_BLIND, DEALER, SMALL_BLIND, BIG_BLIND
    }

}
