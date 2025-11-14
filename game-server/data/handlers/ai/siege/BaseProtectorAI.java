package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.controllers.attack.DamageInfo;
import com.aionemu.gameserver.model.base.Base;
import com.aionemu.gameserver.model.base.BaseOccupier;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.services.BaseService;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("base_protector")
public class BaseProtectorAI extends AggressiveNpcAI {

	public BaseProtectorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected BaseSpawnTemplate getSpawnTemplate() {
		return (BaseSpawnTemplate) super.getSpawnTemplate();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		Base<?> base = BaseService.getInstance().getActiveBase(getSpawnTemplate().getId());
		if (base == null)
			return;
		DamageInfo<AionObject> mostDamage = getAggroList().getFinalDamageList().toTeamDamages().getMostDamage();
		Creature bossKiller = mostDamage.getAttacker() instanceof TemporaryPlayerTeam<?> team ? team.getLeaderObject() : (Creature) mostDamage.getAttacker();
		BaseOccupier newOccupier = base.getOccupier(bossKiller);
		BaseService.getInstance().capture(base.getId(), newOccupier);
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP && getOwner().getLevel() >= 65) // Avoid adjusting low-level zones
			stat.setBaseRate(SiegeConfig.BASE_PROTECTOR_HEALTH_MULTIPLIER);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_LOOT, REMOVE_EFFECTS_ON_MAP_REGION_DEACTIVATE -> false;
			default -> super.ask(question);
		};
	}
}
