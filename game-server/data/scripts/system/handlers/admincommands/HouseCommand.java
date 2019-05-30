package admincommands;

import java.sql.Timestamp;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.HouseOwnerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseStatus;
import com.aionemu.gameserver.model.templates.housing.BuildingType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_ACQUIRE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_OWNER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
public class HouseCommand extends AdminCommand {

	public HouseCommand() {
		super("house", "House teleport and ownership management.");

		// @formatter:off
		setSyntaxInfo(
			"<tp> <address> - Teleports you to the house with the given address.",
			"<own> <address> - Gives ownership of given house to your target.",
			"<revoke> <address> - Revokes ownership of given house."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		int address = NumberUtils.toInt(params[1]);
		House house = HousingService.getInstance().getHouseByAddress(address);
		if (house == null) {
			sendInfo(admin, "Invalid address.");
			return;
		}
		if ("own".equalsIgnoreCase(params[0])) {
			acquireHouse(admin, house);
		} else if ("revoke".equalsIgnoreCase(params[0])) {
			revokeOwnership(admin, house);
		} else if ("tp".equalsIgnoreCase(params[0])) {
			TeleportService.teleportTo(admin, house.getPosition().getWorldMapInstance(), house.getX(), house.getY(), house.getZ(),
				house.getTeleportHeading(), TeleportAnimation.NONE);
		} else {
			sendInfo(admin);
		}
	}

	private void acquireHouse(Player admin, House house) {
		VisibleObject creature = admin.getTarget();
		if (!(creature instanceof Player)) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}
		Player target = (Player) creature;

		if (target.getHouses().size() >= 2) {
			sendInfo(admin, target.getName() + " must sell his old house which is currently in grace time first!");
			return;
		}
		House current = target.getActiveHouse();
		if (current != null) {
			current.revokeOwner();
			if (current.getBuilding().getType() == BuildingType.PERSONAL_INS) {
				sendInfo(admin, "Deleted studio.");
			} else {
				sendInfo(admin, current.getName() + " status is now " + current.getStatus().toString());
			}
		}
		house.setAcquiredTime(new Timestamp(System.currentTimeMillis()));
		house.setOwnerId(target.getObjectId());
		house.setStatus(HouseStatus.ACTIVE);
		house.setFeePaid(true);
		house.setNextPay(null);
		house.save();
		target.setHouseOwnerState(HouseOwnerState.HOUSE_OWNER.getId());
		PacketSendUtility.sendPacket(target, new SM_HOUSE_OWNER_INFO(target));
		PacketSendUtility.sendPacket(target, new SM_HOUSE_ACQUIRE(target.getObjectId(), house.getAddress().getId(), true));
		sendInfo(admin, "House " + house.getName() + " is now owned by " + target.getName());
	}

	private void revokeOwnership(Player admin, House house) {
		int ownerId = house.getOwnerId();
		if (ownerId == 0) {
			sendInfo(admin, "House has no owner.");
			return;
		}
		house.revokeOwner();
		house.getController().updateAppearance();
		for (House inactiveHouse : HousingService.getInstance().findPlayerHouses(ownerId)) {
			if (inactiveHouse.getStatus() == HouseStatus.INACTIVE) {
				inactiveHouse.setStatus(HouseStatus.ACTIVE);
				inactiveHouse.setSellStarted(null);
				inactiveHouse.save();
				break;
			}
		}
		Player owner = World.getInstance().findPlayer(ownerId);
		if (owner != null) {
			if (owner.getHouses().isEmpty())
				owner.setHouseOwnerState(HouseOwnerState.BUY_STUDIO_ALLOWED.getId());
			PacketSendUtility.sendPacket(owner, new SM_HOUSE_OWNER_INFO(owner));
			PacketSendUtility.sendPacket(owner, new SM_HOUSE_ACQUIRE(owner.getObjectId(), house.getAddress().getId(), false));
			sendInfo(admin, "Ownership of house " + house.getAddress().getId() + " was revoked from " + owner.getName());
		} else {
			sendInfo(admin, "Ownership of house " + house.getAddress().getId() + " was revoked from " + PlayerService.getPlayerName(ownerId));
		}
	}
}
