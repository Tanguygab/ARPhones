package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.config.LanguageFile;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class PhoneMenu {

    protected final Player p;
    public final Phone phone;
    public Inventory inv;
    public String chatInput = null;
    public boolean chatInputReopen = false;
    protected LanguageFile lang = Utils.msgs();

    public PhoneMenu(Player p, Phone phone, String title, int slots) {
        this.p = p;
        this.phone = phone;
        this.inv = Bukkit.getServer().createInventory(null,slots,title);
    }
    public PhoneMenu(Player p, Phone phone, String title, InventoryType invtype) {
        this.p = p;
        this.phone = phone;
        this.inv = Bukkit.getServer().createInventory(null,invtype,title);
    }

    public abstract void onOpen();

    public abstract boolean onClick(ItemStack item, int slot, ClickType click);

    public void onClose() {
        ARPhones.get().openedMenus.remove(p);
        p.closeInventory();
    }

    public boolean onChatInput(String message) {
        return true;
    }
    public void chatInput(String message, String inputType, boolean reopen) {
        chatInput = inputType;
        chatInputReopen = reopen;
        p.sendMessage(message);
        p.closeInventory();
    }

    public void setBackButton(int slot) {
        inv.setItem(slot,createMenuItem(Material.ARROW, lang.getBackButton(), null));
    }

    public void fillMenu() {
        ItemStack filler = getFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir() || item.getType().toString().contains("STAINED_GLASS_PANE"))
                inv.setItem(i,filler);
        }
    }
    public void fillSlots(int... slots) {
        ItemStack filler = getFiller();
        for (Integer slot : slots) inv.setItem(slot,filler);
    }

    public ItemStack getFiller() {
        return createMenuItem(phone.getBackgroundColorPane(),"",null);
    }

    public static ItemStack createMenuItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.colors("&f"+name));
        if (lore != null) {
            lore.forEach(e -> lore.set(lore.indexOf(e), ChatColor.GRAY + Utils.colors(e)));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

}
