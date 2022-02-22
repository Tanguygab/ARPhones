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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class ListPhoneMenu extends PhoneMenu {

    private final boolean isContacts;

    public ListPhoneMenu(Player p, Phone phone, boolean isContacts) {
        super(p, phone, Bukkit.getServer().createInventory(null,54,Utils.msgs().getListTitle(isContacts)));
        this.isContacts = isContacts;
    }

    @Override
    public void open() {
        ItemStack filler = MenuUtils.createMenuItem(phone.getBackgroundColorPane(),"",null);
        List<Integer> fillerSlots = List.of(0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,49,50,51,52,53);
        for (int i : fillerSlots) inv.setItem(i,filler);

        ItemStack add = isContacts ? MenuUtils.createMenuItem(Material.LIME_WOOL, lang.getListAdd(), null) : filler;
        inv.setItem(48, add);

        ItemStack back = MenuUtils.createMenuItem(Material.ARROW, lang.getBackButton(), null);
        inv.setItem(isContacts ? 50 : 49, back);

        loadPlayers();

        p.openInventory(inv);
    }

    private void loadPlayers() {
        List<String> list = isContacts ? MenuUtils.sort(phone.getContacts(), phone.getFavorites()) : MenuUtils.getPlayers(phone.getContacts(), p);

        List<Integer> slots = List.of(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43);
        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            inv.setItem(slot,null);
            if (list.size() < i+1) continue;
            String uuid = list.get(i);
            OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuid));
            ItemStack head = phone.getFavorites().contains(uuid)
                    ? MenuUtils.createMenuItem(Material.PLAYER_HEAD,lang.getListFavoriteName(player.getName()),lang.getListFavoriteLore())
                    : MenuUtils.createMenuItem(Material.PLAYER_HEAD,player.getName(),isContacts ? lang.getListContactLore() : lang.getListPlayerLore());
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
            phone.openListMenu(p,false);
            return;
        } if (slot == 50) {
            phone.openMainMenu(p);
            return;
        }
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(Utils.contactName,PersistentDataType.STRING)));

        switch (click) {
            case LEFT, SHIFT_LEFT -> phone.openContactInfoMenu(p,player);
            case RIGHT, SHIFT_RIGHT -> {
                String uuid = player.getUniqueId().toString();
                if (phone.getFavorites().contains(uuid)) {
                    phone.removeFavorite(uuid);
                    p.sendMessage(player.getName() + " was removed from your favorites");
                    loadPlayers();
                    return;
                }
                phone.addFavorite(uuid);
                p.sendMessage(player.getName() + " was added to your favorites");
                loadPlayers();
            }
            case DROP -> {
                String uuid = player.getUniqueId().toString();
                phone.removeContact(uuid);
                p.sendMessage(player.getName() + " was removed from your contacts");
                loadPlayers();
            }
        }
    }

    private void onPlayersClick(ItemStack item, int slot, ClickType click) {
        if (slot == 49) {
            phone.openListMenu(p,true);
            return;
        }
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(Utils.contactName,PersistentDataType.STRING)));

        if (click.isLeftClick()) {
            phone.addContact(player.getUniqueId().toString());
            p.sendMessage(player.getName() + " was added to your contacts");
            loadPlayers();
        }
        else if (click.isRightClick()) PhoneUtils.sendMsg(p,player);
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        if (isContacts) onContactsClick(item, slot, click);
        else onPlayersClick(item, slot, click);
        return true;
    }
}
