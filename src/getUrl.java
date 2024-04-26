import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONObject;

public class getUrl {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Main <PDF Link> <Query>");
            return;
        }

        // Extract PDF link and query from command-line arguments
        String pdfLink = args[0];
        String query = args[1];

        Map<String, String> headers = new HashMap<>();
        headers.put("x-api-key", "your api key");
        headers.put("Content-Type", "application/json");

        JSONObject data = new JSONObject();
        data.put("url", pdfLink);

        String sourceId = addPDFSource(headers, data);

        headers.clear();
        headers.put("x-api-key", "your api key");
        headers.put("Content-Type", "application/json");

        data = new JSONObject();
        data.put("sourceId", sourceId);
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", query);
        data.put("messages", new JSONObject[] { message });

        String result = sendChatMessage(headers, data);
        System.out.println("Cognito: " + result);
    }


    private static String addPDFSource(Map<String, String> headers, JSONObject data) {
        try {
            URL url = new URL("https://api.chatpdf.com/v1/sources/add-url");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            connection.setDoOutput(true);
            connection.getOutputStream().write(data.toString().getBytes());

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                JSONObject responseJson = new JSONObject(response.toString());
                return responseJson.getString("sourceId");
            } else {
                System.out.println("Status: " + responseCode);
                System.out.println("Error: " + connection.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String sendChatMessage(Map<String, String> headers, JSONObject data) {
        try {
            URL url = new URL("https://api.chatpdf.com/v1/chats/message");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            connection.setDoOutput(true);
            connection.getOutputStream().write(data.toString().getBytes());

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                JSONObject responseJson = new JSONObject(response.toString());
                return responseJson.getString("content");
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

