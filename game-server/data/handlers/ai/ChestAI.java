package ai;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.chest.ChestTemplate;
import com.aionemu.gameserver.model.templates.chest.KeyItem;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.drop.DropService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, xTz
 */
@AIName("chest")
public class ChestAI extends ActionItemNpcAI {

	private ChestTemplate chestTemplate;

	public ChestAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(final Player player) {
		chestTemplate = DataManager.CHEST_DATA.getChestTemplate(getNpcId());

		if (chestTemplate == null) {
			LoggerFactory.getLogger(ChestAI.class).warn("Missing chest template or incorrect AI for npc " + getNpcId());
			return;
		}
		super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (tryOpening(player)) {
			if (getOwner().isInState(CreatureState.DEAD)) {
				AuditLogger.log(player, "attempted multiple chest looting!");
				return;
			}

			Collection<Player> players = new HashSet<>();
			TemporaryPlayerTeam<?> playerTeam = player.getCurrentTeam();
			if (playerTeam != null) {
				int range = DropConfig.DISABLE_RANGE_CHECK_MAPS.contains(getPosition().getMapId()) ? 9999 : GroupConfig.GROUP_MAX_DISTANCE;
				for (Player member : playerTeam.getOnlineMembers()) {
					if (PositionUtil.isInRange(member, getOwner(), range))
						players.add(member);
				}
			}
			if (players.isEmpty()) // no team or nobody was in range
				players.add(player);
			DropRegistrationService.getInstance().registerDrop(getOwner(), player, getHighestLevel(players), players);
			AIActions.die(this, player);
			DropService.getInstance().requestDropList(player, getObjectId());
			super.handleUseItemFinish(player);
		} else {
			PacketSendUtility.sendMonologue(player, 1111301); // I'll need a key to open this.
		}
	}

	private boolean tryOpening(Player player) {
		List<KeyItem> keyItems = chestTemplate.getKeyItems();
		for (KeyItem keyItem : keyItems) { // check if enough key items are available
			if (keyItem.getItemIds() != null && keyItem.getItemIds().stream().anyMatch(itemId -> itemId == 0)) // chest can be opened w/o keys
				return true;
			long availableKeys = 0;
			for (Integer keyItemId : keyItem.getItemIds()) {
				availableKeys += player.getInventory().getItemCountByItemId(keyItemId);
			}
			if (availableKeys < keyItem.getCount())
				return false;
		}
		for (KeyItem keyItem : keyItems) { // remove key items
			long keyCountToDecrease = keyItem.getCount();
			for (Integer keyItemId : keyItem.getItemIds()) {
				long availableKeys = player.getInventory().getItemCountByItemId(keyItemId);
				if (availableKeys >= keyCountToDecrease) {
					player.getInventory().decreaseByItemId(keyItemId, keyCountToDecrease);
					keyCountToDecrease = 0;
				} else {
					keyCountToDecrease -= availableKeys;
					player.getInventory().decreaseByItemId(keyItemId, availableKeys);
				}
			}
		}
		return true;
	}

	private int getHighestLevel(Collection<Player> players) {
		return players.stream().mapToInt(Player::getLevel).max().getAsInt();
	}
}
