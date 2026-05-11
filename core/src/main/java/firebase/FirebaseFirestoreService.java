package firebase;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;

public class FirebaseFirestoreService {
    private static final String PROJECT_ID = "azeris-c558d";

    public static void createUserProfile(String userId, String email) {
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
            String json = "{"
                + "\"fields\": {"
                + "\"email\": {\"stringValue\": \"" + email + "\"},"
                + "\"coins\": {\"integerValue\": \"0\"},"
                + "\"level\": {\"integerValue\": \"1\"},"
                + "\"bestScore\": {\"integerValue\": \"0\"}"
                + "}"
                + "}";
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Firebase response: " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
