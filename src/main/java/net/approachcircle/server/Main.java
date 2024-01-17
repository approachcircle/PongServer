package net.approachcircle.server;

public class Main {
    public static void main(String[] args) {
        final int iteration_limit = 5;
        int iterations = 0;
        for (;;) {
            try {
                new Application().run();
                break;
            } catch (RuntimeException e) {
                System.err.println("fatal error caught, stack trace below: ");
                e.printStackTrace(System.err);
                System.err.println("dumping all open rooms: ");
                System.err.println(RoomManager.getInstance().dumpOpenRooms());
                System.err.println("restarting...");
                try (NotificationServer server = new NotificationServer()) {
                    server.send(e.getMessage());
                }
            }
            iterations++;
            if (iterations >= iteration_limit) {
                System.err.println("iteration limit reached. quitting");
                try (NotificationServer server = new NotificationServer()) {
                    server.send("iteration limit reached. quitting");
                }
                break;
            }
        }
    }
}
