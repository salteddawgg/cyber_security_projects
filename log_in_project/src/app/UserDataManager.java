package app;

import java.io.*;

public class UserDataManager {
    private static final String DATA_FILE = "Data.csv";

    public static void saveUser(User user) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE, true))) {
            bw.write(user.toCSV());
            bw.newLine();
        }
    }

    public static User findUser(String username) throws IOException {
        File file = new File(DATA_FILE);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                User user = User.fromCSV(line);
                if (user != null && user.getUsername().equals(username)) {
                    return user;
                }
            }
        }
        return null;
    }

    public static void updateUserPassword(String username, String newHashedPassword) throws IOException {
        File file = new File(DATA_FILE);
        File tempFile = new File(DATA_FILE + ".tmp");

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                User user = User.fromCSV(line);
                if (user != null && user.getUsername().equals(username)) {
                    user.setHashedPassword(newHashedPassword);
                    bw.write(user.toCSV());
                } else {
                    bw.write(line);
                }
                bw.newLine();
            }
        }
        file.delete();
        tempFile.renameTo(file);
    }
}
