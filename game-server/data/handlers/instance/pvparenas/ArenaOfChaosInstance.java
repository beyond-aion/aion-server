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
@InstanceID(300350000)
public class ArenaOfChaosInstance extends ChaosTrainingGroundsInstance {

	public ArenaOfChaosInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected BaseRewards getBaseRewardsPerPlayer(int difficultyId) {
		// Extracted values from instant_dungeon_idarenapvp.xml (Retail Leak 4.6, also verified by different videos)
		return switch (difficultyId) {
			case 2 -> new BaseRewards(900, 0, 425, 50);
			case 3 -> new BaseRewards(1100, 0, 655, 77);
			case 4 -> new BaseRewards(1300, 31, 655, 77);
			default -> new BaseRewards(700, 0, 310, 27);
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
			case 0 -> 0.21f;
			case 1 -> 0.16f;
			case 2 -> 0.135f;
			case 3 -> 0.115f;
			case 4 -> 0.095f;
			case 5 -> 0.075f;
			default -> 0.050f;
		};
	}

	@Override
	protected void setRewardItems(PvPArenaPlayerReward reward, int rank, int difficultyId) {
		switch (rank) {
			case 0, 1 -> {
				if (difficultyId > 2)
					reward.setRewardItem2(new RewardItem(186000185, 1)); // Arena of Glory Ticket, Item2 is indented by retail
			}
			case 2 -> reward.setRewardItem1(new RewardItem(186000165, 1)); // Opportunity Token
			case 3 -> reward.setRewardItem1(new RewardItem(186000165, 2)); // Opportunity Token
			case 4 -> reward.setRewardItem1(new RewardItem(186000165, 3)); // Opportunity Token
			default -> reward.setRewardItem1(new RewardItem(186000165, 5)); // Opportunity Token
		}
	}

	@Override
	protected float getConfigRate(Player player) {
		return Rates.get(player, RatesConfig.PVP_ARENA_CHAOS_REWARD_RATES);
	}
}
