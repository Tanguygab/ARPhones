package io.github.tanguygab.arphones.utils;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.SIMCard;
import io.github.tanguygab.arphones.config.LanguageFile;
import io.github.tanguygab.arphones.phone.PhoneLook;
import io.github.tanguygab.arphones.config.ConfigurationFile;
import io.github.tanguygab.arphones.phone.Phone;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class Utils {

    public static NamespacedKey phoneKey = new NamespacedKey(ARPhones.get(),"phone");
    public static NamespacedKey contactName = new NamespacedKey(ARPhones.get(),"contact-name");
    public static NamespacedKey SIMKey = new NamespacedKey(ARPhones.get(),"sim");

    public static ItemStack getPhone(PhoneLook look) {
        ItemStack item = new ItemStack(look.getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colors("&8[&6"+look.getName()+"&8]"));
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(phoneKey, PersistentDataType.STRING,UUID.randomUUID().toString());
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,1);
        return item;
    }

    public static ItemStack getSIM(UUID uuid, boolean register) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colors("&8[&6SIM Card&8]"));
        if (register) {
            uuid = UUID.randomUUID();
            addSIM(new SIMCard(uuid));
        }
        if (uuid != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(SIMKey, PersistentDataType.STRING,uuid.toString());
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,1);
        return item;
    }

    public static Phone getPhone(ItemStack item, Player owner) {
        if (item == null || item.getItemMeta() == null) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (!data.has(phoneKey,PersistentDataType.STRING)) return null;
        String phoneUUID = data.get(phoneKey, PersistentDataType.STRING);
        Phone phone = ARPhones.get().phones.get(phoneUUID);
        if (phone == null) addPhone(new Phone(UUID.randomUUID(),owner));
        return phone;
    }
    public static SIMCard getSIM(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (!data.has(SIMKey,PersistentDataType.STRING)) return null;
        String simUUID = data.get(SIMKey, PersistentDataType.STRING);
        SIMCard sim = ARPhones.get().sims.get(simUUID);
        if (sim == null) addSIM(new SIMCard(UUID.randomUUID()));
        return sim;
    }

    public static void addPhone(Phone phone) {
        ARPhones.get().phones.put(phone.getUUID().toString(),phone);
        ConfigurationFile config = ARPhones.get().dataFile;
        config.set("phones."+phone.getUUID()+".pin",phone.getPin());
        config.set("phones."+phone.getUUID()+".battery",phone.getBattery());
        config.set("phones."+phone.getUUID()+".owner",phone.getOwner());
        config.set("phones."+phone.getUUID()+".background-color",phone.getBackgroundColor());
        config.set("phones."+phone.getUUID()+".page",phone.getPage().toString());
    }
    public static void addSIM(SIMCard sim) {
        ARPhones.get().sims.put(sim.getUUID().toString(),sim);
        ConfigurationFile config = ARPhones.get().dataFile;
        config.set("sims."+sim.getUUID()+".contacts",sim.getContacts());
        config.set("sims."+sim.getUUID()+".favorites",sim.getFavorites());
    }

    public static String colors(String s) {
        return ChatColor.translateAlternateColorCodes('&',s);
    }

    public static LanguageFile msgs() {
        return ARPhones.get().languageFile;
    }

    public static void removeInvalidPlayers(List<String> list) {
        list.forEach(contact->{
            OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(UUID.fromString(contact));
            if (!p.hasPlayedBefore() && !p.isOnline()) list.remove(contact);
        });
    }

    public static OfflinePlayer getOfflinePlayer(String player) {
        for (OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
            if (player.equals(p.getName())) return p;
        }
        return null;
    }
}
