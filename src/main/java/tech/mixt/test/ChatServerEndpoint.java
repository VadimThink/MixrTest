package tech.mixt.test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

//аннотация чтобы обозначить что это серверный вебсокет
@ServerEndpoint(value = "/app")
public class ChatServerEndpoint {

  private static Set<Session> sessions = new HashSet<>();
  private static Set<BigInteger> uniqueNumberSet = new HashSet<>();
  private static Random random = new Random();

  //при открытии коннекшона проверяем нет ли еще таких же айпишников
  @OnOpen
  public void onOpen(Session session) throws IOException {
    if (isConnectionLimitReached(session)) {
      session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Multiple connections not allowed"));
    } else {
      sessions.add(session);
    }
    System.out.println ("Connected, sessionID = " + session.getId());
  }

  //при получении сообщения - идем считать число или закрываем коннекшон если пришло "quit"
  @OnMessage
  public String onMessage(String message, Session session) {
    if (message.equals("quit")) {
      try {
        session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Bye!"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      BigInteger uniqueNumber = generateUniqueNumber();
      uniqueNumberSet.add(uniqueNumber);
      message = "{\"number\":\"" + uniqueNumber + "\"}";
    }
    return message;
  }

  //обработка закрытия сессии
  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    System.out.println("Session " + session.getId() +
        " closed because " + closeReason);
  }

  //метод генерации случайных уникальных бигинтов
  private BigInteger generateUniqueNumber() {
    BigInteger uniqueNumber = new BigInteger(128, random);
    while (uniqueNumberSet.contains(uniqueNumber)) {
      uniqueNumber = new BigInteger(128, random);
    }
    return uniqueNumber;
  }

  //проверка наших айпишников на уникальность
  private boolean isConnectionLimitReached(Session session) {
    String clientIP = session.getRequestParameterMap().get("Client-IP").get(0);
    //можно было написать в одну строчку, но так нагляднее
    for (Session existingSession : sessions) {
      String existingClientIP = existingSession.getRequestParameterMap().get("Client-IP").get(0);
      if (existingClientIP.equals(clientIP)) {
        return true;
      }
    }
    return false;
  }
}