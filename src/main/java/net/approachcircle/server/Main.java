package net.approachcircle.server;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setPort(1909);
        SocketIOServer server = new SocketIOServer(config);
        server.addConnectListener(client -> System.out.printf("client connected at %s%n", client.getRemoteAddress()));
        server.addDisconnectListener(client -> {
            System.out.printf("client disconnected at %s%n", client.getRemoteAddress());
            String roomCode = RoomManager.getInstance().getRoomByClient(client);
            try {
                RoomManager.getInstance().leaveRoom(roomCode, client);
            } catch (RoomException ignored) {} // room didn't exist when client disconnected, doesn't really matter though
        });
        server.addEventListener("create-game", void.class, ((client, data, ackSender) -> {
            String code = generateRoomCode();
            client.joinRoom(code);
            RoomManager.getInstance().createRoom(code);
            ackSender.sendAckData(code);
        }));
        server.addEventListener("join-game", String.class, ((client, roomCode, ackSender) -> {
            try {
                RoomManager.getInstance().joinRoom(roomCode, client);
            } catch (RoomException e) {
                ackSender.sendAckData(State.Error, e.toString());
                return;
            }
            client.joinRoom(roomCode);
            ackSender.sendAckData(State.OK, "joined room!");
        }));
        server.addEventListener("leave-game", String.class, ((client, roomCode, ackSender) -> {
            try {
                RoomManager.getInstance().leaveRoom(roomCode, client);
            } catch (RoomException e) {
                ackSender.sendAckData(State.Error, e.toString());
                return;
            }
            client.leaveRoom(roomCode);
            ackSender.sendAckData(State.OK, "left room!");
        }));
        System.out.println("server started");
        server.start();
    }

    public static String generateRoomCode() {
        Random rng = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            char c = (char)rng.nextInt(97, 123);
            code.append(c);
        }
        return code.toString();
    }
}
