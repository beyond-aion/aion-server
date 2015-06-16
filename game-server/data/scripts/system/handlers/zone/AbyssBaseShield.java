package zone;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;
import com.aionemu.gameserver.world.zone.handler.ZoneNameAnnotation;

/**
 * @author MrPoke
 */
@ZoneNameAnnotation("ASMODIANS_BASE_400010000 ELYOS_BASE_400010000")
public class AbyssBaseShield implements ZoneHandler {

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		Creature actingCreature = creature.getActingCreature();
		if (actingCreature instanceof Player && !((Player) actingCreature).isGM()) {
			ZoneName currZone = zone.getZoneTemplate().getName();
			if (currZone == ZoneName.get("ASMODIANS_BASE_400010000")) {
				if (((Player) actingCreature).getRace() == Race.ELYOS)
					creature.getController().die();
			}
			else if (currZone == ZoneName.get("ELYOS_BASE_400010000")) {
				if (((Player) actingCreature).getRace() == Race.ASMODIANS)
					creature.getController().die();
			}
		}
	}

	@Override
	public void onLeaveZone(Creature player, ZoneInstance zone) {
	}

}
