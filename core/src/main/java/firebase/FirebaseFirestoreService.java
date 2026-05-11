package firebase;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

public class FirebaseFirestoreService {
    private static final String PROJECT_ID = "azeris-c558d";
    public static void createUserProfile(String userId, String username, String email) {
        try {
            URL url = new URL(
                "https://firestore.googleapis.com/v1/projects/"
                + PROJECT_ID +
                    "/databases/(default)/documents/players?documentId="
                + userId
            );
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setDoOutput(true);
            String today = LocalDate.now().toString();
            String json =
                "{ \"fields\": {"
                    + "\"username\": {\"stringValue\": \"" + username + "\"},"
                    + "\"email\": {\"stringValue\": \"" + email + "\"},"
                    + "\"registerDate\": {\"stringValue\": \"" + today + "\"},"
                    + "\"lastCompletedDate\": {\"stringValue\": \"never\"},"
                    + "\"enemiesKilled\": {\"integerValue\": \"0\"},"
                    + "\"deaths\": {\"integerValue\": \"0\"},"
                    + "\"gamesCompleted\": {\"integerValue\": \"0\"},"
                    + "\"points\": {\"integerValue\": \"0\"}"
                    + "} }";
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Firebase response: " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUserProfile(String userId) {
        try {
            URL url = new URL(
                "https://firestore.googleapis.com/v1/projects/"
                    + PROJECT_ID +
                    "/databases/(default)/documents/players/"
                    + userId
            );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadUserProfile(String userId) {
        try {
            String response = getUserProfile(userId);
            if (response == null) {
                System.out.println("Firestore response null");
                return;
            }
            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            JsonObject fields = root.getAsJsonObject("fields");

            SessionManager.username = safeGet(fields, "username", "Unknown");
            SessionManager.email = safeGet(fields, "email", "NO EMAIL");
            SessionManager.registerDate = safeGet(fields, "registerDate", "unknown");
            SessionManager.lastCompletedDate = safeGet(fields, "lastCompletedDate", "never");
            SessionManager.enemiesKilled = Integer.parseInt(safeGet(fields, "enemiesKilled", "0"));
            SessionManager.deaths = Integer.parseInt(safeGet(fields, "deaths", "0"));
            SessionManager.gamesCompleted = Integer.parseInt(safeGet(fields, "gamesCompleted", "0"));
            SessionManager.points = Integer.parseInt(safeGet(fields, "points", "0"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteUserProfile(String userId) {
        try {
            URL url = new URL(
                "https://firestore.googleapis.com/v1/projects/"
                    + PROJECT_ID +
                    "/databases/(default)/documents/players/"
                    + userId
            );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("DELETE");
            int response = conn.getResponseCode();
            System.out.println("Firestore delete: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String safeGet(JsonObject fields, String key, String defaultValue) {
        if (fields == null) return defaultValue;
        if (!fields.has(key)) return defaultValue;
        JsonObject obj = fields.getAsJsonObject(key);
        if (obj == null) return defaultValue;
        if (obj.has("stringValue"))
            return obj.get("stringValue").getAsString();
        if (obj.has("integerValue"))
            return obj.get("integerValue").getAsString();
        return defaultValue;
    }

    public static void updateStats(String userId, int enemiesKilled, int deaths, int gamesCompleted, int points) {
        try {
            URL url = new URL(
                "https://firestore.googleapis.com/v1/projects/"
                    + PROJECT_ID
                    + "/databases/(default)/documents/players/"
                    + userId
                    + "?updateMask.fieldPaths=enemiesKilled"
                    + "&updateMask.fieldPaths=deaths"
                    + "&updateMask.fieldPaths=gamesCompleted"
                    + "&updateMask.fieldPaths=points"
            );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json =
                "{ \"fields\": {"
                    + "\"enemiesKilled\": {\"integerValue\": \"" + enemiesKilled + "\"},"
                    + "\"deaths\":        {\"integerValue\": \"" + deaths        + "\"},"
                    + "\"gamesCompleted\":{\"integerValue\": \"" + gamesCompleted + "\"},"
                    + "\"points\":        {\"integerValue\": \"" + points        + "\"}"
                    + "} }";
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.close();
            System.out.println("[STATS] Firestore update: " + conn.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateLastCompleted(String userId) {
        try {
            String today = java.time.LocalDate.now().toString();
            URL url = new URL(
                "https://firestore.googleapis.com/v1/projects/"
                    + PROJECT_ID +
                    "/databases/(default)/documents/players/"
                    + userId +
                    "?updateMask.fieldPaths=lastCompletedDate"
            );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            String json =
                "{ \"fields\": {"
                    + "\"lastCompletedDate\": {\"stringValue\": \"" + today + "\"}"
                    + "} }";
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.close();
            System.out.println("Updated lastCompletedDate: " + conn.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
