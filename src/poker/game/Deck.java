package poker.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Deck {

    private final List<Card> deck;

    public Deck() {
        // Create the Deck of Cards
        this.deck = new ArrayList<>();
        for (int s = 0; s < Card.getSuits().length; s++) {
            for (int r = 0; r < Card.getRanks().length; r++) {
                this.getDeck().add(new Card(s, r));
            }
        }

        // Shuffle the deck
        Card tmp;
        for (int i = 0; i < 1000; i++) {
            int index1 = new Random().nextInt(this.getDeck().size());
            int index2 = new Random().nextInt(this.getDeck().size());

            tmp = this.getDeck().get(index1);
            this.getDeck().set(index1, this.getDeck().get(index2));
            this.getDeck().set(index2, tmp);
        }
    }

    public List<Card> getDeck() {
        return deck;
    }

    public Card dealCard() {
        if (this.getDeck().isEmpty()) {
            return null;
        }
        return this.getDeck().remove(0);
    }

}
