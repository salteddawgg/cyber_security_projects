package app;

public class User {
    private String username;
    private String hashedPassword;
    private String encryptedName;
    private String encryptedEmail;
    private String encryptedPhone;

    public User(String username, String hashedPassword, String encryptedName,
                String encryptedEmail, String encryptedPhone) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.encryptedName = encryptedName;
        this.encryptedEmail = encryptedEmail;
        this.encryptedPhone = encryptedPhone;
    }

    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public String getEncryptedName() { return encryptedName; }
    public String getEncryptedEmail() { return encryptedEmail; }
    public String getEncryptedPhone() { return encryptedPhone; }

    public String toCSV() {
        return String.join(",", username, hashedPassword, encryptedName, encryptedEmail, encryptedPhone);
    }

    public static User fromCSV(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 5) return null;
        return new User(parts[0], parts[1], parts[2], parts[3], parts[4]);
    }
}
