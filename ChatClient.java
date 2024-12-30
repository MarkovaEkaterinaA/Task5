import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            System.out.println("Подключено к серверу...");
            new Thread(new IncomingMessagesHandler(socket)).start(); // Поток для приема сообщений

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            String message;
            while ((message = consoleReader.readLine()) != null) {
                out.println(message); // Отправка сообщения на сервер
            }
        } catch (IOException e) {
            System.err.println("Ошибка клиента: " + e.getMessage());
        }
    }

    // Внутренний класс для обработки входящих сообщений
    private static class IncomingMessagesHandler implements Runnable {
        private Socket socket;

        public IncomingMessagesHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Сообщение: " + message); // Печать входящего сообщения
                }
            } catch (IOException e) {
                System.err.println("Отключено от сервера: " + e.getMessage());
            }
        }
    }
}
