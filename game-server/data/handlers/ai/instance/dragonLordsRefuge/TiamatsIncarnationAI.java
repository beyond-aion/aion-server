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
 * @author Estrayl
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
					Player first = nearbyPlayers.remove(Rnd.nextInt(nearbyPlayers.size()));
					Player second = nearbyPlayers.remove(Rnd.nextInt(nearbyPlayers.size()));
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
		getSummonNpcIds().forEach(id -> getPosition().getWorldMapInstance().getNpcs(id).forEach(npc -> npc.getController().delete()));
		super.handleDespawned();
	}

	private List<Integer> getSummonNpcIds() {
		switch (getNpcId()) {
			case 219366: // Graviwing
				return Arrays.asList(282727, 282729); // Gravity Whirlpool, Thunderbolt Whirlpool
			case 236279: // Graviwing HM
				return Arrays.asList(856074, 856076);
			case 219368: // Petriscale
				return Arrays.asList(282731); // Petrification Crystal
			case 236281: // Petriscale HM
				return Arrays.asList(856072);
			case 219365: // Fissure Fang
				return Arrays.asList(282735, 282737); // Cavity of Earth, Collapsing Earth
			case 236278: // Fissure Fang HM
				return Arrays.asList(856068, 856070);
			default:
				return Collections.emptyList();
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
				case REWARD_AP_XP_DP_LOOT, ALLOW_DECAY -> false;
			default -> super.ask(question);
		};
	}
}
