package io.github.tanguygab.arphones.utils;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.config.ConfigurationFile;
import io.github.tanguygab.arphones.config.LanguageFile;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.*;

public class PhoneUtils {

    public static void sendMsg(Player sender, OfflinePlayer receiver, String msg) {
        LanguageFile lang = Utils.msgs();
        saveHistory(msg,sender.getUniqueId().toString(),receiver.getUniqueId().toString());
            if (Utils.isOnline(receiver)) {
                receiver.getPlayer().sendMessage(lang.getIngameMessage(sender,msg));
                sender.sendMessage(lang.getNotified(receiver));
                return;
            }
            if (!ARPhones.get().isDiscordSRVEnabled()) {
                sender.sendMessage(lang.getCantNotififyMsg());
                return;
            }
            if (hasNotificationsOff(receiver)) {
                sender.sendMessage(lang.getDiscordNotificationDisabled());
                return;
            }
            DiscordUtils.sendMsg(sender,receiver,msg);
    }

    public static void call(Player sender, OfflinePlayer receiver) {
        LanguageFile lang = Utils.msgs();
        if (!ARPhones.get().isDiscordSRVEnabled()) {
            sender.sendMessage(lang.getDiscordDisabled());
            return;
        }
        if (sender == receiver.getPlayer()) {
            sender.sendMessage(lang.getCantCallSelf());
            return;
        }
        if (isInCall(receiver.getUniqueId())) {
            sender.sendMessage(lang.isInCall(receiver.getName()));
            return;
        }
        if (Utils.isOnline(receiver)) {
            DiscordUtils.createVoiceChannel(sender,receiver,lang.getNotified(receiver));
            return;
        }
        if (!ARPhones.get().isDiscordSRVEnabled()) {
            sender.sendMessage(lang.getCantNotififyCall());
            return;
        }
        if (hasNotificationsOff(receiver)) {
            sender.sendMessage(lang.getDiscordNotificationDisabled());
            return;
        }
        DiscordUtils.createVoiceChannel(sender,receiver,lang.getDiscordCantNotifify(true));
    }


    public static boolean hasNotificationsOff(OfflinePlayer receiver) {
        return ARPhones.get().dataFile.getStringList("notifications-off", new ArrayList<>()).contains(receiver.getUniqueId().toString());
    }

    public static boolean isInCall(UUID called) {
        if (!ARPhones.get().isDiscordSRVEnabled()) return false;
        String id = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(called);
        Map<String,List<String>> channels = ARPhones.get().dataFile.getConfigurationSection("voice-channels");
        for (List<String> list : channels.values()) {
            if (list.contains(id))
                return true;
        }
        return false;
    }

    public static void saveHistory(String msg, String uuid1, String uuid2) {
        Map<String,String> msgMap = new HashMap<>();
        if (msg != null) msgMap.put("message",msg);
        msgMap.put("sender",uuid1);
        msgMap.put("date", LocalDateTime.now().toString());

        ConfigurationFile file = ARPhones.get().historyFile;
        String path = file.hasConfigOption(uuid1+"|"+uuid2) ? uuid1+"|"+uuid2 : uuid2+"|"+uuid1;
        List<Map<String,String>> list = (List<Map<String, String>>) file.getObject(path);
        if (list == null) list = new ArrayList<>();
        list.add(msgMap);
        file.set(path,list);
    }
    public static List<Map<String,String>> getHistory(String uuid1, String uuid2) {
        ConfigurationFile file = ARPhones.get().historyFile;
        String path = file.hasConfigOption(uuid1+"|"+uuid2) ? uuid1+"|"+uuid2 : uuid2+"|"+uuid1;
        if (!file.hasConfigOption(path)) return List.of();
        List<Map<String,String>> msgs = new ArrayList<>((List<Map<String, String>>) file.getObject(path));
        Collections.reverse(msgs);
        return msgs;
    }

}
