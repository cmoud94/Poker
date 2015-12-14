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
        this.buffSize = 131136;
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

    public ClientWindow getWindow() {
        return window;
    }

    public void init() {
        System.out.println("[Client] Initializing client");

        if (this.getSocketChannel() != null || this.getSelector() != null) {
            System.out.println("[Client] Aborting init");
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

    public synchronized void clientLoop() {
        //System.out.println("[Client] Entering main loop");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                this.getSelector().selectNow();

                Iterator<SelectionKey> keys = this.getSelector().selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isConnectable()) {
                        this.connect(key);
                    }
                    if (key.isReadable()) {
                        this.read(key);
                    }
                    if (key.isWritable()) {
                        this.write(key);
                    }
                }
            }
            System.out.println("[Client] clientLoop ended");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.closeConnection();
        }
    }

    public void closeConnection() {
        System.out.println("[Client] Closing connection");
        if (this.getSelector() != null || this.getSocketChannel() != null) {
            try {
                this.getSelector().close();
                this.getSocketChannel().socket().close();
                this.getSocketChannel().close();
                this.getWindow().getConnectionPanel().clientDisconnected();

                this.setSelector(null);
                this.setSocketChannel(null);
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
            this.getPendingData().put(socketChannel, Utils.serialize(this.getName()));
            socketChannel.register(this.getSelector(), SelectionKey.OP_WRITE, "Server");
            this.getWindow().getConnectionPanel().clientConnected();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void read(SelectionKey key) {
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
            byte[] data = new byte[readBuffer.limit()];
            readBuffer.get(data, 0, readBuffer.limit());

            Thread thread = new Thread(new DataProcessor(this, key, data));
            thread.start();
            thread.join();

            Thread.sleep(250);

            this.sendData(Utils.serialize("ack"));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void write(SelectionKey key) {
        System.out.println("[Client] Writing data");

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            byte[] data = this.getPendingData().get(socketChannel);
            this.getPendingData().remove(socketChannel);

            System.out.println("\tData written: " + Utils.deserialize(data, 0, data.length));

            socketChannel.write(ByteBuffer.wrap(data));

            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
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
                        this.sendData(Utils.serialize(action));
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
