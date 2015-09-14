package com.aionemu.gameserver.model.assemblednpc;

import com.aionemu.gameserver.model.templates.assemblednpc.AssembledNpcTemplate.AssembledNpcPartTemplate;

/**
 * @author xTz
 */
public class AssembledNpcPart {

	private Integer object;
	private AssembledNpcPartTemplate template;

	public AssembledNpcPart(Integer object, AssembledNpcPartTemplate template) {
		this.object = object;
		this.template = template;
	}

	public Integer getObject() {
		return object;
	}

	public AssembledNpcPartTemplate getAssembledNpcPartTemplate() {
		return template;
	}

	public int getNpcId() {
		return template.getNpcId();
	}

	public int getStaticId() {
		return template.getStaticId();
	}
}
