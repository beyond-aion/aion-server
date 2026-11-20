package com.aionemu.gameserver.services.abyss;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.abyss.AbyssRankTemplate;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author ATracer
 */
public class AbyssSkillService {

	public static final void updateSkills(Player player) {
		AbyssRank abyssRank = player.getAbyssRank();
		if (abyssRank == null)
			return;
		// remove all ranker skills of the race first, then learn the ones of the current rank
		for (AbyssRankEnum rank : AbyssRankEnum.values()) {
			AbyssRankTemplate template = DataManager.ABYSS_RANK_TABLE.get(player.getRace(), rank);
			if (template != null)
				for (int skillId : template.getRankerSkills())
					SkillLearnService.removeSkill(player, skillId);
		}
		AbyssRankTemplate currentRank = DataManager.ABYSS_RANK_TABLE.get(player.getRace(), abyssRank.getRank());
		if (currentRank != null)
			for (int skillId : currentRank.getRankerSkills())
				SkillLearnService.learnTemporarySkill(player, skillId, 1);
	}
}
