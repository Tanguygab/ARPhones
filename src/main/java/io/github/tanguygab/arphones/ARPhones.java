package io.github.tanguygab.arphones;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.entities.VoiceChannel;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.util.DiscordUtil;
import io.github.tanguygab.arphones.config.ConfigurationFile;
import io.github.tanguygab.arphones.config.LanguageFile;
import io.github.tanguygab.arphones.config.YamlConfigurationFile;
import io.github.tanguygab.arphones.listener.BukkitListener;
import io.github.tanguygab.arphones.listener.DiscordListener;
import io.github.tanguygab.arphones.menus.PhoneMenu;
import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.phone.PhoneLook;
import io.github.tanguygab.arphones.phone.PhonePage;
import io.github.tanguygab.arphones.utils.PhoneUtils;
import io.github.tanguygab.arphones.utils.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public final class ARPhones extends JavaPlugin implements CommandExecutor {

    private static ARPhones instance;

    public ConfigurationFile configFile;
    public ConfigurationFile dataFile;
    public ConfigurationFile historyFile;
    public LanguageFile languageFile;
    public boolean discord;
    private DiscordListener discordListener;

    public Map<String, Phone> phones = new HashMap<>();
    public Map<String, SIMCard> sims = new HashMap<>();

    public Map<Player, Phone> changingOwners = new HashMap<>();
    public Map<Player, PhoneMenu> openedMenus = new HashMap<>();

    public static ARPhones get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            configFile = new YamlConfigurationFile(getResource("config.yml"), new File(getDataFolder(), "config.yml"));
            languageFile = new LanguageFile(getResource("language.yml"), new File(getDataFolder(), "language.yml"));

            File fileData = new File(getDataFolder(), "data.yml");
            if (!fileData.exists()) fileData.createNewFile();
            dataFile = new YamlConfigurationFile(null, fileData);
            File fileHistory = new File(getDataFolder(), "phones-history.yml");
            if (!fileHistory.exists()) fileHistory.createNewFile();
            historyFile = new YamlConfigurationFile(null, fileHistory);

            loadRecipes();
            getServer().getPluginManager().registerEvents(new BukkitListener(),this);
            if (getServer().getPluginManager().getPlugin("DiscordSRV") != null && configFile.getBoolean("discord-integration.enabled",true)) {
                DiscordSRV.api.subscribe(discordListener = new DiscordListener());
                if (DiscordUtil.getJda() != null) discordInit();
                discord = true;
            } else discord = false;

            Map<String,Map<String,Object>> simsMap = dataFile.getConfigurationSection("sims");
            System.out.println(simsMap);
            for (String el : simsMap.keySet()) {
                Map<String,Object> map = simsMap.get(el);
                UUID uuid = UUID.fromString(el);

                System.out.println(el);

                List<String> contactsStr = map.containsKey("contacts") ? (List<String>) map.get("contacts") : new ArrayList<>();
                List<String> favoritesStr = map.containsKey("favorites") ? (List<String>) map.get("favorites") : new ArrayList<>();

                Utils.removeInvalidPlayers(contactsStr);
                Utils.removeInvalidPlayers(favoritesStr);

                SIMCard sim = new SIMCard(uuid,contactsStr,favoritesStr);
                ARPhones.get().sims.put(el,sim);
            }

            Map<String,Map<String,Object>> phonesMap = dataFile.getConfigurationSection("phones");
            for (String el : phonesMap.keySet()) {
                Map<String,Object> map = phonesMap.get(el);
                UUID uuid = UUID.fromString(el);
                String pin = map.get("pin")+"";
                String sim = map.get("sim")+"";


                int battery = (int) map.get("battery");
                String owner = map.get("owner")+"";
                String backgroundColor = map.get("background-color")+"";
                String page = map.get("page")+"";

                Phone phone = new Phone(uuid,pin,sims.get(sim),battery,owner,backgroundColor,PhonePage.pageFromStr(page));
                ARPhones.get().phones.put(el,phone);
                phone.setContactPage(map.get("page-contact")+"");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void discordInit() {
        DiscordUtil.getJda().addEventListener(discordListener);

        Map<String, List<String>> channels = ARPhones.get().dataFile.getConfigurationSection("voice-channels");
        List<String> removed = new ArrayList<>();
        channels.forEach((id,members)->{
            VoiceChannel c = DiscordUtil.getJda().getVoiceChannelById(id);
            if (c != null && c.getMembers().isEmpty()) {
                c.delete().queue();
                removed.add(id);
            } else if (c == null) removed.add(id);
        });
        removed.forEach(channels::remove);
        ARPhones.get().dataFile.set("voice-channels",channels.isEmpty() ? null : channels);
    }

    public void loadRecipes() {
        for (PhoneLook look : PhoneLook.values()) addRecipe("phone-"+look.toString().toLowerCase(),"phone.craft",Utils.getPhone(look),look.getMaterial());
        addRecipe("sim","sim.craft",Utils.getSIM(null,false),null);
        addRecipe("charger","battery.charger-craft",Utils.getPhone(PhoneLook.ORIGINAL),null);
    }
    public void addRecipe(String name, String config, ItemStack result, Material phoneLook) {
        NamespacedKey key = new NamespacedKey(this, name);
        if (configFile.getBoolean(config+".enabled",true)) {
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape(configFile.getStringList(config+".grid").toArray(new String[]{}));
            configFile.getConfigurationSection(config+".items").forEach((l,mat)->{
                Material material = "PHONE_LOOK".equalsIgnoreCase(mat+"") ? phoneLook : Material.getMaterial(mat+"");
                if (material == null) return;
                recipe.setIngredient((l+"").charAt(0),material);
            });
            if (getServer().getRecipe(key) != null) getServer().removeRecipe(key);
            getServer().addRecipe(recipe);
        } else if (getServer().getRecipe(key) != null) getServer().removeRecipe(key);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        DiscordSRV.api.unsubscribe(discordListener);
        DiscordUtil.getJda().removeEventListener(discordListener);
        phones.clear();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String arg = args.length == 0 ? "" : args[0];
        if (sender instanceof Player p) {
            switch (arg) {
                case "notifications" -> {
                    LanguageFile lang = Utils.msgs();
                    List<String> list = dataFile.getStringList("notifications-off", new ArrayList<>());
                    String uuid = p.getUniqueId().toString();
                    if (list.contains(uuid)) {
                        list.remove(uuid);
                        p.sendMessage(lang.getNotificationsOn());
                    }
                    else {
                        list.add(uuid);
                        p.sendMessage(lang.getNotificationsOff());
                    }
                    if (list.isEmpty()) list = null;
                    dataFile.set("notifications-off", list);
                    return true;
                }
                case "accept","deny" -> {
                    String callername = args[1];
                    OfflinePlayer player = Utils.getOfflinePlayer(callername);
                    if (player == null) {
                        p.sendMessage("This player doesn't exist anymore!");
                        return true;
                    }
                    AccountLinkManager accounts = DiscordSRV.getPlugin().getAccountLinkManager();
                    String callerID = accounts.getDiscordId(player.getUniqueId());
                    if (!PhoneUtils.waitingForCall.containsKey(callerID)) {
                        p.sendMessage("This player isn't calling you!");
                        return true;
                    }
                    User caller = DiscordUtil.getUserById(callerID);
                    String calledID = accounts.getDiscordId(p.getUniqueId());
                    User called = DiscordUtil.getUserById(calledID);
                    PhoneUtils.waitingForCall.remove(callerID);
                    String msg;
                    if (arg.equals("accept")) {
                        msg = " accepted your call.";
                        PhoneUtils.createVoiceChannel(PhoneUtils.getCallsCategory(),caller,called);
                        PhoneUtils.saveHistory(null,player.getUniqueId().toString(),p.getUniqueId().toString());
                    } else msg = " denied your call";

                    if (PhoneUtils.isOnline(player)) player.getPlayer().sendMessage(sender.getName()+msg);
                    else PhoneUtils.sendMsg(caller,called.getAsMention()+msg);

                    if (arg.equals("accept")) p.sendMessage("You accepted "+player.getName()+"'s call!");
                    else p.sendMessage("You denied "+player.getName()+"'s call!");

                    return true;
                }
            }
        }
        if (!sender.hasPermission("arphones.use")) {
            return false;
        }
        switch (arg) {
            case "reload" -> {
                onDisable();
                onEnable();
                sender.sendMessage("Plugin reloaded!");
            }
            case "give" -> {
                if (args.length < 2) {
                    sender.sendMessage("You have to provide an item!");
                    return true;
                }
                Player p;
                if (args.length > 2) {
                    String player = args[2];
                    p = getServer().getPlayer(player);
                    if (p == null) {
                        sender.sendMessage("This player isn't online!");
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player s)){
                        sender.sendMessage("You have to be a player to do that!");
                        return true;
                    }
                    p = s;
                }
                ItemStack item;
                switch (args[1].toLowerCase()) {
                    case "phone" -> {
                        String look = args.length > 3 ? args[3] : "ORIGINAL";
                        item = Utils.getPhone(PhoneLook.get(look));
                    }
                    case "sim" -> item = Utils.getSIM(UUID.randomUUID(),true);
                    default -> {
                        sender.sendMessage("Invalid item type!");
                        return true;
                    }
                }
                p.getInventory().addItem(item);

            }
            default -> sender.sendMessage(Utils.colors(
                    "&m                                        \n"
                    + "&a[ARPhones] &7" + getDescription().getVersion() + "\n"
                    + " - &3/arphones\n"
                    + "   &8| &aDefault help page\n"
                    + " - &3/arphones give phone|sim|charger [player] [phonelook]\n"
                    + "   &8| &aGive yourself or a player a phone, sim card or charger\n"
                    + " - &3/arphones reload\n"
                    + "   &8| &aReload the plugin\n"
                    + "&m                                        "));
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("reload","give","notifications","accept","deny");
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) return List.of("phone","sim","charger");
        if (args.length == 4 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("phone")) return PhoneLook.looks();
        return null;
    }
}
