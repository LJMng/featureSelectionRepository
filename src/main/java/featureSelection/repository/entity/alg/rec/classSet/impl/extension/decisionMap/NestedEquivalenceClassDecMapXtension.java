package featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.interf.extension.decisionMap.DecisionInfoExtendedClassSet;
import lombok.Getter;

/**
 * Full name: NestedEquivalentClassDecisionMapExtension (Nested Equivalent Class Decision Map Extension)
 * <p>
 * An extension model of {@link NestedEquivalenceClass}.
 * <p>{@link DecisionInfoExtendedClassSet} implemented, using a {@link Map} for decision values info: 
 * <p>{ key-decision value({@link Integer}), value-number-({@link Integer}) }.
 * 
 * @see EquivalenceClassDecMapXtension
 * @see NestedEquivalenceClass
 * @see DecisionInfoExtendedClassSet
 * 
 * @author Benjamin_L
 */
public class NestedEquivalenceClassDecMapXtension<Sig extends Number, EquClass extends EquivalenceClassDecMapXtension<Sig>>
	extends NestedEquivalenceClass<EquClass>
	implements DecisionInfoExtendedClassSet<EquClass, Map<Integer, Integer>>
{
	/**
	 * A map to contain decision values info.
	 */
	@Getter protected final Map<Integer, Integer> decisionInfo;
	
	/**
	 * Construct NEC decision map extension.
	 * <p>
	 * The capacity for {@link #equClasses}({@link HashMap} is 16.
	 * <p>
	 * The capacity for {@link #decisionInfo}({@link HashMap} is 16.
	 * 
	 * @param equClass
	 * 		The 1st {@link EquivalenceClassDecMapXtension} for initialisation.
	 */
	public NestedEquivalenceClassDecMapXtension(EquClass equClass) {
		this(equClass, 16);
	}
	/**
	 * Construct NEC decision map extension.
	 * <p>
	 * The capacity for {@link #equClasses}({@link HashMap} is 16.
	 * 
	 * @param equClass
	 * 		The 1st {@link EquivalenceClassDecMapXtension} for initialisation.
	 * @param decHashCapacity
	 * 		The capacity for {@link #decisionInfo} hash map.
	 */
	public NestedEquivalenceClassDecMapXtension(
		EquClass equClass, int decHashCapacity
	) {
		this(equClass, 16, decHashCapacity);
	}
	/**
	 * Construct NEC decision map extension.
	 * 
	 * @param equClass
	 * 		The 1st {@link EquivalenceClassDecMapXtension} for initialisation.
	 * @param equClassCapacity
	 * 		The capacity for {@link EquivalenceClassDecMapXtension} hash map.
	 * @param decHashCapacity
	 * 		The capacity for {@link #decisionInfo} hash map.
	 */
	public NestedEquivalenceClassDecMapXtension(
		EquClass equClass, int equClassCapacity, int decHashCapacity
	) {
		super();
		
		this.setDec(equClass.getDecisionValue());
		this.setInstanceSize(0);
		this.setEquClasses(new HashMap<>(equClassCapacity));
		this.setType(equClass.getType());

		decisionInfo = new HashMap<>(decHashCapacity);
		
		addClassItem(equClass);
	}
	
	/**
	 * Add an {@link EquivalenceClassDecMapXtension} into {@link #equClasses} and update
	 * {@link #decisionInfo}.
	 */
	@Override
	public void addClassItem(EquClass item) {
		super.addClassItem(item);
		for (Map.Entry<Integer, Integer> entry: item.getDecisionInfo().entrySet()) {
			decisionInfo.put(entry.getKey(), 
							decisionInfo.containsKey(entry.getKey())?
							entry.getValue()+decisionInfo.get(entry.getKey()):
							entry.getValue()
			);
		}
	}
	
	/**
	 * Get decision values.(i.e. keys of {@link #decisionInfo})
	 */
	@Override
	public Collection<Integer> decisionValues() {	return decisionInfo.keySet();	}
	
	/**
	 * Get the numbers of decision values in {@link #decisionInfo}.(i.e. values of {@link #decisionInfo})
	 */
	@Override
	public Collection<Integer> numberValues() {		return decisionInfo.values();	}
}