package poker.server;

/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

import poker.game.Game;
import poker.game.Player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class Server {

    private final int port;

    private ServerSocketChannel ssc;

    private Selector selector;

    private final int buffSize = 256;

    private final Game game;

    private final int startingMoney;

    private boolean gameRunning;

    public Server(int port, int numOfPlayers, int bigBlind, int startingMoney) {
        this.port = port;
        this.ssc = null;
        this.selector = null;
        this.game = new Game(numOfPlayers, bigBlind, this);
        this.startingMoney = startingMoney;
        this.gameRunning = false;

        this.start();

        this.serverLoop();

        this.stop();
    }

    public int getPort() {
        return port;
    }

    public ServerSocketChannel getSsc() {
        return ssc;
    }

    public void setSsc(ServerSocketChannel ssc) {
        this.ssc = ssc;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public int getBuffSize() {
        return buffSize;
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

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");

        if (args.length != 4) {
            System.out.println("Usage: Server [port] [numberOfPlayers] [bigBlind] [startingMoney]");
            System.exit(0);
        } else {
            int port, numOfPlayers, bigBlind, startingMoney;

            port = Integer.parseInt(args[0]);
            numOfPlayers = Integer.parseInt(args[1]);
            bigBlind = Integer.parseInt(args[2]);
            startingMoney = Integer.parseInt(args[3]);

            new Server(port, numOfPlayers, bigBlind, startingMoney);
        }
    }

    private void start() {
        InetSocketAddress address = new InetSocketAddress(this.getPort());

        try {
            this.setSsc(ServerSocketChannel.open());
            this.getSsc().configureBlocking(false);
            this.getSsc().socket().bind(address);

            this.setSelector(Selector.open());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.getSsc().isOpen()) {
            System.out.println("[Server] Starting at port " + this.getPort() + " for " + this.getGame().getNumOfPlayers() + " players with Big Blind " + this.getGame().getBigBlind());
        }

        try {
            this.getSsc().register(this.getSelector(), SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    private void serverLoop() {
        if (this.getSsc().isOpen()) {
            System.out.println("[Server] Waiting for clients...");

            Iterator<SelectionKey> iterator;
            SelectionKey key;

            while (this.getSsc().isOpen()) {
                try {
                    this.getSelector().select();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                iterator = this.getSelector().selectedKeys().iterator();

                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();

                    if (key.isConnectable()) {
                        System.out.println("[Server] Connectable");
                    } else if (key.isAcceptable()) {
                        this.handleAccept(key);
                    } else if (key.isReadable()) {
                        this.handleRead(key);
                    } else if (key.isWritable()) {
                        System.out.println("[Server] Writeable");
                    } else {
                        System.out.println("[Server] Nothing to do...");
                    }
                }
            }
        }
    }

    private void stop() {
        try {
            this.getSelector().close();
            this.getSsc().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[Server] Stopped");
    }

    private String getClientName(SelectionKey key) {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(this.getBuffSize());
        String name = "";
        int readBytes;

        buffer.clear();
        try {
            while ((readBytes = sc.read(buffer)) > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.limit()];
                buffer.get(bytes);
                name += new String(bytes, "UTF-8");
                buffer.clear();
            }

            if (readBytes < 0) {
                sc.close();
                System.out.println("[Server] Client disconnected");
            }

            if (!name.equals("")) {
                System.out.println("[Server - getClientName] " + name + " connected");
                return name;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void handleAccept(SelectionKey key) {
        try {
            SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
            sc.configureBlocking(false);
            Selector selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ);

            Iterator<SelectionKey> iterator;
            SelectionKey _key;

            selector.select();
            iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                _key = iterator.next();
                iterator.remove();

                if (_key.isReadable()) {
                    String playerName = this.getClientName(_key);
                    sc.register(this.getSelector(), SelectionKey.OP_READ, playerName);
                    this.getGame().getPlayers().add(new Player(playerName, this.getStartingMoney()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.getGame().getNumOfPlayers() == this.getGame().getPlayers().size() && !this.isGameRunning()) {
            this.setGameRunning(true);
            this.getGame().initGame();
        }
    }

    private void handleRead(SelectionKey key) {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(this.getBuffSize());
        String message = "";
        int readBytes;

        buffer.clear();
        try {
            while ((readBytes = sc.read(buffer)) > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.limit()];
                buffer.get(bytes);
                message += new String(bytes, "UTF-8");
                buffer.clear();
            }

            if (readBytes < 0) {
                sc.close();
                System.out.println("[Server] " + key.attachment() + " disconnected");
            }

            if (!message.equals("")) {
                System.out.println("[Server] " + key.attachment() + ": " + message);
                this.broadcast(key.attachment() + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Player player, String message) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));

            for (SelectionKey key : this.getSelector().keys()) {
                if (key.isValid() && key.channel() instanceof SocketChannel && player.getName().equals(key.attachment())) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    sc.write(buffer);
                    buffer.rewind();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[Server] Message sent to " + player.getName());
    }

    private void broadcast(String message) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));

            for (SelectionKey key : this.getSelector().keys()) {
                if (key.isValid() && key.channel() instanceof SocketChannel) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    sc.write(buffer);
                    buffer.rewind();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[Server] Broadcast sent");
    }

}
