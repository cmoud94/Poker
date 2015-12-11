/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.server;

import poker.game.Game;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server implements Runnable {

    private final int port;

    private ServerSocketChannel serverSocketChannel;

    private Selector selector;

    private Map<SocketChannel, byte[]> pendingData;

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
        this.buffSize = 8192;
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

    public Map<SocketChannel, byte[]> getPendingData() {
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

            this.getServerSocketChannel().register(this.getSelector(), SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serverLoop() {
        System.out.println("[Server] Waiting for clients");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                this.getSelector().select(10000);

                Iterator<SelectionKey> keys = this.getSelector().selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        this.accept(key);
                    }

                    if (key.isWritable()) {
                        this.write(key);
                    }

                    if (key.isReadable()) {
                        this.read(key);
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
        System.out.println("[Server] Client accepted");

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

            this.processData(key, data);
            //this.echo(key, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(SelectionKey key) {
        System.out.println("[Server] Writing data");

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            byte[] data = this.getPendingData().get(socketChannel);
            this.getPendingData().remove(socketChannel);

            socketChannel.write(ByteBuffer.wrap(data));

            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void echo(SelectionKey key, byte[] data) {
        System.out.println("[Server] Echoing data");

        SocketChannel socketChannel = (SocketChannel) key.channel();
        this.getPendingData().put(socketChannel, data);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void processData(SelectionKey key, byte[] data) {
        Object object = Utils.getBytesAsObject(data);

        if (object instanceof String) {
            System.out.println("[Server] Received message: " + object + " (" + key.attachment() + ")");
        }
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
                        this.closeConnection();
                        System.exit(0);
                        break;
                    case "send":
                        /*String name = JOptionPane.showInputDialog("[Server] Type player's name.");
                        action = JOptionPane.showInputDialog("[Server] Type your message.");
                        this.sendData(name, Utils.getObjectAsBytes(action));*/
                        break;
                    case "broadcast":
                        action = JOptionPane.showInputDialog("[Server] What you want to broadcast?");
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: Smazat po implementaci noveho serveru
    /*private String getClientName(SelectionKey key) {
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
    }*/

}
