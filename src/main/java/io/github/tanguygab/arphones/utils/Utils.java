package io.github.tanguygab.arphones.utils;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.menus.PhoneMenu;
import io.github.tanguygab.arphones.phone.sim.SIMCard;
import io.github.tanguygab.arphones.config.LanguageFile;
import io.github.tanguygab.arphones.phone.PhoneLook;
import io.github.tanguygab.arphones.phone.Phone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Utils {

    public static NamespacedKey phoneKey = new NamespacedKey(ARPhones.get(),"phone");
    public static NamespacedKey contactName = new NamespacedKey(ARPhones.get(),"contact-name");
    public static NamespacedKey SIMKey = new NamespacedKey(ARPhones.get(),"sim");

    public static Map<String,PlayerProfile> playerProfiles = new HashMap<>();

    public static boolean isOnline(OfflinePlayer player) {
        return player != null && player.isOnline() && player.getPlayer() != null;
    }

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
        if (phone == null && owner != null) {
            phone = new Phone(UUID.fromString(phoneUUID),owner);
            ARPhones.get().phones.put(phone.getUUID().toString(),phone);
            ARPhones.get().dataFile.set("phones."+phone.getUUID()+".owner",phone.getOwner());
        }
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

    public static void addSIM(SIMCard sim) {
        ARPhones.get().sims.put(sim.getUUID().toString(),sim);
    }

    public static String colors(String s) {
        return ChatColor.translateAlternateColorCodes('&',s);
    }

    public static LanguageFile msgs() {
        return ARPhones.get().languageFile;
    }

    public static OfflinePlayer getOfflinePlayer(String player) {
        for (OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
            if (player.equals(p.getName())) return p;
        }
        return null;
    }

    public static Map<String,Object> keyCardToString(ItemStack item) {
        Map<String,Object> map = new HashMap<>();
        map.put("type",item.getType().toString());
        map.put("amount",item.getAmount());
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) map.put("name",meta.getDisplayName());
        if (meta.hasLore()) map.put("lore",meta.getLore());
        PersistentDataContainer data = meta.getPersistentDataContainer();
        Map<String,String> dataMap = new HashMap<>();
        dataMap.put("keycard-type",data.get(io.github.tanguygab.keycard.Utils.keycardTypeKey,PersistentDataType.STRING));
        dataMap.put("scanner-id",data.get(io.github.tanguygab.keycard.Utils.scannerIdKey,PersistentDataType.STRING));
        map.put("data",dataMap);
        return map;
    }
    public static ItemStack keyCardFromString(Map<String,Object> map) {
        ItemStack item = new ItemStack(Material.getMaterial(map.get("type")+""), (int) map.get("amount"));
        ItemMeta meta = item.getItemMeta();
        if (map.containsKey("name")) meta.setDisplayName(map.get("name")+"");
        if (map.containsKey("lore")) meta.setLore((List<String>) map.get("lore"));
        PersistentDataContainer data = meta.getPersistentDataContainer();
        Map<String,String> dataMap = (Map<String, String>) map.get("data");
        data.set(io.github.tanguygab.keycard.Utils.keycardTypeKey,PersistentDataType.STRING,dataMap.get("keycard-type"));
        data.set(io.github.tanguygab.keycard.Utils.scannerIdKey,PersistentDataType.STRING,dataMap.get("scanner-id"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createHeadItem(String texture, String name, List<String> lore) {
        ItemStack item = PhoneMenu.createMenuItem(Material.PLAYER_HEAD, name,lore);

        PlayerProfile profile;
        if (!playerProfiles.containsKey(texture)) {
            profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
            try {
                profile.getTextures().setSkin(new URL("http://textures.minecraft.net/texture/" + texture));
            } catch (Exception e) {e.printStackTrace();}
            playerProfiles.put(texture,profile);
        }
        profile = playerProfiles.get(texture);
        if (profile == null) return item;
        SkullMeta meta = ((SkullMeta)item.getItemMeta());
        meta.setOwnerProfile(profile);
        item.setItemMeta(meta);
        return item;
    }

}
