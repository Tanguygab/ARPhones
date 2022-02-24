package io.github.tanguygab.arphones.listener;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.SIMCard;
import io.github.tanguygab.arphones.menus.PhoneMenu;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;

public class BukkitListener implements Listener {

    @EventHandler
    public void onPhoneClick(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        boolean useBlock = e.getAction() == Action.RIGHT_CLICK_BLOCK && e.useInteractedBlock() != Event.Result.DENY;
        boolean useItem = e.getAction() == Action.RIGHT_CLICK_AIR && e.useItemInHand() != Event.Result.DENY;
        if (!useBlock && !useItem) return;
        Phone phone = Utils.getPhone(item,e.getPlayer());
        if (phone == null) return;
        e.setCancelled(true);
        phone.openLastMenu(e.getPlayer());
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

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getRecipe() != Bukkit.getServer().getRecipe(new NamespacedKey(ARPhones.get(), "sim"))) return;
        ItemStack sim = e.getCurrentItem();
        UUID uuid = UUID.randomUUID();
        Utils.addSIM(new SIMCard(uuid));
        ItemMeta meta = sim.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(Utils.SIMKey, PersistentDataType.STRING, uuid.toString());
        sim.setItemMeta(meta);
    }

}
