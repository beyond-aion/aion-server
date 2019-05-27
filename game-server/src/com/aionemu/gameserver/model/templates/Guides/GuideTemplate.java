package com.aionemu.gameserver.model.templates.Guides;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GuideTemplate")
public class GuideTemplate {

	@XmlAttribute(name = "level")
	private int level;
	@XmlAttribute(name = "classType")
	private PlayerClass classType;
	@XmlAttribute(name = "title")
	private String title;
	@XmlAttribute(name = "race")
	private Race race;
	@XmlElement(name = "reward_info")
	private String rewardInfo = "";
	@XmlElement(name = "message")
	private String message = "";
	@XmlElement(name = "select")
	private String select = "";
	@XmlElement(name = "survey")
	private List<SurveyTemplate> surveys;
	@XmlAttribute(name = "rewardCount")
	private int rewardCount;
	@XmlTransient
	private boolean isActivated = true;

	/**
	 * @return the level
	 */
	public int getLevel() {
		return this.level;
	}

	/**
	 * @return the classId
	 */
	public PlayerClass getPlayerClass() {
		return this.classType;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * @return the race
	 */
	public Race getRace() {
		return this.race;
	}

	/**
	 * @return the surveys
	 */
	public List<SurveyTemplate> getSurveys() {
		return this.surveys;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @return the select
	 */
	public String getSelect() {
		return this.select;
	}

	/**
	 * @return the select
	 */
	public String getRewardInfo() {
		return this.rewardInfo;
	}

	public int getRewardCount() {
		return this.rewardCount;
	}

	/**
	 * @return the isActivated
	 */
	public boolean isActivated() {
		return isActivated;
	}

	/**
	 * @param isActivated
	 *          the isActivated to set
	 */
	public void setActivated(boolean isActivated) {
		this.isActivated = isActivated;
	}
}
