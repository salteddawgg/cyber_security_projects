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
            System.out.println("3. Forgot Password");
            System.out.println("4. Exit");
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
                        forgotPassword();
                        break;
                    case "4":
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
            // Always require MFA (SMS)
            if (!verifyMFA(username, user.getEncryptedPhone())) {
                System.out.println("MFA verification failed");
                return;
            }

            System.out.println("\nLogin successful!");
            System.out.println("\n=== User Information ===");
            System.out.println("Name:  " + encryption.decrypt(user.getEncryptedName(), SECRET_KEY));
            System.out.println("Email: " + encryption.decrypt(user.getEncryptedEmail(), SECRET_KEY));
            System.out.println("Phone: " + encryption.decrypt(user.getEncryptedPhone(), SECRET_KEY));
        } else {
            System.out.println("Invalid username or password");
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

        // Set up security question for password recovery
        System.out.println("\n=== Set up Security Question ===");
        System.out.println("Choose a security question:");
        for (int i = 0; i < SecurityQuestionManager.SECURITY_QUESTIONS.length; i++) {
            System.out.println((i + 1) + ". " + SecurityQuestionManager.SECURITY_QUESTIONS[i]);
        }
        System.out.print("Select question (1-" + SecurityQuestionManager.SECURITY_QUESTIONS.length + "): ");
        int questionIndex = Integer.parseInt(scanner.nextLine()) - 1;

        if (questionIndex < 0 || questionIndex >= SecurityQuestionManager.SECURITY_QUESTIONS.length) {
            System.out.println("Invalid question selection");
            return;
        }

        System.out.println("Question: " + SecurityQuestionManager.SECURITY_QUESTIONS[questionIndex]);
        System.out.print("Your answer: ");
        String answer = scanner.nextLine();
        String encryptedAnswer = encryption.encrypt(answer, SECRET_KEY);

        // MFA is always enabled (SMS-based)
        boolean mfaEnabled = true;

        String hashedPassword = Hash2.hashWithPBKDF2(password);
        String encryptedName = encryption.encrypt(fullName, SECRET_KEY);
        String encryptedEmail = encryption.encrypt(email, SECRET_KEY);
        String encryptedPhone = encryption.encrypt(phone, SECRET_KEY);

        User newUser = new User(username, hashedPassword, encryptedName, encryptedEmail, encryptedPhone, mfaEnabled);
        UserDataManager.saveUser(newUser);
        SecurityQuestionManager.saveSecurityQuestion(username, questionIndex, encryptedAnswer);

        System.out.println("Sign up successful please log in");
    }

    private static void forgotPassword() throws Exception {
        System.out.println("\n=== Forgot Password ===");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();

        User user = UserDataManager.findUser(username);
        if (user == null) {
            System.out.println("User not found");
            return;
        }

        // Verify security question
        SecurityQuestionManager.SecurityQuestion sq = SecurityQuestionManager.getSecurityQuestion(username);
        if (sq == null) {
            System.out.println("Security question not set up for this user");
            return;
        }

        System.out.println("\nSecurity Question: " + sq.question);
        System.out.print("Your answer: ");
        String answer = scanner.nextLine();

        if (!SecurityQuestionManager.verifySecurityAnswer(username, answer, SECRET_KEY)) {
            System.out.println("Incorrect answer");
            return;
        }

        System.out.println("Security question verified!");
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        if (newPassword.length() < 8 ||
            !newPassword.matches(".*[0-9].*") ||
            !newPassword.matches(".*[^a-zA-Z0-9].*")) {
            System.out.println("Password must be at least 8 characters long and include one number and one special character");
            return;
        }

        String newHashedPassword = Hash2.hashWithPBKDF2(newPassword);
        UserDataManager.updateUserPassword(username, newHashedPassword);
        System.out.println("Password reset successful!");
    }

    private static boolean verifyMFA(String username, String encryptedPhone) throws Exception {
        String phoneNumber = encryption.decrypt(encryptedPhone, SECRET_KEY);
        System.out.println("\n=== Multi-Factor Authentication ===");
        System.out.println("Requesting SMS with verification code to " + maskPhoneNumber(phoneNumber));

        boolean setupOk = SMSService.requestSMSCode(phoneNumber);
        if (!setupOk) {
            System.out.println("Failed to request SMS code. Try again later.");
            return false;
        }

        System.out.print("Enter the code you received by SMS: ");
        String enteredCode = scanner.nextLine();

        boolean verified = SMSService.verifyCode(phoneNumber, enteredCode);
        return verified;
    }

    private static String maskPhoneNumber(String phone) {
        if (phone.length() >= 4) {
            return "*****" + phone.substring(phone.length() - 4);
        }
        return "*****";
    }
}

