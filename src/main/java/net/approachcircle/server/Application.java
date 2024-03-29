package net.approachcircle.server;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

import java.util.Random;

public class Application {
    private final int ROOM_CODE_LENGTH = 6;
    public void run() {
        Configuration config = new Configuration();
        config.setPort(1909);
        SocketIOServer server = new SocketIOServer(config);
        subscribeEventHandlers(server);
        System.out.println("listening");
        server.start();
    }

    private void subscribeEventHandlers(SocketIOServer server) {
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
            RoomManager.getInstance().createRoom(code);
            RoomManager.getInstance().joinRoom(code, client);
            ackSender.sendAckData(State.OK, code);
        }));
        server.addEventListener("join-game", String.class, ((client, roomCode, ackSender) -> {
            try {
                RoomManager.getInstance().joinRoom(roomCode, client);
            } catch (RoomException e) {
                ackSender.sendAckData(State.Error.toString(), e.getMessage());
                return;
            }
            ackSender.sendAckData(State.OK, "joined room!");
        }));
        server.addEventListener("leave-game", String.class, ((client, roomCode, ackSender) -> {
            try {
                RoomManager.getInstance().leaveRoom(roomCode, client);
            } catch (RoomException e) {
                ackSender.sendAckData(State.Error, e.getMessage());
                return;
            }
            ackSender.sendAckData(State.OK, "left room!");
        }));
    }


    private String generateRoomCode() {
        Random rng = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < ROOM_CODE_LENGTH; i++) {
            char c = (char)rng.nextInt(97, 123);
            code.append(c);
        }
        return code.toString();
    }
}
