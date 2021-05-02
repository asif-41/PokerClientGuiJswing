import Thread.ClientToServer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Main {

    //private static String link = "ws://66.42.55.46:8080/WebSocketServerEnd";
    //private static String link = "ws://localhost:8080/WebSocketServerEnd";
    private static String link = "ws://localhost:1112/WebSocketServerEnd";
    //private static String link = "ws://66.42.55.46:1112/WebSocketServerEnd";

    public static void main(String[] args) throws Exception {

        ArrayList<ClientToServer> ccc = new ArrayList();

        try {
            URI uri = new URI(link);

            for (int i = 0; i < 10; i++) {
                ClientToServer c = new ClientToServer(uri, 1112, 60, 1000, 1000);

            }

        } catch (URISyntaxException e) {
            System.out.println("Invalid link -> " + e);
            e.printStackTrace();
        }

    }
}
