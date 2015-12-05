import poker.game.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
class Run {

    public static void main(String[] args) {
        game_test();
    }

    private static void deck_test() {
        Deck deck = new Deck();
        for (Card card : deck.getDeck()) {
            System.out.println(card.getSuitAsString() + " - " + card.getRankAsString() + " - " + card.getRank());
        }
    }

    private static void game_test() {
        Game game = new Game(3, 10);
        Player p1 = new Player("Player_1", 1000);
        Player p2 = new Player("Player_2", 1000);
        Player p3 = new Player("Player_3", 1000);

        game.getPlayers().add(p1);
        game.getPlayers().add(p2);
        game.getPlayers().add(p3);

        game.gameLoop();
    }

    private static void hand_test() {
        Hand hand;
        int x = 0;

        while (true) {
            if (x % 1000 == 0) {
                System.out.println("x: " + x);
            }
            x++;
            Table table = new Table(10);
            Deck deck = new Deck();
            Player p1 = new Player("Player 1", 1000);

            p1.getCards().add(deck.dealCard());
            p1.getCards().add(deck.dealCard());

            deck.dealCard();

            for (int j = 0; j < 5; j++) {
                if (j == 3 || j == 4) {
                    deck.dealCard();
                }
                table.getCommunityCards().add(deck.dealCard());
            }

            hand = new Hand(table.getCommunityCards(), p1);

            if (hand.check()) {
                break;
            }
        }

        hand.test();
    }

    private static void handComparator_test() {
        List<Hand> hands = new ArrayList<>();

        Table table = new Table(10);
        Deck deck = new Deck();
        Player p1 = new Player("Player 1", 1000);
        Player p2 = new Player("Player 2", 1000);
        Player p3 = new Player("Player 3", 1000);

        p1.getCards().add(deck.dealCard());
        p2.getCards().add(deck.dealCard());
        p3.getCards().add(deck.dealCard());

        p1.getCards().add(deck.dealCard());
        p2.getCards().add(deck.dealCard());
        p3.getCards().add(deck.dealCard());

        deck.dealCard();

        for (int j = 0; j < 5; j++) {
            if (j == 3 || j == 4) {
                deck.dealCard();
            }
            table.getCommunityCards().add(deck.dealCard());
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
