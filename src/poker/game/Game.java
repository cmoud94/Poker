/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.game;

import poker.server.Server;
import poker.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Game implements Runnable {

    private final int numOfPlayers;

    private final List<Player> players;

    private final BufferedReader br;

    private final int bigBlind;

    private Table table;

    private Deck deck;

    private boolean isRunning;

    private int lastBet;

    private int dealer;

    private int activePlayers;

    private final Server server;

    public Game(int numOfPlayers, int bigBlind, Server server) {
        this.numOfPlayers = numOfPlayers;
        this.players = new ArrayList<>(numOfPlayers);
        this.br = new BufferedReader(new InputStreamReader(System.in));
        this.bigBlind = bigBlind;
        this.table = new Table(bigBlind);
        this.deck = new Deck();
        this.isRunning = false;
        this.lastBet = 0;
        this.dealer = -1;
        this.activePlayers = numOfPlayers;
        this.server = server;
    }

    public int getNumOfPlayers() {
        return numOfPlayers;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getPlayerByPlayerName(String name) {
        for (Player player : this.getPlayers()) {
            if (player.getName().equals(name)) {
                return player;
            }
        }

        return null;
    }

    private BufferedReader getBr() {
        return br;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public Table getTable() {
        return table;
    }

    private void setTable(Table table) {
        this.table = table;
    }

    public Deck getDeck() {
        return deck;
    }

    private void setDeck(Deck deck) {
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

    private void setLastBet(int lastBet) {
        this.lastBet = lastBet;
    }

    public int getDealer() {
        return dealer;
    }

    private void setDealer(int dealer) {
        this.dealer = dealer;
    }

    public int getActivePlayers() {
        return activePlayers;
    }

    private void setActivePlayers(int activePlayers) {
        this.activePlayers = activePlayers;
    }

    public Server getServer() {
        return server;
    }

    public void init() {
        if (this.getNumOfPlayers() != this.getPlayers().size()) {
            System.out.println("[Game] Player count differs from the one that was set! Aborting...");
            System.exit(0);
        }

        this.setRunning(true);

        if (this.getServer() != null) {
            this.getServer().broadcast(Utils.getObjectAsBytes("[Game] Are you ready?"));
        } else {
            for (Player player : this.getPlayers()) {
                player.setReady(true);
            }
        }
    }

    private void setBlinds() {
        int dealer, small, big;

        this.setDealer((this.getDealer() + 1) % this.getNumOfPlayers());

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

        /*for (Player player : this.getPlayers()) {
            System.out.println("[Game] Sending Player object to " + player.getName());
        }*/
        //this.getServer().broadcast(Utils.getObjectAsBytes(this.getPlayers()));
    }

    private boolean allCaled() {
        for (Player p : this.getPlayers()) {
            if (p.isHasToCall() && p.isPlaying()) {
                return false;
            }
        }

        this.setLastBet(0);
        return true;
    }

    private void bettingLoop(boolean firstBetRound) {
        String action = "";
        List<String> availableActions;
        int money = 0;
        boolean again;
        boolean smallBlind = false;
        boolean bigBlind = false;

        while (this.isRunning()) {
            again = false;

            for (int i = this.getDealer(); i < this.getPlayers().size(); i++) {

                if (i == this.getDealer() && again) {
                    break;
                }

                if (this.getPlayers().get(i).isPlaying()) {
                    availableActions = this.availableActions(this.getPlayers().get(i));

                    if (firstBetRound) {
                        if (this.getPlayers().get(i).getBlind() == Player.Blind.SMALL_BLIND && !smallBlind) {
                            this.actionBetBlind(this.getPlayers().get(i), this.getTable().getBigBlind() / 2);
                            smallBlind = true;
                            continue;
                        }

                        if (this.getPlayers().get(i).getBlind() == Player.Blind.BIG_BLIND && !bigBlind) {
                            this.actionBetBlind(this.getPlayers().get(i), this.getTable().getBigBlind());
                            bigBlind = true;
                            continue;
                        }

                        if (smallBlind && bigBlind) {
                            firstBetRound = false;
                            this.getServer().broadcast(Utils.getObjectAsBytes(this.getPlayers()));
                            this.getServer().broadcast(Utils.getObjectAsBytes(this.getTable()));
                            break;
                        }
                    } else {
                        do {
                            if (this.getServer() != null) {

                                this.getServer().echo(this.getPlayers().get(i).getName(), Utils.getObjectAsBytes(availableActions));

                                while (this.getServer().getLastMessage().equals("")) {
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                action = this.getServer().getLastMessage();
                                this.getServer().setLastMessage("");

                                if (action.contains("bet")) {
                                    money = Integer.parseInt(action.substring(3).trim());
                                    action = "bet";
                                }
                            } else {
                                System.out.println(this.getPlayers().get(i).getName() + " " + availableActions);
                                try {
                                    action = this.getBr().readLine();
                                    if (action.contains("bet")) {
                                        money = Integer.parseInt(action.substring(3).trim());
                                        action = "bet";
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } while (!this.handleAction(action, this.getPlayers().get(i), money));

                        this.getServer().broadcast(Utils.getObjectAsBytes(this.getPlayers()));
                        this.getServer().broadcast(Utils.getObjectAsBytes(this.getTable()));
                    }
                } else {
                    System.out.println(this.getPlayers().get(i).getName() + " not playing this round.");
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

        System.out.println("[Game] All players called...");
    }

    private void newRound() {
        this.setDeck(new Deck());
        this.setTable(new Table(this.getBigBlind()));
        this.setActivePlayers(this.getNumOfPlayers());

        this.setBlinds();
        this.dealCards();

        System.out.println("************************************************************");
        System.out.println("*                         NEW GAME                         *");
        System.out.println("************************************************************");

        for (Player player : this.getPlayers()) {
            if (player.getMoney() == 0) {
                // TODO: Mozna bude lepsi hrace rovnou odstranit ze hry
                player.setPlaying(false);
                continue;
            }

            player.newRound();
            System.out.println("\t" + player.getName() + " | " + player.getBlind() + " | Cards: " +
                    player.getCards().get(0).toString() + " && " +
                    player.getCards().get(1).toString() + " | Money: " + player.getMoney());

            this.getServer().echo(player.getName(), Utils.getObjectAsBytes("new-round"));
        }
        System.out.println();
    }

    private void flop() {
        if (this.getActivePlayers() > 1) {
            this.bettingLoop(true);
        }
        this.getDeck().dealCard();

        for (int i = 0; i < 3; i++) {
            this.getTable().getCommunityCards().add(this.getDeck().dealCard());
        }

        if (this.getServer() != null) {
            this.getServer().broadcast(Utils.getObjectAsBytes(this.getTable()));
        }

        this.getTable().printCommunityCards();
        System.out.println("FLOP");
    }

    private void turn() {
        if (this.getActivePlayers() > 1) {
            this.bettingLoop(false);
        }
        this.getDeck().dealCard();
        this.getTable().getCommunityCards().add(this.getDeck().dealCard());

        if (this.getServer() != null) {
            this.getServer().broadcast(Utils.getObjectAsBytes(this.getTable()));
        }

        this.getTable().printCommunityCards();
        System.out.println("TURN");
    }

    private void river() {
        if (this.getActivePlayers() > 1) {
            this.bettingLoop(false);
        }
        this.getDeck().dealCard();
        this.getTable().getCommunityCards().add(this.getDeck().dealCard());

        if (this.getServer() != null) {
            this.getServer().broadcast(Utils.getObjectAsBytes(this.getTable()));
        }

        this.getTable().printCommunityCards();
        System.out.println("RIVER");
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

        if (winners.size() == 1 && winners.get(0).getPlayer().isHasAllIn()) {
            Player player = winners.get(0).getPlayer();
            player.setMoney(player.getMoney() + player.getInPot());
            System.out.println("Winner is: " + winners.get(0).getPlayer().getName() + " HS: " + winners.get(0).getHandStrength() + " CV: " + winners.get(0).getHandCardsValue() + ". Getting " + this.getTable().getPot() + ". Has all-in so selecting other winners.");

            /*this.getServer().setLastMessage("winner-all-in");
            this.getServer().broadcast(Utils.getObjectAsBytes(winners));*/

            this.getTable().setPot(this.getTable().getPot() - player.getInPot());
            this.getPlayers().get(this.getPlayers().indexOf(winners.get(0).getPlayer())).setPlaying(false);
            if (this.getTable().getPot() > 0) {
                this.checkWinner();
            }
        } else if (winners.size() == 1) {
            Player player = winners.get(0).getPlayer();
            player.setMoney(player.getMoney() + this.getTable().getPot());
            System.out.println("Winner is: " + winners.get(0).getPlayer().getName() + " HS: " + winners.get(0).getHandStrength() + " CV: " + winners.get(0).getHandCardsValue() + ". Getting " + this.getTable().getPot() + ".");

            /*this.getServer().setLastMessage("winner");
            this.getServer().broadcast(Utils.getObjectAsBytes(winners));*/
        } else {
            int money = this.getTable().getPot() / winners.size();
            System.out.println("We have " + winners.size() + " winners.");
            for (Hand winner : winners) {
                winner.getPlayer().setMoney(winner.getPlayer().getMoney() + money);
                System.out.println("\t" + winner.getPlayer().getName() + " HS: " + winner.getHandStrength() + " CV: " + winner.getHandCardsValue() + ". Getting " + money + ".");
                this.getTable().setPot(this.getTable().getPot() - money);
            }

            /*this.getServer().setLastMessage("winners");
            this.getServer().broadcast(Utils.getObjectAsBytes(winners));*/
        }
    }

    public void gameLoop() {
        while ((this.isRunning())) {
            this.newRound();

            this.flop();

            this.turn();

            this.river();

            if (this.getActivePlayers() > 1) {
                this.bettingLoop(false);
            }

            this.checkWinner();
        }
    }

    private List<String> availableActions(Player player) {
        List<String> ret = new ArrayList<>();
        ret.add("fold");

        if (!player.isHasToCall()) {
            ret.add("check");
        } else {
            ret.add("call");
        }

        ret.add("bet");
        ret.add("all-in");

        return ret;
    }

    private boolean handleAction(String action, Player player, int money) {
        boolean ret = false;

        switch (action) {
            case "fold":
                ret = this.actionFold(player);
                break;
            case "check":
                ret = this.actionCheck(player);
                break;
            case "call":
                ret = this.actionCall(player);
                break;
            case "bet":
                ret = this.actionBet(player, money);
                break;
            case "all-in":
                ret = this.actionAllIn(player);
                break;
            default:
                System.out.println("[Game] Wrong action.");
                break;
        }

        return ret;
    }

    private boolean actionFold(Player player) {
        player.setPlaying(false);
        player.setHasToCall(false);
        this.setActivePlayers(this.getActivePlayers() - 1);

        if (this.getServer() != null) {
            this.getServer().broadcast(Utils.getObjectAsBytes(player.getName() + " has folded"));
        }

        return true;
    }

    private boolean actionCheck(Player player) {
        if (this.availableActions(player).contains("check")) {
            System.out.println("\t" + player.getName() + " has checked.");

            if (this.getServer() != null) {
                this.getServer().broadcast(Utils.getObjectAsBytes(player.getName() + " has checked"));
            }

            return true;
        }

        return false;
    }

    private boolean actionCall(Player player) {
        if (player.isHasToCall()) {
            int call = this.getLastBet() - player.getLastBet();

            if (player.getBlind() == Player.Blind.SMALL_BLIND && player.getInPot() == (this.getBigBlind() / 2)) {
                call = this.getLastBet() - player.getInPot();
            }

            player.setMoney(player.getMoney() - call);
            player.setInPot(player.getInPot() + call);
            player.setHasToCall(false);
            this.getTable().setPot(this.getTable().getPot() + call);

            System.out.println("\t" + player.getName() + " | You've called '" + call + "'. Money: '" + player.getMoney() + "'. In Pot '" + this.getTable().getPot() + "'.");

            if (this.getServer() != null) {
                this.getServer().broadcast(Utils.getObjectAsBytes(player.getName() + " has called"));
            }

            this.afterCall();
        } else {
            System.out.println("\tYou don't have to call.");
        }

        return true;
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

    private boolean actionBet(Player player, int money) {
        int bet = money + this.getLastBet();

        player.setMoney(player.getMoney() - bet);
        player.setInPot(player.getInPot() + bet);
        player.setLastBet(bet);
        this.getTable().setPot(this.getTable().getPot() + bet);

        if (player.isHasToCall()) {
            System.out.println("\tYou've called '" + this.getLastBet() + "' and then bet '" + money + "'. Money: '" + player.getMoney() + "'. In Pot '" + this.getTable().getPot() + "'.");
            this.setLastBet(bet);
        } else {
            System.out.println("\tYou've bet '" + money + "'. Money: '" + player.getMoney() + "'. In Pot '" + this.getTable().getPot() + "'.");
            this.setLastBet(money);
        }

        if (this.getServer() != null) {
            this.getServer().broadcast(Utils.getObjectAsBytes(player.getName() + " has bet"));
        }

        this.afterBet(player);

        return true;
    }

    private boolean actionAllIn(Player player) {
        int bet = player.getMoney();

        player.setMoney(0);
        player.setInPot(player.getInPot() + bet);
        player.setLastBet(bet);
        this.getTable().setPot(this.getTable().getPot() + bet);

        System.out.println("\tYou've bet all your money. Money: '" + player.getMoney() + "'. In pot: '" + this.getTable().getPot() + "'.");

        if (this.getServer() != null) {
            this.getServer().broadcast(Utils.getObjectAsBytes(player.getName() + " is all-in"));
        }

        this.setLastBet(bet);
        this.afterBet(player);

        return true;
    }

    private void actionBetBlind(Player player, int blind) {
        player.setMoney(player.getMoney() - blind);
        player.setInPot(player.getInPot() + blind);
        this.getTable().setPot(this.getTable().getPot() + blind);

        System.out.println("\t" + player.getName() + " | " + player.getBlind() + " | You've bet '" + blind + "'. Money: '" + player.getMoney() + "'. In Pot '" + this.getTable().getPot() + "'.");

        if (this.getServer() != null) {
            this.getServer().broadcast(Utils.getObjectAsBytes(player.getName() + " has bet " + player.getBlind()));
        }

        this.setLastBet(blind);
        this.afterBet(player);
    }

    private void afterBet(Player player) {
        for (Player p : this.getPlayers()) {
            if (p == player || !p.isPlaying()) {
                p.setHasToCall(false);
            } else if (p.getInPot() != player.getInPot() && player.isPlaying()) {
                p.setHasToCall(true);
            }
        }
    }

    @Override
    public void run() {
        this.gameLoop();
    }
}
