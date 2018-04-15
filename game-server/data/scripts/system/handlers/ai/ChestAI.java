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
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.chest.ChestTemplate;
import com.aionemu.gameserver.model.templates.chest.KeyItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
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
		if (analyzeOpening(player)) {
			if (getOwner().isInState(CreatureState.DEAD)) {
				AuditLogger.log(player, "attempted multiple chest looting!");
				return;
			}

			Collection<Player> players = new HashSet<>();
			TemporaryPlayerTeam<?> playerTeam = player.getCurrentTeam();
			if (playerTeam != null) {
				for (Player member : playerTeam.getOnlineMembers())
					if (PositionUtil.isInRange(member, getOwner(),
						DropConfig.DISABLE_RANGE_CHECK_MAPS.contains(getPosition().getMapId()) ? 9999 : GroupConfig.GROUP_MAX_DISTANCE))
						players.add(member);
			} else {
				players.add(player);
			}
			DropRegistrationService.getInstance().registerDrop(getOwner(), player, getHighestLevel(players), players);
			AIActions.die(this, player);
			DropService.getInstance().requestDropList(player, getObjectId());
			super.handleUseItemFinish(player);
		} else {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1111301));
		}
	}

	private boolean analyzeOpening(final Player player) {
		List<KeyItem> keyItems = chestTemplate.getKeyItem();
		int i = 0;
		for (KeyItem keyItem : keyItems) {
			if (keyItem.getItemId() == 0) {
				return true;
			}
			Item item = player.getInventory().getFirstItemByItemId(keyItem.getItemId());
			if (item != null) {
				if (item.getItemCount() != keyItem.getQuantity()) {
					int _i = 0;
					for (Item findedItem : player.getInventory().getItemsByItemId(keyItem.getItemId())) {
						_i += findedItem.getItemCount();
					}
					if (_i < keyItem.getQuantity()) {
						return false;
					}
				}
				i++;
				continue;
			} else {
				return false;
			}
		}
		if (i == keyItems.size()) {
			for (KeyItem keyItem : keyItems) {
				player.getInventory().decreaseByItemId(keyItem.getItemId(), keyItem.getQuantity());
			}
			return true;
		}
		return false;
	}

	private int getHighestLevel(Collection<Player> players) {
		return players.stream().mapToInt(p -> p.getLevel()).max().getAsInt();
	}
}
