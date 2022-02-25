package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.SIMCard;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.MenuUtils;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public class MainPhoneMenu extends PhoneMenu {

    public MainPhoneMenu(Player p, Phone phone) {
        super(p, phone, Utils.msgs().getMenuTitle(), 27);
    }

    @Override
    public void open() {

        inv.setItem(10, createMenuItem(Material.CREEPER_HEAD,lang.getContactsName(),lang.getContactsLore()));
        inv.setItem(14,createMenuItem(Material.IRON_DOOR,lang.getLockName(),lang.getLockLore()));
        inv.setItem(16,createMenuItem(Material.NAME_TAG,lang.getOwnerName(),lang.getOwnerLore(Bukkit.getServer().getOfflinePlayer(UUID.fromString(phone.getOwner())).getName())));

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

        //inv.setItem(8,createMenuItem(Material.REDSTONE_TORCH,lang.getConnectionName(),lang.getConnectionLore(5)));
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
            case 10 -> phone.openListMenu(p,true);
            case 12 -> {
                phone.setBackgroundColor(click == ClickType.MIDDLE ? "gray" : MenuUtils.nextColor(phone.getBackgroundColor(),click.isLeftClick() ? 1 : -1));
                loadBackground();
            }
            case 14 -> phone.openPinMenu(p);
            case 16 -> {
                if (!p.getUniqueId().toString().equals(phone.getOwner())) {
                    p.sendMessage("You have to be the owner of the phone to do that!");
                    break;
                }
                p.closeInventory();
                p.sendMessage("Send the name of the new owner of this phone:");
                ARPhones.get().changingOwners.put(p,phone);
            }
            case 22 -> {
                if (!Bukkit.getServer().getPluginManager().isPluginEnabled("KeyCard")) break;
                phone.openKeyCards(p);
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
