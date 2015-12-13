/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.server;

import poker.game.Deck;
import poker.game.Game;
import poker.game.Player;
import poker.game.Table;
import poker.utils.Utils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server implements Runnable {

    private final int port;

    private ServerSocketChannel serverSocketChannel;

    private Selector selector;

    private Map<SocketChannel, List<byte[]>> pendingData;

    private final int buffSize;

    private boolean serverRunning;

    private final Game game;

    private final int startingMoney;

    private boolean gameRunning;

    private String lastMessage;

    public Server(int port, int numOfPlayers, int bigBlind, int startingMoney) {
        this.port = port;
        this.serverSocketChannel = null;
        this.selector = null;
        this.pendingData = new HashMap<>();
        this.buffSize = 131136;
        this.serverRunning = false;
        this.game = new Game(numOfPlayers, bigBlind, this);
        this.startingMoney = startingMoney;
        this.gameRunning = false;
        this.lastMessage = "";
    }

    public int getPort() {
        return port;
    }

    public ServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
    }

    public void setServerSocketChannel(ServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public Map<SocketChannel, List<byte[]>> getPendingData() {
        return pendingData;
    }

    public int getBuffSize() {
        return buffSize;
    }

    public boolean isServerRunning() {
        return serverRunning;
    }

    public void setServerRunning(boolean serverRunning) {
        this.serverRunning = serverRunning;
    }

    public Game getGame() {
        return game;
    }

    public int getStartingMoney() {
        return startingMoney;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void setGameRunning(boolean gameRunning) {
        this.gameRunning = gameRunning;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void init() {
        System.out.println("[Server] Starting at port " + this.getPort() + " for " + this.getGame().getNumOfPlayers() + " players with Big Blind " + this.getGame().getBigBlind());

        if (this.getServerSocketChannel() != null || this.getSelector() != null) {
            return;
        }

        try {
            this.setSelector(Selector.open());
            this.setServerSocketChannel(ServerSocketChannel.open());
            this.getServerSocketChannel().configureBlocking(false);
            this.getServerSocketChannel().socket().bind(new InetSocketAddress(this.getPort()));

            this.getServerSocketChannel().register(this.getSelector(), SelectionKey.OP_ACCEPT, "Server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serverLoop() {
        //System.out.println("[Server] Waiting for clients");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                this.getSelector().select(1000);

                Iterator<SelectionKey> keys = this.getSelector().selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        this.accept(key);
                    } else if (key.isReadable()) {
                        this.read(key);
                    } else if (key.isWritable()) {
                        this.write(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.closeConnection();
        }
    }

    public void closeConnection() {
        System.out.println("[Server] Closing connection");
        if (this.getSelector() != null) {
            try {
                //this.getSelector().close();
                this.getServerSocketChannel().socket().close();
                this.getServerSocketChannel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) {
        //System.out.println("[Server] Client accepted");

        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            socketChannel.register(this.getSelector(), SelectionKey.OP_READ, "accept");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        System.out.println("[Server] Reading data (" + key.attachment() + ")");

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer readBuffer = ByteBuffer.allocate(this.getBuffSize());
            int read;

            readBuffer.clear();

            try {
                read = socketChannel.read(readBuffer);
            } catch (IOException e) {
                System.out.println("[Server] Problem with data read");
                key.cancel();
                socketChannel.close();
                return;
            }

            if (read == -1) {
                System.out.println("[Server] Nothing to read, closing connection");
                key.cancel();
                socketChannel.close();
                return;
            }

            readBuffer.flip();
            byte[] data = new byte[this.getBuffSize()];
            readBuffer.get(data, 0, read);

            //this.processData(key, data);

            new Thread(new DataProcessor(this, key, data)).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(SelectionKey key) {
        System.out.println("[Server] Writing data (" + key.attachment() + ")");

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            List<byte[]> data = this.getPendingData().get(socketChannel);
            this.getPendingData().remove(socketChannel);

            while (!data.isEmpty()) {
                byte[] bytes = data.remove(0);

                System.out.println("\tData written: " + Utils.getBytesAsObject(bytes));

                socketChannel.write(ByteBuffer.wrap(bytes));

                key.interestOps(SelectionKey.OP_READ);

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void echo(SelectionKey key, byte[] data) {
        //System.out.println("[Server] Echoing data (" + key.attachment() + ")");

        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> pendingData = this.getPendingData().get(socketChannel);
        if (pendingData == null) {
            pendingData = new ArrayList<>();
        }
        pendingData.add(data);
        this.getPendingData().remove(socketChannel);
        this.getPendingData().put(socketChannel, pendingData);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void echo(String name, byte[] data) {
        //System.out.println("[Server] Echoing data (" + key.attachment() + ")");

        for (SelectionKey key : this.getSelector().keys()) {
            if (key.channel() instanceof SocketChannel && key.isValid() && key.attachment().equals(name)) {
                this.echo(key, data);
            }
        }
    }

    public void broadcast(byte[] data) {
        System.out.println("[Server] Broadcasting data | " + Utils.getBytesAsObject(data));

        for (SelectionKey key : this.getSelector().keys()) {
            if (key.channel() instanceof SocketChannel && key.isValid()) {
                this.echo(key, data);
            }
        }
    }

    /*private void processData(SelectionKey key, byte[] data) {
        Object object = Utils.getBytesAsObject(data);

        if (object instanceof String) {
            if (key.attachment().equals("accept")) {
                System.out.println("[Server] Player " + object + " has connected");

                key.attach(object);

                int id = this.getGame().getPlayers().size() + 1;
                Player player = new Player(id, (String) object, this.getStartingMoney());
                this.getGame().getPlayers().add(player);

                if (this.getGame().getPlayers().size() == this.getGame().getNumOfPlayers()) {
                    this.getGame().init();
                }
            } else if (object.equals("yes")) {
                Player player = this.getGame().getPlayerByPlayerName((String) key.attachment());
                System.out.println("[Server] " + player.getName() + " is now ready to play");


                this.setLastMessage("");
                player.setReady(true);

                boolean allReady = true;
                for (Player p : this.getGame().getPlayers()) {
                    if (!p.isReady()) {
                        allReady = false;
                    }
                }

                if (allReady) {
                    System.out.println("[Server] Starting gameLoop");
                    Thread gameLoopThread = new Thread(this.getGame(), "serverGameLoop");
                    gameLoopThread.start();
                }
            } else if (this.getGameActions().contains(object)) {
                System.out.println("[Server] Received action " + object + " from " + key.attachment());
                this.setLastMessage((String) object);
            } else {
                System.out.println("[Server] " + key.attachment() + " said: " + object);
                this.echo(key, data);
            }
        }
    }*/

    @Override
    public void run() {
        this.serverLoop();
    }

    public void consoleLoop() {
        String action;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                action = br.readLine();

                switch (action.trim()) {
                    case "quit":
                        this.closeConnection();
                        System.exit(0);
                        break;
                    case "send":
                        String name = JOptionPane.showInputDialog("[Server] Type player's name.");
                        action = JOptionPane.showInputDialog("[Server] Type your message.");
                        this.echo(name, Utils.getObjectAsBytes(action));
                        break;
                    case "broadcast":
                        action = JOptionPane.showInputDialog("[Server] What you want to broadcast?");
                        this.broadcast(Utils.getObjectAsBytes(action));
                        break;
                    case "table":
                        name = JOptionPane.showInputDialog("[Server] Type player's name.");
                        Table table = new Table(10);
                        Deck deck = new Deck();
                        for (int i = 0; i < 5; i++) {
                            table.getCommunityCards().add(deck.dealCard());
                        }
                        this.echo(name, Utils.getObjectAsBytes(table));
                        break;
                    case "player":
                        name = JOptionPane.showInputDialog("[Server] Type player's name.");
                        Player player = new Player(1, "Tlusty kokot", 10000);
                        deck = new Deck();
                        player.getCards().add(deck.dealCard());
                        player.getCards().add(deck.dealCard());
                        this.echo(name, Utils.getObjectAsBytes(player));
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
