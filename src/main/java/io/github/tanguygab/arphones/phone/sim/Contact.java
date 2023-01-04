package io.github.tanguygab.arphones.phone.sim;

import io.github.tanguygab.arphones.ARPhones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Contact {

    private final UUID uuid;
    private final UUID simUUID;
    private boolean isFavorite;
    private final List<String> notes;

    public Contact(UUID simUUID, UUID uuid, boolean isFavorite, List<String> notes) {
        this.simUUID = simUUID;
        this.uuid = uuid;
        this.isFavorite = isFavorite;
        this.notes = notes;
    }

    public Contact(UUID simUUID, UUID uuid) {
        this(simUUID, uuid,false,new ArrayList<>());
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    private void set(String key, Object value) {
        ARPhones.get().dataFile.set("sims."+simUUID+".contacts."+uuid+"."+key,value);
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
        set("favorite",favorite);
    }

    public List<String> getNotes() {
        return notes;
    }
    public void setNote(int pos, String note) {
        notes.addAll(Collections.nCopies(pos, ""));
        if (notes.size() > pos) notes.set(pos,note);
        else notes.add(note);
        set("notes",notes);
    }
}
