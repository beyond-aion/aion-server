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
	protected BaseValuesPerPlayer getBaseValuesPerPlayer(int difficultyId) {
		// Extracted values from instant_dungeon_idarenapvp.xml (Retail Leak 4.6, also verified by different videos)
		return switch (difficultyId) {
			case 2 -> new BaseValuesPerPlayer(900, 0, 425, 50);
			case 3 -> new BaseValuesPerPlayer(1100, 0, 655, 77);
			case 4 -> new BaseValuesPerPlayer(1300, 31, 655, 77);
			default -> new BaseValuesPerPlayer(700, 0, 310, 27);
		};
	}

	@Override
	protected int getBaseAp(int difficultyId) {
		return 200;
	}

	@Override
	protected int getBaseCrucibleInsignia(int difficultyId) {
		return switch (difficultyId) {
			case 1 -> 90;
			case 2 -> 125;
			default -> 195;
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
