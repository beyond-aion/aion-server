package instance.pvparenas;

import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.network.aion.instanceinfo.ArenaScoreWriter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Estrayl
 */
@InstanceID(300550000)
public class ArenaOfGloryInstance extends PvPArenaInstance {

	public ArenaOfGloryInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		pointsPerKill = 1000;
		pointsPerDeath = -200;
		super.onInstanceCreate();
	}

	@Override
	protected int getBoostMoraleEffectDuration(int rank) {
		return switch (rank) {
			case 0, 1 -> 14000;
			default -> 15000;
		};
	}

	@Override
	protected float getRunnerUpScoreMod(int victimRank) {
		return 4f;
	}

	@Override
	protected void sendPacket(Player player, InstanceScoreType scoreType) {
		instance.forEachPlayer(
			p -> PacketSendUtility.sendPacket(p, new SM_INSTANCE_SCORE(instance.getMapId(), new ArenaScoreWriter(instanceScore, p.getObjectId(), false))));
	}

	@Override
	protected BaseRewards getBaseRewardsPerPlayer(int difficultyId) {
		// Extracted values from instant_dungeon_idarenapvp.xml (Retail Leak 4.6, also verified by different videos)
		// Retail4.6, there are 4 difficultyIds - maybe they were removed
		// 1 new BaseValuesPerPlayer(700, 0, 310, 27);
		// 2 new BaseValuesPerPlayer(900, 0, 425, 50);
		// 3 new BaseValuesPerPlayer(1100, 0, 655, 77)
		// 4 new BaseValuesPerPlayer(1300, 31, 655, 77);
		return switch (difficultyId) {
			case 2 -> new BaseRewards(1300, 31, 655, 77);
			default -> new BaseRewards(1100, 0, 655, 77);
		};
	}

	@Override
	protected float getRewardRate(int rank, int difficultyId) {
		return switch (rank) {
			case 0 -> 0.55f;
			case 1 -> 0.25f;
			case 2 -> 0.15f;
			default -> 0.05f;
		};
	}

	@Override
	protected void setRewardItems(PvPArenaPlayerReward reward, int rank, int difficultyId) {
		switch (rank) {
			case 0 -> {
				switch (difficultyId) {
					case 2 -> {
						reward.setRewardItem1(new RewardItem(186000242, 3)); // Ceramium Medal
						reward.setRewardItem2(new RewardItem(182213259, 1)); // Arena of Glory Ticket
					}
					default -> {
						reward.setRewardItem1(new RewardItem(186000147, 5)); // Mithril Medal
						reward.setRewardItem2(new RewardItem(182213259, 1)); // Arena of Glory Ticket
					}
				}
			}
			case 1 -> {
				switch (difficultyId) {
					case 2 -> reward.setRewardItem1(new RewardItem(186000242, 1)); // Ceramium Medal
					default -> {
						reward.setRewardItem1(new RewardItem(186000147, 1)); // Mithril Medal
						reward.setRewardItem1(new RewardItem(186000096, 3)); // Platinum Medal
					}
				}
			}
			case 2 -> {
				switch (difficultyId) {
					case 2 -> reward.setRewardItem1(new RewardItem(186000147, 2)); // Mithril Medal
					default -> reward.setRewardItem1(new RewardItem(186000096, 3)); // Platinum Medal
				}
			}
			default -> {
				switch (difficultyId) {
					case 2 -> reward.setRewardItem1(new RewardItem(162000124, 5)); // Superior Recovery Serum
					default -> reward.setRewardItem1(new RewardItem(162000077, 1)); // Fine Life Serum
				}
			}
		}
	}

	@Override
	protected float getConfigRate(Player player) {
		return Rates.get(player, RatesConfig.PVP_ARENA_GLORY_REWARD_RATES);
	}
}
