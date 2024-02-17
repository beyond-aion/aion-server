package instance.pvparenas;

import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Estrayl
 */
@InstanceID(300360000)
public class ArenaOfDisciplineInstance extends DisciplineTrainingGroundsInstance {

	public ArenaOfDisciplineInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected BaseRewards getBaseRewardsPerPlayer(int difficultyId) {
		// Extracted values from instant_dungeon_idarenapvp.xml (Retail Leak 4.6, also verified by different videos)
		return switch (difficultyId) {
			case 2 -> new BaseRewards(900, 0, 425, 38);
			case 3 -> new BaseRewards(1100, 0, 655, 59);
			case 4 -> new BaseRewards(1200, 39, 655, 59);
			default -> new BaseRewards(700, 0, 310, 20);
		};
	}

	@Override
	protected BaseRewards getBaseRewards(int difficultyId) {
		return switch (difficultyId) {
			case 1 -> new BaseRewards(200, 0, 90, 0);
			case 2 -> new BaseRewards(200, 0, 125, 0);
			default -> new BaseRewards(200, 0, 195, 0);
		};
	}

	@Override
	protected float getRewardRate(int rank, int difficultyId) {
		return switch (rank) {
			case 0 -> difficultyId == 4 ? 0.75f : 0.72f;
			default -> difficultyId == 4 ? 0.25f : 0.28f;
		};
	}

	@Override
	protected void setRewardItems(PvPArenaPlayerReward reward, int rank, int difficultyId) {
		if (rank != 0 && difficultyId > 1)
			reward.setRewardItem1(new RewardItem(186000165, 4)); // Opportunity Token
	}

	@Override
	protected float getConfigRate(Player player) {
		return Rates.get(player, RatesConfig.PVP_ARENA_DISCIPLINE_REWARD_RATES);
	}
}
