package com.minehut.discordbot.commands.general;

import com.minehut.discordbot.Core;
import com.minehut.discordbot.commands.Command;
import com.minehut.discordbot.commands.CommandType;
import com.minehut.discordbot.util.Chat;
import com.minehut.discordbot.util.URLJson;
import com.minehut.discordbot.util.music.VideoThread;
import com.sun.management.OperatingSystemMXBean;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by MatrixTunnel on 1/29/2017.
 * Matcher code/regex made by my friend MeowingTwurtle.
 * Info help from Draem.
 */
public class InfoCommand implements Command {

    private Runtime runtime = Runtime.getRuntime();
    private String minehutLogo = "https://cdn.discordapp.com/attachments/239599059415859200/249694020593254400/" +
            "eJwNyEsSgyAMANC7cACCfAp4mxQpOqOGIbGbTu-ub_l-6hq7mtUq0nkGWDYuNBbNQgNb1Y2o7RX7xrrQASiCZT3qKQzWJxtTitmlHH2Ygn0qv7IL2U8mGRdMtICfL75R97Op_w3lzyJF.png";

    private static String getOwnerName(String UUID) {
        try {
            JSONArray json = URLJson.readJsonArrayFromUrl("https://api.mojang.com/user/profiles/" + UUID.replaceAll("-", "") + "/names");
            return json.getJSONObject(json.length() - 1).getString("name");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "null";
    }

    @Override
    public String getCommand() {
        return "info";
    }

    private String getMb(long bytes) {
        return (bytes / 1024 / 1024) + " MB";
    }

    @Override
    public void onCommand(JDA jda, Guild guild, TextChannel channel, Member member, User sender, Message message, String[] args) {
        Chat.setAutoDelete(message, 5);

        EmbedBuilder embed = Chat.getEmbed();

        if (args.length == 0) {
            Chat.sendMessage(sender.getAsMention() + " Usage: ```" + Command.getPrefix() + getCommand() + getArgs() + "```", channel, 15);
        } else if (args.length >= 1) {
            Message mainMessage = Chat.sendMessage(embed.addField("Gathering Information...", "This may take a few moments", true)
                    .setColor(Chat.CUSTOM_ORANGE), channel, 120);

            switch (args[0]) {
                case "network":
                    String motd = "";

                    try {
                        JSONObject status = URLJson.readJsonObjectFromUrl("https://minehut.com/api/status/");

                        embed.clearFields()
                                .setAuthor("Minehut Network Status", "https://minehut.com", minehutLogo)

                                .addField("Users Online:", status.getJSONObject("ping").getJSONObject("players").get("online") + "/" +
                                        status.getJSONObject("ping").getJSONObject("players").get("max"), true)
                                .addField("Servers Online:", status.getInt("totalPlayerServerCount") + "/" + status.getInt("totalPlayerMaxServerCount"), true)
                                .addField("Ram Usage:", (status.getInt("totalPlayerServerRamUsage") / 1024) + "/" + status.getInt("totalPlayerServerMaxRam") + " GB", true)
                                .addField("Players on Player Servers:", String.valueOf(status.getInt("totalPlayerServerPlayerCount")), true);

                        Matcher matcher = Pattern.compile("(?<=\"text\":\").*?(?=\")").matcher(String.valueOf(status.getJSONObject("ping").getJSONObject("description")));

                        while (matcher.find()) {
                            motd += matcher.group();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        embed.clearFields().setAuthor("Minehut Network Status", "https://minehut.com", minehutLogo);

                        Chat.editMessage("", embed.setDescription("\n**Either something went wrong or the network is down at this time. Please try again later**\n")
                                .setColor(Chat.CUSTOM_RED), mainMessage, 15);
                        return;
                    }

                    Chat.editMessage("", embed.setDescription("`" + motd + "`")
                            .setFooter("System time | " + new Date().toString(), null)
                            .setColor(Chat.CUSTOM_GREEN), mainMessage, 30);
                    break;
                case "server":
                    if (args.length == 1) {
                        Chat.editMessage(sender.getAsMention(), embed.clearFields().setDescription("Usage: `" + Command.getPrefix() + getCommand() + " <server> <name>`")
                                .setColor(Chat.CUSTOM_BLUE), mainMessage, 15);
                        return;
                    }

                    try {
                        JSONArray servers = URLJson.readJsonArrayFromUrl("https://minehut.com/api/servers/");

                        for (Object server : servers) {
                            JSONObject obj = (JSONObject) server;

                            if (obj.get("name").equals(args[1])) {
                                embed.clearFields()
                                        .setAuthor(obj.get("name") + " - Server Info", "https://minehut.com/s/" + obj.get("name"), minehutLogo)
                                        .setDescription("`" + String.valueOf(obj.get("motd")) + "`")
                                        .addField("Owner:", "[`" + getOwnerName(obj.getString("owner")) + "`](https://minehut.com/" + getOwnerName(obj.getString("owner")) + ")", true)
                                        .addField("Players Online:", obj.getInt("player_count") + "/" + obj.getInt("max_players"), true)
                                        .addField("Total Joins:", String.valueOf(obj.getInt("total_joins")), true)
                                        .addField("Unique Joins:", String.valueOf(obj.getJSONArray("user_joins").length()), true)
                                        .addField("Total Server Starts:", String.valueOf(obj.getInt("starts")), true)
                                        .addField("Server Host:", String.valueOf(obj.get("host")), true);

                                Chat.editMessage("", embed.setFooter("System time | " + new Date().toString(), null)
                                        .setColor(Chat.CUSTOM_GREEN), mainMessage, 30);
                                return;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        embed.clearFields().setAuthor("Minehut Network Status", "https://minehut.com", minehutLogo);

                        Chat.editMessage("", embed.setDescription("\n**Either something went wrong or the network is down at this time. Please try again later**\n")
                                .setColor(Chat.CUSTOM_RED), mainMessage, 15);
                        return;
                    }


                    Chat.editMessage("", embed.clearFields().addField("Whoops! :banana: :monkey:", "The server `" + args[1] + "` is offline or not found. Please try again with a different name!", true).setColor(Chat.CUSTOM_RED), mainMessage, 15);
                    break;
                case "user":
                    if (args.length == 1) {
                        Chat.editMessage(sender.getAsMention(), embed.clearFields().setDescription("Usage: `" + Command.getPrefix() + getCommand() + " <user> <name>`")
                                .setColor(Chat.CUSTOM_BLUE), mainMessage, 15);
                        return;
                    } else if (args.length >= 2) {
                        JSONObject user;

                        try {
                            user = URLJson.readJsonObjectFromUrl("http://mctoolbox.me/minehut/friends/?user=" + args[1]);

                            embed.clearFields()
                                    .setAuthor(args[1] + " - User Info", "https://minehut.com/" + args[1], minehutLogo)

                                    .addField("Profile:", "[`" + args[1] + "`](https://minehut.com/" + args[1] + ")", true)
                                    .addField("First Joined:", getJoinDate(user), true)
                                    .addField("Friend Count:", getUserFriendCount(user), true)
                                    .addField("Total Online Time:", getUserOnlineTime(user).replace(" of online time.", ""), true)
                                    .setThumbnail("https://mc-heads.net/avatar/" + args[1] + "/100.png");

                            if (!isOnline(user)) {
                                embed.addField("Last Online:", getLastOnline(user), true);
                            }
                        } catch (JSONException e) {
                            Chat.editMessage("", embed.clearFields().addField("Whoops! :banana: :monkey:", "The user `" + args[1] + "` was not found. Please try again with a different name!", true)
                                    .setColor(Chat.CUSTOM_RED), mainMessage, 15);
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            embed.setAuthor("Minehut Network Status", "https://minehut.com", minehutLogo);

                            Chat.editMessage("", embed.clearFields().setDescription("\n**Either something went wrong or the network is down at this time. Please try again later**\n")
                                    .setColor(Chat.CUSTOM_RED), mainMessage, 15);
                            return;
                        }

                        String rank = null;
                        try {
                            rank = getUserRank(user);
                        } catch (JSONException ignored) {
                        }

                        if (rank == null) {
                            embed.addField("Rank:", "Default", true);
                        } else {
                            embed.addField("Rank:", getUserRank(user), true);
                        }

                        Chat.editMessage("", embed.setFooter("System time | " + new Date().toString(), null)
                                .setColor(isOnlineColor(user)), mainMessage, 30);
                        return;
                    }


                    break;
                case "hosts":
                    if (!sender.getId().equals("118088732753526784")) {
                        Chat.editMessage("", embed.clearFields().setDescription("Usage: `" + Command.getPrefix() + getCommand() + getArgs() + "`")
                                .setColor(Chat.CUSTOM_BLUE), mainMessage, 15);
                        return;
                    }

                    int playersOnline = 0;
                    int usedRam = 0;
                    int totalRam = 0;

                    try {
                        JSONArray hosts = URLJson.readJsonArrayFromUrl("http://mctoolbox.me/minehut/hosts/?token=" + Core.getConfig().getSecretKey());

                        //Chat.editMessage("", embed.clearFields(), mainMessage);
                        embed.clearFields();

                        for (Object host : hosts) {
                            JSONObject obj = (JSONObject) host;

                            embed.addField(obj.get("ip").toString(),
                                    "**Servers:** `" + obj.getInt("server_count") + "/" + obj.getInt("max_servers") + "`" +
                                            "\n**Ram Usage:** `" + (obj.getInt("ram_usage") / 1024) + "/" + obj.getInt("max_ram") + "` GB" +
                                            "\n**Players Online:** `" + obj.getInt("player_count") + "/" + obj.getInt("max_players") + "`", true);
                            playersOnline = playersOnline + obj.getInt("player_count");
                            usedRam = usedRam + obj.getInt("ram_usage");
                            totalRam = totalRam + obj.getInt("max_ram");
                        }

                        embed.setDescription("**Total Hosts:** `" + hosts.length() + "`" +
                                "\n**Total Ram Usage:** `" + (usedRam / 1024) + "/" + totalRam + "` GB" +
                                "\n**Players On Servers:** `" + playersOnline + "`");
                    } catch (IOException e) {
                        e.printStackTrace();
                        embed.setAuthor("Minehut Network Status", "https://minehut.com", minehutLogo);

                        Chat.editMessage("", embed.clearFields().setDescription("\n**Either something went wrong or the network is down at this time. Please try again later**\n")
                                .setColor(Chat.CUSTOM_RED), mainMessage, 15);
                        return;
                    }

                    embed.setAuthor("Minehut Network Status", "https://minehut.com", minehutLogo);
                    Chat.editMessage("", embed.setFooter("System time | " + new Date().toString(), null)
                            .setColor(Chat.CUSTOM_GREEN), mainMessage, 40);
                    break;
                case "bot":
                    Chat.editMessage("", Chat.getEmbed().clearFields()
                            .setAuthor(jda.getSelfUser().getName() + " - Info", "https://minehut.com", jda.getSelfUser().getAvatarUrl())
                            .addField("Memory Usage:", getMb(runtime.totalMemory() - runtime.freeMemory()), true)
                            .addField("Memory Free:", getMb(runtime.freeMemory()), true)
                            .addField("Total Memory:", getMb(runtime.totalMemory()), true)
                            .addField("Video threads:", String.valueOf(VideoThread.VIDEO_THREADS.activeCount()), true)
                            .addField("Total threads:", String.valueOf(Thread.getAllStackTraces().size()), true)
                            .addField("CPU Usage:", ((int) (ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemCpuLoad() * 10000)) / 100f + "%", true)
                            .addField("JDA Version:", JDAInfo.VERSION, true)
                            .setColor(Chat.CUSTOM_GREEN).setFooter("System time | " + new Date().toString(), null), mainMessage, 30);
                    break;
                default:
                    Chat.editMessage("", embed.clearFields().setDescription("Usage: `" + Command.getPrefix() + getCommand() + getArgs() + "`")
                            .setColor(Chat.CUSTOM_BLUE), mainMessage, 15);
                    break;
            }
        }
    }

    private boolean isOnline(JSONObject json) {
        String status;

        try {
            status = json.getJSONArray("children").getJSONObject(1)
                    .getJSONArray("children").getJSONObject(1)
                    .getJSONArray("children").getJSONObject(2)
                    .getJSONArray("children").getJSONObject(0)
                    .getJSONArray("children").getJSONObject(0)
                    .getJSONArray("children").getJSONObject(1)
                    .getJSONArray("children").getJSONObject(1)
                    .getJSONArray("children").getJSONObject(0)
                    .getJSONArray("children").getJSONObject(0)
                    .getString("html");
        } catch (JSONException e1) {
            status = "Offline";
        }

        switch (status) {
            case " Online ":
                return true;
            case "Offline":
                return false;
            default:
                return false;
        }
    }

    private Color isOnlineColor(JSONObject json) {
        String status;

        try {
            status = json.getJSONArray("children").getJSONObject(1)
                    .getJSONArray("children").getJSONObject(1)
                    .getJSONArray("children").getJSONObject(2)
                    .getJSONArray("children").getJSONObject(0)
                    .getJSONArray("children").getJSONObject(0)
                    .getJSONArray("children").getJSONObject(1)
                    .getJSONArray("children").getJSONObject(1)
                    .getJSONArray("children").getJSONObject(0)
                    .getJSONArray("children").getJSONObject(0)
                    .getString("html");
        } catch (JSONException e1) {
            status = "Offline";
        }

        switch (status) {
            case " Online ":
                return Chat.CUSTOM_GREEN;
            case "Offline":
                return Chat.CUSTOM_RED;
            default:
                return Chat.CUSTOM_RED;
        }
    }

    private String getUserRank(JSONObject json) {
        return json.getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(2)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(2)
                .getJSONArray("children").getJSONObject(0)
                .getString("html").replaceAll("\n", "");
    }

    private String getJoinDate(JSONObject json) {
        return json.getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(2)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getString("html");
    }

    private String getUserFriendCount(JSONObject json) {
        return json.getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(2)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(4)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(0)
                .getString("html");
    }

    private String getLastOnline(JSONObject json) {
        return json.getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(2)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(2)
                .getJSONArray("children").getJSONObject(1)
                .getString("html");
    }

    private String getUserOnlineTime(JSONObject json) {
        return json.getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(2)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(1)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(0)
                .getJSONArray("children").getJSONObject(1)
                .getString("html");
    }

    @Override
    public String getArgs() {
        return " <server|network|user|bot> [term]";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

}