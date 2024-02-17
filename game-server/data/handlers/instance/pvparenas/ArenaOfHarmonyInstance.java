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
@InstanceID(300450000)
public class ArenaOfHarmonyInstance extends HarmonyTrainingGroundsInstance {

	public ArenaOfHarmonyInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected BaseRewards getBaseRewardsPerPlayer(int difficultyId) {
		return switch (difficultyId) {
			case 2 -> new BaseRewards(2200, 0, 0, 22);
			case 3 -> new BaseRewards(3200, 0, 0, 34);
			case 4 -> new BaseRewards(2500, 31, 0, 34);
			default -> new BaseRewards(1200, 0, 0, 12);
		};
	}

	@Override
	protected BaseRewards getBaseRewards(int difficultyId) {
		return new BaseRewards(200, 0, 0, 0);
	}

	@Override
	protected float getRewardRate(int rank, int difficultyId) {
		return rank == 0 ? 0.9f : 0.1f;
	}

	@Override
	protected void setRewardItems(PvPArenaPlayerReward reward, int rank, int difficultyId) {
		switch (rank) {
			case 0 -> {
				switch (difficultyId) {
					case 1 -> reward.setRewardItem1(new RewardItem(188052179, 1)); // Lesser Arena Supply Package
					case 2 -> reward.setRewardItem1(new RewardItem(188052180, 1)); // Arena Supply Package
					case 3 -> reward.setRewardItem1(new RewardItem(188052185, 1)); // Composite Manastone Pouch
					case 4 -> {
						reward.setRewardItem1(new RewardItem(188052605, 1)); // Victor's Reward Box
						reward.setRewardItem2(new RewardItem(188052482, 1)); // Superior Arena Supply Package
					}
				}
			}
			case 1 -> {
				switch (difficultyId) {
					case 3 -> reward.setRewardItem1(new RewardItem(188052181, 1)); // Greater Arena Supply Package
					case 4 -> reward.setRewardItem1(new RewardItem(188052606, 1)); // Consolation Prize Box
				}
			}
		}
	}

	@Override
	protected float getConfigRate(Player player) {
		if (player == null) // Happens during calculation for group rewards
			return 1f;
		return Rates.get(player, RatesConfig.PVP_ARENA_HARMONY_REWARD_RATES);
	}
}
