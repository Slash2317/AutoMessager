package com.slash.automessager;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.util.Properties;

public class Application {

    public static void main(String[] args) {
        String env = System.getProperty("app.profiles.active");
        if (env == null) {
            env = "dev";
        }

        Properties props = new Properties();
        try {
            props.load(Application.class.getClassLoader().getResourceAsStream("config-" + env + ".properties"));

            JDABuilder.createDefault(props.getProperty("token"))
                    .setActivity(Activity.playing("Join Tiziland!! - t!help"))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new TizilandBotListener())
                    .build();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
