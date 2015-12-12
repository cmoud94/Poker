/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.client;

import poker.client.gui.ClientWindow;
import poker.game.Player;
import poker.utils.Utils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Client implements Runnable {

    private String address;

    private int port;

    private SocketChannel socketChannel;

    private Selector selector;

    private Map<SocketChannel, byte[]> pendingData;

    private final int buffSize;

    private String name;

    private Player player;

    private final ClientWindow window;

    public Client(ClientWindow window) {
        this(window, "", 0);
    }

    public Client(ClientWindow window, String address, int port) {
        this.address = address;
        this.port = port;
        this.socketChannel = null;
        this.selector = null;
        this.pendingData = new HashMap<>();
        this.buffSize = 8192;
        this.name = "default";
        this.player = null;
        this.window = window;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private SocketChannel getSocketChannel() {
        return socketChannel;
    }

    private void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    private Selector getSelector() {
        return selector;
    }

    private void setSelector(Selector selector) {
        this.selector = selector;
    }

    private Map<SocketChannel, byte[]> getPendingData() {
        return pendingData;
    }

    private int getBuffSize() {
        return buffSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    private ClientWindow getWindow() {
        return window;
    }

    public void init() {
        System.out.println("[Client] Initializing client");

        if (this.getSocketChannel() != null || this.getSelector() != null) {
            return;
        }

        try {
            this.setSelector(Selector.open());

            this.setSocketChannel(SocketChannel.open());
            this.getSocketChannel().configureBlocking(false);

            this.getSocketChannel().register(this.getSelector(), SelectionKey.OP_CONNECT, this.getName());
            this.getSocketChannel().connect(new InetSocketAddress(this.getAddress(), this.getPort()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientLoop() {
        //System.out.println("[Client] Entering main loop");

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

                    if (key.isConnectable()) {
                        this.connect(key);
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
        System.out.println("[Client] Closing connection");
        if (this.getSelector() != null) {
            try {
                //this.getSelector().close();
                this.getSocketChannel().socket().close();
                this.getSocketChannel().close();
                this.getWindow().getConnectionPanel().clientDisconnected();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect(SelectionKey key) {
        System.out.println("[Client] Connected");

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();

            if (socketChannel.isConnectionPending()) {
                socketChannel.finishConnect();
            }

            socketChannel.configureBlocking(false);
            this.getPendingData().put(socketChannel, Utils.getObjectAsBytes(this.getName()));
            socketChannel.register(this.getSelector(), SelectionKey.OP_WRITE, "Server");
            this.getWindow().getConnectionPanel().clientConnected();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        System.out.println("[Client] Reading data from " + key.attachment());

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer readBuffer = ByteBuffer.allocate(this.getBuffSize());
            int read;

            readBuffer.clear();

            try {
                read = socketChannel.read(readBuffer);
            } catch (IOException e) {
                System.out.println("[Client] Problem with data read");
                key.cancel();
                socketChannel.close();
                return;
            }

            if (read == -1) {
                System.out.println("[Client] Nothing to read, closing connection");
                key.cancel();
                socketChannel.close();
                this.closeConnection();
                return;
            }

            readBuffer.flip();
            byte[] data = new byte[this.getBuffSize()];
            readBuffer.get(data, 0, read);

            this.processData(key, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(SelectionKey key) {
        System.out.println("[Client] Writing data");

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            byte[] data = this.getPendingData().get(socketChannel);
            this.getPendingData().remove(socketChannel);

            System.out.println("\tData writed: " + Utils.getBytesAsObject(data));

            socketChannel.write(ByteBuffer.wrap(data));

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processData(SelectionKey key, byte[] data) {
        Object object = Utils.getBytesAsObject(data);

        if (object instanceof String) {
            if (object.equals("[Game] Are you ready?")) {
                System.out.println("[Client] " + object);
                this.getWindow().getConnectionPanel().serverReady();
            } else {
                System.out.println("[Client] " + key.attachment() + ": " + object);
            }
        } else if (object instanceof List) {
            System.out.println("[Client] Available actions: " + object);

            //this.getWindow().getGamePanel().showAvailableActions((List<String>) object, this.getPlayer().getMoney());
        }
    }

    public void sendData(byte[] data) {
        //System.out.println("[Server] Echoing data (" + key.attachment() + ")");

        for (SelectionKey key : this.getSelector().keys()) {
            if (key.channel() instanceof SocketChannel && key.isValid()) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                this.getPendingData().put(socketChannel, data);
                key.interestOps(SelectionKey.OP_WRITE);
            }
        }
    }

    @Override
    public void run() {
        this.clientLoop();
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
                        action = JOptionPane.showInputDialog("[Client] What you want to send?");
                        this.sendData(Utils.getObjectAsBytes(action));
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: Smazat
    /*public void connect(String address, int port) {
        this.setPort(port);

        try {
            this.setAddress(new InetSocketAddress(InetAddress.getByName(address), this.getPort()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            this.setSc(SocketChannel.open());
            this.getSc().configureBlocking(false);
            this.getSc().connect(this.getAddress());

            this.setSelector(Selector.open());

            while (true) {
                if (this.getSc().finishConnect()) {
                    break;
                }
            }

            System.out.println("[Client] Connected");
            if (this.getWindow() != null) {
                this.getWindow().getConnectionPanel().clientConnected();
            }
            this.getSc().write(ByteBuffer.wrap(Utils.getObjectAsBytes(this.getName())));
            this.getSc().register(this.getSelector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientLoop() {
        if (this.getSc().isOpen()) {
            System.out.println("[Client] Entered main loop");

            Iterator<SelectionKey> iterator;
            SelectionKey key;

            while (this.getSc().isOpen()) {
                try {
                    this.getSelector().select();
                    iterator = this.getSelector().selectedKeys().iterator();

                    while (iterator.hasNext()) {
                        key = iterator.next();
                        iterator.remove();

                        if (!key.isValid()) {
                            System.out.println("[Client] Invalid key");
                            continue;
                        }

                        if (key.isConnectable()) {
                            System.out.println("[Client] Connectable.");
                        } else if (key.isReadable()) {
                            System.out.println("[Client] Readable.");
                            this.handleRead(key);
                        } else if (key.isWritable()) {
                            System.out.println("[Client] Writeable.");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void disconnect() {
        if (this.getSc() != null && this.getSc().isOpen()) {
            try {
                this.getSc().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("[Client] Disconnected");
            if (this.getWindow() != null) {
                this.getWindow().getConnectionPanel().clientDisconnected();
            }
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
                System.out.println("[Client] Server disconnected");
                if (this.getWindow() != null) {
                    this.getWindow().getConnectionPanel().serverDisconnected();
                }
                key.cancel();
                sc.close();
            }

            if (finalBytes.length > 0) {
                Object object = Utils.getBytesAsObject(finalBytes);

                if (object != null) {
                    this.processData(finalBytes);
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

    private void processData(byte[] bytes) {
        Object object = Utils.getBytesAsObject(bytes);

        System.out.println("[Client] Received data from server");

        if (object instanceof String) {

            System.out.println("\t" + object);
            if (object.equals("[Game] " + this.getName() + " - Are you ready?") && this.getWindow() != null) {
                this.getWindow().getConnectionPanel().serverReady();
            }

        } else if (object instanceof List) {

            System.out.println("\t" + object.toString());

            if (this.getWindow() != null) {
                this.getWindow().getGamePanel().showPlayerAvailableAction((List<String>) object, this.getPlayer().getMoney());
            }

        } else if (object instanceof Table) {

            System.out.println("\tIn pot: " + ((Table) object).getPot());
            System.out.println("\tCommunity cards:");
            ((Table) object).printCommunityCards();

            if (this.getWindow() != null) {
                this.getWindow().getGamePanel().drawCommunityCards(((Table) object).getCommunityCards());
            }

        } else if (object instanceof Player) {
            if (((Player) object).getName().equals(this.getName())) {
                this.setPlayer((Player) object);
                System.out.println("\tName: " + this.getPlayer().getName() + " money: " + this.getPlayer().getMoney());
                System.out.println("\tYour cards:");
                this.getPlayer().printCards();
            }

        }
    }

    public void sendMessage(byte[] bytes) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);

            this.getSc().write(buffer);
            buffer.clear();

            System.out.println("[Client] Data sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
