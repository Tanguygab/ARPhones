package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.keycard.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class KeyCardMenu extends PhoneMenu {

    public KeyCardMenu(Player p, Phone phone) {
        super(p, phone, "Keycards",27);
    }

    @Override
    public void open() {
        loadKeycards();
        setBackButton(22);
        fillMenu();

        p.openInventory(inv);
    }

    private void loadKeycards() {
        ItemStack empty = createMenuItem(Material.BARRIER,"No Keycard",null);
        List<ItemStack> keycards = phone.getKeycards();
        List<Integer> slots = List.of(10,11,12,13,14,15,16);
        for (Integer slot : slots) {
            int index = slots.indexOf(slot);
            ItemStack item;
            if (index >= keycards.size()) item = empty;
            else item = keycards.get(index);
            inv.setItem(slot,item);
        }
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        if (slot == 22) {
            phone.openMainMenu(p);
            return true;
        }
        if (!Utils.isKeycard(item)) return true;

        if (phone.getKeycards().contains(item)) {
            phone.removeKeyCard(item);
            p.getInventory().addItem(item);
        } else {
            ItemStack clone = item.clone();
            if (clone.getAmount() > 1) {
                clone.setAmount(1);
                item.setAmount(item.getAmount()-1);
            }
            phone.addKeyCard(clone);
            p.getInventory().remove(clone);
        }
        loadKeycards();

        return true;
    }

}
