package com.kea.server;

import com.kea.common.SharedConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.kea.common.SharedConstants.*;

class ClientHandlerRunnable implements Runnable {

    private Socket clientSocket;
    private Server server;
    private String userName;
    DataOutputStream dataOut;

    public String getUserName() {
        return userName;
    }

    ClientHandlerRunnable(Socket clientSocket, Server server) throws IOException {

        this.clientSocket = clientSocket;
        this.server = server;
        this.dataOut = new DataOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void run() {

        try (DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream())) {

            String clientJoin = dataIn.readUTF();
            if (clientJoin.startsWith(SharedConstants.JOIN + " ")) {
                if (clientJoin.length() < 6 || clientJoin.length() > 17) {
                    dataOut.writeUTF(J_ER + " Username must be between 1 and 12 characters long");
                    return;
                } else {
                    userName = clientJoin.substring(5);
                    dataOut.writeUTF(J_OK + " Welcome to the chat: " + userName);
                    System.out.println("User: " + userName + " is connected to the chat");
                    server.clientJoined(this);
                }
            } else {
                dataOut.writeUTF(J_ER + " To join chat please write: \"JOIN 'username'\"");
                return;
            }

            while (true) {
                String message = dataIn.readUTF();
                if (message.startsWith(DATA + " ")) {
                    message = message.substring(5);
                    server.sendToAll(message);
                }
                System.out.println(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.clientDisconnected(this);
        }
    }

    public void sendMessage(String message) {
        try {
            dataOut.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
