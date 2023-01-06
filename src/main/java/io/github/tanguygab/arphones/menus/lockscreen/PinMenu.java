package io.github.tanguygab.arphones.menus.lockscreen;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.phone.PhonePage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PinMenu extends PinEditMenu {

    private String pin;
    public PinMenu(Player p, Phone phone) {
        super(p, phone);
        pin = phone.getLockSystem().getKey();
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        switch (slot) {
            case 0,1,3,4 -> {
                ItemMeta meta = item.getItemMeta();
                int i = slot > 2 ? slot-1 : slot;
                char[] chars = pin.toCharArray();
                int newPin = Integer.parseInt(chars[i]+"");

                if (click.isLeftClick()) newPin++;
                if (click.isRightClick()) newPin--;
                if (newPin > 9) newPin = 0;
                if (newPin < 0) newPin = 9;

                meta.setDisplayName(ChatColor.WHITE+""+newPin);
                item.setItemMeta(meta);

                chars[i] = (newPin+"").charAt(0);
                pin = ""+chars[0]+chars[1]+chars[2]+chars[3];
            }
        }
        return true;
    }

    @Override
    public void onClose() {
        if (!pin.equals(phone.getLockSystem().getKey())) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_RED+"Invalid PIN!"));
            p.openInventory(inv);
            return;
        }

        ARPhones.get().openedMenus.remove(p);
        p.closeInventory();

        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+"Valid PIN!"));
        phone.getLockSystem().setLocked(false);
        Bukkit.getServer().getScheduler().runTask(ARPhones.get(),()->phone.open(p,PhonePage.MAIN));
    }

}
