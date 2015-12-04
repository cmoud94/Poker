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

        this.sortHandsByCardsValue();
        this.sortHandsByHandStrength();
    }

    public List<Hand> getHands() {
        return hands;
    }

    private void sortHandsByHandStrength() {
        for (int i = 0; i < this.getHands().size(); i++) {
            for (int j = 0; j < (this.getHands().size() - 1); j++) {
                int cmp = this.compareHandStrength(this.getHands().get(j), this.getHands().get(j + 1));
                if (cmp == 1) {
                    Hand tmp = this.getHands().get(j);
                    this.getHands().set(j, this.getHands().get(j + 1));
                    this.getHands().set(j + 1, tmp);
                }
            }
        }
    }

    private int compareHandStrength(Hand h1, Hand h2) {
        if (h1.getHandStrength() < h2.getHandStrength()) {
            return 1;
        } else if (h2.getHandStrength() > h2.getHandStrength()) {
            return -1;
        }
        return 0;
    }

    private void sortHandsByCardsValue() {
        for (int i = 0; i < this.getHands().size(); i++) {
            for (int j = 0; j < (this.getHands().size() - 1); j++) {
                int cmp = this.compareCardsValue(this.getHands().get(j), this.getHands().get(j + 1));
                if (cmp == 1) {
                    Hand tmp = this.getHands().get(j);
                    this.getHands().set(j, this.getHands().get(j + 1));
                    this.getHands().set(j + 1, tmp);
                }
            }
        }
    }

    private int compareCardsValue(Hand h1, Hand h2) {
        if (h1.getHandCardsValue() < h2.getHandCardsValue()) {
            return 1;
        } else if (h2.getHandCardsValue() > h2.getHandCardsValue()) {
            return -1;
        }
        return 0;
    }

    private int checkSameHandStrength() {
        int ret = 0;

        for (int i = 0; i < (this.getHands().size() - 1); i++) {
            if (this.getHands().get(i).getHandStrength() == this.getHands().get(i + 1).getHandStrength()) {
                ret += (ret == 0) ? 2 : 1;
            }
        }

        return ret;
    }

    private int checkSameCardsValue(int numOfSameHandStrength) {
        int ret = 0;

        for (int i = 0; i < (numOfSameHandStrength - 1); i++) {
            if (this.getHands().get(i).getHandCardsValue() == this.getHands().get(i + 1).getHandCardsValue()) {
                ret += (ret == 0) ? 2 : 1;
            }
        }

        return ret;
    }

    public List<Hand> highestHands() {
        List<Hand> ret = new ArrayList<>();
        int numSameHS = this.checkSameHandStrength();

        if (numSameHS == 0) {
            ret.add(this.getHands().get(0));
        } else {
            int numSameCV = this.checkSameCardsValue(numSameHS);

            if (numSameCV == 0) {
                ret.add(this.getHands().get(0));
            } else {
                for (int i = 0; i < numSameCV; i++) {
                    ret.add(this.getHands().get(i));
                }
            }
        }

        return ret;
    }

}
