package com.resumeanalyser;

import com.resumeanalyser.api.ApiClient;
import com.resumeanalyser.ui.AppContext;
import com.resumeanalyser.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point of the Swing desktop client. Backend URL: -Dapi.url=... or env API_URL (default http://localhost:8080). */
public class ResumeAnalyserApp {

    public static void main(String[] args) {
        String url = System.getProperty("api.url", System.getenv("API_URL") != null ? System.getenv("API_URL") : "http://localhost:8080");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to the default look and feel
            }
            AppContext ctx = new AppContext(new ApiClient(url));
            MainFrame frame = new MainFrame(ctx);
            frame.setVisible(true);
        });
    }
}
