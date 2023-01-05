package io.github.tanguygab.arphones.utils;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.Permission;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Category;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.ActionRow;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button;
import github.scarsz.discordsrv.util.DiscordUtil;
import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.config.LanguageFile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DiscordUtils {

    public static Map<String, String> waitingForCall = new HashMap<>();

    private static void async(Runnable run) {
        ARPhones.get().getServer().getScheduler().runTaskAsynchronously(ARPhones.get(),run);
    }

    public static void sendMsg(User user, String msg) {
        user.openPrivateChannel().queue(c -> c.sendMessage(msg).queue());
    }

    public static boolean isWaitingForCall(UUID caller, UUID called) {
        if (!waitingForCall.containsKey(caller.toString())) return false;
        return waitingForCall.get(caller.toString()).equals(called.toString());
    }

    private static User getDiscord(OfflinePlayer player) {
        String id = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
        if (id == null) return null;
        return DiscordUtil.getUserById(id);
    }

    public static Category getCallsCategory() {
        String cat = ARPhones.get().configFile.getString("discord-integration.calls-category");
        return DiscordUtil.getJda().getCategoryById(cat);
    }

    public static void sendMsg(Player sender, OfflinePlayer receiver, String msg) {
        async(()-> {
            LanguageFile lang = Utils.msgs();
            User user = getDiscord(receiver);
            if (user != null) {
                sendMsg(user, lang.getDiscordMessage(sender, msg));
                sender.sendMessage(lang.getDiscordCantNotifify(true));
            } else sender.sendMessage(lang.getDiscordCantNotifify(false));
        });
    }

    public static void createVoiceChannel(Category cat, User sender, User receiver) {
        Guild guild = cat.getGuild();
        Member caller = guild.getMember(sender);
        Member called = guild.getMember(receiver);
        if (caller == null || called == null) return;
        List<Permission> perms = List.of(Permission.VOICE_CONNECT,Permission.VIEW_CHANNEL);

        cat.createVoiceChannel(sender.getName()+"-"+receiver.getName())
                .addPermissionOverride(guild.getPublicRole(),null, perms)
                .addPermissionOverride(caller,perms,null)
                .addPermissionOverride(called,perms,null)
                .queue(v->{
                    // adding vc to dataFile
                    ARPhones.get().dataFile.set("voice-channels."+v.getId(),List.of(caller.getId(),called.getId()));
                    // moving caller && called if in voice channel
                    if (caller.getVoiceState() != null && caller.getVoiceState().inVoiceChannel()) {
                        guild.moveVoiceMember(caller,v).queue();
                        sendMsg(sender,"You were moved to your call with "+called.getAsMention()+".");
                    }
                    if (called.getVoiceState() != null && called.getVoiceState().inVoiceChannel()) {
                        guild.moveVoiceMember(called,v).queue();
                        sendMsg(receiver,"You were moved to your call with "+caller.getAsMention()+".");
                    }

                });
    }

    protected static void createVoiceChannel(Player sender, OfflinePlayer receiver,String senderMsg) {
        LanguageFile lang = Utils.msgs();
        User user1 = getDiscord(sender);
        if (user1 == null) {
            sender.sendMessage(lang.getNotLinked());
            return;
        }
        User user2 = getDiscord(receiver);
        if (user2 == null) {
            sender.sendMessage(lang.getReceiverNotLinked());
            return;
        }

        Category cat = getCallsCategory();
        if (cat == null) {
            sender.sendMessage(lang.getNoCallsCategory());
            return;
        }
        waitingForCall.put(user1.getId(),user2.getId());

        sender.sendMessage(senderMsg);
        if (Utils.isOnline(receiver)) {
            String msg = lang.getIngameCall(sender);
            TextComponent comp = new TextComponent(msg);
            TextComponent accept = new TextComponent("✔");
            accept.setColor(ChatColor.DARK_GREEN);
            accept.setBold(true);
            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/arphones accept "+sender.getName()));
            accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to accept")));
            TextComponent deny = new TextComponent("✖");
            deny.setColor(ChatColor.DARK_RED);
            deny.setBold(true);
            deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/arphones deny "+sender.getName()));
            deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to deny")));
            comp.addExtra(accept);
            comp.addExtra(deny);
            receiver.getPlayer().spigot().sendMessage(comp);
        }
        else {
            async(()-> user2.openPrivateChannel().queue(c -> c.sendMessage(Utils.msgs().getDiscordCall(user1.getAsMention())).queue(msg->
                    msg.editMessageComponents(ActionRow.of(
                            Button.success("arphones-accept-"+user1.getId(),"Accept"),
                            Button.danger("arphones-deny-"+user1.getId(),"Deny")
                    )).queue())));
        }
    }

}
