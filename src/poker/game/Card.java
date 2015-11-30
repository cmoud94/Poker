package poker.game;

public class Card {

    private static final String[] suits = {"Hearts", "Clubs", "Spades", "Diamonds"};

    private static final String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};

    private final int suit;

    private final int rank;

    public Card(int suit, int rank) {
        this.suit = suit;
        this.rank = rank + 2;
    }

    public int getSuit() {
        return this.suit;
    }

    public int getRank() {
        return this.rank;
    }

    public String getSuitAsString() {
        return Card.suits[this.getSuit()];
    }

    public String getRankAsString() {
        return Card.ranks[this.getRank() - 2];
    }

    private String getSuitAsString(Card card) {
        return Card.suits[card.getSuit()];
    }

    private String getRankAsString(Card card) {
        return Card.ranks[card.getRank() - 2];
    }

    public String toString() {
        return this.getSuitAsString(this) + " | " + this.getRankAsString(this);
    }

    public static String[] getSuits() {
        return Card.suits;
    }

    public static String[] getRanks() {
        return Card.ranks;
    }
}
