import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.StringTokenizer;

/**
 * Created by Jeroen on 20/01/2017.
 */
public class RequestHandler extends Thread {

    private final static String HTML_FOLDER_LOCATION = "src" + File.separator + "view" + File.separator + "html" + File.separator;
    private Socket socket;
    private Boolean authorized;

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        System.out.println("Start of requesthandler.run");

        try {

            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            BufferedReader r = new BufferedReader(isr);
            String line = r.readLine();
            String authToken = "";
            StringTokenizer st = new StringTokenizer(line);
            while (line != null && !line.isEmpty()) {
                line = r.readLine();
                if (line.startsWith("Authorization")) {
                    //store auth token
                    authToken = line;
                }
                System.out.println(line);
            }
            if (st.hasMoreElements() && st.nextToken().equalsIgnoreCase("GET")
                    && st.hasMoreElements()) {
                //get the filename
                String fileName = st.nextToken();
                //find htaccess file at file location
                File htaccess = findHtaccess((HTML_FOLDER_LOCATION+ fileName).split("/"));
                if (htaccess != null) { //theres a htaccess file, requires authenticating before able to view page
                    authorized = false;
                    //if authtoken not set, definitely unauthorized
                    if (authToken != "") { //Auth is set, check it.
                        //strip everything but the token
                        authToken = authToken.replaceAll("Authorization: Basic ", "");
                        //authenticate user
                        authUser(htaccess, authToken);
                    }
                    if (!authorized) {
                        //Show Login dialog until user is authorized in authUser() and authorized = true
                        StringBuilder authsb = new StringBuilder();
                        authsb.append("HTTP/1.1 401\r\n");
                        authsb.append("WWW-Authenticate: Basic auth=\"test\"\r\n\r\n");
                        authsb.append("<h1> UnAuthorized <h1>");
                        writer.write(authsb.toString());
                        writer.flush();
                    }else{  //he was authorized show file he requested
                        getFile(fileName);
                    }
                } else {//No htaccess found this file is allowed to be visited without login
                    getFile(fileName);
                }
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    /**
     * Authenticates the user by looping through the htaccess file and checking if the user is in there
     * @param htaccessFile
     * @param authToken
     */
    public void authUser(File htaccessFile, String authToken) {
        System.out.println("how many times is this run");
        String credentials = new String(Base64.getDecoder().decode(authToken),
                Charset.forName("UTF-8"));
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(htaccessFile));
            String userLine;
            while ((userLine = br.readLine()) != null) {
                if (userLine.equals(credentials)) {
                    authorized = true;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFile(String fileName) {

        if (fileName.endsWith(".html")) {
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

    /**
     * (
     * Returns the html-code of a html page as a single String.
     *
     * @param pageName (in the form of "X.html")
     * @return HTML as Stringd
     */
    public String htmlToString(String pageName) {
        StringBuilder builder = new StringBuilder();
        String s;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(HTML_FOLDER_LOCATION + pageName));
            while ((s = reader.readLine()) != null) {
                builder.append(s);
            }
            reader.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return builder.toString();
    }

    /**
     * Find htaccess file at location of html file
     * @param paths
     * @return
     */
    public File findHtaccess(String[] paths) {
        String path = "";
        for (String partPath : paths) {
            path += partPath;
            File newFile = new File(path + "/" + ".htaccess");

            if (newFile.exists()) {
                System.out.println("found "+newFile);
                return newFile;
            }
        }
        return null;
    }
}
