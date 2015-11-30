package poker.game;

import java.util.ArrayList;
import java.util.List;

public class HandComparator {

    private final List<Hand> hands;

    public HandComparator(List<Hand> hands) {
        this.hands = hands;

        for (Hand hand : this.hands) {
            hand.check();
        }
    }

    public List<Hand> getHands() {
        return this.hands;
    }

    public List<Hand> highestHands() {
        List<Hand> ret = new ArrayList<>();

        return ret;
    }

}
