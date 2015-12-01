package poker.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Game {

    private final int numOfPlayers;

    private final List<Player> players;

    private final BufferedReader br;

    private final int bigBlind;

    private Table table;

    private Deck deck;

    private boolean isRunning;

    private int lastBet;

    private int dealer;

    public Game(int numOfPlayers, int bigBlind) {
        this.numOfPlayers = numOfPlayers;
        this.players = new ArrayList<>(numOfPlayers);
        this.br = new BufferedReader(new InputStreamReader(System.in));
        this.bigBlind = bigBlind;
        this.table = new Table(bigBlind);
        this.deck = new Deck();
        this.isRunning = false;
        this.lastBet = 0;
        this.dealer = 0;
    }

    public int getNumOfPlayers() {
        return numOfPlayers;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public BufferedReader getBr() {
        return br;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public int getLastBet() {
        return lastBet;
    }

    public void setLastBet(int lastBet) {
        this.lastBet = lastBet;
    }

    public int getDealer() {
        return dealer;
    }

    public void setDealer(int dealer) {
        this.dealer = dealer;
    }

    private void initGame() {
        String action;

        if (this.getNumOfPlayers() != this.getPlayers().size()) {
            System.out.println("\033[1m[Game]\033[0m Player count differs from the one that was set! Aborting...");
            System.exit(0);
        }

        this.setRunning(true);
        for (Player player : this.getPlayers()) {
            System.out.println("\033[1m" + player.getName() + "\033[0m are you ready? (\033[1my\033[0m/\033[1mn\033[0m)");
            try {
                action = this.getBr().readLine();
                switch (action) {
                    case "y":
                        player.setReady(true);
                        player.setPlaying(true);
                        break;
                    case "n":
                        player.setReady(false);
                        player.setPlaying(false);
                        break;
                    default:
                        System.out.println("\033[1m[Game]\033[0m Wrong answer!");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!player.isReady()) {
                this.setRunning(false);
            }
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
        for (Player player : this.getPlayers()) {
            player.getCards().clear();
        }

        for (int i = 0; i < 2; i++) {
            for (Player player : this.getPlayers()) {
                player.getCards().add(this.getDeck().dealCard());
            }
        }
    }

    private boolean allCaled() {
        for (Player p : this.getPlayers()) {
            if (p.isHasAllIn() && p.isPlaying()) {
                return false;
            }
        }

        this.setLastBet(0);
        return true;
    }

    private void bettingLoop(boolean firstBetRound) {
        String action;
        boolean again;

        while (this.isRunning()) {
            again = false;
            for (int i = this.getDealer(); i < this.getPlayers().size(); i++) {

                if (i == this.getDealer() && again) {
                    break;
                }

                if (this.getPlayers().get(i).isPlaying()) {
                    if (firstBetRound) {
                        if (this.getPlayers().get(i).getBlind() == Player.Blind.SMALL_BLIND) {
                            this.actionBetBlind(this.getPlayers().get(i), this.getTable().getBigBlind() / 2);
                            continue;
                        }

                        if (this.getPlayers().get(i).getBlind() == Player.Blind.BIG_BLIND) {
                            this.actionBetBlind(this.getPlayers().get(i), this.getTable().getBigBlind());
                            firstBetRound = false;
                            continue;
                        }
                    } else {
                        System.out.println("\033[1m" + this.getPlayers().get(i).getName() + "\033[0m choose your action. (" + this.availableActions(this.getPlayers().get(i)) + ")");
                        try {
                            action = this.getBr().readLine();
                            this.handleAction(action, this.getPlayers().get(i));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("\033[1m" + this.getPlayers().get(i).getName() + "\033[0m not playing this round.");
                    continue;
                }

                if (i == (this.getPlayers().size() - 1)) {
                    i = -1;
                    again = true;
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

        System.out.println("\033[1m[Game]\033[0m All players called...");
    }

    private void newRound() {
        this.setDeck(new Deck());
        this.setTable(new Table(this.getBigBlind()));

        this.setDealer(this.getDealer());
        this.setBlinds();
        this.dealCards();

        for (Player player : this.getPlayers()) {
            player.newRound();
            System.out.println("\t\033[1m" + player.getName() + "\033[0m | " + player.getBlind() + " | Cards: \033[1m" +
                    player.getCards().get(0).toString() + "\033[0m && \033[1m" +
                    player.getCards().get(1).toString() + "\033[0m");
        }
        System.out.println();
    }

    private void flop() {
        this.bettingLoop(true);
        this.getDeck().dealCard();

        for (int i = 0; i < 3; i++) {
            this.getTable().getCommunityCards().add(this.getDeck().dealCard());
        }

        this.getTable().printCommunityCards();
        System.out.println("\033[1mFLOP\033[0m");
    }

    private void turn() {
        this.bettingLoop(false);
        this.getDeck().dealCard();
        this.getTable().getCommunityCards().add(this.getDeck().dealCard());

        this.getTable().printCommunityCards();
        System.out.println("\033[1mTURN\033[0m");
    }

    private void river() {
        this.bettingLoop(false);
        this.getDeck().dealCard();
        this.getTable().getCommunityCards().add(this.getDeck().dealCard());

        this.getTable().printCommunityCards();
        System.out.println("\033[1mRIVER\033[0m");
    }

    private void checkWinner() {
        List<Hand> hands = new ArrayList<>();

        for (Player player : this.getPlayers()) {
            if (player.isPlaying()) {
                hands.add(new Hand(this.getTable().getCommunityCards(), player));
            }
        }

        HandComparator hc = new HandComparator(hands);
        List<Hand> winners = hc.highestHands();

        for (Hand winner : winners) {
            System.out.printf("Winner is %s HS: %d CV: %d\n", winner.getPlayer().getName(), winner.getHandStrength(), winner.getHandCardsValue());
        }
    }

    public void gameLoop() {
        this.initGame();

        while ((this.isRunning())) {
            this.newRound();
            this.flop();
            this.turn();
            this.river();
            this.bettingLoop(false);
            this.checkWinner();
            this.setDealer(this.getDealer() + 1);
        }
    }

    private String availableActions(Player player) {
        String ret = "fold";

        if (!player.isHasToCall()) {
            ret += ", check";
        }

        ret += ", call, bet, all-in";

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
            case "all-in":
                this.actionAllIn(player);
                break;
            default:
                System.out.println("\033[1m[Game]\033[0m Wrong action.");
                break;
        }
    }

    private void actionFold(Player player) {
        player.setPlaying(false);
        player.setHasToCall(false);
    }

    private void actionCheck(Player player) {
        if (this.availableActions(player).contains("check")) {
            System.out.println("\t" + player.getName() + " has checked.");
        } else {
            System.out.println("\033[1m" + player.getName() + "\033[0m choose new action. (" + this.availableActions(player) + ")");
            try {
                String action = this.getBr().readLine();
                this.handleAction(action, player);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void actionCall(Player player) {
        if (player.isHasToCall()) {
            int call = this.getLastBet() - player.getInPot();
            call *= (call < 0) ? -1 : 1;
            player.setMoney(player.getMoney() - call);
            player.setInPot(player.getInPot() + call);
            player.setHasToCall(false);
            this.getTable().setPot(this.getTable().getPot() + call);

            System.out.println("\t\033[1m" + player.getName() + "\033[0m | You've called '" + call + "'. Money: '" + player.getMoney() + "'. In Pot '" + this.getTable().getPot() + "'.");

            this.afterCall();
        } else {
            System.out.println("\tYou don't have to call.");
        }
    }

    private void afterCall() {
        boolean allCalled = true;

        for (Player p : this.getPlayers()) {
            if (p.isHasToCall() && p.isPlaying()) {
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

        System.out.println("\033[1m[Game]\033[0m How much you want to bet?");

        try {
            money = br.readLine();
            int iMoney = Integer.parseInt(money);
            int bet = iMoney + this.getLastBet();

            player.setMoney(player.getMoney() - bet);
            player.setInPot(player.getInPot() + bet);
            this.getTable().setPot(this.getTable().getPot() + bet);

            if (player.isHasToCall()) {
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

    private void actionAllIn(Player player) {
        int bet = player.getMoney();

        player.setMoney(player.getMoney() - bet);
        player.setInPot(player.getInPot() + bet);
        this.getTable().setPot(this.getTable().getPot() + bet);

        System.out.println("\tYou've bet all your money. Money: '" + player.getMoney() + "'. In pot: '" + this.getTable().getPot() + "'.");

        this.setLastBet(bet);
        this.afterBet(player);
    }

    private void actionBetBlind(Player player, int blind) {
        player.setMoney(player.getMoney() - blind);
        player.setInPot(player.getInPot() + blind);
        this.getTable().setPot(this.getTable().getPot() + blind);

        System.out.println("\t\033[1m" + player.getName() + "\033[0m | " + player.getBlind() + " | You've bet '" + blind + "'. Money: '" + player.getMoney() + "'. In Pot '" + this.getTable().getPot() + "'.");

        this.setLastBet(blind);
        this.afterBet(player);
    }

    private void afterBet(Player player) {
        for (Player p : this.getPlayers()) {
            if (p == player || !p.isPlaying()) {
                p.setHasToCall(false);
            }

            if (p.getInPot() != player.getInPot() && player.isPlaying()) {
                p.setHasToCall(true);
            }
        }
    }

}
