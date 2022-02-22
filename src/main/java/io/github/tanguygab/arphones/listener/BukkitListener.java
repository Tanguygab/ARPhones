package io.github.tanguygab.arphones.listener;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.menus.PhoneMenu;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BukkitListener implements Listener {

    @EventHandler
    public void onPhoneClick(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR || !Utils.isPhone(item)) return;
        e.setCancelled(true);
        Phone phone = Utils.getPhone(item);
        if (phone != null) {
            phone.openLastMenu(e.getPlayer());
            return;
        }
        Utils.addPhone(new Phone(e.getPlayer()),item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (ARPhones.get().openedMenus.containsKey(e.getWhoClicked()))
            e.setCancelled(ARPhones.get().openedMenus.get(e.getWhoClicked()).onClick(e.getCurrentItem(),e.getRawSlot(),e.getClick()));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Map<Player,PhoneMenu> menus = ARPhones.get().openedMenus;
        Player p = (Player) e.getPlayer();
        if (menus.containsKey(p)) menus.remove(e.getPlayer()).close();
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!ARPhones.get().changingOwners.containsKey(p)) return;
        e.setCancelled(true);
        String msg = e.getMessage();
        if (msg.equalsIgnoreCase("cancel")) {
            p.sendMessage("Cancelled...");
            ARPhones.get().changingOwners.remove(p);
            return;
        }
        Phone phone = ARPhones.get().changingOwners.get(p);
        OfflinePlayer newOwner = Bukkit.getServer().getOfflinePlayer(msg);
        phone.setOwner(newOwner.getUniqueId().toString());
        p.sendMessage("Phone owner changed! New owner: "+newOwner.getName());

    }

}
