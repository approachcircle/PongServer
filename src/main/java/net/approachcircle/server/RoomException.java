package net.approachcircle.server;

public class RoomException extends Exception {
    public RoomException(String message, String id) {
        super(String.format("[ROOM '%s'] %s", id, message));
    }

    public RoomException(String id) {
        this("an unknown error occurred", id);
    }
}
