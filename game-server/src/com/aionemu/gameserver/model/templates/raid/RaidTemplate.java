package com.aionemu.gameserver.model.templates.raid;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * @author Alcapwnd
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Raid")
public class RaidTemplate {

    @XmlAttribute(name = "id")
    protected int id;
    @XmlAttribute(name = "world")
    protected int world;
    @XmlAttribute(name = "x")
    protected float x;
    @XmlAttribute(name = "y")
    protected float y;
    @XmlAttribute(name = "z")
    protected float z;
    @XmlAttribute(name = "h")
    protected int h;

    /**
     * @return the location id
     */
    public int getId() {
        return this.id;
    }

    /**
     * @return the world id
     */
    public int getWorldId() {
        return this.world;
    }
    
    /**
     * @return x
     */
    public float getX() {
    	return this.x;
    }
    
    /**
     * @return y
     */
    public float getY() {
    	return this.y;
    }
    
    /**
     * @return z
     */
    public float getZ() {
    	return this.z;
    }
    
    /**
     * @return h
     */
    public int getH() {
    	return this.h;
    }
}
