package com.kea.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static com.kea.common.SharedConstants.*;

public class Client {

    public static void main(String[] args) {

        Client client = new Client();
        client.start();
    }

    private void start() {

        try (Socket socket = new Socket("localhost", PORT);
             DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
             DataInputStream reader = new DataInputStream((socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Enter your username");
            String userName = scanner.nextLine();
            String joinChat = JOIN + " " + userName;
            writer.writeUTF(joinChat);
            String msgFromServer = reader.readUTF();
            if (msgFromServer.startsWith(J_OK + " ")) {
                System.out.println(msgFromServer.substring(5));
            } else {
                System.out.println(msgFromServer.substring(5));
                return;
            }

            Thread serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String msg = reader.readUTF();
                            System.out.println(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            serverThread.start();

            while (true) {
                String txt = scanner.nextLine();
                writer.writeUTF(DATA + " " + userName + ": " + txt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}