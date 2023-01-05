package io.github.tanguygab.arphones.menus.lockscreen;

import io.github.tanguygab.arphones.menus.PhoneMenu;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.phone.PhonePage;
import io.github.tanguygab.arphones.phone.lock.LockMode;
import io.github.tanguygab.arphones.phone.lock.LockSystem;
import io.github.tanguygab.arphones.phone.lock.UnlockMode;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LockScreenInfoMenu extends PhoneMenu {

    private final LockSystem system;
    public LockScreenInfoMenu(Player p, Phone phone) {
        super(p, phone, "Lock Screen Info Menu",27);
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
        inv.setItem(10, createMenuItem(mode.getMaterial(),"Change Lock Mode", Arrays.asList("","None","PIN","Password")));
    }
    private void setUnlockMode(UnlockMode mode) {
        system.setUnlockMode(mode);
        inv.setItem(12, createMenuItem(mode.getMaterial(),"Change Unlock Mode", Arrays.asList("","Always Locked","Lock on Drop","Always Unlocked")));

    }
    private void setFaceRecognition(boolean enabled) {
        system.setFaceRecognition(enabled);
        inv.setItem(14, createMenuItem(enabled ? Material.PLAYER_HEAD : Material.SKELETON_SKULL,(enabled ? "Enabled" : "Disabled")+" Face Recognition",null));
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        switch (slot) {
            case 10 -> setLockMode(getNext(LockMode.values(),system.getLockMode()));
            case 12 -> setUnlockMode(getNext(UnlockMode.values(),system.getUnlockMode()));
            case 14 -> setFaceRecognition(!system.hasFaceRecognition());
            case 16 -> {
                if (!p.getUniqueId().toString().equals(phone.getOwner())) {
                    p.sendMessage("You have to be the owner of the phone to do that!");
                    break;
                }
                chatInput("Send the name of the new owner of this phone:","setOwner",true);
            }
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
        return true;
    }
}
