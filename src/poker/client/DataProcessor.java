package poker.client;

import poker.game.Player;
import poker.game.Table;
import poker.utils.Utils;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;

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
        } else if (object instanceof ArrayList) {
            if (((ArrayList) object).get(0) instanceof String) {
                System.out.println("[Client] Available actions: " + object);
                this.getParent().getWindow().getGamePanel().showAvailableActions((ArrayList) object, this.getParent().getPlayer().getMoney());
            }
        } else if (object instanceof Player) {
            System.out.println("[Client] Received Player object (" + ((Player) object).getName() + ", " + ((Player) object).getMoney() + ")");
            this.getParent().setPlayer((Player) object);
            this.getParent().getWindow().getGamePanel().showPlayerInfo((Player) object, false);
        } else if (object instanceof Table) {
            System.out.println("[Client] Received Table object");
            this.getParent().getWindow().getGamePanel().drawCommunityCards((Table) object);
        }
    }

    @Override
    public void run() {
        this.processData(this.getKey(), this.getData());
    }
}
