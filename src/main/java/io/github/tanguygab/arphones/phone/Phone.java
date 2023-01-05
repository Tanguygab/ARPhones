package io.github.tanguygab.arphones.phone;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.menus.lockscreen.LockScreenInfoMenu;
import io.github.tanguygab.arphones.menus.lockscreen.PinMenu;
import io.github.tanguygab.arphones.phone.lock.LockSystem;
import io.github.tanguygab.arphones.phone.sim.Contact;
import io.github.tanguygab.arphones.phone.sim.SIMCard;
import io.github.tanguygab.arphones.menus.*;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Phone {

    private final UUID uuid;
    private LockSystem lockSystem;
    private String owner;
    private SIMCard sim;

    private String backgroundColor;
    private int battery;
    private PhonePage page;
    private String contactPage = null;

    // keycard hook
    private final List<ItemStack> keycards;

    public Phone(UUID uuid, LockSystem lockSystem, SIMCard sim, int battery, String owner, String backgroundColor, PhonePage page, List<ItemStack> keycards) {
        this.uuid = uuid;
        this.lockSystem = lockSystem;
        this.sim = sim;
        this.battery = battery;
        this.owner = owner;
        this.backgroundColor = backgroundColor;
        if (page == null) setPage(PhonePage.MAIN);
        else this.page = page;
        this.keycards = keycards;
    }
    public Phone(UUID uuid, Player p) {
        this(uuid,new LockSystem(),null,100,p.getUniqueId().toString(),"gray", PhonePage.MAIN,new ArrayList<>());
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

    public void setPage(PhonePage page) {
        this.page = page;
        set("page",page.toString());
    }
    public void setContactPage(String contact) {
        contactPage = contact;
        set("contact-page",contact);
    }

    public LockSystem getLockSystem() {
        return lockSystem;
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


    public void openLastMenu(Player p) {
        open(p,page,contactPage == null ? null : getSim().getContact(UUID.fromString(contactPage)));
    }
    public void open(Player p, PhonePage page) {
        open(p,page,null);
    }
    public void open(Player p, PhonePage page, Contact contact) {
        PhoneMenu menu = switch (page) {
            case MAIN -> new MainPhoneMenu(p,this);
            case CONTACTS,PLAYERS -> {
                if (sim == null) {
                    p.sendMessage("You need a SIM card for this!");
                    yield null;
                }
                yield new ListPhoneMenu(p,this,page == PhonePage.CONTACTS);
            }
            case CONTACT_INFO -> {
                if (contact == null) {
                    open(p,PhonePage.MAIN);
                    yield null;
                }
                setContactPage(contact.getUUID().toString());
                yield new ContactInfoMenu(p,this,contact);
            }
            case LOCK_SCREEN -> new PinMenu(p,this);
            case LOCK_SCREEN_INFO -> new LockScreenInfoMenu(p,this);
            case PIN -> null;
            case KEYCARDS -> {
                if (!Bukkit.getServer().getPluginManager().isPluginEnabled("KeyCard")) {
                    p.sendMessage("The KeyCard plugin isn't loaded!");
                    yield null;
                }
                yield new KeyCardMenu(p,this);
            }
        };
        if (menu == null) return;
        ARPhones.get().openedMenus.put(p,menu);
        setPage(page);
        menu.onOpen();
    }
}
