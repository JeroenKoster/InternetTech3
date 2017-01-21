
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Jeroen on 20/01/2017.
 */
public class Server {

    private final static int SERVER_PORT = 8080;
    private ServerSocket serverSocket;
    Socket socket;

    public static void main(String[] args)
    {
        new Server().run();
    }

    public Server()
    {
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Waiting for connection on port " + SERVER_PORT + "...");
            while (true) {
                socket = serverSocket.accept();
                Thread t = new RequestHandler(socket);
                t.start();
            }
        }
        catch (IOException ioe) {
            System.out.println("Exception: " + ioe.getMessage());
        }
    }
}
