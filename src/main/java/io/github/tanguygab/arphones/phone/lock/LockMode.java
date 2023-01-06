package io.github.tanguygab.arphones.phone.lock;

import org.bukkit.Material;

public enum LockMode {

    NONE(Material.BARRIER,null),
    PIN(Material.COMMAND_BLOCK, "0000"),
    PASSWORD(Material.OAK_SIGN, "password");

    private final Material material;
    private final String defaultKey;

    LockMode(Material material, String defaultKey) {
        this.material = material;
        this.defaultKey = defaultKey;
    }

    public Material getMaterial() {
        return material;
    }

    public static LockMode get(String mode) {
        try {return valueOf(mode);}
        catch (IllegalArgumentException e) {return NONE;}
    }

    public String getDefaultKey() {
        return defaultKey;
    }
}
