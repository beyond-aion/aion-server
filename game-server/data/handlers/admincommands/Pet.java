package admincommands;

import java.awt.Color;
import java.util.Iterator;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.pet.PetFunction;
import com.aionemu.gameserver.model.templates.pet.PetTemplate;
import com.aionemu.gameserver.services.toypet.PetAdoptionService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer, Neon
 */
public class Pet extends AdminCommand {

	public Pet() {
		super("pet", "Adds or removes a pet.", """
			list - Lists all available Pet IDs.
			add <pet ID> <name> - Adds the pet with the specified ID and names it.
			del <pet ID> - Deletes the pet with the specified ID.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		String action = params[0];
		if (action.equalsIgnoreCase("list")) {
			StringBuilder sb = new StringBuilder("List of pets:");
			DataManager.PET_DATA.getPetIds().stream().sorted().forEach(id -> {
				PetTemplate template = DataManager.PET_DATA.getPetTemplate(id);
				sb.append('\n');
				sb.append(template.getTemplateId());
				sb.append(" - ");
				sb.append(ChatUtil.color(template.getL10n(), Color.WHITE));
				sb.append("\n\tFunctions: ");
				for (Iterator<PetFunction> iter = template.getPetFunctions().iterator(); iter.hasNext(); )
					sb.append(iter.next().getPetFunctionType()).append(iter.hasNext() ? ", " : "");
			});
			sendInfo(admin, sb.toString());
		} else {
			int petId = Integer.parseInt(params[1]);
			if (DataManager.PET_DATA.getPetTemplate(petId) == null) {
				sendInfo(admin, "Invalid pet ID.");
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
