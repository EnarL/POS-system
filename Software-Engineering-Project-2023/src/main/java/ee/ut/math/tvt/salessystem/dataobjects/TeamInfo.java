package ee.ut.math.tvt.salessystem.dataobjects;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;

public class TeamInfo {
    public static String[] teamInfo() {
        Properties properties = new Properties();
        String[] teamdata = new String[4];
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            InputStream inputStream = loader.getResourceAsStream("application.properties");
            properties.load(inputStream);
            assert inputStream != null;
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        teamdata[0] = properties.getProperty("team.name");
        teamdata[1] = properties.getProperty("team.contact");
        teamdata[2] = properties.getProperty("team.members");
        teamdata[3] = properties.getProperty("team.logo");
        return teamdata;
    }


}
