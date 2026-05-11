package firebase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseAuthService {
    private static final String API_KEY = "AIzaSyBQu2gt4h9eYtfYz9pwFsRZsJdD-kgRKbs";

    public static String login(String email, String password) {
        try {
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
            + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            String body =
                "{"
                    + "\"email\":\"" + email + "\","
                    + "\"password\":\"" + password + "\","
                    + "\"returnSecureToken\":true"
                    + "}";

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes());
            os.close();

            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String register(String email, String password) {
        try {
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key="
                    + API_KEY);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setDoInput(true);
            conn.setDoOutput(true);
            String body = "{"
                + "\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\","
                + "\"returnSecureToken\":true"
                + "}";

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes());
            os.close();

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
}
