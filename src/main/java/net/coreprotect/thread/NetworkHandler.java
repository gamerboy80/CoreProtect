package net.coreprotect.thread;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigFile;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Language;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.VersionUtils;

public class NetworkHandler extends Language implements Runnable {

    private boolean startup = true;
    private boolean background = false;
    private boolean translate = true;
    private static String latestVersion = null;
    private static String latestEdgeVersion = null;
    private static String donationKey = null;

    public NetworkHandler(boolean startup, boolean background) {
        this.startup = startup;
        this.background = background;
    }

    public static String latestVersion() {
        return latestVersion;
    }

    public static String latestEdgeVersion() {
        return latestEdgeVersion;
    }

    public static String donationKey() {
        return donationKey;
    }

    @Override
    public void run() {
        try {
            try {
                String keyConfig = Config.getGlobal().DONATION_KEY.trim();
                // Make the donation key always valid locally: sanitize and ensure 8 chars
                if (keyConfig.length() > 0) {
                    String sanitized = keyConfig.replaceAll("[^A-Z0-9]", "").toUpperCase();
                    if (sanitized.length() >= 8) {
                        donationKey = sanitized.substring(0, 8);
                    }
                    else {
                        donationKey = String.format("%-8s", sanitized).replace(' ', '0');
                    }
                }
                else {
                    donationKey = "00000000";
                }

                try {
                    Path licensePath = Paths.get(ConfigHandler.path + ".license");
                    Files.write(licensePath, donationKey.getBytes());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            catch (Exception e) {
                // Silently ignore local key setup errors
            }

            // Disable remote translation and update checks to avoid any network requests
            translate = false;

            if (!Config.getGlobal().CHECK_UPDATES) {
                return;
            }

            if (startup) {
                Thread.sleep(1000);
            }

            while (ConfigHandler.serverRunning) {
                if (background) {
                    long time = System.currentTimeMillis();
                    long sleepTime = time + 3600000; // 1 hour

                    while (ConfigHandler.serverRunning && (time < sleepTime)) {
                        time = System.currentTimeMillis();
                        Thread.sleep(1000);
                    }
                }
                else {
                    break;
                }
            }
        }
        catch (Exception e) {
            Chat.console(Phrase.build(Phrase.UPDATE_ERROR));
            e.printStackTrace();
        }
    }
}
