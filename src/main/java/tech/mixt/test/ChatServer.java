package tech.mixt.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.glassfish.tyrus.server.Server;

public class ChatServer {

  //мейн с поднятием вебсокет-сервера
  public static void main(String[] args) {
    Server server;
    server = new Server("localhost", 8081, "/socket", ChatServerEndpoint.class);
    try {
      server.start();
      System.out.println("--- server is running");
      System.out.println("--- press any key to stop the server");
      BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
      bufferRead.readLine();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      server.stop();
    }
  }

}