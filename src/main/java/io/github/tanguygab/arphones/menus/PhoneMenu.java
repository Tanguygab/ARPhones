package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.config.LanguageFile;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class PhoneMenu {

    protected final Player p;
    public final Phone phone;
    protected Inventory inv;
    protected LanguageFile lang = Utils.msgs();

    public PhoneMenu(Player p, Phone phone, Inventory inv) {
        this.p = p;
        this.phone = phone;
        this.inv = inv;
    }

    public abstract void open();

    public abstract boolean onClick(ItemStack item, int slot, ClickType click);

    public void close() {
        ARPhones.get().openedMenus.remove(p);
    }

}
