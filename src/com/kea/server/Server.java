package com.kea.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.kea.common.SharedConstants.J_ER;
import static com.kea.common.SharedConstants.PORT;

public class Server {

    public static void main(String[] args) {

        Server server = new Server();
        server.start();
    }

    private List<ClientHandlerRunnable> joinedClients = new ArrayList<>();
    private final int MAX_CLIENTS = 5;
    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_CLIENTS);
    private final Object mutex = new Object();

    private void start() {

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                System.out.println("Waiting for new client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connection accepted!");
                try {
                    ClientHandlerRunnable runnable = new ClientHandlerRunnable(clientSocket, this);
                    if (joinedClients.size() < MAX_CLIENTS) {
                        executorService.execute(runnable);
                    } else {
                        runnable.sendMessage(J_ER + " Client not accepted. Too many clients!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientJoined(ClientHandlerRunnable clientHandlerRunnable) {
        synchronized (mutex) {
            joinedClients.add(clientHandlerRunnable);
            StringBuilder sb = new StringBuilder("LIST ");
            for (ClientHandlerRunnable client : joinedClients) {
                sb.append(client.getUserName()).append(" ");
            }
            String message = sb.substring(0, sb.length() - 1);

            for (ClientHandlerRunnable client : joinedClients) {
                client.sendMessage(message);
            }
        }
    }

    public void clientDisconnected(ClientHandlerRunnable clientHandlerRunnable) {
        synchronized (mutex) {
            joinedClients.remove(clientHandlerRunnable);
        }
    }

    public void sendToAll(String message) {
        synchronized (mutex) {
            for (ClientHandlerRunnable client : joinedClients) {
                client.sendMessage(message);
            }
        }
    }
}