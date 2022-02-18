package io.github.tanguygab.arphones.listener;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.entities.VoiceChannel;
import github.scarsz.discordsrv.dependencies.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.ButtonClickEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.util.DiscordUtil;
import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.utils.PhoneUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DiscordListener extends ListenerAdapter {

    @Subscribe
    public void discordReadyEvent(DiscordReadyEvent e) {
        ARPhones.get().discordInit();
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent e) {
        if (e.getButton() == null || e.isFromGuild()) return;
        String id = e.getButton().getId();
        if (!id.startsWith("arphones-")) return;
        User user = e.getUser();
        String[] arr = id.split("-");
        String action = arr[1];
        String callerID = arr[2];
        if (!PhoneUtils.waitingForCall.containsKey(callerID)) {
            e.reply("This player isn't calling you!").queue();
            return;
        }
        AccountLinkManager accounts = DiscordSRV.getPlugin().getAccountLinkManager();
        UUID uuid1 = accounts.getUuid(callerID);
        UUID uuid2 = accounts.getUuid(callerID);

        Player player = Bukkit.getServer().getPlayer(uuid1);
        User caller = DiscordUtil.getUserById(callerID);
        PhoneUtils.waitingForCall.remove(callerID);
        String msg;
        if (action.equals("accept")) {
            msg = user.getAsTag()+" accepted your call.";
            PhoneUtils.createVoiceChannel(PhoneUtils.getCallsCategory(),caller,user);
            PhoneUtils.saveHistory(null,uuid1.toString(),uuid2.toString());
        } else msg = user.getAsTag()+" denied your call";

        if (PhoneUtils.isOnline(player)) player.sendMessage(msg);
        else PhoneUtils.sendMsg(caller,msg);

        if (action.equals("accept")) e.reply("You accepted "+player.getName()+"'s call!").queue();
        else e.reply("You denied "+player.getName()+"'s call!").queue();
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent e) {
        onLeave(e.getMember(),e.getChannelLeft());
    }
    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent e) {
        onLeave(e.getMember(),e.getChannelLeft());
    }

    public void onLeave(Member m, VoiceChannel c) {
        Map<String, List<String>> channels = ARPhones.get().dataFile.getConfigurationSection("voice-channels");
        if (!channels.containsKey(c.getId())) return;
        if (!channels.get(c.getId()).contains(m.getId())) return;
        if (c.getMembers().isEmpty()) {
            c.delete().queue();
            List<String> users = channels.get(c.getId());
            User user1 = DiscordUtil.getUserById(users.get(0));
            User user2 = DiscordUtil.getUserById(users.get(1));
            ARPhones.get().dataFile.set("voice-channels."+c.getId(),null);
            user1.openPrivateChannel().queue(pc->pc.sendMessage("Your call with "+user2.getAsMention()+" has ended.").queue());
            user2.openPrivateChannel().queue(pc->pc.sendMessage("Your call with "+user1.getAsMention()+" has ended.").queue());
        }
    }

}
