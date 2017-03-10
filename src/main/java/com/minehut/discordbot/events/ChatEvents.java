package com.minehut.discordbot.events;

import com.minehut.discordbot.Core;
import com.minehut.discordbot.commands.Command;
import com.minehut.discordbot.commands.CommandType;
import com.minehut.discordbot.util.Bot;
import com.minehut.discordbot.util.Chat;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by MatrixTunnel on 11/29/2016.
 * Some code provided by the FlareBot developers
 */
public class ChatEvents extends ListenerAdapter {

    HashMap<String, String> messages = new HashMap<>();
    HashMap<String, Integer> amount = new HashMap<>();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        User sender = message.getAuthor();
        TextChannel channel = event.getChannel();
        JDA jda = event.getJDA();

        if (guild == null || !Core.getDiscord().isReady() || sender.equals(Core.getClient().getSelfUser()) || sender.isBot() || sender.isFake()) {
            return;
        }

        if (guild == Bot.getMainGuild()) {
            if (guild.getMember(sender).getNickname() == null) {
                Core.log.info(Chat.getChannelName(channel) + Chat.getFullName(sender) + ": " + message.getContent());
            } else {
                Core.log.info(Chat.getChannelName(channel) + Chat.getFullName(sender) + " (" + guild.getMember(sender).getNickname() + "): " + message.getContent());
            }

            if (!Bot.isTrusted(sender) && !message.getContent().startsWith(Command.getPrefix())) {
                if (message.getRawContent().equalsIgnoreCase(messages.get(sender.getId()))) {
                    amount.put(sender.getId(), (amount.get(sender.getId()) + 1));
                } else {
                    messages.put(sender.getId(), message.getRawContent());
                    amount.put(sender.getId(), 0);
                }

                if (amount.get(sender.getId()) != null && ((amount.get(sender.getId()) + 1) == 3 || (amount.get(sender.getId()) + 1) == 4)) {
                    Chat.removeMessage(event.getMessage());
                    Chat.sendMessage(sender.getAsMention() + ", please do not repeat the same message!", channel);
                    return;
                } else if ((amount.get(sender.getId()) + 1) >= 5) {
                    Chat.removeMessage(event.getMessage());

                    guild.getController().addRolesToMember(member, Core.getDiscord().getRoleByID(Core.getConfig().getMutedRoleID())).queue();
                    Chat.sendMessage(sender.getAsMention() + " has been auto muted for spam", channel);

                    Chat.sendMessage(Chat.getEmbed().setDescription(":no_bell:  *" + member.getAsMention() + " was auto muted for spam!*")
                            .setFooter("System time | " + new Date().toString(), null)
                            .setColor(Chat.CUSTOM_PURPLE), Bot.getLogChannel());
                    return;
                }
            }

            if (message.getRawContent().toLowerCase().contains("discord.gg") && !Bot.isTrusted(sender)) {
                Chat.removeMessage(message);

                Chat.sendMessage(sender.getAsMention() + ", please do not advertise Discord servers. Thanks!", channel);
                return;
            }


            if (!channel.getName().contains("meme") && message.getRawContent().contains(
                    "▔╲▂▂▂▂╱▔╲▂")) {

                Chat.removeMessage(message);
                Chat.sendMessage(sender.getAsMention() + ", please do not post cooldog in this channel.", channel, 60);
                return;
            } else if (message.getRawContent().contains("He is going around sending friend requests to random Discord users, " +
                    "and those who accept his friend requests will have their accounts DDoSed and their IP Addresses revealed to him. " +
                    "Spread the word and send this to as many discord servers as you can. " +
                    "If you see this user, DO NOT accept his friend request and immediately block him")) {
                Chat.removeMessage(message);
                Chat.sendMessage(sender.getAsMention() + "\n\nDiscord doesn't relay ips through how they do things. If users could somehow get your ip like this, " +
                        "Discord would have banned these users already. Please don't copy paste that message everywhere since it is not real.", channel, 120);
                return;
            }

            //if (channel.getId().equals("284833888482754561") && message.getMentionedUsers().contains(jda.getSelfUser()) && !message.getRawContent().substring(14).equals("")) {
            //    Core.log.info("test");
//
            //    try {
            //        MonkeyLearn ml = new MonkeyLearn("YOUR API KEY HERE");
//
            //        // Classify some texts
            //        String[] textList = {message.getRawContent().substring(14)};
            //        String moduleId = "cl_hS9wMk9y";
            //        MonkeyLearnResponse res = ml.classifiers.classify(moduleId, textList, true);
//
            //        Chat.sendMessage(res.arrayResult.toString(), channel);
            //    } catch (MonkeyLearnException e) {
            //        e.printStackTrace();
            //    }
            //    return;
            //}
        }

