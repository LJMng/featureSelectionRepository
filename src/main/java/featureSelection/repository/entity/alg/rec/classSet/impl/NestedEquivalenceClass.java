package featureSelection.repository.entity.alg.rec.classSet.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.interf.ClassSet;
import lombok.Getter;
import lombok.Setter;

/**
 * An entity for Nested Equivalent Class, highly similar to {@link RoughEquivalenceClass}. However, this
 * class is designed for Nested Equivalent class based algorithms and reductions. So, some adjustments
 * are made in this class comparing to its early version {@link RoughEquivalenceClass}.
 * 
 * @see RoughEquivalenceClass
 * 
 * @author Benjamin_L
 *
 * @param <EquClass>
 */
public class NestedEquivalenceClass<EquClass extends EquivalenceClass>
	implements ClassSet<EquClass>, Cloneable
{
	public static final int DEC_VALUE_IGNORE = -1;
	
	/**
	 * Decision value. Ignored if {@link #type} is not {@link ClassSetType#POSITIVE}.
	 */
	@Setter @Getter protected int dec;
	/**
	 * The size of {@link Instance} in {@link EquivalenceClass} {@link Collection}.
	 */
	@Setter @Getter protected int instanceSize;
	/**
	 * Equivalent Classes
	 */
	@Setter @Getter protected Map<IntArrayKey, EquClass> equClasses;
	/**
	 * Class set consistent status.
	 */
	@Setter @Getter protected ClassSetType type;
	
	protected NestedEquivalenceClass() {}
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
	public NestedEquivalenceClass(EquClass equClass) {
		this(equClass, 16);
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
	public NestedEquivalenceClass(EquClass equClass, int equClassCapacity) {
		dec = equClass.getDecisionValue();
		instanceSize = 0;
		equClasses = new HashMap<>(equClassCapacity);
		type = equClass.getType();
		addClassItem(equClass);
	}
	
	/**
	 * {@link #instanceSize} and {@link #equClasses} is updated.
	 * <p>
	 * {@link #dec}, {@link #type} are not updated in this method and require update manually.
	 */
	public void addClassItem(EquClass item) {
		IntArrayKey key = new IntArrayKey(item.attrValue);
		equClasses.putIfAbsent(key, item);
		instanceSize += item.getInstanceSize();
	}
	
	public int getItemSize() {
		return equClasses.size();
	}

	public String toString() {
		return String.format("NEC(Ux%d) %s d=%2d %s", instanceSize, getType().getAbbreviation(),
							dec, equClasses.values());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public NestedEquivalenceClass<EquClass> clone() throws CloneNotSupportedException {
		NestedEquivalenceClass<EquClass> cloneItem = new NestedEquivalenceClass<>();
		cloneItem.setDec(dec);
		cloneItem.setInstanceSize(instanceSize);
		cloneItem.setType(type);
		EquClass equClass;
		Map<IntArrayKey, EquClass> equClassesMap = new HashMap<>(equClasses.size());
		for (Map.Entry<IntArrayKey, EquClass> entry: equClasses.entrySet()) {
			equClassesMap.put(entry.getKey(), equClass = (EquClass) entry.getValue().clone());
			equClass.setAttrValue(entry.getValue().attrValue);
			equClass.setDecValue(entry.getValue().decValue);
			equClass.setInstanceCount(entry.getValue().instanceCount);
		}
		cloneItem.setEquClasses(equClassesMap);
		return cloneItem;
	}
}
