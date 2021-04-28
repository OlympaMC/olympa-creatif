package fr.olympa.olympacreatif.data;

import org.bukkit.Material;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.report.ReportReason;

public class ReportReasonsList {

	public static final ReportReason BAD_BUILD = new ReportReason(10, "Construction inappropriée");
	//public static final ReportReason BAD_SYSTEM = new ReportReason(11, "Parcelle causant des lags (redstone ou commandblocks)");
	public static final ReportReason BAD_LAG = new ReportReason(11, "Parcelle causant des lags");
	public static final ReportReason BAD_EXPERIENCE = new ReportReason(12, "Mauvaise utilisation commandblocks (tp infini, spam chat, ...)");
	public static final ReportReason BAD_OTHER = new ReportReason(13, "Autre raison pour laquelle la parcelle mérite signalement");
	
	static {
		BAD_BUILD.setItem(new OlympaItemBuild(ItemUtils.item(Material.PODZOL, "§7Construction inappropriée", "§eApologie du terrorisme, signe nazi, ...")));
		//BAD_SYSTEM.setItem(new OlympaItemBuild(ItemUtils.item(Material.REDSTONE_TORCH, "")));
		BAD_LAG.setItem(new OlympaItemBuild(ItemUtils.item(Material.REDSTONE_LAMP, "§7Parcelle à lag", "§eUtilisation d'une technique quelconque", "§epour faire lag le serveur ou les", "§evisiteurs de la parcelle")));
		BAD_EXPERIENCE.setItem(new OlympaItemBuild(ItemUtils.item(Material.TNT, "§7Mauvaise expérience", "§eUtilisation des commandblocks pour affecter", "§enégativement et de manière délibérée l'expérience", "§edes visiteurs de la parcelle")));
		//BAD_OTHER.setItem(new OlympaItemBuild(ItemUtils.item(Material.RED_BANNER, "")));
	}

}
