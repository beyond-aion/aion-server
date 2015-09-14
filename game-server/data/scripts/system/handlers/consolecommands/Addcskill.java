package consolecommands;

import java.io.FileInputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Addcskill extends ConsoleCommand {

	public Addcskill() {
		super("addcskill");
	}

	@Override
	public void execute(Player admin, String... params) {
		if ((params.length < 0) || (params.length < 1)) {
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

		try {

			JAXBContext jc = JAXBContext.newInstance(StaticData.class);
			Unmarshaller un = jc.createUnmarshaller();
			SkillData data = (SkillData) un.unmarshal(new FileInputStream("./data/scripts/system/handlers/consolecommands/data/skills.xml"));

			SkillTemplate skillTemplate = data.getSkillTemplate(skillName);

			if(skillTemplate != null){
				skillId = skillTemplate.getTemplateId();
			}

		}
		catch (Exception e) {
			PacketSendUtility.sendMessage(admin, "Skill templates reload failed!" );
			System.out.println(e);
		}

		if (skillId > 0) {
			player.getSkillList().addSkill(player, skillId, 1);
			PacketSendUtility.sendMessage(admin, "You have success add skill");
		}
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///addcskill <skill name>");
	}

	@XmlRootElement(name = "ae_static_data")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class StaticData {
		@XmlElement(name = "skills")
		public SkillData skillData;
	}

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlType(namespace = "", name = "SkillTemplate")
	private static class SkillTemplate {

		@XmlAttribute(name = "id", required = true)
		@XmlID
		private String id;

		@XmlAttribute(name = "name")
		private String name;

		public String getName() {
			return name;
		}

		public int getTemplateId() {
			return skillId;
		}

		private int skillId;

		public void setSkillId(int skillId) {
			this.skillId = skillId;
		}

		@SuppressWarnings("unused")
		void afterUnmarshal(Unmarshaller u, Object parent) {
			setSkillId(Integer.parseInt(id));
		}

	}

	@XmlRootElement(name = "skills")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class SkillData{

		@XmlElement(name = "skill")
		private List<SkillTemplate> its;

		public SkillTemplate getSkillTemplate(String skill) {

			for (SkillTemplate it : getData()) {
				if(it.getName().toLowerCase().equals(skill.toLowerCase()))
					return it;
			}
			return null;
		}

		protected List<SkillTemplate> getData() {
			return its;
		}
	}
}