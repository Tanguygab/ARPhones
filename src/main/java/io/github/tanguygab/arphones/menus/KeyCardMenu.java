package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.keycard.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class KeyCardMenu extends PhoneMenu {

    public KeyCardMenu(Player p, Phone phone) {
        super(p, phone, "Keycards",27);
    }

    @Override
    public void open() {

        p.openInventory(inv);
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        if (!Utils.isKeycard(item)) {
            phone.openMainMenu(p);
            return true;
        }

        if (phone.getKeycards().contains(item)) phone.removeKeyCard(item);
        else phone.addKeyCard(item);

        return true;
    }

}
