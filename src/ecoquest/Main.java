package ecoquest;

import ecoquest.http.EcoQuestServer;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        EcoQuestServer server = new EcoQuestServer(port);
        server.start();
    }
}
