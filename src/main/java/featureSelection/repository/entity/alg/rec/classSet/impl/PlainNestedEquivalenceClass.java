package featureSelection.repository.entity.alg.rec.classSet.impl;

/**
 * Plain version of {@link NestedEquivalenceClass}. This class extends directly from
 * {@link NestedEquivalenceClass} and only serves to differentiate from other kinds of implementations
 * of {@link NestedEquivalenceClass}.
 * 
 * @author Benjamin_L
 */
public class PlainNestedEquivalenceClass
	extends NestedEquivalenceClass<EquivalenceClass>
{
	/**
	 * Construct the class by setting:
	 * <ul>
	 * 	<li>{@link #dec}</li>
	 * 	<li>{@link #instanceSize}</li>
	 * 	<li>{@link #equClasses}</li>
	 * 	<li>{@link #type}</li>
	 * </ul>
	 * 
	 * @see #addClassItem(EquivalenceClass)
	 * 
	 * @param equClass
	 * 		An {@link EquivalenceClass} instance for initializing this.
	 */
	public PlainNestedEquivalenceClass(EquivalenceClass equClass) {
		super(equClass, 16);
	}
	/**
	 * Construct the class by setting:
	 * <ul>
	 * 	<li>{@link #dec}</li>
	 * 	<li>{@link #instanceSize}</li>
	 * 	<li>{@link #equClasses}</li>
	 * 	<li>{@link #type}</li>
	 * </ul>
	 * 
	 * @see #addClassItem(EquivalenceClass)
	 * 
	 * @param equClass
	 * 		An {@link EquivalenceClass} instance for initializing this.
	 * @param equClassCapacity
	 * 		Hash capacity for {@link #equClasses}.
	 */
	public PlainNestedEquivalenceClass(EquivalenceClass equClass, int equClassCapacity) {
		super(equClass, equClassCapacity);
	}
}