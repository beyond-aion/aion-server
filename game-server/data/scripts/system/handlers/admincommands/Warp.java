package admincommands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Source
 */
public class Warp extends AdminCommand {

	public Warp() {
		super("warp");
	}

	@Override
	public void execute(Player player, String... params) {
		String locS, first, last;
		float xF, yF, zF;
		locS = "";
		int mapL, unk = 0, layerI = -1;
		
		if(params.length < 5) {
			onFail(player, "");
			return;
		}

		first = params[0];
		mapL = Integer.parseInt(params[1]);
		xF = Float.parseFloat(params[2]);
		yF = Float.parseFloat(params[3]);
		zF = Float.parseFloat(params[4]);
		last = params[5];

		Pattern f = Pattern.compile("\\[pos:([^;]+);\\s*+(\\d{9})");
		Pattern l = Pattern.compile("(\\d)\\]");
		Matcher fm = f.matcher(first);
		Matcher lm = l.matcher(last);

		if (fm.find()) {
			locS = fm.group(1);
			unk = Integer.parseInt(fm.group(2));
		}
		if (lm.find())
			layerI = Integer.parseInt(lm.group(1));

		zF = GeoService.getInstance().getZ(mapL, xF, yF);
		PacketSendUtility.sendMessage(player, "MapId (" + mapL + ") unk:" + unk +"\n" + "x:" + xF + " y:" + yF + " z:" + zF + " l("
			+ layerI + ")");

		if (mapL == 400010000)
			PacketSendUtility.sendMessage(player, "Sorry you can't warp at abyss");
		else {
			TeleportService2.teleportTo(player, mapL, xF, yF, zF);
			PacketSendUtility.sendMessage(player, "You have successfully warp -> " + locS);
		}
	}

	@Override
	public void onFail(Player player, String message) {
		if (!GeoDataConfig.GEO_ENABLE) {
			PacketSendUtility.sendMessage(player, "You must turn on geo in config to use this command!");
			return;
		}
		PacketSendUtility.sendMessage(player, "syntax //warp <@link>");
	}

}