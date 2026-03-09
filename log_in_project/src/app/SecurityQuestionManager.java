package app;

import java.io.*;

public class SecurityQuestionManager {
	private static final String SECURITY_FILE = "security_questions.csv";
    
	public static final String[] SECURITY_QUESTIONS = {
		"What is the name of your first pet?",
		"What city were you born in?",
		"What is your mother's maiden name?",
		"What is the name of your best friend?",
		"What was the name of your first school?",
		"What is your favorite book?",
		"What was your first car model?"
	};
    
	public static void saveSecurityQuestion(String username, int questionIndex, String encryptedAnswer) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(SECURITY_FILE, true))) {
			bw.write(username + "," + questionIndex + "," + encryptedAnswer);
			bw.newLine();
		}
	}
    
	public static class SecurityQuestion {
		public int questionIndex;
		public String question;
		public String encryptedAnswer;
        
		public SecurityQuestion(int questionIndex, String encryptedAnswer) {
			this.questionIndex = questionIndex;
			this.question = SECURITY_QUESTIONS[questionIndex];
			this.encryptedAnswer = encryptedAnswer;
		}
	}
    
	public static SecurityQuestion getSecurityQuestion(String username) throws IOException {
		File file = new File(SECURITY_FILE);
		if (!file.exists()) return null;
        
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",", 3);
				if (parts.length >= 3 && parts[0].equals(username)) {
					int questionIndex = Integer.parseInt(parts[1]);
					String encryptedAnswer = parts[2];
					return new SecurityQuestion(questionIndex, encryptedAnswer);
				}
			}
		}
		return null;
	}
    
	public static boolean verifySecurityAnswer(String username, String answer, String secretKey) throws IOException {
		SecurityQuestion sq = getSecurityQuestion(username);
		if (sq == null) return false;

		try {
			Encryption encryption = new Encryption();
			String decryptedAnswer = encryption.decrypt(sq.encryptedAnswer, secretKey);
			return decryptedAnswer.equalsIgnoreCase(answer);
		} catch (Exception e) {
			// Decryption error -> treat as verification failure
			System.out.println("Security answer decryption failed: " + e.getMessage());
			return false;
		}
	}
}

