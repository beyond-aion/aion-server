package instance;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu, Luzien
 * @see <a href="http://gameguide.na.aiononline.com/aion/Padmarashka%27s+Cave+Walkthrough">Padmarashka's Cave</a>
 */
@InstanceID(320150000)
public class PadmarashkasCaveInstance extends GeneralInstanceHandler {

	private final AtomicBoolean moviePlayed = new AtomicBoolean();
	private final AtomicInteger killedPadmarashkaProtector = new AtomicInteger();
	private final AtomicInteger killedEggs = new AtomicInteger();

	public PadmarashkasCaveInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onDie(Npc npc) {
		final int npcId = npc.getNpcId();
		switch (npcId) {
			case 218670:
			case 218671:
			case 218673:
			case 218674:
				if (killedPadmarashkaProtector.incrementAndGet() == 4) {
					killedPadmarashkaProtector.set(0);
					final Npc padmarashka = getNpc(218756);
					if (padmarashka != null && !padmarashka.isDead()) {
						padmarashka.getEffectController().unsetAbnormal(AbnormalState.SLEEP);
						// padmarashka.getEffectController().broadCastEffects(0);
						SkillEngine.getInstance().getSkill(padmarashka, 19187, 55, padmarashka).useNoAnimationSkill();
						padmarashka.getEffectController().removeEffect(19186); // skill should handle this TODO: fix
						ThreadPoolManager.getInstance()
							.schedule(() -> padmarashka.getAi().onCreatureEvent(AIEventType.CREATURE_AGGRO, instance.getPlayersInside().get(0)), 1000);
					}
				}
				break;
			case 282613:
			case 282614:
				if (killedEggs.incrementAndGet() == 20) { // TODO: find value
					final Npc padmarashka = getNpc(218756);
					if (padmarashka != null && !padmarashka.isDead()) {
						SkillEngine.getInstance().applyEffectDirectly(20101, padmarashka, padmarashka);
					}
				}
				break;
		}
	}

	@Override
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (zone.getAreaTemplate().getZoneName() == ZoneName.get("PADMARASHKAS_NEST_320150000") && moviePlayed.compareAndSet(false, true))
			PacketSendUtility.broadcastToMap(instance, new SM_PLAY_MOVIE(false, 0, 0, 488, true));
	}

	@Override
	public void onInstanceDestroy() {
		moviePlayed.set(false);
		killedPadmarashkaProtector.set(0);
	}
}
