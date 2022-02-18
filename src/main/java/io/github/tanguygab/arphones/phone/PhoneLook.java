package io.github.tanguygab.arphones.phone;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public enum PhoneLook {

    ORIGINAL(Material.OAK_DOOR,"Phone"),
    REINFORCED(Material.IRON_DOOR,"Reinforced Phone"),
    SPRUCE(Material.SPRUCE_DOOR,"Spruce Phone"),
    VANILLA(Material.BIRCH_DOOR,"Vanilla Phone"),
    JUNGLE(Material.JUNGLE_DOOR,"Jungle Phone"),
    THIN(Material.ACACIA_DOOR,"Thin Phone"),
    CHOCOLATE(Material.DARK_OAK_DOOR,"Chocolate Phone"),
    FIRE(Material.CRIMSON_DOOR,"Fire Phone"),
    WATER(Material.WARPED_DOOR,"Water Phone");

    private final Material material;
    private final String name;

    PhoneLook(Material material, String name) {
        this.material = material;
        this.name = name;
    }

    public Material getMaterial() {
        return material;
    }
    public String getName() {
        return name;
    }

    public static PhoneLook get(String look) {
        look = look.toUpperCase();
        for (PhoneLook phoneLook : values())
            if (phoneLook.toString().equals(look))
                return phoneLook;
        return ORIGINAL;
    }

    public static List<String> looks() {
        List<String> list = new ArrayList<>();
        for (PhoneLook look : values())
            list.add(look.toString().toLowerCase());
        return list;
    }
}
