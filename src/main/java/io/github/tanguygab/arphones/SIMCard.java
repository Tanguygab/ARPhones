package io.github.tanguygab.arphones;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SIMCard {

    private final UUID uuid;

    private final List<String> contacts;
    private final List<String> favorites;

    public SIMCard(UUID uuid, List<String> contacts, List<String> favorites) {
        this.uuid = uuid;
        this.contacts = contacts;
        this.favorites = favorites;
    }
    public SIMCard(UUID uuid) {
        this(uuid,new ArrayList<>(),new ArrayList<>());
    }

    private void set(String path, Object value) {
        ARPhones.get().dataFile.set("sims."+uuid+"."+path,value);
    }

    public UUID getUUID() {
        return uuid;
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
}
