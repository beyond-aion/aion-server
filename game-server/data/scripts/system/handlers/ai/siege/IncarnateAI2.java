package ai.siege;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Source
 */
@AIName("incarnate")
public class IncarnateAI2 extends SiegeNpcAI2 {

	Future<?> avatar_scan;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		avatar_scan = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (SiegeConfig.SIEGE_IDA_ENABLED) {
					getOwner().getKnownList().doOnAllPlayers(new Visitor<Player>() {

						@Override
						public void visit(Player player) {
							if (player.getAbyssRank().getRank().getId() > AbyssRankEnum.STAR4_OFFICER.getId()) {
								boolean inform = false;
								EffectController controller = player.getEffectController();
								for (Effect eff : controller.getAbnormalEffects()) {
									if (eff.isDeityAvatar()) {
										eff.endEffect();
										getOwner().getEffectController().clearEffect(eff);
										inform = true;
									}
								}

								if (inform) {
									String message = "The power of incarnation removes " + player.getName() + " morph state.";
									PacketSendUtility.broadcastPacket(getOwner(), new SM_MESSAGE(getObjectId(), getOwner().getName(), message,
										ChatType.BRIGHT_YELLOW_CENTER));
								}
							}
						}

					});
				}
			}

		}, 10000, 10000);
	}

	// spawn for quest
	@Override
	protected void handleDied() {
		super.handleDied();
		int npc = getOwner().getNpcId();
		switch (npc) {
			case 259614:
				spawn(701237, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
				despawnClaw();
				break;
		}
	}

	private void despawnClaw() {
		final Npc claw = getPosition().getWorldMapInstance().getNpc(701237);
		com.aionemu.commons.network.util.ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				claw.getController().onDelete();
			}
		}, 60000 * 5);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		avatar_scan.cancel(true);
	}

}