        for (String id : Core.getConfig().getBlockedUsers()) {
            if (sender.getId().equals(id) && !Bot.isTrusted(sender)) {
                return;
            }
        }

        if (message.getRawContent() != null && message.getContent().startsWith(Command.getPrefix()) && !sender.isBot()) {

            String msg = event.getMessage().getRawContent();
            String command = msg.substring(1);
            String[] args = new String[0];
            if (msg.contains(" ")) {
                command = command.substring(0, msg.indexOf(" ") - 1);
                args = msg.substring(msg.indexOf(" ") + 1).split(" ");
            }

            for (Command cmd : Core.getCommands()) {
                if (cmd.getCommand().equalsIgnoreCase(command)) {
                    if (cmd.getType() == CommandType.ADMINISTRATIVE && !Bot.isTrusted(sender)) {
                        return;
                    }

                    if (guild.equals(Bot.getMainGuild()) && cmd.getType() == CommandType.MUSIC &&
                            !Arrays.asList(Core.getConfig().getMusicCommandChannels()).contains(channel.getId())) {
                        return;
                    }

                    try {
                        cmd.onCommand(jda, guild, channel, member, sender, message, args);
                    } catch (Exception ex) {
                        Core.log.error("Exception in guild " + "!\n" + '\'' + cmd.getCommand() + "' "
                                + Arrays.toString(args) + " in " + channel + "! Sender: " +
                                sender.getName() + '#' + sender.getDiscriminator(), ex);
                        ex.printStackTrace();
                    }
                    return;
                } else {
                    for (String alias : cmd.getAliases()) {
                        if (alias.equalsIgnoreCase(command)) {
                            if (cmd.getType() == CommandType.ADMINISTRATIVE && !Bot.isTrusted(sender)) {
                                return;
                            }

                            if (guild.equals(Bot.getMainGuild()) && cmd.getType() == CommandType.MUSIC &&
                                    !Arrays.asList(Core.getConfig().getMusicCommandChannels()).contains(channel.getId())) {
                                return;
                            }

                            try {
                                cmd.onCommand(jda, guild, channel, member, sender, message, args);
                            } catch (Exception ex) {
                                Core.log.error("Exception in guild " + "!\n" + '\'' + cmd.getCommand() + "' "
                                        + Arrays.toString(args) + " in " + channel + "! Sender: " +
                                        sender.getName() + '#' + sender.getDiscriminator(), ex);
                                ex.printStackTrace();
                            }
                            return;
                        } else {
                            //not a valid command
                            Chat.setAutoDelete(message, 120);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (event.getGuild() != Bot.getMainGuild()) return;
        Message message = event.getMessage();
        User sender = message.getAuthor();
        Channel channel = event.getChannel();

        /*
        if (!sender.equals(Core.getDiscord().getOurUser())) {
            if (newMessage.getContent() == null || newMessage.getContent().equals("")) {
                return;
            }

            if (newMessage.getContent().startsWith(Command.getPrefix())) return;
            if (!Chat.logRemove) return;

            if (sender.getName().equals(sender.getDisplayName(oldMessage.getGuild()))) {
                Core.log.info(Chat.getChannelName(channel) + sender.getDisplayName(oldMessage.getGuild()) + " updated message \"" +
                        Chat.fixDiscordMentions(oldMessage) + "\" -> \"" + Chat.fixDiscordMentions(newMessage) + "\"");
            } else {
                Core.log.info(Chat.getChannelName(channel) + sender.getDisplayName(oldMessage.getGuild()) + " (" +
                        sender.getName() + ") updated message \"" +
                        Chat.fixDiscordMentions(oldMessage) + "\" -> \"" + Chat.fixDiscordMentions(newMessage) + "\"");
            }
        }
        */
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        if (event.getMessage() == null || event.getMessage().getGuild() != Bot.getMainGuild()) return;
        //Message message = event.getMessage();
        //User sender = message.getAuthor();
        //Channel channel = event.getChannel();

        /*
        if (!sender.equals(Core.getDiscord().getOurUser())) {
            if (message.getContent() == null || message.getContent().equals("")) {
                return;
            }

            if (message.getContent().startsWith(Command.getPrefix())) return;
            if (!Chat.logRemove) return;

            if (sender.getName().equals(sender.getDisplayName(message.getGuild()))) {
                Core.log.info(Chat.getChannelName(channel) + sender.getDisplayName(message.getGuild()) + " removed message \"" + Chat.fixDiscordMentions(message) + "\"");
            } else {
                Core.log.info(Chat.getChannelName(channel) + sender.getDisplayName(message.getGuild()) + " (" +
                        sender.getName() + ") removed message \"" + Chat.fixDiscordMentions(message) + "\"");
            }
        }
        */
    }

}
