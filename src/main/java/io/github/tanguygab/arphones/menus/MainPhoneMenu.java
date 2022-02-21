package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.MenuUtils;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class MainPhoneMenu extends PhoneMenu {

    public MainPhoneMenu(Player p, Phone phone) {
        super(p, phone, Bukkit.getServer().createInventory(null,27,Utils.msgs().getMenuTitle()));
    }

    @Override
    public void open() {

        inv.setItem(10, MenuUtils.createMenuItem(Material.CREEPER_HEAD,lang.getContactsName(),lang.getContactsLore()));
        inv.setItem(14,MenuUtils.createMenuItem(Material.IRON_DOOR,lang.getLockName(),lang.getLockLore()));
        inv.setItem(16,MenuUtils.createMenuItem(Material.NAME_TAG,lang.getOwnerName(),lang.getOwnerLore(Bukkit.getServer().getOfflinePlayer(UUID.fromString(phone.getOwner())).getName())));

        inv.setItem(7,MenuUtils.createMenuItem(Material.REDSTONE,lang.getBatteryName(),lang.getBatteryLore(phone.getBattery())));
        inv.setItem(8,MenuUtils.createMenuItem(Material.REDSTONE_TORCH,lang.getConnectionName(),lang.getConnectionLore(5)));

        loadBackground();

        p.openInventory(inv);
    }

    private void loadBackground() {
        inv.setItem(12,MenuUtils.createMenuItem(phone.getBackgroundColorBlock(),lang.getBackgroundColorName(),lang.getBackgroundColorLore(phone.getBackgroundColor())));

        ItemStack filler = MenuUtils.createMenuItem(phone.getBackgroundColorPane(),"",null);
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir() || item.getType().toString().contains("STAINED_GLASS_PANE"))
                inv.setItem(i,filler);
        }
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        switch (slot) {
            case 7 -> {
                if (p.getItemOnCursor().getType() != Material.REDSTONE || phone.getBattery() == 100) break;
                p.setItemOnCursor(null);
                phone.setBattery(phone.getBattery()+5);
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
            default -> {
                if ((item != null && item.getType() == Material.REDSTONE) || p.getItemOnCursor().getType() == Material.REDSTONE)
                    return false;
            }
        }
        return true;
    }
}
