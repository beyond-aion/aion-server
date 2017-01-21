package ai.instance.tallocsHollow;

import java.util.function.Consumer;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.SummonerAI;

/**
 * @author xTz
 */
@AIName("queenmosqua")
public class QueenMosquaAI extends SummonerAI {

	private boolean isHome = true;

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (isHome) {
			isHome = false;
			getPosition().getWorldMapInstance().getDoors().get(7).setOpen(false);
		}
	}

	@Override
	protected void handleBackHome() {
		isHome = true;
		getPosition().getWorldMapInstance().getDoors().get(7).setOpen(true);
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		getPosition().getWorldMapInstance().getDoors().get(7).setOpen(true);

		Npc npc = instance.getNpc(700738);
		if (npc != null) {
			SpawnTemplate template = npc.getSpawn();
			spawn(700739, template.getX(), template.getY(), template.getZ(), template.getHeading(), 11);
			npc.getKnownList().forEachPlayer(new Consumer<Player>() {

				@Override
				public void accept(Player player) {
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400476));
					Summon summon = player.getSummon();
					if (summon != null) {
						if (summon.getNpcId() == 799500 || summon.getNpcId() == 799501) {
							SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.UNSPECIFIED);
							PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 435));
						}
					}
				}
			});
			npc.getController().delete();
		}
	}

}
