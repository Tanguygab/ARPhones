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
            ItemStack item = Utils.createHeadItem(getTexture(letter,usedLetters.contains(letter)),letter+"",null);
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
            case 'A' -> used ? "2ac58b1a3b53b9481e317a1ea4fc5eed6bafca7a25e741a32e4e3c2841278c" : "a67d813ae7ffe5be951a4f41f2aa619a5e3894e85ea5d4986f84949c63d7672e";
            case 'B' -> used ? "d4c711571e7e214ee78dfe4ee0e1263b92516e418de8fc8f3257ae0901431" : "50c1b584f13987b466139285b2f3f28df6787123d0b32283d8794e3374e23";
            case 'C' -> used ? "fff5aabead6feafaaecf4422cdd7837cbb36b03c9841dd1b1d2d3edb7825e851" : "abe983ec478024ec6fd046fcdfa4842676939551b47350447c77c13af18e6f";
            case 'D' -> used ? "893e622b581975792f7c119ec6f40a4f16e552bb98776b0c7ae2bdfd4154fe7" : "3193dc0d4c5e80ff9a8a05d2fcfe269539cb3927190bac19da2fce61d71";
            case 'E' -> used ? "a157d65b19921c760ff4910b3404455b9c2ee36afc202d8538baefec676953" : "dbb2737ecbf910efe3b267db7d4b327f360abc732c77bd0e4eff1d510cdef";
            case 'F' -> used ? "c54cf261b2cd6ab54b0c624f8f6ff565a7b63e28e3b50c6dbfb52b5f0d7cf9f" : "b183bab50a3224024886f25251d24b6db93d73c2432559ff49e459b4cd6a";
            case 'G' -> used ? "d3c9f8a74ca01ba8c54de1edc82e1fc07a83923e66574b6ffe606919240c6" : "1ca3f324beeefb6a0e2c5b3c46abc91ca91c14eba419fa4768ac3023dbb4b2";
            case 'H' -> used ? "f8c58c509034617bf81ee0db9be0ba3e85ca15568163914c87669edb2fd7" : "31f3462a473549f1469f897f84a8d4119bc71d4a5d852e85c26b588a5c0c72f";
            case 'I' -> used ? "4246323c9fb319326ee2bf3f5b63ec3d99df76a12439bf0b4c3ab32d13fd9" : "46178ad51fd52b19d0a3888710bd92068e933252aac6b13c76e7e6ea5d3226";
            case 'J' -> used ? "c58456cd9bb8a7e978591ae0cb26af1aadad4fa7a16725b295145e09bed8064" : "3a79db9923867e69c1dbf17151e6f4ad92ce681bcedd3977eebbc44c206f49";
            case 'K' -> used ? "af49fb708369e7bc2944ad706963fb6ac6ce6d4c67081ddadecfe5da51" : "9461b38c8e45782ada59d16132a4222c193778e7d70c4542c9536376f37be42";
            case 'L' -> used ? "8c84f75416e853a74f6c70fc7e1093d53961879955b433bd8c7c6d5a6df" : "319f50b432d868ae358e16f62ec26f35437aeb9492bce1356c9aa6bb19a386";
            case 'M' -> used ? "31fde91b19b9309913724fea9e85311271c67bcb78578d461bf65d9613074" : "49c45a24aaabf49e217c15483204848a73582aba7fae10ee2c57bdb76482f";
            case 'N' -> used ? "1c7c972e6785d6b0aceb779abdd7702d98341c24c2a71e702930eca58055" : "35b8b3d8c77dfb8fbd2495c842eac94fffa6f593bf15a2574d854dff3928";
            case 'O' -> used ? "8073bb44f9345f9bb31a679027e7939e461842a8c27486d7a6b842c39eb38c4e" : "d11de1cadb2ade61149e5ded1bd885edf0df6259255b33b587a96f983b2a1";
            case 'P' -> used ? "64b231a8d55870cfb5a9f4e65db06dd7f8e34282f1416f95878b19acc34ac95" : "a0a7989b5d6e621a121eedae6f476d35193c97c1a7cb8ecd43622a485dc2e912";
            case 'Q' -> used ? "ffedd6f9efdb156b86935699b2b4834df0f5d214513c01d38af3bd031cbcc92" : "43609f1faf81ed49c5894ac14c94ba52989fda4e1d2a52fd945a55ed719ed4";
            case 'R' -> used ? "c03a1cd583cbbffde08f943e56ac3e3afafecaede834221a81e6db6c64667f7d" : "a5ced9931ace23afc351371379bf05c635ad186943bc136474e4e5156c4c37";
            case 'S' -> used ? "b6572e655725d78375a9817eb9ee8b37829ca1fea93b6095cc7aa19e5eac" : "3e41c60572c533e93ca421228929e54d6c856529459249c25c32ba33a1b1517";
            case 'T' -> used ? "708c9ef3a3751e254e2af1ad8b5d668ccf5c6ec3ea2641877cba575807d39" : "1562e8c1d66b21e459be9a24e5c027a34d269bdce4fbee2f7678d2d3ee4718";
            case 'U' -> used ? "55a6e3ae5ae625923524838fac9fef5b42527f5027c9ca149e6c207792eb" : "607fbc339ff241ac3d6619bcb68253dfc3c98782baf3f1f4efdb954f9c26";
            case 'V' -> used ? "975121f7d9c68da0e5b6a96ac615298b12b2ee5bd19989436ee647879da5b" : "cc9a138638fedb534d79928876baba261c7a64ba79c424dcbafcc9bac7010b8";
            case 'W' -> used ? "67e165c3edc5541d4654c4728871e6908f613fc0ec46e823c96eac82ac62e62" : "269ad1a88ed2b074e1303a129f94e4b710cf3e5b4d995163567f68719c3d9792";
            case 'X' -> used ? "1919d1594bf809db7b44b3782bf90a69f449a87ce5d18cb40eb653fdec2722" : "5a6787ba32564e7c2f3a0ce64498ecbb23b89845e5a66b5cec7736f729ed37";
            case 'Y' -> used ? "e35424bb86305d7747604b13e924d74f1efe38906e4e458dd18dcc67b6ca48" : "c52fb388e33212a2478b5e15a96f27aca6c62ac719e1e5f87a1cf0de7b15e918";
            case 'Z' -> used ? "4e91200df1cae51acc071f85c7f7f5b8449d39bb32f363b0aa51dbc85d133e" : "90582b9b5d97974b11461d63eced85f438a3eef5dc3279f9c47e1e38ea54ae8d";
            default -> "";
        };
    }
}
