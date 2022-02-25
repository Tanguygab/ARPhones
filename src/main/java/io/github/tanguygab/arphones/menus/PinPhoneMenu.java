package io.github.tanguygab.arphones.menus;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.net.URL;
import java.util.UUID;

public class PinPhoneMenu extends PhoneMenu {

    public PinPhoneMenu(Player p, Phone phone) {
        super(p, phone, Utils.msgs().getChangePinMenuTitle(), InventoryType.HOPPER);
    }

    @Override
    public void open() {
        String pin = phone.getPin();

        char pinChar1 = pin.charAt(0);
        ItemStack pin1 = createMenuItem(Material.PLAYER_HEAD, pinChar1+"",lang.getPinLore());
        setPinHead(pin1,pinChar1);
        inv.setItem(0,pin1);

        char pinChar2 = pin.charAt(1);
        ItemStack pin2 = createMenuItem(Material.PLAYER_HEAD,pinChar2+"",lang.getPinLore());
        setPinHead(pin2,pinChar2);
        inv.setItem(1,pin2);

        char pinChar3 = pin.charAt(2);
        ItemStack pin3 = createMenuItem(Material.PLAYER_HEAD,pinChar3+"",lang.getPinLore());
        setPinHead(pin3,pinChar3);
        inv.setItem(3,pin3);

        char pinChar4 = pin.charAt(3);
        ItemStack pin4 = createMenuItem(Material.PLAYER_HEAD,pinChar4+"",lang.getPinLore());
        setPinHead(pin4,pinChar4);
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

    public static void setPinHead(ItemStack item, char num) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        setTexture(meta,num);
        item.setItemMeta(meta);
    }

    public static String getTexture(char num) {
        String texture = "";
        switch (num) {
            case '0' -> texture = "0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27";
            case '1' -> texture = "71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530";
            case '2' -> texture = "4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847";
            case '3' -> texture = "1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5";
            case '4' -> texture = "d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5";
            case '5' -> texture = "6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2";
            case '6' -> texture = "334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab";
            case '7' -> texture = "6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9";
            case '8' -> texture = "59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5";
            case '9' -> texture = "e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840";
        }
        return texture;
    }

    public static void setTexture(SkullMeta meta, char num) {
        try {
            PlayerProfile profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
            profile.getTextures().setSkin(new URL("http://textures.minecraft.net/texture/" + getTexture(num)));
            meta.setOwnerProfile(profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
