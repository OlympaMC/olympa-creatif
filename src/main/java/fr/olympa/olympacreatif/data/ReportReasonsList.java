package fr.olympa.olympacreatif.data;

import org.bukkit.Material;

public class ReportReasonsList {

	public static final ReportReason BAD_BUILD = new ReportReasonItem(10, "Construction incorrecte (signe nazi, ...)");
	public static final ReportReason BAD_SYSTEM = new ReportReasonItem(11, "Parcelle causant des lags (redstone ou commandblocks)");
	public static final ReportReason BAD_ENTITIES = new ReportReasonItem(12, "Parcelle causant des lags (entités)");
	public static final ReportReason BAD_EXPERIENCE = new ReportReasonItem(13, "Mauvaise utilisation commandblocks (tp infini, spam chat, ...)");
	public static final ReportReason BAD_OTHER = new ReportReasonItem(14, "Autre raison pour laquelle la parcelle vous semble inappropriée");
		
	public static final ReportReason BAD_BUILD = new ReportReason(10, "Construction incorrecte (signe nazi, ...)");
	public static final ReportReason BAD_SYSTEM = new ReportReason(11, "Parcelle causant des lags (redstone ou commandblocks)");
	public static final ReportReason BAD_ENTITIES = new ReportReason(12, "Parcelle causant des lags (entités)");
	public static final ReportReason BAD_EXPERIENCE = new ReportReason(13, "Mauvaise utilisation commandblocks (tp infini, spam chat, ...)");
	public static final ReportReason BAD_OTHER = new ReportReason(14, "Autre raison pour laquelle la parcelle vous semble inappropriée");

	static {
		((ReportReasonItem)BAD_BUILD).setItem(new OlympaItemBuild(ItemUtils.item(Material.PODZOL, "")));
		((ReportReasonItem)BAD_SYSTEM).setItem(new OlympaItemBuild(ItemUtils.item(Material.REDSTONE_TORCH, "")));
		((ReportReasonItem)BAD_ENTITIES).setItem(new OlympaItemBuild(ItemUtils.item(Material.BAT_SPAWN_EGG, "")));
		((ReportReasonItem)BAD_EXPERIENCE).setItem(new OlympaItemBuild(ItemUtils.item(Material.TNT, "")));
		((ReportReasonItem)BAD_OTHER).setItem(new OlympaItemBuild(ItemUtils.item(Material.RED_BANNER, "")));
	}
}
