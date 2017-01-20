import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Created by Jeroen on 20/01/2017.
 */
public class RequestHandler extends Thread {

    private final static String folderLocation = "src" + File.separator + "html";
    private Socket socket;

    public RequestHandler(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        System.out.println("Start of requesthandler.run");
        try {
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            BufferedReader r = new BufferedReader(isr);
            String line = r.readLine();
            StringTokenizer st = new StringTokenizer(line);
            while (line != null && !line.isEmpty()) {
                System.out.println(line);
                line = r.readLine();
            }
            if (st.hasMoreElements() && st.nextToken().equalsIgnoreCase("GET")
                    && st.hasMoreElements()) {
                String fileName = st.nextToken();
                System.out.println("GET on " + fileName);
                getFile(fileName);
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public void getFile(String fileName) {

        if(fileName.endsWith(".html")) {
            try {
                StringBuilder response = new StringBuilder();
                response.append("HTTP/1.1 200 OK\r\n\r\n");
                response.append(htmlToString(fileName));
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer.write(response.toString());
                writer.flush();
                writer.close();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }

    }

    /**(
     * Returns the html-code of a html page as a single String.
     * @param pageName (in the form of "X.html")
     * @return HTML as String
     */
    public String htmlToString(String pageName) {
        StringBuilder builder = new StringBuilder();
        String s;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(folderLocation + pageName));
            while((s = reader.readLine()) != null) {
                builder.append(s);
            }
            reader.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return builder.toString();
    }
}
