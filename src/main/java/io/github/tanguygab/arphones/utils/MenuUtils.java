package io.github.tanguygab.arphones.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.util.*;

public class MenuUtils {

    public static List<String> getPlayers(List<String> contacts, Player player) {
        List<String> list = new ArrayList<>();
        OfflinePlayer[] offps = Bukkit.getServer().getOfflinePlayers();
        for (OfflinePlayer p : offps) {
            String uuid = p.getUniqueId().toString();
            if (!contacts.contains(uuid) && !player.getUniqueId().toString().equals(uuid) && (p.isOnline() || p.hasPlayedBefore())) list.add(uuid);
        }
        return list;
    }

    public static List<String> sort(List<String> contacts, List<String> favorites) {
        Map<String,String> contactsNames = new HashMap<>();
        contacts.forEach(uuid->contactsNames.put(Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName(),uuid));

        Map<String,String> sortedMap = new TreeMap<>();
        contactsNames.forEach((name,uuid)->{
            if (favorites.contains(contactsNames.get(name))) sortedMap.put("0"+name,uuid);
            else sortedMap.put("1"+name,uuid);
        });
        return new ArrayList<>(sortedMap.values());
    }

    public static String nextColor(String color,int nextI) {
        List<String> colors = List.of("white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black");
        int i = colors.indexOf(color);
        if (i == -1) return "gray";
         i = i+nextI >= colors.size() ? i+nextI-colors.size() : i+nextI;
        return colors.get(i);
    }

}
