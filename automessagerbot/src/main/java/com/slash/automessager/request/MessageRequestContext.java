package com.slash.automessager.request;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.*;
import java.util.regex.Matcher;

public class MessageRequestContext implements RequestContext {

    private MessageReceivedEvent event;
    private Command command;
    private Map<String, String> nameToArgument = new HashMap<>();
    private String prefix;

    public MessageRequestContext(MessageReceivedEvent event, String prefix) {
        this.event = event;
        this.prefix = prefix;

        Command command = Command.getCommandByMessage(event.getMessage().getContentRaw(), prefix);
        if (command != null) {
            this.command = command;

            if (event.getMessage().getContentRaw().length() > command.getCommandName().length() + 1 + prefix.length()) {
                String arguments = event.getMessage().getContentRaw().substring(command.getCommandName().length() + 1 + prefix.length());

                List<String> args = getArguments(arguments, " ", command.getParameters().size());
                if (args.size() == command.getParameters().size()) {
                    for (int i = 0; i < command.getParameters().size(); i++) {
                        OptionInfo optionInfo = command.getParameters().get(i);
                        if (optionInfo.optionType() == OptionType.CHANNEL) {
                            this.nameToArgument.put(optionInfo.name(), extractChannelId(args.get(i)));
                        }
                        else {
                            this.nameToArgument.put(optionInfo.name(), args.get(i));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Command getCommand() {
        return command;
    }

    @Override
    public <T> T getArgument(String name, Class<T> clazz) {
        String argument = nameToArgument.get(name);
        if (clazz == Integer.class) {
            return (T) Integer.valueOf(argument);
        }
        else if (clazz == Boolean.class) {
            if (isBlank(argument)) {
                return null;
            }
            if (argument.toLowerCase().equals("true")) {
                return (T) Boolean.TRUE;
            }
            if (argument.toLowerCase().equals("false")) {
                return (T) Boolean.FALSE;
            }
            return null;
        }
        else if (clazz == String.class) {
            return (T) argument;
        }
        return null;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Member getMember() {
        return event.getMember();
    }

    @Override
    public Guild getGuild() {
        return event.getGuild();
    }

    @Override
    public MessageChannelUnion getChannel() {
        return event.getChannel();
    }

    @Override
    public void sendMessage(String message) {
        event.getChannel().sendMessage(message).setAllowedMentions(Collections.emptyList()).queue();
    }

    @Override
    public void sendMessageEmbeds(MessageEmbed embed) {
        event.getChannel().sendMessageEmbeds(embed).setAllowedMentions(Collections.emptyList()).queue();
    }

    @Override
    public void sendMessageEmbeds(Collection<MessageEmbed> embeds) {
        event.getChannel().sendMessageEmbeds(embeds).setAllowedMentions(Collections.emptyList()).queue();
    }

    private List<String> getArguments(String argumentsString, String delimiter, int numOfArguments) {
        if (argumentsString == null || argumentsString.isEmpty()) {
            return Collections.emptyList();
        }

        if (numOfArguments == 1) {
            return List.of(argumentsString);
        }
        List<String> splitArguments = List.of(argumentsString.split(delimiter));

        if (splitArguments.size() <= numOfArguments) {
            return splitArguments;
        }

        List<String> arguments = new ArrayList<>(splitArguments.subList(0, numOfArguments - 1));
        arguments.add(String.join(delimiter, splitArguments.subList(numOfArguments - 1, splitArguments.size())));
        return arguments;
    }

    private String extractChannelId(String mention) {
        Matcher matcher = Message.MentionType.CHANNEL.getPattern().matcher(mention);
        if (matcher.find()) {
            String channelMention = matcher.group();
            return channelMention.substring(2, channelMention.length() - 1);
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
