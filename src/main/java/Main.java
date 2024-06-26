import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public class Main {

    static String OK_200= "HTTP/1.1 200 OK";
    static String NOT_FOUND_404= "HTTP/1.1 404 Not Found";
    static String CLRF= "\r\n";
    static String EOSL= "\r\n";
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        try {
            ServerSocket serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            int requestCounter = 0; // track requests

            while (true) {
                final Socket clientSocket = serverSocket.accept();
                requestCounter++;

                String directory =
                        (args.length > 1 && args[0].equals("--directory")) ? args[1] : "./";

                // Start a new thread only if server recieves multiple requests
                if (requestCounter > 1) {
                    Thread thread = new Thread(() -> {
                        try {
                            handleRequest(clientSocket, directory);
                        } catch (IOException e) {
                            System.out.println("Error handling request " + e.getMessage());
                        }
                    });
                    thread.start();
                } else {
                    handleRequest(clientSocket, directory);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleRequest(Socket clientSocket, String directory) throws IOException {
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
            String fileName= null;
            String requestType= null;
            if (requestInParts.length > 1) {
                requestType= requestInParts[0];
                path = requestInParts[1];
            }
            if(requestInParts.length > 4) {
                userAgent= requestInParts[4].split("\\s+")[0];;
            }
            if(requestInParts[1].startsWith("/files")) {
                fileName= requestInParts[1].substring(7);
            }
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
            } else if (requestType.equals("GET") && path.contains("files")) {
                String body= null;
                if((body= getFile(fileName, directory)) == null) {
                    dataOut.writeBytes(NOT_FOUND_404 + CLRF +
                            "Content-Type: application/octet-stream" + CLRF +
                            "Content-Length: 0" + CLRF + CLRF);
                }  else {
                    System.out.println("to send ok");
                    dataOut.writeBytes(OK_200 + CLRF +
                            "Content-Type: application/octet-stream" + CLRF +
                            "Content-Length: " + body.length() + CLRF + CLRF +
                            body);
                }
            } else if (requestType.equals("POST") && path.contains("files")) {
                postFile(fileName, directory, clientSocket, dataOut);
            } else {
                dataOut.writeBytes(NOT_FOUND_404 + CLRF + EOSL);
            }
            dataOut.flush();
            System.out.println("accepted new connection");
        } finally {
            clientSocket.close();
        }
        }

    private static void postFile(String fileName, String directory, Socket clientSocket, DataOutputStream dataOut) throws IOException {
        try (InputStream inputStream = clientSocket.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(Paths.get(directory, fileName).toString())) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        System.out.println("File uploaded successfully!");
        dataOut.writeBytes(OK_200 + CLRF + CLRF);
    }

        private static String getFile(String fileName, String directory) {
            File file= new File(Paths.get(directory, fileName).toString());
            if(file.exists()) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    int character= 0;
                    var string= new StringBuilder();
                    while((character=br.read()) != -1) {
                        string.append( (char) character);
                    }
                    return string.toString();
                } catch (FileNotFoundException e) {
                    System.out.println("No such file " + e.getMessage());
                } catch (IOException i) {
                    System.out.println("IO exception: " + i.getMessage());
                }
            }
            return null;
        }

}


