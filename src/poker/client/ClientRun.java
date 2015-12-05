package poker.client;

public class ClientRun {

    public static void main(String[] args) {
        Client client = new Client();

        client.setClientName("Player_1");
        client.connect("localhost", 9999);
    }

}
