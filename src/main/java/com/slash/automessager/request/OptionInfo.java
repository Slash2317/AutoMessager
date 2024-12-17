package com.slash.automessager.request;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public record OptionInfo(String name, String description, OptionType optionType) {

}
