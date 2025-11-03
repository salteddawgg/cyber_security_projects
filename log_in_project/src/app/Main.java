package app;

import java.util.Scanner;

public class Main {
    private static final String SECRET_KEY = "SuperSecretKey123!@#";
    private static Scanner scanner = new Scanner(System.in);
    private static Encryption encryption = new Encryption();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Welcome! Please log in or sign up! ===");
            System.out.println("1. Login");
            System.out.println("2. Sign up");
            System.out.println("3. Get me out of here!!");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        login();
                        break;
                    case "2":
                        signup();
                        break;
                    case "3":
                        System.out.println(":(");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid input");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void login() throws Exception {
        System.out.println("\n=== Login ===");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        User user = UserDataManager.findUser(username);
        if (user == null) {
            System.out.println("User not found");
            return;
        }

        if (Hash2.verifyPassword(password, user.getHashedPassword())) {
            System.out.println("\n Login successful!");
            System.out.println("\n=== User Information ===");
            System.out.println("Name:  " + encryption.decrypt(user.getEncryptedName(), SECRET_KEY));
            System.out.println("Email: " + encryption.decrypt(user.getEncryptedEmail(), SECRET_KEY));
            System.out.println("Phone: " + encryption.decrypt(user.getEncryptedPhone(), SECRET_KEY));
        } else {
            System.out.println(" Invalid username or password");
        }
    }

    private static void signup() throws Exception {
        System.out.println("\n=== Sign up ===");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();

        if (UserDataManager.findUser(username) != null) {
            System.out.println("Username already exists!");
            return;
        }

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (password.length() < 8 ||
            !password.matches(".*[0-9].*") ||
            !password.matches(".*[^a-zA-Z0-9].*")) {
            System.out.println("Password must be at least 8 characters long and include one number and one special character");
            return;
        }

        System.out.print("Enter your Full Name: ");
        String fullName = scanner.nextLine();
        System.out.print("Enter your Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter your Phone Number: ");
        String phone = scanner.nextLine();

        String hashedPassword = Hash2.hashWithPBKDF2(password);
        String encryptedName = encryption.encrypt(fullName, SECRET_KEY);
        String encryptedEmail = encryption.encrypt(email, SECRET_KEY);
        String encryptedPhone = encryption.encrypt(phone, SECRET_KEY);

        User newUser = new User(username, hashedPassword, encryptedName, encryptedEmail, encryptedPhone);
        UserDataManager.saveUser(newUser);

        System.out.println(" Sign up successful please log in");
    }
}
