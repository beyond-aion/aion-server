package ai.instance.dragonLordsRefuge;

import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller, Yeats, Estrayl
 */
@AIName("IDTiamat_2_calindi_flamelord")
public class CalindiFlamelordAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(75, 50, 25, 12);

	public CalindiFlamelordAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		blazeEngraving();
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 75, 50, 25 -> startHallucinatoryVictoryEvent();
			case 12 -> getOwner().queueSkill(20942, 1);
		}
	}

	protected void startHallucinatoryVictoryEvent() {
		if (getPosition().getWorldMapInstance().getNpc(730695) == null && getPosition().getWorldMapInstance().getNpc(730696) == null)
			getOwner().queueSkill(20911, 1);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 20911:
				spawn(730695, 482.21f, 458.06f, 427.42f, (byte) 98);
				spawn(730696, 482.21f, 571.16f, 427.42f, (byte) 22);
				rndSpawn(283133);
				break;
			case 20913:
				Player target = getRandomTarget();
				if (target != null)
					spawn(283131, target.getX(), target.getY(), target.getZ(), (byte) 0);
		}
	}

	protected void blazeEngraving() {
		if (Rnd.chance() < 2 && getPosition().getWorldMapInstance().getNpc(283131) == null)
			getOwner().queueSkill(20913, 60);
	}

	protected void rndSpawn(int npcId) {
		for (int i = 0; i < 10; i++) {
			rndSpawnInRange(npcId, 5, 20);
		}
	}

	protected Player getRandomTarget() {
		List<Player> players = getKnownList().getKnownPlayers().values().stream()
			.filter(player -> !player.isDead() && PositionUtil.isInRange(player, getOwner(), 50)).collect(Collectors.toList());
		return Rnd.get(players);
	}
}
