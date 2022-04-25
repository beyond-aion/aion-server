package consolecommands;

import java.io.File;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * @author ginho1
 */
public class Addcskill extends ConsoleCommand {

	public Addcskill() {
		super("addcskill");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		final VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "This command can only be used on a player!");
			return;
		}

		final Player player = (Player) target;

		String skillName = params[0];
		int skillId = 0;

		File xml = new File("./data/handlers/consolecommands/data/skills.xml");
		SkillData data = JAXBUtil.deserialize(xml, SkillData.class);
		SkillTemplate skillTemplate = data.getSkillTemplate(skillName);

		if (skillTemplate != null)
			skillId = skillTemplate.getSkillId();

		if (skillId > 0) {
			player.getSkillList().addSkill(player, skillId, 1);
			PacketSendUtility.sendMessage(admin, "You have success add skill");
		}
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///addcskill <skill name>");
	}

	@XmlRootElement(name = "skill")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class SkillTemplate {

		@XmlAttribute(name = "id", required = true)
		private int skillId;

		@XmlAttribute(name = "name")
		private String name;

		public String getName() {
			return name;
		}

		public int getSkillId() {
			return skillId;
		}

	}

	@XmlRootElement(name = "skills")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class SkillData {

		@XmlElement(name = "skill")
		private List<SkillTemplate> its;

		public SkillTemplate getSkillTemplate(String skill) {

			for (SkillTemplate it : getData()) {
				if (it.getName().toLowerCase().equals(skill.toLowerCase()))
					return it;
			}
			return null;
		}

		protected List<SkillTemplate> getData() {
			return its;
		}
	}
}
