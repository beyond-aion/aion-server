package com.aionemu.commons.callbacks;

/**
 * Basic callback class.<br>
 * Each enhanced method will call "beforeCall" and "afterCall" methods
 *
 * @author SoulKeeper
 */
@SuppressWarnings("rawtypes")
public interface Callback<T> {

    /**
     * Method that is called before actual method is invoked.<br>
     * <p/>
     * Callback should return one of the following results:
     * <ul>
     * <li>{@link CallbackResult#newContinue()}</li>
     * <li>{@link CallbackResult#newCallbackBlocker()}</li>
     * <li>{@link CallbackResult#newFullBlocker(Object)}</li>
     * </ul>
     * <p/>
     * if result is not {@link CallbackResult#newFullBlocker(Object)} then method will be executed normally. In other
     * case {@link CallbackResult#getResult()} will be returned.
     *
     * @param obj  on whom method should be invoked
     * @param args method args
     * @return see {@link CallbackResult}
     */
    public CallbackResult beforeCall(T obj, Object[] args);

    /**
     * Method that is called after actual method call.<br>
     * <p/>
     * Callback should return one of the following results:
     * <ul>
     * <li>{@link CallbackResult#newContinue()}</li>
     * <li>{@link CallbackResult#newCallbackBlocker()}</li>
     * <li>{@link CallbackResult#newFullBlocker(Object)}</li>
     * </ul>
     * <p/>
     * if result is not {@link CallbackResult#newFullBlocker(Object)} then mehodResult will return unmodified. In other
     * case {@link CallbackResult#getResult()} will be returned.
     *
     * @param obj          on whom method was called
     * @param args         method args
     * @param methodResult result of method invocation
     * @return see {@link CallbackResult}
     */
    public CallbackResult afterCall(T obj, Object[] args, Object methodResult);

    /**
     * Returns base class that will be used as callback identificator.<br> {@link com.aionemu.commons.callbacks.metadata.ObjectCallback#value()} should contain
     * class that will be invoked
     *
     * @return base class of callback
     */
		public Class<? extends Callback> getBaseClass();
}
