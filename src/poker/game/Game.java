package poker.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Game {

    private final Table table;

    private final Deck deck;

    private final int numOfPlayers;

    private final List<Player> players;

    private boolean isRunning;

    private int lastBet;

    private int dealer;

    private final BufferedReader br;

    public Game(int numOfPlayers) {
        this.table = new Table(10);
        this.deck = new Deck();
        this.numOfPlayers = numOfPlayers;
        this.players = new ArrayList<>(numOfPlayers);
        this.isRunning = false;
        this.lastBet = 0;
        this.dealer = 0;
        this.br = new BufferedReader(new InputStreamReader(System.in));
    }

    private Table getTable() {
        return this.table;
    }

    public Deck getDeck() {
        return this.deck;
    }

    private int getNumOfPlayers() {
        return this.numOfPlayers;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    private boolean getIsRunning() {
        return this.isRunning;
    }

    private void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    private int getLastBet() {
        return this.lastBet;
    }

    private void setLastBet(int bet) {
        this.lastBet = bet;
    }

    private int getDealer() {
        return this.dealer;
    }

    private void setDealer(int index) {
        this.dealer = index;
    }

    private void initGame() {
        String action;

        if (this.getNumOfPlayers() != this.getPlayers().size()) {
            System.out.println("Player count differs from the one that was set! Aborting...");
            System.exit(0);
        }

        this.setIsRunning(true);
        for (Player player : this.getPlayers()) {
            System.out.println("\033[1m" + player.getName() + "\033[0m are you ready? (\033[1my\033[0m/\033[1mn\033[0m)");
            try {
                action = this.br.readLine();
                switch (action) {
                    case "y":
                        player.setIsReady(true);
                        player.setIsPlaying(true);
                        break;
                    case "n":
                        player.setIsReady(false);
                        player.setIsPlaying(false);
                        break;
                    default:
                        System.out.println("\033[1m[Game] Wrong answer!\033[0m");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!player.getIsReady()) {
                this.setIsRunning(false);
            }
        }

        if (this.getIsRunning()) {
            this.setBlinds();
            this.dealCards();
        }
    }

    private void setBlinds() {
        int dealer, small, big;

        dealer = this.getDealer() % this.getNumOfPlayers();

        for (Player player : this.getPlayers()) {
            player.setBlind(Player.Blind.NO_BLIND);
        }

        if (this.getNumOfPlayers() == 2) {
            small = dealer;
            big = (dealer + 1) % this.getNumOfPlayers();

            this.getPlayers().get(small).setBlind(Player.Blind.SMALL_BLIND);
            this.getPlayers().get(big).setBlind(Player.Blind.BIG_BLIND);
        } else {
            small = (dealer + 1) % this.getNumOfPlayers();
            big = (dealer + 2) % this.getNumOfPlayers();

            this.getPlayers().get(dealer).setBlind(Player.Blind.DEALER);
            this.getPlayers().get(small).setBlind(Player.Blind.SMALL_BLIND);
            this.getPlayers().get(big).setBlind(Player.Blind.BIG_BLIND);
        }
    }

    private void dealCards() {
        for (int i = 0; i < 2; i++) {
            for (Player player : this.getPlayers()) {
                player.addCard(this.getDeck().dealCard());
            }
        }

        // TODO: Smazat
        for (Player player : this.getPlayers()) {
            System.out.println("\t\033[1m" + player.getName() + "\033[0m | " + player.getBlind() + " | Cards: \033[1m" +
                    player.getCard(0).toString() + "\033[0m && \033[1m" +
                    player.getCard(1).toString() + "\033[0m");
        }
    }

    private boolean allCaled() {
        for (Player p : this.getPlayers()) {
            if (p.getHasToCall() && p.getIsPlaying()) {
                return false;
            }
        }

        this.setLastBet(0);
        return true;
    }

    private void bettingLoop(boolean firstBetRound) {
        String action;
        boolean again = false;
        boolean smallBlind = false;
        boolean bigBlind = false;

        while (this.getIsRunning()) {
            for (int i = this.getDealer(); i < this.getPlayers().size(); i++) {

                if (i == this.getDealer() && again) {
                    break;
                }

                if (this.getPlayers().get(i).getIsPlaying()) {
                    if (firstBetRound) {
                        if (this.getPlayers().get(i).getBlind() == Player.Blind.SMALL_BLIND && !smallBlind) {
                            this.actionBetBlind(this.getPlayers().get(i), this.getTable().getBigBlind() / 2);
                            smallBlind = true;
                            continue;
                        }

                        if (this.getPlayers().get(i).getBlind() == Player.Blind.BIG_BLIND && !bigBlind) {
                            this.actionBetBlind(this.getPlayers().get(i), this.getTable().getBigBlind());
                            bigBlind = true;
                            firstBetRound = false;
                            continue;
                        }
                    } else {
                        System.out.println("\033[1m" + this.getPlayers().get(i).getName() + "\033[0m choose your action. (" + this.availableActions(this.getPlayers().get(i)) + ")");
                        try {
                            action = this.br.readLine();
                            this.handleAction(action, this.getPlayers().get(i));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("\t\033[1m" + this.getPlayers().get(i).getName() + "\033[0m not playing this round.");
                    continue;
                }

                if (i == (this.getPlayers().size() - 1)) {
                    i = -1;
                    again = true;
                    continue;
                }
            }

            if (this.allCaled()) {
                this.setLastBet(0);
                break;
            }
        }

        for (Player player : this.getPlayers()) {
            player.setHasToCall(false);
        }

        System.out.println("All players called...");
    }

    private void flop() {
        this.bettingLoop(true);

        this.getDeck().dealCard();

        for (int i = 0; i < 3; i++) {
            this.getTable().addCommunityCard(this.getDeck().dealCard());
        }

        List<Card> cards = this.getTable().getCommunityCards();
        System.out.println("Community cards:");
        for (Card card : cards) {
            System.out.println("\t" + card.toString());
        }

        System.out.println("\033[1mFLOP\033[0m");
    }

    private void fourthStreet() {
        this.bettingLoop(false);

        this.getDeck().dealCard();

        this.getTable().addCommunityCard(this.getDeck().dealCard());


        List<Card> cards = this.getTable().getCommunityCards();
        System.out.println("Community cards:");
        for (Card card : cards) {
            System.out.println("\t" + card.toString());
        }

        System.out.println("\033[1mFOURTH STREET\033[0m");
    }

    private void fifthStreet() {
        this.bettingLoop(false);

        this.getDeck().dealCard();

        this.getTable().addCommunityCard(this.getDeck().dealCard());

        List<Card> cards = this.getTable().getCommunityCards();
        System.out.println("Community cards:");
        for (Card card : cards) {
            System.out.println("\t" + card.toString());
        }

        System.out.println("\033[1mFIFTH STREET\033[0m");
    }

    private void checkWinner() {
        List<Hand> hands = new ArrayList<>();

        for (Player player : this.getPlayers()) {
            if (player.getIsPlaying()) {
                hands.add(new Hand(this.getTable().getCommunityCards(), player));
            }
        }

        HandComparator hc = new HandComparator(hands);
        List<Hand> winners = hc.highestHands();

        if (winners.size() == 1) {
            System.out.println("The winner is: " + winners.get(0).getPlayer().getName() + " HS " + winners.get(0).getHandStrength() + " and CV " + winners.get(0).getHandCardsValue());
        } else {
            for (Hand winner : winners) {
                System.out.println(winner.getPlayer().getName() + "'s HS " + winner.getHandStrength() + " and CV " + winner.getHandCardsValue());
            }
        }
    }

    public void gameLoop() {
        this.initGame();

        this.flop();

        this.fourthStreet();

        this.fifthStreet();

        this.bettingLoop(false);

        this.checkWinner();

        this.setDealer(this.getDealer() + 1);
    }

    private String availableActions(Player player) {
        String ret = "fold";

        if (!player.getHasToCall()) {
            ret += ", check";
        }

        ret += ", call, bet";

        return ret;
    }

    private void handleAction(String action, Player player) {
        switch (action) {
            case "fold":
                this.actionFold(player);
                break;
            case "check":
                this.actionCheck(player);
                break;
            case "call":
                this.actionCall(player);
                break;
            case "bet":
                this.actionBet(player);
                break;
            default:
                System.out.println("\033[1m[Game] Wrong action.\033[0m");
                break;
        }
    }

    private void actionFold(Player player) {
        player.setIsPlaying(false);
        player.setHasToCall(false);
    }

    private void actionCheck(Player player) {
        if (this.availableActions(player).contains("check")) {
            System.out.println("\t" + player.getName() + " has checked.");
        } else {
            System.out.println("\033[1m" + player.getName() + "\033[0m choose new action. (" + this.availableActions(player) + ")");
            try {
                String action = this.br.readLine();
                this.handleAction(action, player);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void actionCall(Player player) {
        if (player.getHasToCall()) {
            int call = this.getLastBet() - player.getInPot();
            call *= (call < 0) ? -1 : 1;
            player.addMoney(-call);
            player.addInPot(call);
            player.setHasToCall(false);
            this.getTable().addToPot(call);

            System.out.println("\t\033[1m" + player.getName() + "\033[0m | You've called '" + call + "'. Money: '" + player.getMoney() + "'. In Pot '" + this.getTable().getPot() + "'.");

            this.afterCall();
        } else {
            System.out.println("\tYou don't have to call.");
        }
    }

    private void afterCall() {
        boolean allCalled = true;

        for (Player p : this.getPlayers()) {
            if (p.getHasToCall() && p.getIsPlaying()) {
                allCalled = false;
            }
        }

        if (allCalled) {
            this.setLastBet(0);
        }
    }

    private void actionBet(Player player) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String money;

        System.out.println("\033[1mHow much you want to bet?\033[0m");

        try {
            money = br.readLine();
            int iMoney = Integer.parseInt(money);
            int bet = iMoney + this.getLastBet();

            player.addMoney(-bet);
            player.addInPot(bet);
            this.getTable().addToPot(bet);

            if (player.getHasToCall()) {
                System.out.println("\tYou've called '" + this.getLastBet() + "' and then bet '" + iMoney + "'. Money: '" + player.getMoney() + "'. In Pot '" + this.getTable().getPot() + "'.");
            } else {
                System.out.println("\tYou've bet '" + iMoney + "'. Money: '" + player.getMoney() + "'. In Pot '" + this.getTable().getPot() + "'.");
            }

            this.setLastBet(iMoney);
            this.afterBet(player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void actionBetBlind(Player player, int blind) {
        player.addMoney(-blind);
        player.addInPot(blind);
        this.getTable().addToPot(blind);

        System.out.println("\t\033[1m" + player.getName() + "\033[0m | " + player.getBlind() + " | You've bet '" + blind + "'. Money: '" + player.getMoney() + "'. In Pot '" + this.getTable().getPot() + "'.");

        this.setLastBet(blind);
        this.afterBet(player);
    }

    private void afterBet(Player player) {
        for (Player p : this.getPlayers()) {
            if (p == player || !p.getIsPlaying()) {
                p.setHasToCall(false);
            }

            if (p.getInPot() != player.getInPot() && player.getIsPlaying()) {
                p.setHasToCall(true);
            }
        }
    }

}
