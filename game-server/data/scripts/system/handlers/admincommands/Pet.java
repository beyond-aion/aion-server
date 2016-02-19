package admincommands;

import java.awt.Color;
import java.security.InvalidParameterException;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.pet.PetFunction;
import com.aionemu.gameserver.model.templates.pet.PetTemplate;
import com.aionemu.gameserver.services.toypet.PetAdoptionService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer
 * @reworked Neon
 */
public class Pet extends AdminCommand {

	public Pet() {
		super("pet", "Adds or removes a pet.");

		setParamInfo(
			"<list> - Lists all available Pet IDs.",
			"<add> <pet id> <name> - Adds the pet with the specified ID and names it.",
			"<del> <pet id> - Deletes the pet with the specified ID."
		);
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		String action = params[0];
		if (action.equalsIgnoreCase("list")) {
			StringBuilder sb = new StringBuilder();
			for (int id : DataManager.PET_DATA.getPetIds()) {
				PetTemplate template = DataManager.PET_DATA.getPetTemplate(id);
				sb.append(template.getId() + " - " + ChatUtil.color(StringUtils.capitalize(template.getName()), Color.WHITE) + "\n\tFunctions: ");
				Iterator<PetFunction> iter = template.getPetFunctions().iterator();
				while (iter.hasNext())
					sb.append(iter.next().getPetFunctionType() + (iter.hasNext() ? ", " : ""));
				sb.append("\n");
			}
			sendInfo(admin, sb.toString());
		} else {
			int petId;

			try {
				petId = Integer.parseInt(params[1]);
				if (DataManager.PET_DATA.getPetTemplate(petId) == null)
					throw new InvalidParameterException();
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException | InvalidParameterException e) {
				sendInfo(admin, e instanceof ArrayIndexOutOfBoundsException ? "You must specify the pet ID." : "Pet ID is invalid.");
				return;
			}

			if (action.equalsIgnoreCase("add")) {
				if (params.length != 3) {
					sendInfo(admin, "You must specify a name for the pet.");
					return;
				}
				PetAdoptionService.addPet(admin, petId, params[2], 0, 0);
			} else if (action.equalsIgnoreCase("del")) {
				PetAdoptionService.surrenderPet(admin, petId);
			}
		}
	}
}
