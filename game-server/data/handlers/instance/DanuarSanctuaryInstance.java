package instance;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Ritsu, Tibald
 */
@InstanceID(301380000)
public class DanuarSanctuaryInstance extends GeneralInstanceHandler {

	private final AtomicBoolean cannonUsed = new AtomicBoolean(false);

	public DanuarSanctuaryInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		spawnRndBoss();
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 233187:
				spawn(233087, 906.4945f, 861.5854f, 280.5441f, (byte) 73, 1699);
				npc.getController().delete();
				break;
			case 235624:
			case 235625:
			case 235626:
				spawn(701876, 1071.9772f, 682.4911f, 282.0391f, (byte) 60); // Emergency Exit
				break;
			case 730866:
			case 233448:
			case 233447:
				npc.getController().delete();
				break;

		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 701873:
				TeleportService.teleportTo(player, instance, 1029.273f, 362.651f, 297.89f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
				break;
			case 701871:
				TeleportService.teleportTo(player, instance, 1006.0412f, 1366.468f, 337.26f, (byte) 105, TeleportAnimation.FADE_OUT_BEAM);
				break;
			case 701872:
				TeleportService.teleportTo(player, instance, 846.172f, 991.731f, 300.04f, (byte) 110, TeleportAnimation.FADE_OUT_BEAM);
				break;
			case 730863:
				if (cannonUsed.compareAndSet(false, true)) {
					Npc target = instance.getNpc(730866);
					SkillEngine.getInstance().getSkill(npc, 20385, 1, target).useWithoutPropSkill();
					if (target != null) {
						ThreadPoolManager.getInstance().schedule(() -> target.getController().die(), 5700);
					}
				}
				break;
			case 701876:
				TeleportService.moveToInstanceExit(player, mapId, player.getRace());
				break;

		}
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, true, 0);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		player.getGameStats().updateStatsAndSpeedVisually();
		TeleportService.teleportTo(player, instance, 388.6437f, 1184.639f, 55.30134f);
		return true;
	}

	private void spawnRndBoss() {
		/*
		 * 235624 Warmage Suyaroka
		 * 235625 Chief Medic Tagnu
		 * 235626 Virulent Ukahim
		 */
		spawn((235624 + Rnd.get(0, 2)), 1056.6105f, 694.0836f, 282.04f, (byte) 30);
	}
}
