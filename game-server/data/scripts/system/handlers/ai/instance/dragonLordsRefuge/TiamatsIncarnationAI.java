package ai.instance.dragonLordsRefuge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl March 9th, 2018
 */
@AIName("tiamats_incarnation")
public class TiamatsIncarnationAI extends AggressiveNpcAI {

	public TiamatsIncarnationAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleActivate() {
		super.handleActivate();
		scheduleSummons(20000);
	}

	private void scheduleSummons(int delay) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead() && getTarget() != null) {
				List<Player> nearbyPlayers = getNearbyPlayers();
				if (nearbyPlayers.size() > 1) {
					Player first = nearbyPlayers.remove(Rnd.get(nearbyPlayers.size()));
					Player second = nearbyPlayers.remove(Rnd.get(nearbyPlayers.size()));
					int summonId = Rnd.get(getSummonNpcIds());
					spawn(summonId, first.getX(), first.getY(), first.getZ(), (byte) 0);
					spawn(summonId, second.getX(), second.getY(), second.getZ(), (byte) 0);
					scheduleSummons(30000);
				}
			}
		}, delay);
	}

	private List<Player> getNearbyPlayers() {
		return getKnownList().getKnownPlayers().values().stream().filter(player -> !player.isDead() && PositionUtil.isInRange(player, getOwner(), 30))
			.collect(Collectors.toList());
	}

	@Override
	protected void handleDespawned() {
		getSummonNpcIds().stream().forEach(id -> getPosition().getWorldMapInstance().getNpcs(id).stream().forEach(npc -> npc.getController().delete()));
	}

	private List<Integer> getSummonNpcIds() {
		switch (getNpcId()) {
			case 219366: // Graviwing
				return Arrays.asList(282727, 282729); // Gravity Whirlpool, Thunderbolt Whirlpool
			case 219368: // Petriscale
				return Arrays.asList(282731); // Petrification Crystal
			case 219365: // Fissure Fang
				return Arrays.asList(282735, 282737); // Cavity of Earth, Collapsing Earth
			default:
				return Collections.emptyList();
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
