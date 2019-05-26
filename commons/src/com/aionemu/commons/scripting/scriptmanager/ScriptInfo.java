package com.aionemu.commons.scripting.scriptmanager;

import java.io.File;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simple class that represents script info.<br>
 * <br>
 * It contains Script root, list of libraries and list of child contexts
 * 
 * @author SoulKeeper
 */
@XmlRootElement(name = "scriptinfo")
@XmlAccessorType(XmlAccessType.NONE)
public class ScriptInfo {

	/**
	 * Root of this script context. Child directories of root will be scanned for script files
	 */
	@XmlAttribute(required = true)
	private String root;

	/**
	 * List of libraries of this script context
	 */
	@XmlElement(name = "library")
	private List<File> libraries;

	/**
	 * List of child contexts
	 */
	@XmlElement(name = "scriptinfo")
	private List<ScriptInfo> scriptInfos;

	/**
	 * Returns root of script context
	 * 
	 * @return root of script context
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * Sets root for script context
	 * 
	 * @param root
	 *          root for script context
	 */
	public void setRoot(String root) {
		this.root = root;
	}

	/**
	 * Returns list of libraries that will be used byscript context and it's children
	 * 
	 * @return lib of libraries
	 */
	public List<File> getLibraries() {
		return libraries;
	}

	/**
	 * Sets list of libraries that will be used by script context and it's children
	 * 
	 * @param libraries
	 *          sets list of libraries
	 */
	public void setLibraries(List<File> libraries) {
		this.libraries = libraries;
	}

	/**
	 * Return list of child context descriptors
	 * 
	 * @return list of child context descriptors
	 */
	public List<ScriptInfo> getScriptInfos() {
		return scriptInfos;
	}

	/**
	 * Sets list of child context descriptors
	 * 
	 * @param scriptInfos
	 *          list of child context descriptors
	 */
	public void setScriptInfos(List<ScriptInfo> scriptInfos) {
		this.scriptInfos = scriptInfos;
	}

	/**
	 * @param o
	 *          object to compare with
	 * @return true if this ScriptInfo and another ScriptInfo have the same root
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ScriptInfo that = (ScriptInfo) o;

		return root.equals(that.root);

	}

	/**
	 * @return hashcode of root
	 */
	@Override
	public int hashCode() {
		return root.hashCode();
	}

	@Override
	public String toString() {
		return "ScriptInfo{root=" + root + ", libraries=" + libraries + ", scriptInfos=" + scriptInfos + '}';
	}
}
