package fr.olympa.olympacreatif.data;

import org.bukkit.Material;

import fr.olympa.api.common.report.ReportReason;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.item.OlympaItemBuild;

public class ReportReasonsList {

	public static final ReportReason BAD_BUILD = new ReportReason(10, "Construction inappropriée");
	public static final ReportReason BAD_LAG = new ReportReason(11, "Parcelle causant des lags");
	public static final ReportReason BAD_EXPERIENCE = new ReportReason(12, "Mauvaise utilisation commandblocks (tp infini, spam chat, ...)");

	static {
		BAD_BUILD.setItem(new OlympaItemBuild(ItemUtils.item(Material.PODZOL, "§7Construction inappropriée", "§eApologie du terrorisme, signe nazi, ...")));
		BAD_LAG.setItem(new OlympaItemBuild(ItemUtils.item(Material.REDSTONE_LAMP, "§7Parcelle à lag", "§eUtilisation d'une technique quelconque", "§epour faire lag le serveur ou les", "§evisiteurs de la parcelle")));
		BAD_EXPERIENCE.setItem(new OlympaItemBuild(ItemUtils.item(Material.TNT, "§7Mauvaise expérience",
				"§eUtilisation des commandblocks pour affecter", "§enégativement et de manière délibérée l'expérience", "§edes visiteurs de la parcelle")));
	}

}
