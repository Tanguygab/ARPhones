package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.phone.sim.Contact;
import io.github.tanguygab.arphones.utils.DiscordUtils;
import io.github.tanguygab.arphones.utils.PhoneUtils;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ContactInfoMenu extends PhoneMenu {

    private final OfflinePlayer contact;

    public ContactInfoMenu(Player p, Phone phone, OfflinePlayer contact) {
        super(p, phone, Utils.msgs().getContactInfoTitle(contact.getName()), 54);
        this.contact = contact;
    }

    @Override
    public void open() {
        setBackButton(49);

        fillSlots(0,1,2,3,4,8,9,13,17,18,22,26,27,31,35,36,40,44,45,46,47,48,53);


        ItemStack head = createMenuItem(Material.PLAYER_HEAD, contact.getName(),null);
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
        boolean isWaitingForCall = ARPhones.get().isDiscordSRVEnabled() && DiscordUtils.isWaitingForCall(contact,p);
        ItemStack callItem;
        if (isWaitingForCall) callItem = createMenuItem(Material.LIME_WOOL,"Click to accept call!",null);
        else callItem = createMenuItem(isInCall ? Material.BARRIER : Material.NOTE_BLOCK,isInCall ? lang.getContactInfoIsInCall(contact.getName()) : lang.getContactInfoCall(contact.getName()),null);
        inv.setItem(19,callItem);
        inv.setItem(21,createMenuItem(Material.OAK_SIGN,lang.getContactInfoMsg(contact.getName()),null));

        loadNotes();
        loadMessages(head);
        p.openInventory(inv);
    }

    private void loadNotes() {
        Contact contact = phone.getSim().getContact(this.contact.getUniqueId());
        List<String> notes = contact.getNotes();

        List<Integer> slots = List.of(28,29,30,37,38,39);
        for (int i = 0; i < slots.size(); i++) {
            String note = "\nClick to add a note";
            Material item = Material.MAP;
            if (notes.size() > i && !notes.get(i).equals("")) {
                note = notes.get(i);
                item = Material.PAPER;
            }
            inv.setItem(slots.get(i),createMenuItem(item,"Note "+(i+1),Arrays.asList(note.split("\\n"))));
        }
    }

    private void loadMessages(ItemStack contactHead) {
        String uuid1 = p.getUniqueId().toString();
        String uuid2 = contact.getUniqueId().toString();
        List<Map<String,String>> msgs = PhoneUtils.getHistory(uuid1,uuid2);
        List<Integer> msgsSlots = List.of(51,42,33,24,15,6);

        ItemStack viewerHead = createMenuItem(Material.PLAYER_HEAD, p.getName(),null);
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
            inv.setItem(msgsSlots.get(i), createMenuItem(isCall ? Material.NOTE_BLOCK : Material.PAPER, time.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")),lore));
        }
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        switch (slot) {
            case 28,29,30,37,38,39 -> {
                int note = slot < 31 ? slot-28 : slot-34;
                Contact contact1 = phone.getSim().getContact(contact.getUniqueId());
                if (click == ClickType.DROP) {
                    if (item.getType() == Material.MAP) break;
                    contact1.setNote(note,"");
                    inv.setItem(slot,createMenuItem(Material.MAP,"Note "+(note+1),Arrays.asList("","Click to add a note")));
                    break;
                }
                p.closeInventory();
                p.sendMessage("Write a note for "+contact.getName()+":");
                ARPhones.get().settingNote.put(p,List.of(contact1,note));
            }
            case 49 -> phone.openListMenu(p,true);
            case 19 -> PhoneUtils.call(p,contact);
            case 21 -> {
                p.closeInventory();
                p.sendMessage("Write a message to send to "+contact.getName()+":");
                ARPhones.get().sendingMsg.put(p,contact);
            }
        }
        return true;
    }

    @Override
    public void close() {
        phone.setContactPage(null);
        super.close();
    }
}
