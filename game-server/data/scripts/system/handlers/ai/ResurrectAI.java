package ai;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dao.PlayerBindPointDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.BindPointPosition;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.BindPointTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEVEL_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldType;

/**
 * @author ATracer
 */
@AIName("resurrect")
public class ResurrectAI extends NpcAI {

	private static Logger log = LoggerFactory.getLogger(ResurrectAI.class);

	@Override
	protected void handleDialogStart(Player player) {
		BindPointTemplate bindPointTemplate = DataManager.BIND_POINT_DATA.getBindPointTemplate(getNpcId());
		Race race = player.getRace();
		if (bindPointTemplate == null) {
			log.info("There is no bind point template for npc: " + getNpcId());
			return;
		}

		if (player.getBindPoint() != null
			&& player.getBindPoint().getMapId() == getPosition().getMapId()
			&& MathUtil.getDistance(player.getBindPoint().getX(), player.getBindPoint().getY(), player.getBindPoint().getZ(), getPosition().getX(),
				getPosition().getY(), getPosition().getZ()) < 20) {
			PacketSendUtility.sendPacket(player, STR_ALREADY_REGISTER_THIS_RESURRECT_POINT());
			return;
		}

		WorldType worldType = player.getWorldType();
		if (!CustomConfig.ENABLE_CROSS_FACTION_BINDING && !getTribe().equals(TribeClass.FIELD_OBJECT_ALL)) {
			if ((!getRace().equals(Race.NONE) && !getRace().equals(race))
				|| (race.equals(Race.ASMODIANS) && getTribe().equals(TribeClass.FIELD_OBJECT_LIGHT))
				|| (race.equals(Race.ELYOS) && getTribe().equals(TribeClass.FIELD_OBJECT_DARK))) {
				PacketSendUtility.sendPacket(player, STR_MSG_BINDSTONE_CANNOT_FOR_INVALID_RIGHT(player.getOppositeRace().toString()));
				return;
			}
		}
		if (worldType == WorldType.PRISON) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_REGISTER_RESURRECT_POINT_FAR_FROM_NPC());
			return;
		}
		bindHere(player, bindPointTemplate);
	}

	private void bindHere(Player player, final BindPointTemplate bindPointTemplate) {

		String price = Integer.toString(bindPointTemplate.getPrice());
		AIActions.addRequest(this, player, SM_QUESTION_WINDOW.STR_ASK_REGISTER_RESURRECT_POINT, 0, new AIRequest() {

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				// check if this both creatures are in same world
				if (responder.getWorldId() == requester.getWorldId()) {
					// check enough kinah
					if (responder.getInventory().getKinah() < bindPointTemplate.getPrice()) {
						PacketSendUtility.sendPacket(responder, STR_CANNOT_REGISTER_RESURRECT_POINT_NOT_ENOUGH_FEE());
						return;
					} else if (MathUtil.getDistance(requester, responder) > 5) {
						PacketSendUtility.sendPacket(responder, STR_CANNOT_REGISTER_RESURRECT_POINT_FAR_FROM_NPC());
						return;
					}

					BindPointPosition old = responder.getBindPoint();
					BindPointPosition bpp = new BindPointPosition(requester.getWorldId(), responder.getX(), responder.getY(), responder.getZ(), responder
						.getHeading());
					bpp.setPersistentState(old == null ? PersistentState.NEW : PersistentState.UPDATE_REQUIRED);
					responder.setBindPoint(bpp);
					if (DAOManager.getDAO(PlayerBindPointDAO.class).store(responder)) {
						responder.getInventory().decreaseKinah(bindPointTemplate.getPrice());
						TeleportService.sendSetBindPoint(responder);
						PacketSendUtility.broadcastPacket(responder, new SM_LEVEL_UPDATE(responder.getObjectId(), 2, responder.getCommonData().getLevel()), true);
						PacketSendUtility.sendPacket(responder, STR_DEATH_REGISTER_RESURRECT_POINT("")); // TODO
						old = null;
					} else
						// if any errors happen, left that player with old bind point
						responder.setBindPoint(old);
				}
			}
		}, price);
	}

}
