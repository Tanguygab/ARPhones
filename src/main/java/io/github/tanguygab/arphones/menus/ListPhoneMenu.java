package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.phone.PhonePage;
import io.github.tanguygab.arphones.phone.sim.Contact;
import io.github.tanguygab.arphones.phone.sim.SIMCard;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.MenuUtils;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class ListPhoneMenu extends PhoneMenu {

    private final boolean isContacts;
    private final SIMCard sim;

    public ListPhoneMenu(Player p, Phone phone, boolean isContacts) {
        super(p, phone, Utils.msgs().getListTitle(isContacts),6);
        sim = phone.getSim();
        this.isContacts = isContacts;
    }

    @Override
    public void onOpen() {
        fillSlots(0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,49,50,51,52,53);

        ItemStack add = isContacts ? createMenuItem(Material.LIME_WOOL, lang.getListAdd(), null) : getFiller();
        inv.setItem(48, add);

        setBackButton(isContacts ? 50 : 49);

        loadPlayers();

        p.openInventory(inv);
    }

    private void loadPlayers() {
        List<UUID> list = isContacts ? MenuUtils.sort(sim.getContacts()) : MenuUtils.getPlayers(sim.getContacts(), p);

        List<Integer> slots = List.of(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43);
        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            inv.setItem(slot,null);
            if (list.size() < i+1) continue;
            UUID uuid = list.get(i);
            OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);
            ItemStack head = isContacts && sim.getContact(uuid).isFavorite()
                    ? createMenuItem(Material.PLAYER_HEAD,lang.getListFavoriteName(player.getName()),lang.getListFavoriteLore())
                    : createMenuItem(Material.PLAYER_HEAD,player.getName(),isContacts ? lang.getListContactLore() : lang.getListPlayerLore());
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (player.getPlayerProfile().isComplete()) meta.setOwningPlayer(player);
            else player.getPlayerProfile().update().thenRunAsync(()->{
                meta.setOwningPlayer(player);
                head.setItemMeta(meta);
                inv.setItem(slot,head);
            });
            meta.getPersistentDataContainer().set(Utils.contactName, PersistentDataType.STRING,player.getUniqueId().toString());
            head.setItemMeta(meta);
            inv.setItem(slot,head);
        }
    }

    private void onContactsClick(ItemStack item, int slot, ClickType click) {
        if (slot == 48) {
            phone.open(p, PhonePage.PLAYERS);
            return;
        } if (slot == 50) {
            phone.open(p,PhonePage.MAIN);
            return;
        }
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        Contact contact = sim.getContact(UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(Utils.contactName,PersistentDataType.STRING)));

        switch (click) {
            case LEFT, SHIFT_LEFT -> phone.open(p,PhonePage.CONTACT_INFO,contact);
            case RIGHT, SHIFT_RIGHT -> {
                boolean newStatus = !contact.isFavorite();
                contact.setFavorite(newStatus);
                p.sendMessage(contact.getName() + " was "+(newStatus ? "added" : "removed")+" from your favorites");
                loadPlayers();
            }
            case DROP -> {
                sim.removeContact(contact.getUUID());
                p.sendMessage(contact.getName() + " was removed from your contacts");
                loadPlayers();
            }
        }
    }

    private void onPlayersClick(ItemStack item, int slot, ClickType click) {
        if (slot == 49) {
            phone.open(p,PhonePage.CONTACTS);
            return;
        }
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        Contact contact = sim.getContact(UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(Utils.contactName,PersistentDataType.STRING)));

        if (click.isRightClick()) {
            sim.addContact(contact.getUUID());
            p.sendMessage(contact.getName() + " was added to your contacts");
            loadPlayers();
        }
        else if (click.isLeftClick()) phone.open(p,PhonePage.CONTACT_INFO,contact);
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        if (isContacts) onContactsClick(item, slot, click);
        else onPlayersClick(item, slot, click);
        return true;
    }
}
