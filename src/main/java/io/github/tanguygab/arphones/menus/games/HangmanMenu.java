package io.github.tanguygab.arphones.menus.games;

import io.github.tanguygab.arphones.menus.PhoneMenu;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.Utils;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.PacketPlayOutWindowItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class HangmanMenu extends PhoneMenu {

    private int mistakes = 0;
    private String word;
    private StringBuilder wordHidden;
    private final List<Character> usedLetters = new ArrayList<>();

    private static final ItemStack fence = createMenuItem(Material.OAK_FENCE,"",null);
    private static final ItemStack stairs = createMenuItem(Material.OAK_STAIRS,"",null);
    private static final ItemStack stick = createMenuItem(Material.STICK,"",null);
    private final ItemStack viewerHead;
    private static final ItemStack chestplate = createMenuItem(Material.IRON_CHESTPLATE,"",null);
    private static final ItemStack leggings = createMenuItem(Material.IRON_LEGGINGS,"",null);
    private static final ItemStack boots = createMenuItem(Material.IRON_BOOTS,"",null);

    public HangmanMenu(Player p, Phone phone) {
        super(p, phone, "Hangman",6);

        viewerHead = createMenuItem(Material.PLAYER_HEAD, p.getName(),null);
        SkullMeta meta = (SkullMeta) viewerHead.getItemMeta();
        meta.setOwningPlayer(p);
        viewerHead.setItemMeta(meta);

        word = "MINECRAFT";
        wordHidden = new StringBuilder(word.replaceAll("[A-Z]", "_"));
    }

    @Override
    public void onOpen() {
        inv = Bukkit.getServer().createInventory(null, 54,wordHidden.toString());
        hangMan();

        fillMenu();
        p.openInventory(inv);
        sendLetters();
    }

    private void hangMan() {
        switch (mistakes) {
            case 11: inv.setItem(42,boots);
            case 10: inv.setItem(33,leggings);
            case 9: fillSlots(stick,23,25);
            case 8: inv.setItem(24,chestplate);
            case 7: inv.setItem(15,viewerHead);
            case 6: inv.setItem(12,stick);
            case 5: fillSlots(fence,3,4,5,6);
            case 4: inv.setItem(12,stick);
            case 3: inv.setItem(39,stairs);
            case 2: fillSlots(fence,2,11,20,29,38);
            case 1: fillSlots(fence,46,47,48,49,50,51,52);
        }
    }

    private void sendLetters() {
        NonNullList<net.minecraft.world.item.ItemStack> letters = NonNullList.a();
        net.minecraft.world.item.ItemStack empty = net.minecraft.world.item.ItemStack.b;
        for (int i = 0; i < 9; i++) letters.add(empty);
        for (char letter = 'A'; letter <= 'Z'; letter++) {
            ItemStack item = usedLetters.contains(letter) ? new ItemStack(Material.BEDROCK) : Utils.createHeadItem(getTexture(letter,usedLetters.contains(letter)),letter+"",null);
            letters.add(CraftItemStack.asNMSCopy(item));
        }
        runSync(()->((CraftPlayer)p).getHandle().b.a(new PacketPlayOutWindowItems(0,1,letters, empty)));
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        onClick(slot);
        sendLetters();
        return true;
    }

    private void onClick(int slot) {
        if (mistakes >= 11 || wordHidden.toString().equals(word)) return;
        slot = slot-54;
        if (slot < 0 || slot > 26) return;

        char letter = (char) ('A'+slot);
        if (usedLetters.contains(letter)) return;
        usedLetters.add(letter);
        String letterString = letter+"";

        if (!word.contains(letterString)) {
            mistakes++;
            hangMan();
            if (mistakes >= 11)
                p.sendMessage("You died! The word was "+word);
            return;
        }
        for (int i = 0; i < word.length(); i++) {
            if (letter == word.charAt(i))
                wordHidden.replace(i,i+1,letterString);
        }
        onOpen();
        if (wordHidden.toString().equals(word))
            p.sendMessage("You won!");
    }

    @Override
    public void onClose() {
        runSync(p::updateInventory);
        phone.setPageArgument(null);
        super.onClose();
    }

    private static String getTexture(char letter, boolean used) {
        return switch (letter) {
            case 'A' -> "0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27";
            case 'B' -> "71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530";
            case 'C' -> "4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847";
            case 'D' -> "1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5";
            case 'E' -> "d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5";
            case 'F' -> "6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2";
            case 'G' -> "334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab";
            case 'H' -> "6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9";
            case 'I' -> "59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5";
            case 'J' -> "e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840";
            case 'K' -> "0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27";
            case 'L' -> "71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530";
            case 'M' -> "4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847";
            case 'N' -> "1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5";
            case 'O' -> "d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5";
            case 'P' -> "6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2";
            case 'Q' -> "334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab";
            case 'R' -> "6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9";
            case 'S' -> "59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5";
            case 'T' -> "e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840";
            case 'U' -> "0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27";
            case 'V' -> "71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530";
            case 'W' -> "4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847";
            case 'X' -> "1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5";
            case 'Y' -> "d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5";
            case 'Z' -> "6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2";
            default -> "";
        };
    }
}
