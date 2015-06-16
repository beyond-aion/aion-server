package com.aionemu.commons.database.dao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that is used to mark disabled DAO's so they will be ignored by {@link DAOLoader}
 * 
 * @author SoulKeeper
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisabledDAO {

}
