package io.github.tanguygab.arphones.phone;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.menus.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.util.*;

public class Phone {

    private final UUID uuid;
    private String pin;
    private String owner;
    private final List<String> contacts;
    private final List<String> favorites;

    private String backgroundColor;
    private int battery;
    private PhonePage page;
    private String contactPage = null;

    public Phone(UUID uuid, String pin, List<String> contacts, List<String> favorites, int battery, String owner, String backgroundColor, PhonePage page) {
        this.uuid = uuid;
        this.pin = pin;
        this.contacts = contacts;
        this.favorites = favorites;
        this.battery = battery;
        this.owner = owner;
        this.backgroundColor = backgroundColor;
        this.page = page == null ? PhonePage.MAIN : page;
    }
    public Phone(Player p) {
        this(UUID.randomUUID(), "0000",new ArrayList<>(),new ArrayList<>(),100,p.getUniqueId().toString(),"gray", PhonePage.MAIN);
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

    public List<String> getContacts() {
        return contacts;
    }
    public void addContact(String uuid) {
        if (contacts.contains(uuid)) return;
        contacts.add(uuid);
        set("contacts",contacts);
    }
    public void removeContact(String uuid) {
        removeFavorite(uuid);
        contacts.remove(uuid);
        if (contacts.isEmpty()) set("contacts",null);
        else set("contacts",contacts);
    }

    public List<String> getFavorites() {
        return favorites;
    }
    public void addFavorite(String uuid) {
        if (favorites.contains(uuid)) return;
        favorites.add(uuid);
        set("favorites",favorites);
    }
    public void removeFavorite(String uuid) {
        favorites.remove(uuid);
        set("favorites",favorites);
        if (favorites.isEmpty()) set("favorites",null);
        else set("favorites",favorites);
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
        }
    }



}
