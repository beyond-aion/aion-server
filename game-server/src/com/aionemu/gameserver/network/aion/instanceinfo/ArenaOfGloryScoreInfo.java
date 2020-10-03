package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.PvPArenaReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;

/**
 * @author xTz
 */
public class ArenaOfGloryScoreInfo extends ArenaScoreInfo {

	private final Integer ownerObject;

	public ArenaOfGloryScoreInfo(PvPArenaReward arenaReward, Integer ownerObject, List<Player> players) {
		super(arenaReward, players);
		this.ownerObject = ownerObject;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		PvPArenaPlayerReward rewardedPlayer = arenaReward.getPlayerReward(ownerObject);

		writePlayerRewards(buf);
		if (arenaReward.isRewarded() && arenaReward.canRewarded() && rewardedPlayer != null) {
			writeAP(buf, rewardedPlayer);
			writeGP(buf, rewardedPlayer);
			writeB(buf, new byte[32]);
			if (rewardedPlayer.getCeramiumMedal() != 0) {
				writeD(buf, 186000242); // 186000242
				writeD(buf, rewardedPlayer.getCeramiumMedal()); // mithril medal
			} else if (rewardedPlayer.getMithrilMedal() != 0) {
				writeD(buf, 186000147); // 186000147
				writeD(buf, rewardedPlayer.getMithrilMedal()); // mithril medal
			} else if (rewardedPlayer.getPlatinumMedal() != 0) {
				writeD(buf, 186000096); // 186000096
				writeD(buf, rewardedPlayer.getPlatinumMedal()); // platinum medal
			} else if (rewardedPlayer.getLifeSerum() != 0) {
				writeD(buf, 162000077); // 162000077
				writeD(buf, rewardedPlayer.getLifeSerum()); // life serum
			} else {
				writeD(buf, 0);
				writeD(buf, 0);
			}
			if (rewardedPlayer.getGloriousInsignia() != 0) {
				writeD(buf, 182213259); // 182213259
				writeD(buf, rewardedPlayer.getGloriousInsignia()); // glorious insignia
			} else {
				writeD(buf, 0);
				writeD(buf, 0);
			}
		} else {
			writeB(buf, new byte[60]);
		}
		writeD(buf, arenaReward.getBuffId()); // instance buff id
		writeD(buf, 0); // unk
		writeD(buf, arenaReward.getRound()); // round
		writeD(buf, arenaReward.getCapPoints()); // cap points
		writeD(buf, 3); // possible rounds
		writeD(buf, 0); // unk
	}

}
