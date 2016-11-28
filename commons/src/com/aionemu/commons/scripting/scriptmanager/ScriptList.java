package com.aionemu.commons.scripting.scriptmanager;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Root element for script descriptors
 * 
 * @author SoulKeeper
 */
@XmlRootElement(name = "scriptlist")
@XmlAccessorType(XmlAccessType.NONE)
public class ScriptList {

	/**
	 * List of Script descriptors
	 */
	@XmlElement(name = "scriptinfo", type = ScriptInfo.class)
	private Set<ScriptInfo> scriptInfos;

	/**
	 * Returns list of script descriptors
	 * 
	 * @return list of script descriptors
	 */
	public Set<ScriptInfo> getScriptInfos() {
		return scriptInfos;
	}

	/**
	 * Sets list of script descriptors
	 * 
	 * @param scriptInfos
	 *          lisft of script descriptors
	 */
	public void setScriptInfos(Set<ScriptInfo> scriptInfos) {
		this.scriptInfos = scriptInfos;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("ScriptList");
		sb.append("{scriptInfos=").append(scriptInfos);
		sb.append('}');
		return sb.toString();
	}
}
