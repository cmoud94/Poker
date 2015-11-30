import poker.game.*;

import java.util.ArrayList;
import java.util.List;

class Run {

    private static long time(long start, long end) {
        return end - start;
    }

    public static void main(String[] args) {
        handComparator_test();
    }

    private static void deck_test() {
        Deck deck = new Deck();

        while (!deck.isEmpty()) {
            Card card = deck.dealCard();
            System.out.println("Suit: " + card.getSuit() + "\t\tRank: " + card.getRank() + "\t\t" + card.toString());
        }
    }

    private static void game_test() {
        Game game = new Game(3);
        Player p1 = new Player("Player_1", 1000);
        Player p2 = new Player("Player_2", 1000);
        Player p3 = new Player("Player_3", 1000);

        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);

        game.gameLoop();
    }

    private static void hand_test() {
        Hand hand = null;
        int x = 0;

        System.out.println("x: " + ++x);
        Table table = new Table(10);
        Deck deck = new Deck();
        Player p1 = new Player("Player 1", 1000);

        p1.addCard(deck.dealCard());
        p1.addCard(deck.dealCard());

        deck.dealCard();

        for (int j = 0; j < 5; j++) {
            if (j == 3 || j == 4) {
                deck.dealCard();
            }
            table.addCommunityCard(deck.dealCard());
        }

        hand = new Hand(table.getCommunityCards(), p1);

        hand.check();
        hand.test();
    }

    private static void handComparator_test() {
        List<Hand> hands = new ArrayList<>();

        Table table = new Table(10);
        Deck deck = new Deck();
        Player p1 = new Player("Player 1", 1000);
        Player p2 = new Player("Player 2", 1000);
        Player p3 = new Player("Player 3", 1000);

        p1.addCard(deck.dealCard());
        p2.addCard(deck.dealCard());
        p3.addCard(deck.dealCard());

        p1.addCard(deck.dealCard());
        p2.addCard(deck.dealCard());
        p3.addCard(deck.dealCard());

        deck.dealCard();

        for (int j = 0; j < 5; j++) {
            if (j == 3 || j == 4) {
                deck.dealCard();
            }
            table.addCommunityCard(deck.dealCard());
        }

        hands.add(new Hand(table.getCommunityCards(), p1));
        hands.add(new Hand(table.getCommunityCards(), p2));
        hands.add(new Hand(table.getCommunityCards(), p3));

        for (Hand hand : hands) {
            hand.check();
            hand.test();
            System.out.println("------------------------------");
        }

        HandComparator hc = new HandComparator(hands);

        List<Hand> winners = hc.highestHands();

        for (Hand winner : winners) {
            System.out.println("Winner is: " + winner.getPlayer().getName() + " HS: " + winner.getHandStrength() + " CV: " + winner.getHandCardsValue());
        }
    }

}
