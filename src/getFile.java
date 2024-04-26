import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class getFile {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Main <File Path> <Query>");
            return;
        }

        // Extract file path and query from command-line arguments
//        String filePath = args[0];
        String filePath = args[0];
        String query = args[1];

        String apiKey = "your api key";

        String sourceId = addPDFSource(apiKey, filePath);

        if (sourceId != null) {
            String result = sendChatMessage(apiKey, sourceId, query);
            System.out.println("Result: " + result);
        }
    }

    private static String addPDFSource(String apiKey, String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            URL url = new URL("https://api.chatpdf.com/v1/sources/add-file");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-api-key", apiKey);
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            outputStream.close();
            fileInputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = responseReader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                responseReader.close();
                String jsonResponse = responseBuilder.toString();
                JSONObject jsonObject = new JSONObject(jsonResponse);
                return jsonObject.getString("sourceId");
            } else {
                System.out.println("Status: " + responseCode);
                System.out.println("Error: " + connection.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String sendChatMessage(String apiKey, String sourceId, String query) {
        try {
            URL url = new URL("https://api.chatpdf.com/v1/chats/message");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-api-key", apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject data = new JSONObject();
            data.put("sourceId", sourceId);

            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", query);

            data.put("messages", new JSONObject[] { message });

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.toString().getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = responseReader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                responseReader.close();
                String jsonResponse = responseBuilder.toString();
                JSONObject jsonObject = new JSONObject(jsonResponse);
                return jsonObject.getString("content");
            } else {
                System.out.println("Status: " + responseCode);
                System.out.println("Error: " + connection.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
