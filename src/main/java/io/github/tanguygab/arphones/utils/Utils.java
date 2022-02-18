package io.github.tanguygab.arphones.utils;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.config.LanguageFile;
import io.github.tanguygab.arphones.phone.PhoneLook;
import io.github.tanguygab.arphones.config.ConfigurationFile;
import io.github.tanguygab.arphones.phone.Phone;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class Utils {

    public static NamespacedKey isPhoneKey = new NamespacedKey(ARPhones.get(),"isPhone");
    public static NamespacedKey phoneKey = new NamespacedKey(ARPhones.get(),"phone");
    public static NamespacedKey contactName = new NamespacedKey(ARPhones.get(),"contact-name");

    public static ItemStack getPhone(PhoneLook look) {
        ItemStack item = new ItemStack(look.getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colors("&8[&6"+look.getName()+"&8]"));
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(isPhoneKey, PersistentDataType.BYTE,(byte)1);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,1);
        return item;
    }

    public static boolean isPhone(ItemStack phone) {
        return phone != null && phone.getItemMeta() != null && phone.getItemMeta().getPersistentDataContainer().has(isPhoneKey,PersistentDataType.BYTE);
    }

    public static Phone getPhone(ItemStack item) {
        if (!isPhone(item)) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String phoneUUID = data.get(phoneKey, PersistentDataType.STRING);
        return ARPhones.get().phones.get(phoneUUID);
    }

    public static void addPhone(Phone phone, ItemStack item) {
        ARPhones.get().phones.put(phone.getUUID().toString(),phone);
        ConfigurationFile config = ARPhones.get().dataFile;
        config.set("phones."+phone.getUUID()+".pin",phone.getPin());
        config.set("phones."+phone.getUUID()+".contacts",phone.getContacts());
        config.set("phones."+phone.getUUID()+".favorites",phone.getFavorites());
        config.set("phones."+phone.getUUID()+".battery",phone.getBattery());
        config.set("phones."+phone.getUUID()+".owner",phone.getOwner());
        config.set("phones."+phone.getUUID()+".background-color",phone.getBackgroundColor());
        config.set("phones."+phone.getUUID()+".page",phone.getPage().toString());
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(phoneKey, PersistentDataType.STRING,phone.getUUID().toString());
            item.setItemMeta(meta);
        }
    }
    public static void removePhone(Phone phone) {
        ARPhones.get().phones.remove(phone.getUUID().toString());
        ARPhones.get().dataFile.set("phones."+phone.getUUID(),null);
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
