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
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept();
            handleRequest(clientSocket);
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
            if (requestInParts.length > 1) {
                path = requestInParts[1];
            }
            System.out.println("path is " +path);
            if ("/".equals(path)) {
                dataOut.writeBytes(OK_200 + CLRF + EOSL);
            } else if (path != null && path.contains("/echo")) {
                path = path.split("/")[2];
                String str= path.substring(6);
                System.out.println("str is "+ str);
                dataOut.writeBytes(OK_200 + CLRF
                        + "Content-Type: text/plain" + CLRF
                        + "Content-Length: " + str.length() + CLRF
                        + CLRF
                        + path);
            } else {
                dataOut.writeBytes(NOT_FOUND_404 + CLRF + EOSL);
            }
            dataOut.flush();
            System.out.println("accepted new connection");
        } finally {
            clientSocket.close();
        }
        }

}
