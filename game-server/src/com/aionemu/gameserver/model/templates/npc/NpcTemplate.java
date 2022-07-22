package com.aionemu.gameserver.model.templates.npc;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.CreatureTemplate;
import com.aionemu.gameserver.model.items.NpcEquippedGear;
import com.aionemu.gameserver.model.templates.BoundRadius;
import com.aionemu.gameserver.model.templates.stats.KiskStatsTemplate;
import com.aionemu.gameserver.model.templates.stats.StatsTemplate;

/**
 * @author Luno
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "npc_template")
public class NpcTemplate extends CreatureTemplate {

	private int npcId;

	@XmlAttribute(name = "level", required = true)
	private byte level;

	@XmlAttribute(name = "name_id", required = true)
	private int nameId;

	@XmlAttribute(name = "title_id")
	private int titleId;

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "group_drop")
	private GroupDropType groupDrop;

	@XmlAttribute(name = "height")
	private float height = 1;

	@XmlElement(name = "stats")
	private StatsTemplate statsTemplate;

	@XmlElement(name = "equipment")
	private NpcEquippedGear equipment;

	@XmlElement(name = "kisk_stats")
	private KiskStatsTemplate kiskStatsTemplate;

	@XmlElement(name = "ammo_speed")
	private int ammoSpeed = 0;

	@XmlAttribute(name = "rank")
	private NpcRank rank;

	@XmlAttribute(name = "rating")
	private NpcRating rating;

	@XmlAttribute(name = "srange")
	private int aggrorange;

	@XmlAttribute(name = "sangle")
	private int aggroAngle = 360;

	@XmlAttribute(name = "arange")
	private int attackRange;

	@XmlAttribute(name = "attack_speed")
	private int attackSpeed = 2000;

	@XmlAttribute(name = "cast_speed")
	private int castSpeed = 1000;

	@XmlAttribute(name = "mevent")
	private int mobileEvent;

	@XmlAttribute(name = "flag_type")
	private int flagType;

	@XmlAttribute(name = "war_flag")
	private int warFlagGroupId;

	/*
	 * @XmlAttribute(name = "item_upgrade") private int itemUpgrade;
	 */

	@XmlAttribute(name = "hpgauge")
	private int hpGauge;

	@XmlAttribute(name = "tribe")
	private TribeClass tribe;

	@XmlAttribute(name = "ai")
	private String ai;

	@XmlAttribute
	private Race race = Race.NONE;

	@XmlAttribute
	private int state;

	@XmlAttribute
	private boolean floatcorpse;

	@XmlElement(name = "bound_radius")
	private BoundRadius boundRadius;

	@XmlAttribute(name = "type")
	private NpcTemplateType npcTemplateType;

	@XmlAttribute(name = "abyss_type")
	private AbyssNpcType abyssNpcType;

	@XmlElement(name = "talk_info")
	private TalkInfo talkInfo;

	@XmlElement(name = "massive_loot")
	private MassiveLoot massiveLoot;

	protected void afterUnmarshal(Unmarshaller u, Object parent) {
		if (level > 1 && !"noaction".equals(ai) && getAbyssNpcType().equals(AbyssNpcType.TELEPORTER)) // TODO: reparse npc_template
			ai = "siege_teleporter";
	}

	public void internAiName() {
		if (ai != null)
			ai = ai.intern(); // intern to save RAM, since most npcs use same ai names
	}

	@Override
	public int getTemplateId() {
		return npcId;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}

	public int getTitleId() {
		return titleId;
	}

	@Override
	public String getName() {
		return name;
	}

	public float getHeight() {
		return height;
	}

	public NpcEquippedGear getEquipment() {
		return equipment;
	}

	public byte getLevel() {
		return level;
	}

	public StatsTemplate getStatsTemplate() {
		return statsTemplate;
	}

	public KiskStatsTemplate getKiskStatsTemplate() {
		return kiskStatsTemplate;
	}

	public TribeClass getTribe() {
		return tribe;
	}

	@Override
	public String getAiName() {
		return ai;
	}

	@Override
	public String toString() {
		return "Npc Template id: " + npcId + " name: " + name;
	}

	@XmlID
	@XmlAttribute(name = "npc_id", required = true)
	private void setXmlUid(String uid) {
		/*
		 * This method is used only by JAXB unmarshaller. I couldn't set annotations at field, because ID must be a string.
		 */
		npcId = Integer.parseInt(uid);
	}

	public final NpcRank getRank() {
		return rank;
	}

	public final NpcRating getRating() {
		return rating;
	}

	public int getAggroRange() {
		return aggrorange;
	}

	public int getAggroAngle() {
		return aggroAngle;
	}

	public int getMinimumShoutRange() {
		if (aggrorange < 10)
			return 10;
		return aggrorange;
	}

	public int getAttackRange() {
		return attackRange;
	}

	public int getCastSpeed() {
		return castSpeed;
	}

	public int getAttackSpeed() {
		return attackSpeed;
	}

	public int getMobileEvent() {
		return mobileEvent;
	}

	public int getFlagType() {
		return flagType;
	}

	public int getWarFlag() {
		return warFlagGroupId;
	}

	/*
	 * public int getItemUpgrade() { return itemUpgrade; }
	 */

	public int getHpGauge() {
		return hpGauge;
	}

	public Race getRace() {
		return race;
	}

	public int getState() {
		return state;
	}

	@Override
	public BoundRadius getBoundRadius() {
		// TODO all npcs should have BR in xml
		return boundRadius != null ? boundRadius : super.getBoundRadius();
	}

	public NpcTemplateType getNpcTemplateType() {
		return npcTemplateType != null ? npcTemplateType : NpcTemplateType.NONE;
	}

	public AbyssNpcType getAbyssNpcType() {
		return abyssNpcType != null ? abyssNpcType : AbyssNpcType.NONE;
	}

	public final int getTalkDistance() {
		return talkInfo == null ? 2 : talkInfo.getDistance();
	}

	public int getTalkDelay() {
		return talkInfo == null ? 0 : talkInfo.getDelay();
	}

	public List<Integer> getFuncDialogIds() {
		return talkInfo == null ? null : talkInfo.getFuncDialogIds();
	}

	/**
	 * @param action
	 * @return True if the npc supports this function/action.
	 */
	public boolean supportsAction(int dialogActionId) {
		List<Integer> dialogIds = getFuncDialogIds();
		return dialogIds != null && dialogIds.contains(dialogActionId);
	}

	public int getMassiveLootCount() {
		return massiveLoot.getMLootCount();
	}

	public int getMassiveLootItem() {
		return massiveLoot.getMLootItem();
	}

	public int getMassiveLootMinLevel() {
		return massiveLoot.getMLootMinLevel();
	}

	public int getMassiveLootMaxLevel() {
		return massiveLoot.getMLootMaxLevel();
	}

	/**
	 * @return if no data is present for the talk
	 */
	public boolean canInteract() {
		return talkInfo != null;
	}

	/**
	 * @return the hasDialog
	 */
	public boolean isDialogNpc() {
		return talkInfo != null && talkInfo.isDialogNpc();
	}

	public TalkInfo getTalkInfo() {
		return talkInfo;
	}

	public MassiveLoot getMassiveLoot() {
		return massiveLoot;
	}

	/**
	 * @return the floatcorpse
	 */
	public boolean isFloatCorpse() {
		return floatcorpse;
	}

	public GroupDropType getGroupDrop() {
		return groupDrop;
	}
}
