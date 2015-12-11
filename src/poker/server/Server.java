/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.server;

import poker.game.Game;
import poker.game.Player;
import poker.utils.Utils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class Server implements Runnable {

    private final int port;

    private ServerSocketChannel ssc;

    private Selector selector;

    private final int buffSize;

    private boolean serverRunning;

    private final Game game;

    private final int startingMoney;

    private boolean gameRunning;

    private String lastMessage;

    public Server(int port, int numOfPlayers, int bigBlind, int startingMoney) {
        this.port = port;
        this.ssc = null;
        this.selector = null;
        this.buffSize = 8192;
        this.serverRunning = false;
        this.game = new Game(numOfPlayers, bigBlind, this);
        this.startingMoney = startingMoney;
        this.gameRunning = false;
        this.lastMessage = "";

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
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

    public void startServer() {
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
            this.setServerRunning(true);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    public void serverLoop() {
        if (this.getSsc().isOpen() && this.isServerRunning()) {
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

                    if (!key.isValid()) {
                        System.out.println("[Server] Invalid key");
                        continue;
                    }

                    if (key.isAcceptable()) {
                        System.out.println("[Server] Acceptable");
                        this.handleAccept(key);
                    } else if (key.isReadable()) {
                        System.out.println("[Server] Readable");
                        this.handleRead(key);
                    } else if (key.isWritable()) {
                        System.out.println("[Server] Writeable");
                    }
                }
            }
        } else {
            Thread.currentThread().interrupt();
        }
    }

    public void stopServer() {
        try {
            //this.getSelector().close();
            this.getSsc().close();
            this.setServerRunning(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[Server] Stopped");
        System.exit(0);
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
                name += Utils.getBytesAsObject(bytes);
                buffer.clear();
            }

            if (readBytes < 0) {
                System.out.println("[Server] Client disconnected");
                key.cancel();
                sc.close();
            }

            if (!name.equals("")) {
                System.out.println("[Server] " + name + " connected");
                return name;
            }
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            try {
                sc.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
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
                    // TODO: Potrebuje otestovat
                    for (Player player : this.getGame().getPlayers()) {
                        if (player.getName().equals(playerName)) {
                            System.out.println("[Server] Not possible to have two players with the same name!");
                            break;
                        }
                    }
                    sc.register(this.getSelector(), SelectionKey.OP_READ, playerName);

                    Player player = new Player(playerName, this.getStartingMoney());
                    this.getGame().getPlayers().add(player);
                    this.sendData(player, Utils.getObjectAsBytes(player));
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
        byte[] finalBytes = new byte[0];
        int readBytes;

        buffer.clear();
        try {
            while ((readBytes = sc.read(buffer)) > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.limit()];
                buffer.get(bytes);
                finalBytes = Utils.concatByteArrays(finalBytes, bytes);
                buffer.clear();
            }

            if (readBytes < 0) {
                System.out.println("[Server] Client disconnected");
                key.cancel();
                sc.close();
            }

            if (finalBytes.length > 0) {
                Object object = Utils.getBytesAsObject(finalBytes);

                if (object != null) {
                    this.processData((String) key.attachment(), finalBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            try {
                sc.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void processData(String playerName, byte[] bytes) {
        Player player = this.getGame().getPlayerByPlayerName(playerName);
        Object object = Utils.getBytesAsObject(bytes);

        System.out.println("[Server] Received data from " + player.getName());

        if (object instanceof String) {
            if (!player.isReady() && object.equals("yes")) {
                this.setLastMessage("");
                player.setReady(true);
                System.out.println("[Server] " + player.getName() + " is now ready to play");

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
            } else {
                this.setLastMessage((String) object);
                System.out.println("[Server] Received message from " + player.getName() + " saying: " + this.getLastMessage());
            }
        }
    }

    public void sendData(Player player, byte[] bytes) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);

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

        System.out.println("[Server] Data sent to " + player.getName());
    }

    public void sendData(String name, byte[] bytes) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);

            for (SelectionKey key : this.getSelector().keys()) {
                if (key.isValid() && key.channel() instanceof SocketChannel && name.equals(key.attachment())) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    sc.write(buffer);
                    buffer.rewind();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[Server] Data sent to " + name);
    }

    public void broadcastData(byte[] bytes) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);

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
                        this.stopServer();
                        break;
                    case "send":
                        /*String name = JOptionPane.showInputDialog("[Server] Type player's name.");
                        action = JOptionPane.showInputDialog("[Server] Type your message.");
                        this.sendData(name, Utils.getObjectAsBytes(action));*/

                        for (int i = 0; i < 5; i++) {
                            this.sendData("Player_1", Utils.getObjectAsBytes(i + ". Hello world!"));
                        }

                        break;
                    case "broadcast":
                        action = JOptionPane.showInputDialog("[Server] What you want to broadcast?");
                        this.broadcastData(Utils.getObjectAsBytes(action));
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
