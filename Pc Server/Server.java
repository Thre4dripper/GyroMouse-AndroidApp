import java.awt.*;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Server {
    ServerSocket serverSocket;
    static int x;
    static int y;

    Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                new Thread(() -> {
                    while (socket.isConnected()) {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String message = reader.readLine();
                            controlMouse(message);
                        } catch (Exception e) {
                            disconnectClient(socket);
                            break;
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            System.out.println("Server closed");
        }
    }

    public static void controlMouse(String message) {
//        System.out.println(message);
        String[] coords = message.split(" ");
        String action = coords[0];
        x = MouseInfo.getPointerInfo().getLocation().x;
        y = MouseInfo.getPointerInfo().getLocation().y;
        int dx = Integer.parseInt(coords[1]);
        int dy = Integer.parseInt(coords[2]);

        try {
            Robot robot = new Robot();
            if (Objects.equals(action, "move")) {
                robot.mouseMove(960+dx, 540+dy);
            } else if (Objects.equals(action, "leftClick")) {
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            } else if (Objects.equals(action, "rightClick")) {
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            }

        } catch (Exception e) {
            System.out.println("Server closed");
        }

    }

    public static void disconnectClient(Socket socket) {
        System.out.println("Client disconnected");
        try {
            socket.close();
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            Server server = new Server(serverSocket);


            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server is running on " + ip);

            server.startServer();

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
