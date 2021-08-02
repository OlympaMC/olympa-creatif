package fr.olympa.olympacreatif.commands;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.perks.KitsManager;
import fr.olympa.olympacreatif.perks.UpgradesManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;

public class WebShop extends OlympaCommand {


    public WebShop(OlympaCreatifMain plugin) {
        super(plugin, "webshop", OcPermissions.WEBSHOP);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp())
            return false;

        OlympaPlayerCreatif pc = AccountProviderAPI.getter().get(Objects.requireNonNull(Bukkit.getPlayer(args[0])).getUniqueId());

        BuyType type = BuyType.getBuyType(args[1]);

        switch (type){

            case GROUP:
                switch(args[2]) {
                    case "constructeur":
                        pc.addGroup(OlympaGroup.CREA_CONSTRUCTOR);
                        type.sendMsg((Player) pc.getPlayer(), OlympaGroup.CREA_CONSTRUCTOR.getName(pc.getGender()));
                        break;
                    case "architecte":
                        pc.addGroup(OlympaGroup.CREA_ARCHITECT);
                        type.sendMsg((Player) pc.getPlayer(), OlympaGroup.CREA_ARCHITECT.getName(pc.getGender()));
                        break;
                    case "createur":
                        pc.addGroup(OlympaGroup.CREA_CREATOR);
                        type.sendMsg((Player) pc.getPlayer(), OlympaGroup.CREA_CREATOR.getName(pc.getGender()));
                        break;
                }
                break;

            case KIT:
                switch (args[2]){
                    case "fluids":
                        pc.addKit(KitsManager.KitType.FLUIDS);
                        type.sendMsg((Player) pc.getPlayer(), "§6fuides");
                        break;
                    case "redstone":
                        pc.addKit(KitsManager.KitType.REDSTONE);
                        type.sendMsg((Player) pc.getPlayer(), "§6redstone");
                        break;
                    case "commandblocks":
                        pc.addKit(KitsManager.KitType.COMMANDBLOCK);
                        type.sendMsg((Player) pc.getPlayer(), "§6commandblocks");
                        break;
                    case "peaceful_mobs":
                        pc.addKit(KitsManager.KitType.PEACEFUL_MOBS);
                        type.sendMsg((Player) pc.getPlayer(), "§6animaux");
                        break;
                    case "hostile_mobs":
                        pc.addKit(KitsManager.KitType.HOSTILE_MOBS);
                        type.sendMsg((Player) pc.getPlayer(), "§6monstres");
                        break;
                }
                break;

            case UPGRADE:
                switch (args[2]){
                    case "commandblocks":
                        pc.incrementUpgradeLevel(UpgradesManager.UpgradeType.CB_LEVEL, 1);
                        type.sendMsg((Player) pc.getPlayer(), "§6vitesse de régénération des tickets commandblocks");
                        break;
                    case "plots":
                        pc.incrementUpgradeLevel(UpgradesManager.UpgradeType.BONUS_PLOTS_LEVEL, 1);
                        type.sendMsg((Player) pc.getPlayer(), "§6nombre de parcelles");
                        break;
                    case "members":
                        pc.incrementUpgradeLevel(UpgradesManager.UpgradeType.BONUS_MEMBERS_LEVEL, 1);
                        type.sendMsg((Player) pc.getPlayer(), "§6nombre de membres par parcelle");
                        break;
                }
                break;

        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }
}

enum BuyType {
    GROUP("group", "§bMerci pour votre achat du grade §6%s§b, profitez bien de votre achat :D"),
    KIT("kit", "§bMerci pour votre achat du kit %s§b, profitez bien de votre achat :D"),
    UPGRADE("upgrade", "§bMerci pour votre achat de l'amélioration %s§b, profitez bien de votre achat :D"),
    ;

    private String key;
    private String msg;

    BuyType(String key, String msg){
        this.key = key;
        this.msg = msg;
    }

    public static BuyType getBuyType(String val){
        for (BuyType t : BuyType.values()){
            if (t.key.equals(val))
                return t;
        }
        return null;
    }

    public void sendMsg(Player p, String arg){
        Prefix.DEFAULT_GOOD.sendMessage(p, msg, arg);
    }
}








