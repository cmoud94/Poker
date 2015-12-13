/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.client;

import poker.game.Player;
import poker.game.Table;
import poker.utils.Utils;

import java.nio.channels.SelectionKey;
import java.util.List;

public class DataProcessor implements Runnable {

    private Client parent;

    private SelectionKey key;

    private byte[] data;

    public DataProcessor(Client parent, SelectionKey key, byte[] data) {
        this.parent = parent;
        this.key = key;
        this.data = data;
    }

    public Client getParent() {
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
            if (object.equals("[Game] Are you ready?")) {
                System.out.println("[Client] " + object);
                this.getParent().getWindow().getConnectionPanel().serverReady();
            } else if (object.equals("new-round")) {
                this.getParent().getWindow().getGamePanel().newRound();
            } else {
                System.out.println("[Client] " + key.attachment() + ": " + object);
            }
        } else if (object instanceof List) {
            if (((List) object).get(0) instanceof String) {
                System.out.println("[Client] Available actions: " + object);
                this.getParent().getWindow().getGamePanel().showAvailableActions((List) object, this.getParent().getPlayer().getMoney());
            } else if (((List) object).get(0) instanceof Player) {
                System.out.println("[Client] Received players info");
                for (int i = 0; i < ((List) object).size(); i++) {
                    Player player = ((Player) ((List) object).get(i));
                    System.out.println("\tPlayer " + (i + 1) + ": " + player.getName() + " | " + player.getMoney());
                    if (this.getParent().getName().equals(player.getName())) {
                        this.getParent().setPlayer(player);
                    }
                }
                this.getParent().getWindow().getGamePanel().drawPlayers((List) object, false);
            }
        } else if (object instanceof Table) {
            System.out.println("[Client] Received Table object");
            this.getParent().getWindow().getGamePanel().drawCommunityCards((Table) object);
        } else if (object instanceof Integer) {
            System.out.println("[Client] Received table pot");
            this.getParent().getWindow().getGamePanel().updatePot((Integer) object);
        }

        /*else if (object instanceof Player) {
            System.out.println("[Client] Received Player object (" + ((Player) object).getName() + ", " + ((Player) object).getMoney() + ")");
            this.getParent().setPlayer((Player) object);
            //this.getParent().getWindow().getGamePanel().drawPlayer((Player) object);
        }*/
    }

    @Override
    public void run() {
        this.processData(this.getKey(), this.getData());
    }

}
