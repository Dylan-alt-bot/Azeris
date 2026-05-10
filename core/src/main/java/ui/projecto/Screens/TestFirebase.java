package ui.projecto.Screens;

import firebase.FirebaseAuthService;

public class TestFirebase {
    public static void main(String[] args) {
        String response = FirebaseAuthService.login(
            "losguapos@gmail.com",
            "12345678"
        );
        System.out.println("RESPUESTA FIREBASE:");
        System.out.println(response);
    }
}
