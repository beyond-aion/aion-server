package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Estrayl
 */
@AIName("drakenspire_ghastly_protector")
public class GhastlyProtectorAI extends AggressiveNoLootNpcAI {

	public GhastlyProtectorAI(Npc owner) {
		super(owner);
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_WIND;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(this::aggroPlayer, 1000);
		getOwner().getGameStats().setNextSkillDelay(0);
	}

	private void aggroPlayer() {
		getKnownList().getKnownPlayers().values().stream().filter(p -> !p.isDead() && PositionUtil.isInRange(p, 152.38f, 518.68f, 1749.6f, 24)).findAny()
			.ifPresent(p -> getAggroList().addHate(p, 10000));
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21883)
			getAggroList().getList().stream().limit(1).forEach(ai -> ai.addHate(10000));
	}
}
