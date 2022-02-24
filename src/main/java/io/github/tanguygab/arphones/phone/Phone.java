package io.github.tanguygab.arphones.phone;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.SIMCard;
import io.github.tanguygab.arphones.menus.*;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Phone {

    private final UUID uuid;
    private String pin;
    private String owner;
    private SIMCard sim;

    private String backgroundColor;
    private int battery;
    private PhonePage page;
    private String contactPage = null;

    // keycard hook
    private final List<ItemStack> keycards;

    public Phone(UUID uuid, String pin, SIMCard sim, int battery, String owner, String backgroundColor, PhonePage page, List<ItemStack> keycards) {
        this.uuid = uuid;
        this.pin = pin;
        this.sim = sim;
        this.battery = battery;
        this.owner = owner;
        this.backgroundColor = backgroundColor;
        if (page == null) setPage(PhonePage.MAIN);
        else this.page = page;
        this.keycards = keycards;
    }
    public Phone(UUID uuid, Player p) {
        this(uuid, "0000",null,100,p.getUniqueId().toString(),"gray", PhonePage.MAIN,new ArrayList<>());
        openPinMenu(p);
    }

    private void set(String path, Object value) {
        ARPhones.get().dataFile.set("phones."+uuid+"."+path,value);
    }

    public UUID getUUID() {
        return uuid;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
        set("owner",owner);
    }

    public SIMCard getSim() {
        return sim;
    }
    public void setSim(SIMCard sim) {
        this.sim = sim;
        set("sim",sim == null ? null : sim.getUUID().toString());
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }
    public void setBackgroundColor(String color) {
        Material mat = Material.getMaterial(backgroundColor.toUpperCase()+"_STAINED_GLASS");
        backgroundColor = mat == null ? "gray" : color;
        set("background-color",backgroundColor);
    }
    public Material getBackgroundColorBlock() {
        Material mat = Material.getMaterial(backgroundColor.toUpperCase()+"_STAINED_GLASS");
        return mat == null ? Material.GRAY_STAINED_GLASS : mat;
    }
    public Material getBackgroundColorPane() {
        Material mat = Material.getMaterial(backgroundColor.toUpperCase()+"_STAINED_GLASS_PANE");
        return mat == null ? Material.GRAY_STAINED_GLASS_PANE : mat;
    }

    public PhonePage getPage() {
        return page;
    }
    public void setPage(PhonePage page) {
        this.page = page;
        set("page",page.toString());
    }
    public void setContactPage(String contact) {
        contactPage = contact;
    }

    public String getPin() {
        return pin;
    }
    public void setPin(String pin) {
        this.pin = pin;
        set("pin",pin);
    }

    public int getBattery() {
        return battery;
    }
    public void setBattery(int battery) {
        if (battery > 100) battery = 100;
        if (battery < 0) battery = 0;
        this.battery = battery;
        set("battery",battery);
    }

    public void openMenu(Player p, PhoneMenu menu, PhonePage page) {
        p.closeInventory();

        ARPhones.get().openedMenus.put(p,menu);
        setPage(page);
        menu.open();
    }

    public void openMainMenu(Player p) {
        openMenu(p,new MainPhoneMenu(p,this),PhonePage.MAIN);
    }

    public void openPinMenu(Player p) {
        openMenu(p,new PinPhoneMenu(p,this),PhonePage.LOCK_SCREEN);
    }

    public void openListMenu(Player p, boolean isContacts) {
        if (sim == null) {
            p.sendMessage("You need a SIM card for this!");
            return;
        }
        openMenu(p,new ListPhoneMenu(p,this,isContacts),isContacts ? PhonePage.CONTACTS : PhonePage.PLAYERS);
    }

    public void openContactInfoMenu(Player p, OfflinePlayer contact) {
        if (contact == null) {
            openMainMenu(p);
            return;
        }
        setContactPage(contact.getUniqueId().toString());
        openMenu(p,new ContactInfoMenu(p,this,contact),PhonePage.CONTACT_INFO);
    }

    public void openLastMenu(Player p) {
        switch (page) {
            case MAIN -> openMainMenu(p);
            case LOCK_SCREEN -> openPinMenu(p);
            case CONTACTS -> openListMenu(p,true);
            case PLAYERS -> openListMenu(p,false);
            case CONTACT_INFO -> openContactInfoMenu(p,Bukkit.getServer().getOfflinePlayer(UUID.fromString(contactPage)));
            case KEYCARDS -> openKeyCards(p);
        }
    }

    public void openKeyCards(Player p) {
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("KeyCard")) {
            p.sendMessage("The KeyCard plugin isn't loaded!");
            return;
        }
        openMenu(p,new KeyCardMenu(p,this),PhonePage.KEYCARDS);
    }

    public List<ItemStack> getKeycards() {
        return keycards;
    }
    public void addKeyCard(ItemStack card) {
        keycards.add(card);
        List<Object> list = (List<Object>) ARPhones.get().dataFile.getObject("phones."+uuid+".keycards", new ArrayList<>());
        list.add(Utils.keyCardToString(card));
        set("keycards",list);
    }
    public void removeKeyCard(ItemStack card) {
        keycards.remove(card);
        List<Object> list = (List<Object>) ARPhones.get().dataFile.getObject("phones."+uuid+".keycards",new ArrayList<>());
        list.remove(Utils.keyCardToString(card));
        set("keycards",list);
    }
}
