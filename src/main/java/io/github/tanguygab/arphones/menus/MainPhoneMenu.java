package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.phone.PhoneGame;
import io.github.tanguygab.arphones.phone.PhonePage;
import io.github.tanguygab.arphones.phone.sim.SIMCard;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.MenuUtils;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MainPhoneMenu extends PhoneMenu {

    public MainPhoneMenu(Player p, Phone phone) {
        super(p, phone, Utils.msgs().getMenuTitle(), 3);
    }

    @Override
    public void onOpen() {

        inv.setItem(10, createMenuItem(Material.CREEPER_HEAD,lang.getContactsName(),lang.getContactsLore()));
        boolean isOwner = phone.isOwner(p);
        inv.setItem(14,createMenuItem(Material.IRON_DOOR,isOwner ? lang.getLockName() : "Lock your Phone",isOwner ? lang.getLockLore() : null));
        inv.setItem(16,Utils.createHeadItem("b0f10e85418e334f82673eb4940b208ecaee0c95c287685e9eaf24751a315bfa","Videogames",Arrays.asList("","Play various videogames","from Hangman to Snake!")));

        inv.setItem(7,createMenuItem(Material.REDSTONE,lang.getBatteryName(),lang.getBatteryLore(phone.getBattery())));

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("KeyCard"))
            inv.setItem(22,createMenuItem(Material.BOOK,"KeyCards",null));

        loadSIMItem();
        loadBackground();


        p.openInventory(inv);
    }

    private void loadSIMItem() {
        ItemStack sim = phone.getSim() == null
                ? createMenuItem(Material.BARRIER,"No SIM card found",null)
                : createMenuItem(Material.BOOK,"SIM card", Arrays.asList("","Click to take out"));
        inv.setItem(8,sim);

        //inv.setItem(6,createMenuItem(Material.REDSTONE_TORCH,lang.getConnectionName(),lang.getConnectionLore(5)));
    }

    private void loadBackground() {
        inv.setItem(12,createMenuItem(phone.getBackgroundColorBlock(),lang.getBackgroundColorName(),lang.getBackgroundColorLore(phone.getBackgroundColor())));
        fillMenu();
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        ItemStack cursor = p.getItemOnCursor();
        switch (slot) {
            case 7 -> {
                if (cursor.getType() != Material.REDSTONE || phone.getBattery() == 100) break;
                p.setItemOnCursor(null);
                phone.setBattery(phone.getBattery()+5);
            }
            case 8 -> {
                SIMCard sim = Utils.getSIM(cursor);
                if (sim != null) {
                    p.setItemOnCursor(phone.getSim() != null ? Utils.getSIM(phone.getSim().getUUID(),false) : null);
                    phone.setSim(sim);
                    loadSIMItem();
                }
                if (phone.getSim() != null && cursor.getType().isAir()) {
                    p.setItemOnCursor(Utils.getSIM(phone.getSim().getUUID(),false));
                    phone.setSim(null);
                    loadSIMItem();
                }
            }
            case 10 -> phone.open(p,PhonePage.CONTACTS);
            case 12 -> {
                phone.setBackgroundColor(click == ClickType.MIDDLE ? "gray" : MenuUtils.nextColor(phone.getBackgroundColor(),click.isLeftClick() ? 1 : -1));
                loadBackground();
            }
            case 14 -> {
                if (!phone.isOwner(p)) {
                    phone.getLockSystem().setLocked(true);
                    onClose();
                    break;
                }
                phone.open(p,PhonePage.LOCK_SCREEN_INFO);
            }
            case 16 -> phone.open(p,PhonePage.VIDEOGAME);
            case 22 -> {
                if (!Bukkit.getServer().getPluginManager().isPluginEnabled("KeyCard")) break;
                phone.open(p,PhonePage.KEYCARDS);
            }
            default -> {
                boolean isRedstone = (item != null && item.getType() == Material.REDSTONE) || cursor.getType() == Material.REDSTONE;
                boolean isSIM = Utils.getSIM(item) != null || Utils.getSIM(cursor) != null;
                if (isRedstone || isSIM)
                    return false;
            }
        }
        return true;
    }
}
