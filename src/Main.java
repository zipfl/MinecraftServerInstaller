import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {
        try {
            String path = "servers/";
            File serverDir = new File(path);

            if (!serverDir.exists())
                if (serverDir.mkdir())
                    System.out.println("Server directory created at: " + path);

            JSONObject json = readJsonFromUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json");
            String latest = json.getJSONObject("latest").getString("release");
            String version;
            if (args.length != 0) {
                version = args[0];
            } else {
                version = latest;
            }

            String stringDate = SDF.format(new Date());
            String pathServer = path + version + "_" + stringDate;
            if (!new File(pathServer).mkdir()) {
                System.out.println("Could not create directory: " + pathServer);
                System.exit(1);
            } else {
                System.out.println("Created server at: " + pathServer);
            }

            String url = null;
            JSONArray versionsJson = json.getJSONArray("versions");
            for (int i = 0; i < versionsJson.length(); ++i) {
                JSONObject v = versionsJson.getJSONObject(i);
                String id = v.getString("id");
                if (id.equals(version)) {
                    url = readJsonFromUrl(v.getString("url")).getJSONObject("downloads").getJSONObject("server").getString("url");
                }
            }

            System.out.println("Downloading minecraft_server." + version + ".jar");
            System.out.println(url);

            assert url != null;
            BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
            FileOutputStream fos = new FileOutputStream(pathServer + "/minecraft_server." + version + ".jar");
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fos.write(dataBuffer, 0, bytesRead);
            }
            fos.close();

            writeFile(pathServer + "/eula.txt", "eula=true");
            writeFile(pathServer + "/run.sh", "screen java -jar minecraft_server." + version + ".jar nogui");
        } catch (JSONException | IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void writeFile(String path, String text) {
        try {
            File f = new File(path);
            if (f.createNewFile()) {
                FileWriter myWriter = new FileWriter(path);
                myWriter.write(text);
                myWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }

            String jsonText = sb.toString();
            return new JSONObject(jsonText);
        }
    }
}
