
package app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SMSService {
    // Professor-provided MFA endpoints
    private static final String BASE = "https://wa-ocu-mfa-fre6d6guhve2afcw.centralus-01.azurewebsites.net/mfa";

    // Call the setup endpoint which triggers an SMS to be sent to the phone
    public static boolean requestSMSCode(String phone) {
        try {
            String encoded = URLEncoder.encode(phone, "UTF-8");
            String urlStr = BASE + "/setup/sms/" + encoded;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int code = conn.getResponseCode();
            conn.disconnect();
            return code >= 200 && code < 300;
        } catch (Exception e) {
            System.out.println("SMS setup request failed: " + e.getMessage());
            return false;
        }
    }

    // Verify the SMS code by POSTing JSON { "id": "[PHONE]", "code": "..." }
    public static boolean verifyCode(String phone, String codeValue) {
        try {
            String urlStr = BASE + "/verify/sms";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String json = String.format("{\"id\":\"%s\",\"code\":\"%s\"}", phone, codeValue);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            BufferedReader br;
            if (status >= 200 && status < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            }

            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                resp.append(line.trim());
            }
            br.close();
            conn.disconnect();

            String body = resp.toString();
            // The service returns TRUE or FALSE (per instructions); accept many variants
            if (body.equalsIgnoreCase("true") || body.equalsIgnoreCase("t") || body.equalsIgnoreCase("1") || body.contains("true") ) {
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("SMS verify request failed: " + e.getMessage());
            return false;
        }
    }
}