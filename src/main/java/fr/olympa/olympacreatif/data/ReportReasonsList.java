package fr.olympa.olympacreatif.data;

import org.bukkit.Material;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.report.ReportReasonItem;

public class ReportReasonsList {

	public static final ReportReasonItem BAD_BUILD = new ReportReasonItem(10, "Construction incorrecte (signe nazi, ...)");
	public static final ReportReasonItem BAD_SYSTEM = new ReportReasonItem(11, "Parcelle causant des lags (redstone ou commandblocks)");
	public static final ReportReasonItem BAD_ENTITIES = new ReportReasonItem(12, "Parcelle causant des lags (entités)");
	public static final ReportReasonItem BAD_EXPERIENCE = new ReportReasonItem(13, "Mauvaise utilisation commandblocks (tp infini, spam chat, ...)");
	public static final ReportReasonItem BAD_OTHER = new ReportReasonItem(14, "Autre raison pour laquelle la parcelle vous semble inappropriée");
			
	static {
		BAD_BUILD.setItem(new OlympaItemBuild(ItemUtils.item(Material.PODZOL, "")));
		BAD_SYSTEM.setItem(new OlympaItemBuild(ItemUtils.item(Material.REDSTONE_TORCH, "")));
		BAD_ENTITIES.setItem(new OlympaItemBuild(ItemUtils.item(Material.BAT_SPAWN_EGG, "")));
		BAD_EXPERIENCE.setItem(new OlympaItemBuild(ItemUtils.item(Material.TNT, "")));
		BAD_OTHER.setItem(new OlympaItemBuild(ItemUtils.item(Material.RED_BANNER, "")));
	}
}
