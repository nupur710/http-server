import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    static String OK_200= "HTTP/1.1 200 OK";
    static String NOT_FOUND_404= "HTTP/1.1 404 Not Found";
    static String CLRF= "\r\n";
    static String EOSL= "\r\n";
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");
        /**
         * ServerSocket object binds itself to a particular port on server
         * machine. The port acts like an address for incoming client requests
         * The server continously listens to this port, waiting for clients to
         * initate communication. When a client tries to connect to the servers port,
         * the ServerSocket accepts the communication request. This creates a new Socket object
         * representing the established connectin b/w client and server.
         */
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            //This method blocks the server's execution until a client connects. Once a client
            //is established, accept() returns a new Socket() object
            clientSocket = serverSocket.accept(); // Wait for connection from client.
            BufferedReader br= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream dataOut= new DataOutputStream(clientSocket.getOutputStream());
            String request= br.readLine();
            System.out.println("request is " + request);
            int contentLength= 1;
            while (br.readLine() != null) {
                contentLength++;
            }
            System.out.println("content length is " + contentLength);
            String[] requestInParts= request.split(" ");
            String path= null;
            if(requestInParts.length > 2) {
                path= requestInParts[1];
            }
            System.out.println("path is " + path);
            if("/".equals(path)) dataOut.writeBytes(OK_200+CLRF+EOSL);
            else if (path.contains("/echo")) {
                path= path.split("/")[2];
                dataOut.writeBytes(OK_200+CLRF
                        +"Content-Type: text/plain"+CLRF
                        +"Content-Length: "+contentLength+CLRF
                        +path);
            } else dataOut.writeBytes(NOT_FOUND_404+CLRF+EOSL);
            dataOut.flush();
            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOExcep tion: " + e.getMessage());
        }
    }
}