package tech.mixt.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;

@ClientEndpoint
public class ChatClientEndpoint {
  //В роли ждуна, чтобы мейн не закрывался
  private static CountDownLatch latch;

  //При коннекте выводим коннект и отправляем сообщение
  @OnOpen
  public void onOpen(Session session) {
    System.out.println ("--- Connected " + session.getId());
    try {
      session.getBasicRemote().sendText("start");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  //При получении сообщения с сервера - выводим
  @OnMessage
  public String onMessage(String message, Session session) {
    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
    try {
      System.out.println ("--- Received " + message);
      return bufferRead.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  //При закрытии коннекшона пишем что закрыли
  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    System.out.println("Session " + session.getId() +
        " closed because " + closeReason);
    latch.countDown();
  }

  //Мейн с созданием коннекшона
  public static void main(String[] args) {
    latch = new CountDownLatch(1);
    ClientManager client = ClientManager.createClient();
    try {
      URI uri = new URI("ws://localhost:8081/socket/app");
      client.connectToServer(ChatClientEndpoint.class, uri);
      latch.await();
    } catch (DeploymentException | URISyntaxException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
