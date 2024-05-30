package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Estrayl
 */
@AIName("camp_defense_cannon")
public class CampDefenseCannonAI extends AggressiveNoLootNpcAI {

	public CampDefenseCannonAI(Npc owner) {
		super(owner);
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP)
			stat.setBaseRate(SiegeConfig.AHSERION_MAX_PLAYERS_PER_TEAM / 100f);
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		if (attacker instanceof Npc && effect != null) {
			switch (effect.getSkillId()) {
				case 21755: // Bombarding targets.
				case 21578: // Shield Penetration
				case 21583: // Artillery Blast
				case 21584: // Area Bombardment
					return damage * (SiegeConfig.AHSERION_MAX_PLAYERS_PER_TEAM / 100f);
			}
		}
		return super.modifyDamage(attacker, damage, effect);
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_FIRE;
	}

	@Override
	public void handleFinishAttack() {
		if (!canThink())
			return;
		Npc npc = getOwner();
		EmoteManager.emoteStopAttacking(npc);
		npc.getController().loseAggro(false);
		npc.setSkillNumber(0);
	}
}
