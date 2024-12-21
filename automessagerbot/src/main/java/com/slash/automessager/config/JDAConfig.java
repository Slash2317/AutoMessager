package com.slash.automessager.config;

import com.slash.automessager.AutoMessagerBotListener;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class JDAConfig {

    @Autowired
    private JDA jda;

    @Autowired
    private AutoMessagerBotListener listener;

    @EventListener(ApplicationReadyEvent.class)
    private void addListener() {
        jda.addEventListener(listener);
    }
}
