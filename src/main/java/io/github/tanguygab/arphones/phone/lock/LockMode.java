package io.github.tanguygab.arphones.phone.lock;

import org.bukkit.Material;

public enum LockMode {

    NONE(Material.BARRIER),
    PIN(Material.COMMAND_BLOCK),
    PASSWORD(Material.OAK_SIGN);

    private final Material material;

    LockMode(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public static LockMode get(String mode) {
        try {return valueOf(mode);}
        catch (IllegalArgumentException e) {return NONE;}
    }
}
