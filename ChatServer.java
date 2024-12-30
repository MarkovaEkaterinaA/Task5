import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345; // Порт для сервера
    private static Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Сервер запущен...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept(); // Ожидание подключения клиента
                System.out.println("Клиент подключен: " + socket.getInetAddress());
                ClientHandler handler = new ClientHandler(socket);
                clientHandlers.add(handler);
                new Thread(handler).start(); // Запуск обработчика клиента в отдельном потоке
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    // Отправка сообщения всем клиентам
    public static void broadcast(String message, ClientHandler sender) {
        synchronized (clientHandlers) {
            for (ClientHandler client : clientHandlers) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    // Удаление клиента из списка
    public static void removeClient(ClientHandler client) {
        clientHandlers.remove(client);
    }

    // Внутренний класс для обработки подключения клиента
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Получено: " + message);
                    ChatServer.broadcast(message, this);
                }
            } catch (IOException e) {
                System.err.println("Клиент отключен: " + e.getMessage());
            } finally {
                ChatServer.removeClient(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии сокета: " + e.getMessage());
                }
            }
        }

        // Отправка сообщения клиенту
        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
