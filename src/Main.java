import Thread.ClientToServer;

import java.net.URI;
import java.net.URISyntaxException;

public class Main {

    //private static String link = "ws://66.42.55.46:8080/WebSocketServerEnd";
    //private static String link = "ws://localhost:8080/WebSocketServerEnd";
    private static String link = "ws://localhost:1112/WebSocketServerEnd";
    //private static String link = "ws://66.42.55.46:1112/WebSocketServerEnd";
    //private static String link = "ws://66.42.55.46:8080/WebSocketServerEnd";
    //private static String link = "ws://66.42.55.46:1113/WebSocketServerEnd";

    public static void main(String[] args) throws Exception {

        try {
            URI uri = new URI(link);

            for (int i = 0; i < 3; i++) {
                ClientToServer c = new ClientToServer(uri, 1112, 60, 1000, 1000);
                c.check(i);

                //Thread.sleep(50);
                //System.out.println("done " + i);
            }

        } catch (URISyntaxException e) {
            System.out.println("Invalid link -> " + e);
            e.printStackTrace();
        }

    }
}
