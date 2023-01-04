package io.github.tanguygab.arphones.utils;

import io.github.tanguygab.arphones.phone.sim.Contact;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.util.*;

public class MenuUtils {

    public static List<UUID> getPlayers(List<Contact> contacts, Player player) {
        List<String> contactsUUIDs = contacts.stream().map(contact -> contact.getUUID().toString()).toList();

        return Arrays.stream(Bukkit.getServer().getOfflinePlayers())
                .filter(p->!contactsUUIDs.contains(p.getUniqueId().toString())
                        && !player.getUniqueId().toString().equals(p.getUniqueId().toString())
                        && (p.isOnline() || p.hasPlayedBefore()))
                .map(OfflinePlayer::getUniqueId)
                .toList();
    }

    public static List<UUID> sort(List<Contact> contacts) {
        Map<String,UUID> sortedMap = new TreeMap<>();
        contacts.forEach(contact->sortedMap.put(contact.isFavorite() ? "0"+contact.getUUID() : "1"+contact.getUUID(),contact.getUUID()));
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
