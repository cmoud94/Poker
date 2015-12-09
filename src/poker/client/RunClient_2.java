package poker.client;

/**
 * Project: Poker
 * Created by cmoud94 on 12/9/15.
 */
public class RunClient_2 {

    public static void main(String[] args) {
        Client client = new Client();
        client.setName("Player_2");
        client.connect("localhost", 9999);
        client.clientLoop();
    }

}
