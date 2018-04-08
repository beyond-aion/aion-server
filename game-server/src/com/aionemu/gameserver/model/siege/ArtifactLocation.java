package com.aionemu.gameserver.model.siege;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.siegelocation.ArtifactActivation;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author Source
 */
public class ArtifactLocation extends SiegeLocation {

	private ArtifactStatus status;
	private long lastArtifactActivation;
	
	public ArtifactLocation(SiegeLocationTemplate template) {
		super(template);
		// Artifacts Always Vulnerable
		setVulnerable(true);
	}

	@Override
	public int getNextState() {
		return STATE_VULNERABLE;
	}

	public long getLastActivation() {
		return lastArtifactActivation;
	}

	public void setInitialDelay(long capturedTime) {
		long cd = getTemplate().getActivation().getCd();
		lastArtifactActivation = cd > 900000 ? capturedTime - cd + 900000 : capturedTime;
	}

	public void setLastActivation(long lastActivation) {
		lastArtifactActivation = lastActivation;
	}

	public int getCoolDown() {
		long cd = getTemplate().getActivation().getCd();
		long millisSinceLastActivation = System.currentTimeMillis() - lastArtifactActivation;
		if (millisSinceLastActivation > cd)
			return 0;
		else
			return (int) ((cd - millisSinceLastActivation) / 1000);
	}

	/**
	 * @return L10N that describes name of this artifact (for chat usage)
	 */
	public String getL10n() {
		ArtifactActivation activation = getTemplate().getActivation();
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(activation.getSkillId());
		return skillTemplate.getL10n();
	}

	public boolean isStandAlone() {
		return !SiegeService.getInstance().getFortresses().containsKey(getLocationId());
	}

	public FortressLocation getOwningFortress() {
		return SiegeService.getInstance().getFortress(getLocationId());
	}

	/**
	 * @return the status
	 */
	public ArtifactStatus getStatus() {
		return status != null ? status : ArtifactStatus.IDLE;
	}

	/**
	 * @param status
	 *          the status to set
	 */
	public void setStatus(ArtifactStatus status) {
		this.status = status;
	}

}
