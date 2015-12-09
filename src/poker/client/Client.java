package poker.client;

/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

import poker.game.Player;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Client implements Runnable {

    private InetSocketAddress address;

    private int port;

    private SocketChannel sc;

    private Selector selector;

    private final int buffSize;

    private Player player;

    private String name;

    public Client() {
        this.address = null;
        this.port = 0;
        this.sc = null;
        this.selector = null;
        this.buffSize = 256;
        this.player = null;
        this.name = "default";
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public SocketChannel getSc() {
        return sc;
    }

    public void setSc(SocketChannel sc) {
        this.sc = sc;
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

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void connect(String address, int port) {
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
            this.getSc().write(ByteBuffer.wrap(this.getName().getBytes()));
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

                        if (key.isConnectable()) {
                            System.out.println("[Client] Connectable.");
                        } else if (key.isAcceptable()) {
                            this.handleAccept(key);
                        } else if (key.isReadable()) {
                            this.handleRead(key);
                        } else if (key.isWritable()) {
                            System.out.println("[Client] Writeable.");
                        } else {
                            System.out.println("[Client] Nothing to do...");
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

            System.out.println("[Client] Dissconnected");
        }
    }

    public void sendMessage(String message) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));

            this.getSc().write(buffer);
            buffer.clear();

            System.out.println("[Client] Message sent: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept(SelectionKey key) {
        try {
            SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
            sc.configureBlocking(false);
            sc.register(this.getSelector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[Client] Connected to server");
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
                System.out.println("[Client] Server dissconnected");
            }

            if (!message.equals("")) {
                System.out.println("[Client] Message received: " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        this.clientLoop();
    }
}
