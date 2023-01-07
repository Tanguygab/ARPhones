package io.github.tanguygab.arphones.listener;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.phone.lock.LockSystem;
import io.github.tanguygab.arphones.phone.lock.UnlockMode;
import io.github.tanguygab.arphones.phone.sim.SIMCard;
import io.github.tanguygab.arphones.menus.PhoneMenu;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
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
        Player p = (Player) e.getWhoClicked();
        if (ARPhones.get().openedMenus.containsKey(p)) {
            e.setCancelled(ARPhones.get().openedMenus.get(p).onClick(e.getCurrentItem(), e.getRawSlot(), e.getClick()));
            return;
        }
        if (e.isCancelled()) return;

        ItemStack item = switch (e.getAction()) {
            case MOVE_TO_OTHER_INVENTORY -> e.getCurrentItem();
            case PLACE_ONE,PLACE_SOME,PLACE_ALL,SWAP_WITH_CURSOR -> e.getClickedInventory() == e.getView().getTopInventory() ? e.getCursor() : null;
            case HOTBAR_SWAP,HOTBAR_MOVE_AND_READD -> {
                if (e.getClickedInventory() != e.getView().getTopInventory()) yield null;
                if (e.getClick() == ClickType.SWAP_OFFHAND) yield p.getInventory().getItemInOffHand();
                ItemStack movedItem = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                if (movedItem == null || movedItem.getType().isAir()) yield movedItem;
                yield null;
            }
            default -> null;
        };

        Phone phone = Utils.getPhone(item,null);
        if (phone != null && phone.getLockSystem().getUnlockMode() == UnlockMode.LOCK_ON_DROP)
            phone.getLockSystem().setLocked(true);

    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack item = e.getItemDrop().getItemStack();
        Phone phone = Utils.getPhone(item,null);
        if (phone != null && phone.getLockSystem().getUnlockMode() == UnlockMode.LOCK_ON_DROP)
            phone.getLockSystem().setLocked(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Map<Player,PhoneMenu> menus = ARPhones.get().openedMenus;
        Player p = (Player) e.getPlayer();
        PhoneMenu menu = menus.get(p);
        if (menu == null || menu.chatInput != null) return;
        if (menu.inv.equals(e.getInventory())) menus.get(p).onClose();
        menu.runSync(()->{
            LockSystem lock = menu.phone.getLockSystem();
            if (!menus.containsKey(p) && lock.getUnlockMode() == UnlockMode.ALWAYS_LOCKED)
                lock.setLocked(true);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        PhoneMenu menu = ARPhones.get().openedMenus.get(p);
        if (menu == null || menu.chatInput == null) return;
        e.setCancelled(true);
        if (!menu.onChatInput(e.getMessage())) return;
        menu.chatInput = null;
        if (menu.chatInputReopen)
            menu.runSync(menu::onOpen);
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
