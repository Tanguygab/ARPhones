package io.github.tanguygab.arphones.listener;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.config.LanguageFile;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitListener implements Listener {

    @EventHandler
    public void onPhoneClick(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR || !Utils.isPhone(item)) return;
        e.setCancelled(true);
        Phone phone = Utils.getPhone(item);
        if (phone != null) {
            phone.openMenu(e.getPlayer());
            return;
        }
        Utils.addPhone(new Phone(e.getPlayer()),item);
    }

    private final Pattern contactInfoPattern = Pattern.compile(Utils.msgs().getContactInfoTitle("(?<player>[a-zA-Z0-9*_.]+)"));

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        LanguageFile lang = Utils.msgs();
        Player p = (Player) e.getWhoClicked();
        String contact = null;
        String menu = lang.getMenuTitle().equals(title) ? "menu"
                : lang.getChangePinMenuTitle().equals(title) ? "pin"
                : lang.getListTitle(true).equals(title) ? "contacts"
                : lang.getListTitle(false).equals(title) ? "players"
                : null;
        if (menu == null) {
            Matcher matcher = contactInfoPattern.matcher(title);
            if (!matcher.find()) return;
            contact = matcher.group("player");
            menu = "contact";
        }
        Phone phone = Utils.getPhone(p.getInventory().getItemInMainHand());
        if (phone == null) return;
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        int slot = e.getRawSlot();
        ClickType click = e.getClick();

        switch (menu) {
            case "menu" -> e.setCancelled(phone.onMenuClick(p,item,slot,click));
            case "pin" -> phone.onPinMenuClick(item,slot,click);
            case "contacts" -> phone.onContactsMenu(p,item,slot,click);
            case "players" -> phone.onPlayersMenu(p,item,slot,click);
            case "contact" -> phone.onContactInfoClick(p,contact,item,slot,click);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().equals(Utils.msgs().getChangePinMenuTitle())) {
            Player p = (Player) e.getPlayer();
            p.sendMessage("Pin changed!");
            Phone phone = Utils.getPhone(p.getInventory().getItemInMainHand());
            if (phone != null) Bukkit.getServer().getScheduler().runTaskLater(ARPhones.get(),()->phone.openMenu(p),1);
        }
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
