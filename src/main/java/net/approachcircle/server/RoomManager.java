package net.approachcircle.server;

import com.corundumstudio.socketio.SocketIOClient;

import java.net.Socket;
import java.util.*;

public class RoomManager {
    private Dictionary<String, List<SocketIOClient>> rooms;
    private static RoomManager instance;

    private RoomManager() {
        rooms = new Hashtable<>();
    }

    public static RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public void createRoom(String id) {
        rooms.put(id, new ArrayList<>());
        System.out.printf("room '%s' created!%n", id);
    }

    public void joinRoom(String id, SocketIOClient client) throws RoomException {
        if (rooms.get(id) == null) {
            throw new RoomException("this room no longer exists", id);
        }
        rooms.get(id).add(client);
        System.out.printf("client %s joined room '%s'%n", client.getRemoteAddress(), id);
        rooms.get(id).get(0).sendEvent("opponent-joined");
    }

    public void leaveRoom(String id, SocketIOClient client) throws RoomException {
        if (rooms.get(id) == null) {
            throw new RoomException("this room no longer exists", id);
        }
        rooms.get(id).remove(client);
        System.out.printf("client %s left room '%s'%n", client.getRemoteAddress(), id);
        if (!rooms.get(id).isEmpty()) {
            rooms.get(id).get(0).sendEvent("opponent-left");
        }
        System.out.println("finished up");
        if (rooms.get(id).isEmpty()) {
            discardRoom(id);
        }
    }

    public void discardRoom(String id) {
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
}
