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


        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try {
            while(true) {
                serverSocket = new ServerSocket(4221);
                serverSocket.setReuseAddress(true);
                clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("IOExcep tion: " + e.getMessage());
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        try(BufferedReader br= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream dataOut= new DataOutputStream(clientSocket.getOutputStream());)
        {
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }
            String request = requestBuilder.toString();
            String[] requestInParts = request.split(" ");
            String path = null;
            String userAgent= null;
            if (requestInParts.length > 1) {
                path = requestInParts[1];
            }
            if(requestInParts.length > 4) {
                userAgent= requestInParts[4].split("\\s+")[0];;
            }
            System.out.println("userAGent is " +userAgent);
            if ("/".equals(path)) {
                dataOut.writeBytes(OK_200 + CLRF + EOSL);
            } else if (path.contains("/echo")) {
                path = path.substring(6);
                dataOut.writeBytes(OK_200 + CLRF
                        + "Content-Type: text/plain" + CLRF
                        + "Content-Length: " + path.length() + CLRF
                        + CLRF
                        + path);
            } else if(path.contains("/user-agent")) {
                path = path.substring(6);
                System.out.println("path is " +path);
                System.out.println("user agent is " + userAgent);
                dataOut.writeBytes(OK_200+ CLRF +
                        "Content-Type: text/plain" + CLRF +
                        "Content-Length: " + userAgent.length() + CLRF
                        + CLRF
                        + userAgent);
            }
            else {
                dataOut.writeBytes(NOT_FOUND_404 + CLRF + EOSL);
            }
            dataOut.flush();
            System.out.println("accepted new connection");
        } finally {
            clientSocket.close();
        }
        }

}
