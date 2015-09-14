package com.aionemu.commons.objects.filter;

/**
 * This filter is used to combine a few ObjectFilters into one. Its acceptObject method returns true only if all filters, that were passed through
 * constructor return true
 * 
 * @author Luno
 * @param <T>
 */
public class AndObjectFilter<T> implements ObjectFilter<T> {

	/** All filters that are used when running acceptObject() method */
	private ObjectFilter<? super T>[] filters;

	/**
	 * Constructs new <tt>AndObjectFilter</tt> object, that uses given filters.
	 * 
	 * @param filters
	 */
	@SafeVarargs
	public AndObjectFilter(ObjectFilter<? super T>... filters) {
		this.filters = filters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean acceptObject(T object) {
		if (filters == null)
			return true;
		for (ObjectFilter<? super T> filter : filters) {
			if (filter != null && !filter.acceptObject(object))
				return false;
		}
		return true;
	}
}
