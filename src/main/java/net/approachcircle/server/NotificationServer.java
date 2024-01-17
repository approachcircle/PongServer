package net.approachcircle.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NotificationServer implements Closeable {
    private URL url;
    private HttpURLConnection connection;
    private OutputStream stream;

    public NotificationServer() {
        try {
            url = new URL("https://ntfy.sh/ns1TVxOd3fPftcKq");
        } catch (MalformedURLException e) {
            e.printStackTrace(System.err);
            System.err.println("notification server URL is malformed.");
            return;
        }
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.err.println("failed to open connection to notification server");
            return;
        }
        try {
            connection.setRequestMethod("PUT");
        } catch (ProtocolException e) {
            e.printStackTrace(System.err);
            System.err.println("notification server protocol is malformed.");
            return;
        }
        connection.setDoOutput(true);
        connection.addRequestProperty("Content-Type", "text/plain");
    }

    public void send(Object message) {
        byte[] buffer = message.toString().getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(buffer.length);
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.err.println("failed to establish connection to notification server");
            return;
        }
        try {
            stream = connection.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.err.println("failed to get output stream to notification server");
            return;
        }
        try {
            stream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.err.println("failed to write to notification server");
        }
    }

    @Override
    public void close() {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.err.println("failed to close stream to notification server");
        }
        connection.disconnect();
    }
}
