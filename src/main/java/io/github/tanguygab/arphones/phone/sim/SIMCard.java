package io.github.tanguygab.arphones.phone.sim;

import io.github.tanguygab.arphones.ARPhones;

import java.util.*;

public class SIMCard {

    private final UUID uuid;

    private final Map<UUID,Contact> contacts;

    public SIMCard(UUID uuid, Map<UUID,Contact> contacts) {
        this.uuid = uuid;
        this.contacts = contacts;
    }
    public SIMCard(UUID uuid) {
        this(uuid,new HashMap<>());
    }

    private void save() {
        if (contacts.isEmpty()) {
            ARPhones.get().dataFile.set("sims."+uuid+".contacts",null);
            return;
        }
        Map<String,Map<String,Object>> map = new HashMap<>();
        contacts.forEach((uuid,contact)->{
            Map<String,Object> contactMap = new HashMap<>();
            contactMap.put("favorite",contact.isFavorite());
            contactMap.put("notes",contact.getNotes());
            map.put(uuid.toString(),contactMap);
        });

        ARPhones.get().dataFile.set("sims."+uuid+".contacts",map);
    }

    public UUID getUUID() {
        return uuid;
    }
    public List<Contact> getContacts() {
        return contacts.values().stream().toList();
    }
    public Contact getContact(UUID uuid) {
        return contacts.get(uuid);
    }
    public void addContact(UUID uuid) {
        if (contacts.containsKey(uuid)) return;
        contacts.put(uuid,new Contact(this.uuid,uuid));
        save();
    }

    public void removeContact(UUID uuid) {
        contacts.remove(uuid);
        if (contacts.isEmpty()) save();
        else save();
    }

}
