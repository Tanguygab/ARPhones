package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.MenuUtils;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PinPhoneMenu extends PhoneMenu {

    public PinPhoneMenu(Player p, Phone phone) {
        super(p, phone, Utils.msgs().getChangePinMenuTitle(), InventoryType.HOPPER);
    }

    @Override
    public void open() {
        String pin = phone.getPin();

        char pinChar1 = pin.charAt(0);
        ItemStack pin1 = MenuUtils.createMenuItem(Material.PLAYER_HEAD, pinChar1+"",lang.getPinLore());
        MenuUtils.setPinHead(pin1,pinChar1);
        inv.setItem(0,pin1);

        char pinChar2 = pin.charAt(1);
        ItemStack pin2 = MenuUtils.createMenuItem(Material.PLAYER_HEAD,pinChar2+"",lang.getPinLore());
        MenuUtils.setPinHead(pin2,pinChar2);
        inv.setItem(1,pin2);

        char pinChar3 = pin.charAt(2);
        ItemStack pin3 = MenuUtils.createMenuItem(Material.PLAYER_HEAD,pinChar3+"",lang.getPinLore());
        MenuUtils.setPinHead(pin3,pinChar3);
        inv.setItem(3,pin3);

        char pinChar4 = pin.charAt(3);
        ItemStack pin4 = MenuUtils.createMenuItem(Material.PLAYER_HEAD,pinChar4+"",lang.getPinLore());
        MenuUtils.setPinHead(pin4,pinChar4);
        inv.setItem(4,pin4);

        p.openInventory(inv);
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        switch (slot) {
            case 0,1,3,4 -> {
                ItemMeta meta = item.getItemMeta();
                int i = slot > 2 ? slot-1 : slot;
                char[] chars = phone.getPin().toCharArray();
                int newPin = Integer.parseInt(chars[i]+"");

                if (click.isLeftClick()) newPin++;
                if (click.isRightClick()) newPin--;
                if (newPin > 9) newPin = 0;
                if (newPin < 0) newPin = 9;

                meta.setDisplayName(ChatColor.WHITE+""+newPin);
                item.setItemMeta(meta);

                chars[i] = (newPin+"").charAt(0);
                phone.setPin(""+chars[0]+chars[1]+chars[2]+chars[3]);
            }
        }
        return true;
    }

    @Override
    public void close() {
        super.close();
        p.sendMessage("Pin changed!");
        Phone phone = Utils.getPhone(p.getInventory().getItemInMainHand(),p);
        if (phone != null) Bukkit.getServer().getScheduler().runTaskLater(ARPhones.get(),()->phone.openMainMenu(p),1);
    }
}
