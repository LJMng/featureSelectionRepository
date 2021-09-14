package featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap;

import java.util.Map;

import featureSelection.repository.entity.alg.rec.classSet.impl.RoughEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.interf.extension.decisionMap.DecisionInfoExtendedClassSet;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;

/**
 * An extension model of {@link RoughEquivalenceClass} containing info. of decision values.
 * <p>{@link DecisionInfoExtendedClassSet} implemented, using a {@link Map} for decision values info:
 * <pre>{ key-decision value, value-correspondent number }</pre>
 * 
 * @see EquivalenceClassDecMapXtension
 * @see RoughEquivalenceClass
 * @see DecisionInfoExtendedClassSet
 * 
 * @author Benjamin_L
 */
public class RoughEquivalenceClassDecMapXtension<Sig extends Number>
	extends RoughEquivalenceClass<EquivalenceClassDecMapXtension<Sig>>
	implements DecisionInfoExtendedClassSet<EquivalenceClassDecMapXtension<Sig>, Map<Integer, Integer>>
{
	@Getter private Map<Integer, Integer> decisionInfo;
		
	public RoughEquivalenceClassDecMapXtension() {
		super();
		decisionInfo = new HashMap<>();
	}

	/**
	 * Add an {@link EquivalenceClassDecMapXtension} into the current rough equivalence class.
	 * 
	 * @see #addClassItem(EquivalenceClassDecMapXtension)
	 * 
	 * @param item
	 *      The {@link EquivalenceClassDecMapXtension} to add.
	 */
	@Override
	public void addClassItem(EquivalenceClassDecMapXtension<Sig> item) {
		super.addClassItem(item);
		for (Map.Entry<Integer, Integer> entry: item.getDecisionInfo().entrySet()) {
			decisionInfo.put(entry.getKey(), 
							decisionInfo.containsKey(entry.getKey())?
							entry.getValue()+decisionInfo.get(entry.getKey()):
							entry.getValue()
			);
		}
	}

	@Override
	public Collection<Integer> decisionValues() {	return decisionInfo.keySet();	}

	@Override
	public Collection<Integer> numberValues() {		return decisionInfo.values();	}
}