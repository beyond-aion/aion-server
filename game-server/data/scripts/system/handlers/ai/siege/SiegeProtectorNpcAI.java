package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.siege.Siege;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer, Source
 */
@AIName("siege_protector")
public class SiegeProtectorNpcAI extends SiegeNpcAI {

	public SiegeProtectorNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> {
			if (getOwner().getAbyssNpcType() != AbyssNpcType.BOSS)
				return;
			Siege<?> siege = SiegeService.getInstance().getSiege(((SiegeNpc) getOwner()).getSiegeId());
			if (siege != null)
				SkillEngine.getInstance().applyEffectDirectly(19111, getOwner(), getOwner());
		}, 3000);
	}

	@Override
	public void handleBackHome() {
		super.handleBackHome();
		if (getOwner().getAbyssNpcType() != AbyssNpcType.BOSS)
			return;
		Siege<?> siege = SiegeService.getInstance().getSiege(((SiegeNpc) getOwner()).getSiegeId());
		if (siege != null)
			siege.getSiegeCounter().clearDamageCounters();
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP && getOwner().getRating() == NpcRating.LEGENDARY)
			stat.setBaseRate(SiegeConfig.SIEGE_HEALTH_MULTIPLIER);
	}
}
