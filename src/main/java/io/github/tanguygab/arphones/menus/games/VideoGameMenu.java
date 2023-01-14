package io.github.tanguygab.arphones.menus.games;

import io.github.tanguygab.arphones.menus.PhoneMenu;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.phone.PhoneGame;
import io.github.tanguygab.arphones.phone.PhonePage;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class VideoGameMenu extends PhoneMenu {

    public VideoGameMenu(Player p, Phone phone) {
        super(p, phone, "Videogames",3);
    }

    @Override
    public void onOpen() {
        inv.setItem(10,createMenuItem(Material.APPLE,"Snake",null));
        inv.setItem(11,createMenuItem(Material.PAPER,"Rock Paper Scissors",null));
        inv.setItem(13,createMenuItem(Material.CHAIN,"Hangman",null));
        inv.setItem(15, Utils.createHeadItem("944c4df7e17db3c7e996cc67b17e8f8a97d62c81fe32f852e1a4779a9fc588b8","Tic Tac Toe",null));
        inv.setItem(16, Utils.createHeadItem("4c43b5a92fd8a5290a4be002420e6c22a5cff7e631dd5ac1d03dcef4ec98d701","Chess",null));

        setBackButton(22);
        inv.setItem(21,createMenuItem(Material.PLAYER_HEAD,"Spectate your friends",null));
        fillMenu();

        p.openInventory(inv);
    }

    @Override
    public boolean onClick(ItemStack item, int slot, ClickType click) {
        switch (slot) {
            case 10,11,15,16 -> p.sendMessage("WIP");
            case 13 -> phone.open(p,PhonePage.VIDEOGAME,PhoneGame.HANGMAN);
            case 21 -> p.sendMessage("Soon");
            case 22 -> phone.open(p,PhonePage.MAIN);
        }
        return true;
    }
}
