package io.github.tanguygab.arphones.menus.lockscreen;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.menus.PhoneMenu;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.phone.PhonePage;
import io.github.tanguygab.arphones.phone.lock.LockMode;
import io.github.tanguygab.arphones.phone.lock.LockSystem;
import io.github.tanguygab.arphones.phone.lock.UnlockMode;
import io.github.tanguygab.arphones.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LockScreenInfoMenu extends PhoneMenu {

    private final LockSystem system;
    public LockScreenInfoMenu(Player p, Phone phone) {
        super(p, phone, "Lock Screen Info Menu",3);
        system = phone.getLockSystem();
    }

    @Override
    public void onOpen() {
        setLockMode(system.getLockMode());
        setUnlockMode(system.getUnlockMode());
        setFaceRecognition(system.hasFaceRecognition());
        inv.setItem(16,createMenuItem(Material.NAME_TAG,lang.getOwnerName(),lang.getOwnerLore(Bukkit.getServer().getOfflinePlayer(UUID.fromString(phone.getOwner())).getName())));
        inv.setItem(26,createMenuItem(Material.IRON_DOOR,"Lock your Phone",null));
        setBackButton(22);

        fillMenu();

        p.openInventory(inv);
    }

    private void setLockMode(LockMode mode) {
        system.setLockMode(mode);

        List<String> lore = new ArrayList<>();
        lore.add("");
        for (LockMode m : LockMode.values())
            lore.add((m == mode ? "&6" : "&e") + m);

        if (mode != LockMode.NONE) {
            lore.add(0,"");
            lore.add(1,"Current "+mode+": &f"+system.getKey());
            lore.add("");
            lore.add("Drop to edit");
        }

        inv.setItem(10, createMenuItem(mode.getMaterial(),"Change Lock Mode", lore));
    }
    private void setUnlockMode(UnlockMode mode) {
        system.setUnlockMode(mode);

        List<String> lore = new ArrayList<>();
        lore.add("");
        for (UnlockMode m : UnlockMode.values())
            lore.add((m == mode ? "&6" : "&e") + m);

        inv.setItem(12, createMenuItem(mode.getMaterial(),"Change Unlock Mode", lore));

    }
    private void setFaceRecognition(boolean enabled) {
        system.setFaceRecognition(enabled);
        ItemStack item = createMenuItem(enabled ? Material.PLAYER_HEAD : Material.SKELETON_SKULL,(enabled ? "Enabled" : "Disabled")+" Face Recognition",null);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getServer().getOfflinePlayer(UUID.fromString(phone.getOwner())));
        item.setItemMeta(meta);
        inv.setItem(14, item);
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        switch (slot) {
            case 10 -> {
                switch (click) {
                    case LEFT,SHIFT_LEFT -> setLockMode(getNext(LockMode.values(),system.getLockMode()));
                    case RIGHT,SHIFT_RIGHT -> setLockMode(getPrevious(LockMode.values(),system.getLockMode()));
                    case DROP,CONTROL_DROP -> {
                        switch (system.getLockMode()) {
                            case PASSWORD -> {
                                onClose();
                                new AnvilGUI.Builder()
                                        .plugin(ARPhones.get())
                                        .title("Change your password")
                                        .text(system.getKey())
                                        .itemLeft(new ItemStack(Material.PAPER))
                                        .onComplete(completion -> List.of(AnvilGUI.ResponseAction.run(()->{
                                            system.setKey(completion.getText());
                                            phone.open(p,PhonePage.LOCK_SCREEN_INFO);
                                        })))
                                        .open(p);
                            }
                            case PIN -> phone.open(p,PhonePage.PIN_EDIT);
                        }
                    }
                }
            }
            case 12 -> {
                switch (click) {
                    case LEFT, SHIFT_LEFT -> setUnlockMode(getNext(UnlockMode.values(), system.getUnlockMode()));
                    case RIGHT, SHIFT_RIGHT -> setUnlockMode(getPrevious(UnlockMode.values(), system.getUnlockMode()));
                }
            }
            case 14 -> setFaceRecognition(!system.hasFaceRecognition());
            case 16 -> chatInput("Send the name of the new owner of this phone:","setOwner",false);
            case 22 -> phone.open(p,PhonePage.MAIN);
            case 26 -> {
                system.setLocked(true);
                phone.setPage(PhonePage.MAIN);
                onClose();
            }
        }
        return true;
    }

    private <T> T getNext(T[] array, T object) {
        int index = List.of(array).indexOf(object);
        index++;
        return array[index >= array.length ? 0 : index];
    }
    private <T> T getPrevious(T[] array, T object) {
        int index = List.of(array).indexOf(object);
        index--;
        return array[index < 0 ? array.length-1 : index];
    }

    @Override
    public boolean onChatInput(String msg) {
        if (msg.equalsIgnoreCase("cancel")) {
            p.sendMessage("Cancelled...");
            return true;
        }
        OfflinePlayer newOwner = Utils.getOfflinePlayer(msg);
        if (newOwner == null) {
            p.sendMessage("That player doesn't exist!");
            return false;
        }
        phone.setOwner(newOwner.getUniqueId().toString());
        p.sendMessage("Phone owner changed! New owner: "+newOwner.getName());
        runSync(()->phone.open(p, PhonePage.MAIN));
        return true;
    }
}
