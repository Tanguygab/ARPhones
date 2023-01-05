package io.github.tanguygab.arphones.phone.lock;

import org.bukkit.Material;

public enum UnlockMode {

    ALWAYS_LOCKED(Material.IRON_DOOR),
    LOCK_ON_DROP(Material.HOPPER),
    ALWAYS_UNLOCKED(Material.OAK_DOOR);

    private final Material material;

    UnlockMode(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public static UnlockMode get(String mode) {
        try {return valueOf(mode);}
        catch (IllegalArgumentException e) {return ALWAYS_LOCKED;}
    }
}
