package firebase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SessionManager {
    private static Preferences prefs;
    public static String localId;
    public static String username;
    public static String email;
    public static String idToken;
    public static String registerDate;
    public static String lastCompletedDate;

    public static int enemiesKilled;
    public static int deaths;
    public static int gamesCompleted;
    public static int points;

    private static void init() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("AzerisSession");
        }
    }

    public static void saveSession() {
        init();
        prefs.putString("localId", localId != null ? localId : "");
        prefs.putString("username", username != null ? username : "");
        prefs.putString("email", email  != null ? email : "");
        prefs.putString("idToken", idToken != null ? idToken : "");
        prefs.putString("registerDate", registerDate != null ? registerDate : "");
        prefs.putString("lastCompletedDate", lastCompletedDate != null ? lastCompletedDate : "");

        prefs.putInteger("enemiesKilled", enemiesKilled);
        prefs.putInteger("deaths", deaths);
        prefs.putInteger("gamesCompleted", gamesCompleted);
        prefs.putInteger("points", points);

        prefs.flush();
    }

    public static boolean loadSession() {
        init();
        localId = prefs.getString("localId", null);
        if (localId == null || localId.isEmpty()) {
            return false;
        }
        username = prefs.getString("username", "");
        email = prefs.getString("email", "");
        idToken = prefs.getString("idToken", "");
        registerDate = prefs.getString("registerDate", "");
        lastCompletedDate = prefs.getString("lastCompletedDate", "");

        enemiesKilled = prefs.getInteger("enemiesKilled", 0);
        deaths = prefs.getInteger("deaths", 0);
        gamesCompleted = prefs.getInteger("gamesCompleted", 0);
        points = prefs.getInteger("points", 0);

        return true;
    }

    public static void clear() {
        init();
        localId = null;
        email = null;
        username = null;
        idToken = null;

        enemiesKilled = 0;
        deaths = 0;
        gamesCompleted = 0;
        points = 0;

        prefs.clear();
        prefs.flush();
    }
}
