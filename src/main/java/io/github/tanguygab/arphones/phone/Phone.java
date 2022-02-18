package io.github.tanguygab.arphones.phone;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.config.ConfigurationFile;
import io.github.tanguygab.arphones.config.LanguageFile;
import io.github.tanguygab.arphones.utils.MenuUtils;
import io.github.tanguygab.arphones.utils.PhoneUtils;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Phone {

    private final UUID uuid;
    private String pin;
    private String owner;
    private final List<String> contacts;
    private final List<String> favorites;

    private String backgroundColor;
    private int battery;
    private PhonePage page;

    public Phone(UUID uuid, String pin, List<String> contacts, List<String> favorites, int battery, String owner, String backgroundColor, PhonePage page) {
        this.uuid = uuid;
        this.pin = pin;
        this.contacts = contacts;
        this.favorites = favorites;
        this.battery = battery;
        this.owner = owner;
        this.backgroundColor = backgroundColor;
        this.page = page == null ? PhonePage.MAIN : page;
    }
    public Phone(Player p) {
        this(UUID.randomUUID(), "0000",new ArrayList<>(),new ArrayList<>(),100,p.getUniqueId().toString(),"gray", PhonePage.MAIN);
        openPinMenu(p);
    }

    private void set(String path, Object value) {
        ARPhones.get().dataFile.set("phones."+uuid+"."+path,value);
    }

    public UUID getUUID() {
        return uuid;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
        set("owner",owner);
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }
    public void setBackgroundColor(String color) {
        Material mat = Material.getMaterial(backgroundColor.toUpperCase()+"_STAINED_GLASS");
        backgroundColor = mat == null ? "gray" : color;
        set("background-color",backgroundColor);
    }
    public Material getBackgroundColorBlock() {
        Material mat = Material.getMaterial(backgroundColor.toUpperCase()+"_STAINED_GLASS");
        return mat == null ? Material.GRAY_STAINED_GLASS : mat;
    }
    public Material getBackgroundColorPane() {
        Material mat = Material.getMaterial(backgroundColor.toUpperCase()+"_STAINED_GLASS_PANE");
        return mat == null ? Material.GRAY_STAINED_GLASS_PANE : mat;
    }
    public PhonePage getPage() {
        return page;
    }
    public void setPage(PhonePage page) {
        this.page = page;
        set("page",page.toString());
    }

    public String getPin() {
        return pin;
    }
    public void setPin(String pin) {
        this.pin = pin;
        set("pin",pin);
    }

    public int getBattery() {
        return battery;
    }
    public void setBattery(int battery) {
        if (battery > 100) battery = 100;
        if (battery < 0) battery = 0;
        this.battery = battery;
        set("battery",battery);
    }

    public List<String> getContacts() {
        return contacts;
    }
    public void addContact(String uuid) {
        if (contacts.contains(uuid)) return;
        contacts.add(uuid);
        set("contacts",contacts);
    }
    public void removeContact(String uuid) {
        removeFavorite(uuid);
        contacts.remove(uuid);
        if (contacts.isEmpty()) set("contacts",null);
        else set("contacts",contacts);
    }

    public List<String> getFavorites() {
        return favorites;
    }
    public void addFavorite(String uuid) {
        if (favorites.contains(uuid)) return;
        favorites.add(uuid);
        set("favorites",favorites);
    }
    public void removeFavorite(String uuid) {
        favorites.remove(uuid);
        set("favorites",favorites);
        if (favorites.isEmpty()) set("favorites",null);
        else set("favorites",favorites);
    }

    public void openMenu(Player p) {
        LanguageFile lang = Utils.msgs();
        Inventory inv = Bukkit.getServer().createInventory(null,27,lang.getMenuTitle());

        inv.setItem(10,MenuUtils.createMenuItem(Material.CREEPER_HEAD,lang.getContactsName(),lang.getContactsLore()));
        inv.setItem(12,MenuUtils.createMenuItem(getBackgroundColorBlock(),lang.getBackgroundColorName(),lang.getBackgroundColorLore(backgroundColor)));
        inv.setItem(14,MenuUtils.createMenuItem(Material.IRON_DOOR,lang.getLockName(),lang.getLockLore()));
        inv.setItem(16,MenuUtils.createMenuItem(Material.NAME_TAG,lang.getOwnerName(),lang.getOwnerLore(Bukkit.getServer().getOfflinePlayer(UUID.fromString(this.owner)).getName())));

        inv.setItem(7,MenuUtils.createMenuItem(Material.REDSTONE,lang.getBatteryName(),lang.getBatteryLore(this.battery)));
        inv.setItem(8,MenuUtils.createMenuItem(Material.REDSTONE_TORCH,lang.getConnectionName(),lang.getConnectionLore(5)));

        ItemStack filler = MenuUtils.createMenuItem(getBackgroundColorPane(),"",null);
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir())
                inv.setItem(i,filler);
        }

        p.openInventory(inv);
    }
    public boolean onMenuClick(Player p, ItemStack item, int slot, ClickType click) {
        switch (slot) {
            case 7 -> {
                if (p.getItemOnCursor().getType() != Material.REDSTONE || battery == 100) break;
                p.setItemOnCursor(null);
                setBattery(battery+5);
            }
            case 10 -> openListMenu(p,true);
            case 12 -> {
                setBackgroundColor(click == ClickType.MIDDLE ? "gray" : MenuUtils.nextColor(backgroundColor,click.isLeftClick() ? 1 : -1));
                openMenu(p);
            }
            case 14 -> openPinMenu(p);
            case 16 -> {
                if (!p.getUniqueId().toString().equals(owner)) {
                    p.sendMessage("You have to be the owner of the phone to do that!");
                    break;
                }
                p.closeInventory();
                p.sendMessage("Send the name of the new owner of this phone:");
                ARPhones.get().changingOwners.put(p,this);
            }
            default -> {
                if ((item != null && item.getType() == Material.REDSTONE) || p.getItemOnCursor().getType() == Material.REDSTONE)
                    return false;
            }
        }
        return true;
    }

    public void openPinMenu(Player p) {
        LanguageFile lang = Utils.msgs();
        Inventory inv = Bukkit.getServer().createInventory(null, InventoryType.HOPPER,lang.getChangePinMenuTitle());

        ItemStack pin1 = MenuUtils.createMenuItem(Material.PLAYER_HEAD,pin.charAt(0)+"",lang.getPinLore());
        MenuUtils.setPinHead(pin1,0);
        inv.setItem(0,pin1);

        ItemStack pin2 = MenuUtils.createMenuItem(Material.PLAYER_HEAD,pin.charAt(1)+"",lang.getPinLore());
        MenuUtils.setPinHead(pin2,0);
        inv.setItem(1,pin2);

        ItemStack pin3 = MenuUtils.createMenuItem(Material.PLAYER_HEAD,pin.charAt(2)+"",lang.getPinLore());
        MenuUtils.setPinHead(pin3,0);
        inv.setItem(3,pin3);

        ItemStack pin4 = MenuUtils.createMenuItem(Material.PLAYER_HEAD,pin.charAt(3)+"",lang.getPinLore());
        MenuUtils.setPinHead(pin4,0);
        inv.setItem(4,pin4);

        p.openInventory(inv);
    }
    public void onPinMenuClick(ItemStack item, int slot, ClickType click) {
        switch (slot) {
            case 0,1,3,4 -> {
                ItemMeta meta = item.getItemMeta();
                int i = slot > 2 ? slot-1 : slot;
                char[] chars = pin.toCharArray();
                int newPin = Integer.parseInt(chars[i]+"");

                if (click.isLeftClick()) newPin++;
                if (click.isRightClick()) newPin--;
                if (newPin > 9) newPin = 0;
                if (newPin < 0) newPin = 9;

                meta.setDisplayName(ChatColor.WHITE+""+newPin);
                item.setItemMeta(meta);

                chars[i] = (newPin+"").charAt(0);
                setPin(""+chars[0]+chars[1]+chars[2]+chars[3]);
            }
        }
    }

    public void openListMenu(Player p, boolean isContacts) {
        LanguageFile lang = Utils.msgs();
        List<String> list = isContacts ? MenuUtils.sort(contacts, favorites) : MenuUtils.getPlayers(contacts,p);
        Inventory inv = Bukkit.getServer().createInventory(null,54,lang.getListTitle(isContacts));

        ItemStack filler = MenuUtils.createMenuItem(getBackgroundColorPane(),"",null);
        List<Integer> fillerSlots = List.of(0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,49,50,51,52,53);
        for (int i : fillerSlots) inv.setItem(i,filler);

        ItemStack add = isContacts ? MenuUtils.createMenuItem(Material.LIME_WOOL, lang.getListAdd(), null) : filler;
        inv.setItem(48, add);

        ItemStack back = MenuUtils.createMenuItem(Material.ARROW, lang.getBackButton(), null);
        inv.setItem(isContacts ? 50 : 49, back);

        int el = 0;
        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack item = inv.getItem(slot);
            if (item != null && !item.getType().isAir()) continue;
            if (list.size() < el+1) break;
            String uuid = list.get(el);
            OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuid));
            ItemStack head = MenuUtils.createMenuItem(Material.PLAYER_HEAD,favorites.contains(uuid) ? lang.getListFavoriteName(player.getName()) : player.getName(),!isContacts ? lang.getListPlayerLore() : favorites.contains(player.getUniqueId().toString()) ? lang.getListFavoriteLore(): lang.getListContactLore());
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            int finalSlot = slot;
            if (player.getPlayerProfile().isComplete()) meta.setOwningPlayer(player);
            else player.getPlayerProfile().update().thenRunAsync(()->{
                meta.setOwningPlayer(player);
                head.setItemMeta(meta);
                inv.setItem(finalSlot,head);
            });
            meta.getPersistentDataContainer().set(Utils.contactName, PersistentDataType.STRING,player.getUniqueId().toString());
            head.setItemMeta(meta);
            inv.setItem(slot,head);
            el++;
        }

        p.openInventory(inv);
    }

    public void onContactsMenu(Player p, ItemStack item, int slot, ClickType click) {
        if (slot == 48) {
            openListMenu(p, false);
            return;
        } if (slot == 50) {
            openMenu(p);
            return;
        }
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(Utils.contactName,PersistentDataType.STRING)));

        switch (click) {
            case LEFT, SHIFT_LEFT -> openContactInfoMenu(p,player);
            case RIGHT, SHIFT_RIGHT -> {
                String uuid = player.getUniqueId().toString();
                if (favorites.contains(uuid)) {
                    removeFavorite(uuid);
                    p.sendMessage(player.getName() + " was removed from your favorites");
                    openListMenu(p,true);
                    return;
                }
                addFavorite(uuid);
                p.sendMessage(player.getName() + " was added to your favorites");
                openListMenu(p,true);
            }
            case DROP -> {
                String uuid = player.getUniqueId().toString();
                removeContact(uuid);
                p.sendMessage(player.getName() + " was removed from your contacts");
                openListMenu(p, true);
            }
        }
    }
    public void onPlayersMenu(Player p, ItemStack item, int slot, ClickType click) {
        if (slot == 49) {
            openListMenu(p, true);
            return;
        }
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(Utils.contactName,PersistentDataType.STRING)));

        if (click.isLeftClick()) {
            addContact(player.getUniqueId().toString());
            p.sendMessage(player.getName() + " was added to your contacts");
            openListMenu(p,false);
        }
        else if (click.isRightClick()) PhoneUtils.sendMsg(p,player);
    }

    public void openContactInfoMenu(Player p, OfflinePlayer contact) {
        LanguageFile lang = Utils.msgs();
        Inventory inv = Bukkit.getServer().createInventory(null, 54,lang.getContactInfoTitle(contact.getName()));

        ItemStack filler = MenuUtils.createMenuItem(getBackgroundColorPane(),"",null);
        List<Integer> fillerSlots = List.of(0,1,2,3,4,8,9,13,17,18,22,26,27,31,35,36,40,44,45,46,47,48,49,53);
        for (int i : fillerSlots) inv.setItem(i,filler);

        ItemStack head = MenuUtils.createMenuItem(Material.PLAYER_HEAD, contact.getName(),null);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (contact.getPlayerProfile().isComplete()) meta.setOwningPlayer(contact);
        else contact.getPlayerProfile().update().thenRunAsync(()->{
            meta.setOwningPlayer(contact);
            head.setItemMeta(meta);
            inv.setItem(11,head);
        });
        head.setItemMeta(meta);
        inv.setItem(11,head);

        boolean isInCall = PhoneUtils.isInCall(contact);
        boolean isWaitingForCall = PhoneUtils.isWaitingForCall(contact,p);
        ItemStack callItem;
        if (isWaitingForCall) callItem = MenuUtils.createMenuItem(Material.LIME_WOOL,"Click to accept call!",null);
        else callItem = MenuUtils.createMenuItem(isInCall ? Material.BARRIER : Material.NOTE_BLOCK,isInCall ? lang.getContactInfoIsInCall(contact.getName()) : lang.getContactInfoCall(contact.getName()),null);
        inv.setItem(19,callItem);
        inv.setItem(21,MenuUtils.createMenuItem(Material.OAK_SIGN,lang.getContactInfoMsg(contact.getName()),null));

        Map<String,List<String>> notes = new HashMap<>();
        notes.put("Job",Arrays.asList("Mojang"));
        notes.put("Location",Arrays.asList("Ur Mom"));
        List<String> titles = new ArrayList<>(notes.keySet());
        List<Integer> slots = List.of(28,29,30,37,38,39);
        for (int i = 0; i < slots.size(); i++) {
            if (titles.size() > i) {
                String title = titles.get(i);
                List<String> note = notes.get(title);
                inv.setItem(slots.get(i), MenuUtils.createMenuItem(Material.PAPER, title,note));
            } else inv.setItem(slots.get(i),MenuUtils.createMenuItem(Material.MAP,"Note "+i,Arrays.asList("","Nothing to see here")));
        }

        loadMessages(inv,p,contact.getUniqueId().toString(),head);

        p.openInventory(inv);
    }
    public void loadMessages(Inventory inv, Player viewer, String uuid2, ItemStack contactHead) {
        String uuid1 = viewer.getUniqueId().toString();
        List<Map<String,String>> msgs = PhoneUtils.getHistory(uuid1,uuid2);
        List<Integer> msgsSlots = List.of(51,42,33,24,15,6);

        ItemStack viewerHead = MenuUtils.createMenuItem(Material.PLAYER_HEAD, viewer.getName(),null);
        SkullMeta meta = (SkullMeta) viewerHead.getItemMeta();
        meta.setOwningPlayer(viewer);
        viewerHead.setItemMeta(meta);

        for (int i = 0; i < msgsSlots.size(); i++) {
            if (msgs.size() <= i) break;
            Map<String,String> msg = msgs.get(i);
            boolean isCall = !msg.containsKey("message");
            String message = isCall ? "Call started" : msg.get("message");
            String sender = msg.get("sender");
            LocalDateTime time = LocalDateTime.parse(msg.get("date"));
            List<String> lore = Arrays.asList(message);
            if (uuid1.equals(sender)) inv.setItem(msgsSlots.get(i)+1,viewerHead);
            else inv.setItem(msgsSlots.get(i)-1,contactHead);
            inv.setItem(msgsSlots.get(i), MenuUtils.createMenuItem(isCall ? Material.NOTE_BLOCK : Material.PAPER, time.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")),lore));
        }
    }

    public void onContactInfoClick(Player p, String player, ItemStack item, int slot, ClickType click) {
        OfflinePlayer contact = Utils.getOfflinePlayer(player);
        if (contact == null) {
            p.sendMessage("This player doesn't exist anymore!");
            return;
        }
        switch (slot) {
            case 19 -> PhoneUtils.call(p,contact);
            case 21 -> {
                PhoneUtils.sendMsg(p,contact);
                openContactInfoMenu(p,contact);
            }
        }
    }

}
