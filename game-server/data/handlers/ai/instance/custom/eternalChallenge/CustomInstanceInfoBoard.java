package ai.instance.custom.eternalChallenge;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;

import ai.GeneralNpcAI;

/**
 * @author Neon
 */
@AIName("custom_instance_info_board")
public class CustomInstanceInfoBoard extends GeneralNpcAI {

	private static final Map<Integer, Long> lastLeaderboardOpenTime = new HashMap<>();

	public CustomInstanceInfoBoard(Npc owner) {
		super(owner);
		owner.setMasterName("Eternal Challenge");
	}

	@Override
	protected void handleDialogStart(Player player) {
		synchronized (lastLeaderboardOpenTime) {
			long now = System.currentTimeMillis();
			Long lastOpenTime = lastLeaderboardOpenTime.get(player.getObjectId());
			if (lastOpenTime != null && lastOpenTime + TimeUnit.SECONDS.toMillis(3) > now)
				return; // simple flood protection so we don't have to cache the whole leaderboard
			lastLeaderboardOpenTime.put(player.getObjectId(), now);
		}
		CustomInstanceService.getInstance().openLeaderboard(player, getOwner().getRace());
	}
}
