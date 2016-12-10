package com.minehut.discordbot.events;

import com.minehut.discordbot.Core;
import com.minehut.discordbot.util.Chat;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.MessageUpdateEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by MatrixTunnel on 11/29/2016.
 */
public class ChatEvents {

    public static ArrayList<String> badWords = new ArrayList<>();

    @EventSubscriber
    public void handle(MessageReceivedEvent event) throws IOException, RateLimitException, DiscordException {
        IMessage message = event.getMessage();
        IUser sender = message.getAuthor();
        IChannel channel = message.getChannel();

        if (event.getMessage().getChannel() instanceof IPrivateChannel) {
            Chat.sendDiscordMessage("Lol... I don't do things", event.getMessage().getChannel());
            return;
        }

        if (sender.getName().equals(sender.getDisplayName(message.getGuild()))) {
            Core.log.info(Chat.getChannelName(channel) + sender.getDisplayName(message.getGuild()) + ": " +
                    Chat.fixDiscordMentions(event.getMessage()));
        } else {
            Core.log.info(Chat.getChannelName(channel) + sender.getDisplayName(message.getGuild()) + " (" +
                    sender.getName() + "): " + Chat.fixDiscordMentions(message));
        }

        /*
        for (String word : badWords) {
            if (event.getMessage().getContent().toLowerCase().contains(word)) {
                if (Objects.equals(event.getMessage().getChannel().getID(), "240296608338673664"))
                try {
                    event.getMessage().delete();
                    //Chat.sendDiscordMessage(event.getMessage().getAuthor().toString() + ", please do not use bad language on this discord server!", event.getMessage().getChannel());
                    return;
                } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
                    e.printStackTrace();
                }
            }
        }
        */

        // Anywhere Luuke is mentioned that is not general
        if (!channel.getID().equals("239599059415859200")) {
            if (event.getMessage().getMentions().toString().contains("144607902525554688")) {
                Chat.sendDiscordMessage(sender.mention() + ", please do not mention staff in this channel. If it's urgent, please use " +
                    event.getMessage().getGuild().getChannelByID("239599059415859200").mention() + " Thanks! ^-^", channel);
                return;
            }
        }

        // #feature-request and #bug-report
        if (channel.getID().equals("240917433731383298") || channel.getID().equals("240274864462626818")) {
            if (message.getRoleMentions().size() != 0) {

                Chat.sendDiscordMessage(sender.mention() + ", please do not mention staff in this channel as if gets checked regularly. If it's urgent, please use " +
                        event.getMessage().getGuild().getChannelByID("239599059415859200").mention() + " Thanks! ^-^", channel);
            }
        }
    }

    @EventSubscriber
    public void handle(MessageUpdateEvent event) {
        IMessage oldMessage = event.getOldMessage();
        IMessage newMessage = event.getNewMessage();
        IUser sender = oldMessage.getAuthor();
        IChannel channel = oldMessage.getChannel();

        if (!sender.equals(Core.getDiscord().getOurUser())) {
            if (sender.getName().equals(sender.getDisplayName(oldMessage.getGuild()))) {
                Core.log.info(Chat.getChannelName(channel) + sender.getDisplayName(oldMessage.getGuild()) + " updated message \"" +
                        Chat.fixDiscordMentions(oldMessage) + "\" -> \"" + Chat.fixDiscordMentions(newMessage) + "\"");
            } else {
                Core.log.info(Chat.getChannelName(channel) + sender.getDisplayName(oldMessage.getGuild()) + " (" +
                        sender.getName() + ") updated message \"" +
                        Chat.fixDiscordMentions(oldMessage) + "\" -> \"" + Chat.fixDiscordMentions(newMessage) + "\"");
            }
        }
    }

    @EventSubscriber
    public void handle(MessageDeleteEvent event) {
        IMessage message = event.getMessage();
        IUser sender = message.getAuthor();
        IChannel channel = message.getChannel();

        if (!sender.equals(Core.getDiscord().getOurUser())) {
            if (message == null) return;
            if (message.getContent().startsWith("!")) return;
            if (!Chat.logRemove) return;

            if (sender.getName().equals(sender.getDisplayName(message.getGuild()))) {
                Core.log.info(Chat.getChannelName(channel) + sender.getDisplayName(message.getGuild()) + " removed message \"" + Chat.fixDiscordMentions(message) + "\"");
            } else {
                Core.log.info(Chat.getChannelName(channel) + sender.getDisplayName(message.getGuild()) + " (" +
                        sender.getName() + ") removed message \"" + Chat.fixDiscordMentions(message) + "\"");
            }
        }
    }

}