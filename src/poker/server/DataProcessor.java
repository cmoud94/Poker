/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.server;

import poker.game.Player;
import poker.utils.Utils;

import java.nio.channels.SelectionKey;

public class DataProcessor implements Runnable {

    private Server parent;

    private SelectionKey key;

    private byte[] data;

    public DataProcessor(Server parent, SelectionKey key, byte[] data) {
        this.parent = parent;
        this.key = key;
        this.data = data;
    }

    public Server getParent() {
        return parent;
    }

    public SelectionKey getKey() {
        return key;
    }

    public byte[] getData() {
        return data;
    }

    public void processData(SelectionKey key, byte[] data) {
        Object object = Utils.getBytesAsObject(data);

        if (object instanceof String) {
            if (key.attachment().equals("accept")) {
                System.out.println("[Server] Player " + object + " has connected");

                key.attach(object);

                int id = this.getParent().getGame().getPlayers().size() + 1;
                this.getParent().getGame().getPlayers().add(new Player(id, (String) object, this.getParent().getStartingMoney()));

                if (this.getParent().getGame().getPlayers().size() == this.getParent().getGame().getNumOfPlayers()) {
                    this.getParent().getGame().init();
                }
            } else if (object.equals("yes")) {
                Player player = this.getParent().getGame().getPlayerByPlayerName((String) key.attachment());
                System.out.println("[Server] " + player.getName() + " is now ready to play");


                this.getParent().setLastMessage("");
                player.setReady(true);

                boolean allReady = true;
                for (Player p : this.getParent().getGame().getPlayers()) {
                    if (!p.isReady()) {
                        allReady = false;
                    }
                }

                if (allReady) {
                    System.out.println("[Server] Starting gameLoop");
                    Thread gameLoopThread = new Thread(this.getParent().getGame(), "serverGameLoop");
                    gameLoopThread.start();
                }
            } else if (object.equals("check") || object.equals("fold") || object.equals("call") || ((String) object).contains("bet") || object.equals("all-in")) {
                System.out.println("[Server] " + key.attachment() + ": " + object);
                this.getParent().setLastMessage((String) object);
            }
        }
    }

    @Override
    public void run() {
        this.processData(this.getKey(), this.getData());
    }

}
