package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.MenuUtils;
import io.github.tanguygab.arphones.utils.PhoneUtils;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ContactInfoMenu extends PhoneMenu {

    private final OfflinePlayer contact;

    public ContactInfoMenu(Player p, Phone phone, OfflinePlayer contact) {
        super(p, phone, Bukkit.getServer().createInventory(null, 54,Utils.msgs().getContactInfoTitle(contact.getName())));
        this.contact = contact;
    }

    @Override
    public void open() {
        ItemStack back = MenuUtils.createMenuItem(Material.ARROW, lang.getBackButton(), null);
        inv.setItem(49, back);

        ItemStack filler = MenuUtils.createMenuItem(phone.getBackgroundColorPane(),"",null);
        List<Integer> fillerSlots = List.of(0,1,2,3,4,8,9,13,17,18,22,26,27,31,35,36,40,44,45,46,47,48,53);
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
        notes.put("Job", Arrays.asList("Mojang"));
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

        loadMessages(head);
        p.openInventory(inv);
    }

    public void loadMessages(ItemStack contactHead) {
        String uuid1 = p.getUniqueId().toString();
        String uuid2 = contact.getUniqueId().toString();
        List<Map<String,String>> msgs = PhoneUtils.getHistory(uuid1,uuid2);
        List<Integer> msgsSlots = List.of(51,42,33,24,15,6);

        ItemStack viewerHead = MenuUtils.createMenuItem(Material.PLAYER_HEAD, p.getName(),null);
        SkullMeta meta = (SkullMeta) viewerHead.getItemMeta();
        meta.setOwningPlayer(p);
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

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        switch (slot) {
            case 49 -> phone.openListMenu(p,true);
            case 19 -> PhoneUtils.call(p,contact);
            case 21 -> {
                PhoneUtils.sendMsg(p,contact);
                loadMessages(inv.getItem(11));
            }
        }
        return true;
    }
}
