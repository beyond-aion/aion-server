package com.aionemu.gameserver.model.siege;

import java.util.List;

import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeReward;

/**
 * @author Estrayl
 */
public class AgentLocation extends SiegeLocation {
	
	protected List<SiegeReward> siegeRewards;

	public AgentLocation() {
	}

	public AgentLocation(SiegeLocationTemplate template) {
		super(template);
		this.siegeRewards = template.getSiegeRewards() != null ? template.getSiegeRewards() : null;
	}
	
	public List<SiegeReward> getReward() {
		return this.siegeRewards;
	}
	
	@Override
	public int getNextState() {
		return isVulnerable() ? STATE_INVULNERABLE : STATE_VULNERABLE;
	}

	@Override
	public SiegeRace getRace() {
		return SiegeRace.BALAUR;
	}
}
