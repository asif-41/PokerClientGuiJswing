import Thread.ClientToServer;

import java.net.URI;
import java.net.URISyntaxException;

public class Main {

    //private static String link = "ws://66.42.55.46:8080/WebSocketServerEnd";
    private static String link = "ws://localhost:8080/WebSocketServerEnd";

    public static void main(String[] args) {

        try {

            URI uri = new URI(link);

            for (int i = 0; i < 1; i++) {
                ClientToServer c = new ClientToServer(uri, 8080, 60, 1000, 1000);


            }

        } catch (URISyntaxException e) {
            System.out.println("Invalid link -> " + e);
            e.printStackTrace();
        }

    }
}
