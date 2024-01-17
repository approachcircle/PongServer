package net.approachcircle.server;

import com.corundumstudio.socketio.SocketIOClient;
import java.util.*;

public class RoomManager {
    private final Dictionary<String, List<SocketIOClient>> rooms;
    private static RoomManager instance;
    private final int ROOM_CODE_LENGTH = 6;

    private RoomManager() {
        rooms = new Hashtable<>();
    }

    public static RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public void createRoom(String id) throws RoomException {
        if (id.length() > ROOM_CODE_LENGTH) {
            throw new RoomException("this room code is invalid", id);
        }
        rooms.put(id, new ArrayList<>());
        System.out.printf("room '%s' created!%n", id);
    }

    public void joinRoom(String id, SocketIOClient client) throws RoomException {
        if (rooms.get(id).contains(client)) {
            throw new RoomException("you are already in this room", id);
        }
        if (getRoomByClient(client) != null) {
            throw new RoomException("you are still in another room", id);
        }
        if (rooms.get(id) == null) {
            throw new RoomException("this room does not exist", id);
        }
        client.joinRoom(id);
        rooms.get(id).add(client);
        System.out.printf("client %s joined room '%s'%n", client.getRemoteAddress(), id);
        if (rooms.get(id).size() > 1) { // somebody else is in the room, let them know we've joined
            rooms.get(id).get(0).sendEvent("opponent-joined");
        }
    }

    public void leaveRoom(String id, SocketIOClient client) throws RoomException {
        if (rooms.get(id) == null) {
            throw new RoomException("this room does not exist", id);
        }
        client.leaveRoom(id);
        rooms.get(id).remove(client);
        System.out.printf("client %s left room '%s'%n", client.getRemoteAddress(), id);
        if (!rooms.get(id).isEmpty()) {
            rooms.get(id).get(0).sendEvent("opponent-left");
        }
        if (rooms.get(id).isEmpty()) {
            discardRoom(id);
        }
    }

    public void discardRoom(String id) {
        // ensure room is empty before discarding
        for (SocketIOClient client : getClientsInRoom(id)) {
            try {
                leaveRoom(id, client);
            } catch (RoomException ignored) {} // the room may already not exist, which doesn't really matter to us
        }
        rooms.remove(id);
        System.out.printf("room '%s' discarded!%n", id);
    }

    public List<SocketIOClient> getClientsInRoom(String id) {
        return rooms.get(id);
    }

    public String getRoomByClient(SocketIOClient client) {
        for (Iterator<String> it = rooms.keys().asIterator(); it.hasNext();) {
            String roomCode = it.next();
            if (rooms.get(roomCode).contains(client)) {
                return roomCode;
            }
        }
        return null;
    }

    public String dumpOpenRooms() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> keyIt = rooms.keys().asIterator(); keyIt.hasNext(); ) {
            String code = keyIt.next();
            sb.append(String.format("\tROOM CODE: '%s'%n", code));
            for (SocketIOClient client : getClientsInRoom(code)) {
                sb.append(String.format("\t\tCLIENT: '%s'%n", client.getRemoteAddress()));
            }
        }
        return sb.toString();
    }
}
