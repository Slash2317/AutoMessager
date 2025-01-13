package com.slash.automessager;

import com.slash.automessager.service.AutoMessageBotService;
import com.slash.automessager.service.AutoMessageBotServiceImpl;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public class Application {

    private static DataSource dataSource;
    private static JDA jda;
    private static BotCache botCache;

    public static void main(String[] args) throws IOException, InterruptedException {
        String env = System.getenv("app.profiles.active");
        String propertiesFilename;
        if (env == null) {
            propertiesFilename = "application.properties";
        }
        else {
            propertiesFilename = "application-" + env + ".properties";
        }

        Properties props = new Properties();
        try {
            props.load(Application.class.getClassLoader().getResourceAsStream(propertiesFilename));
        }
        catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        System.setProperty("embed.color", props.getProperty("embed.color", "#5566f2"));

        dataSource = createDataSource(props);
        try {
            jda = createJda(props);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }

        AutoMessageBotService botService = new AutoMessageBotServiceImpl();
        botCache = botService.createBotCache();

        AutoMessageScheduler scheduler = new AutoMessageScheduler();
        scheduler.start();
        System.out.println("Bot started successfully!");
    }

    private static DataSource createDataSource(Properties props) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUser(props.getProperty("database.username"));
        dataSource.setPassword(props.getProperty("database.password"));
        dataSource.setDatabaseName(props.getProperty("database.name"));
        dataSource.setServerNames(new String[]{ props.getProperty("database.server") });
        return dataSource;
    }

    private static JDA createJda(Properties props) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(props.getProperty("bot.token"))
                .addEventListeners(new AutoMessagerBotListener())
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
        jda.awaitReady();
        return jda;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static JDA getJda() {
        return jda;
    }

    public static BotCache getBotCache() {
        return botCache;
    }
}
