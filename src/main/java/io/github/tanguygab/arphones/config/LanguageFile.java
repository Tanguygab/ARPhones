package io.github.tanguygab.arphones.config;

import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LanguageFile extends YamlConfigurationFile {

    public LanguageFile(InputStream source, File destination) throws IOException {
        super(source, destination);
    }

    public String get(String str, String def) {
        return Utils.colors(getString(str,def));
    }

    public List<String> get(String str, List<String> def) {
        List<String> list = getStringList(str,def);
        list.forEach(e->list.set(list.indexOf(e),Utils.colors(e)));
        return list;
    }
    public String getMenuTitle() {
        return get("phone-menu.title","Your Phone");
    }

    public String getBatteryName() {
        return get("phone-menu.battery.name","Battery");
    }
    public List<String> getBatteryLore(int battery) {
        List<String> list = get("phone-menu.battery.lore",new ArrayList<>(List.of("","Your battery: %battery%%")));
        list.forEach(e->list.set(list.indexOf(e),e.replace("%battery%",battery+"")));
        return list;
    }
    public String getConnectionName() {
        return get("phone-menu.connection.name","Connection");
    }
    public List<String> getConnectionLore(int connection) {
        List<String> list = get("phone-menu.connection.lore",new ArrayList<>(List.of("","Your Connection: %connection%")));
        String co = "";
        for (int i = 0; i < 5; i++) {
            co+=(i < connection ? ChatColor.GREEN : ChatColor.DARK_GRAY)+"|";
        }
        String finalCo = co;
        list.forEach(e->list.set(list.indexOf(e),e.replace("%connection%", finalCo)));
        return list;
    }

    public String getContactsName() {
        return get("phone-menu.contacts.name","Your Contacts");
    }
    public List<String> getContactsLore() {
        return get("phone-menu.contacts.lore",new ArrayList<>(List.of("","Click to see your contacts")));
    }
    public String getBackgroundColorName() {
        return get("phone-menu.background-color.name","Background Color");
    }
    public List<String> getBackgroundColorLore(String backgroundColor) {
        List<String> list = get("phone-menu.background-color.lore",new ArrayList<>(List.of("","Current color: %color%","","Click to change!")));
        list.forEach(e->list.set(list.indexOf(e),e.replace("%color%",backgroundColor)));
        return list;
    }
    public String getLockName() {
        return get("phone-menu.lock.name","Lock Screen");
    }
    public List<String> getLockLore() {
        return get("phone-menu.lock.lore",new ArrayList<>(List.of("","Click to edit your Lock screen!")));
    }
    public String getOwnerName() {
        return get("phone-menu.owner.name","Phone Owner");
    }
    public List<String> getOwnerLore(String owner) {
        List<String> list = get("phone-menu.owner.lore",new ArrayList<>(List.of("","This phone belongs to %owner%","","Click to change!")));
        list.forEach(e->list.set(list.indexOf(e),e.replace("%owner%",owner == null ? "Invalid user" : owner)));
        return list;
    }

    public String getChangePinMenuTitle() {
        return get("change-pin-menu.title","Your Phone's PIN");
    }

    public List<String> getPinLore() {
        return get("change-pin-menu.pin",new ArrayList<>(List.of("","Left-Click to add 1","Right-Click to remove 1")));
    }

    public String getListTitle(boolean isContacts) {
        String path = isContacts ? "contact.title" : "players.title";
        String def = isContacts ? "Your Contacts" : "Add a Contact";
        return get("contacts-menu."+path,def);
    }
    public String getBackButton() {
        return get("contacts-menu.back-button","&4Back");
    }
    public String getListAdd() {
        return get("contacts-menu.contact.add","Add a Contact");
    }
    public List<String> getListContactLore() {
        return get("contacts-menu.contact.contact-lore",List.of("","Left-Click to see profile","Right-Click to add favorite","Drop to remove contact"));
    }
    public String getListFavoriteName(String name) {
        return get("contacts-menu.contact.favorite-name","&6✪ %player% ✪").replace("%player%",name);
    }
    public List<String> getListFavoriteLore() {
        return get("contacts-menu.contact.favorite-lore",List.of("","Left-Click to see profile","Right-Click to remove favorite","Drop to remove contact"));
    }

    public List<String> getListPlayerLore() {
        return get("contacts-menu.players.player-lore",List.of("","Left-Click to see profile","Right-Click to add contact"));
    }
    public String getServerName() {
        return get("msgs-calls.server-name","ARandomServer");
    }
    public String getNotificationMsg(String path, String def, String p, String msg, boolean server) {
        String str = get("msgs-calls.notifications."+path,def).replace("%player%",p).replace("%msg%",msg);
        if (server) str = str.replace("%server%",getServerName());
        return str;
    }
    public String getNotified(OfflinePlayer p) {
        return get("msgs-calls.notifications.ingame.notified","%player% was notified!").replace("%player%",p.getName());
    }
    public String getIngameMessage(Player p,String msg) {
        return getNotificationMsg("ingame.msg","%player% sent you a phone message: %msg%", p.getName(), msg, false);
    }
    public String getIngameCall(Player p) {
        return getNotificationMsg("ingame.call","%player% is calling you on his phone! Join him on Discord!",p.getName(),"",true);
    }
    public String getDiscordMessage(Player p,String msg) {
        return getNotificationMsg("discord.msg","%player% sent you a phone message on %server%: %msg%",p.getName(),msg,true);
    }
    public String getDiscordCall(String p) {
        return getNotificationMsg("discord.call","%player% is calling you on his phone on %server%!",p,"",true);
    }
    public String getCantCallSelf() {
        return get("msgs-calls.notifications.ingame.cant-call-self","You can't call yourself!");
    }

    public String getDiscordCantNotifify(boolean discordExists) {
        if (discordExists) return get("msgs-calls.notifications.discord.cant-notify.mc-but-discord","This player isn't online, but has been notified on Discord!");
        return get("msgs-calls.notifications.discord.cant-notify.mc-and-discord","This player isn't online, and isn't linked to Discord. Ask them to do /discord link next time you see them!");
    }
    public String getCantNotififyMsg() {
        return get("msgs-calls.notifications.ingame.cant-notify.msg","This player isn't online, but your message was sent.");
    }
    public String getCantNotififyCall() {
        return get("msgs-calls.notifications.ingame.cant-notify.call","This player isn't online.");
    }
    public String getDiscordNotificationDisabled() {
        return get("msg-calls.discord.cant-notify.notifications-disabled","This player isn't online and has Discord notification disabled!");
    }

    public String getContactInfoTitle(String name) {
        return get("contact-info.title","%player%'s profile").replace("%player%",name);
    }
    public String getContactInfoCall(String name) {
        return get("contact-info.call","Call %player%").replace("%player%",name);
    }
    public String getContactInfoIsInCall(String name) {
        return get("contact-info.is-in-call","%player% is already in a call!").replace("%player%",name);
    }
    public String getContactInfoMsg(String name) {
        return get("contact-info.msg","Message %player%").replace("%player%",name);
    }

    public String getDiscordDisabled() {
        return get("discord.feature-disabled","This feature isn't enabled!");
    }
    public String getNotLinked() {
        return get("discord.not-linked","You are not linked to Discord! Do /discord link");
    }
    public String getReceiverNotLinked() {
        return get("discord.receiver-not-linked","This player isn't linked to Discord! Ask them to do /discord link next time you see them!");
    }
    public String getNoCallsCategory() {
        return get("discord.no-calls-category","No calls category were found! This is a misconfiguration error, contact an admin!");
    }
    public String isInCall(String name) {
        return get("discord.is-in-call","%player% is already in a call!").replace("%player%",name);
    }

    public String getNotificationsOff() {
        return get("discord.notifications.disabled","You won't get Discord notifications anymore!");
    }
    public String getNotificationsOn() {
        return get("discord.notifications.enabled","You will now get Discord notifications!");
    }
}
