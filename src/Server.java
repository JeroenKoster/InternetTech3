import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Jeroen on 20/01/2017.
 */
public class Server {

    private final static int SERVER_PORT = 1500;
    private ServerSocket serverSocket;

    public static void main(String[] args)
    {
        new Server().run();
    }


    public void run() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = serverSocket.accept();
            }
        }
        catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

}
