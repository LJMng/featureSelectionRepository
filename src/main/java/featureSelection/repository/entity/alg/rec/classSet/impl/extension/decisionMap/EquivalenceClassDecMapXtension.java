package featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap;

import java.util.Map;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.interf.extension.decisionMap.DecisionInfoExtendedClassSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;

/**
 * An extension model of {@link EquivalenceClass}.
 * <p>{@link DecisionInfoExtendedClassSet} implemented, using a {@link Map} to count decision values:
 * <pre>{ key-decision value, value-correspondent number }</pre>
 *
 * @see EquivalenceClass
 * @see DecisionInfoExtendedClassSet
 * @see RoughEquivalenceClassDecMapXtension
 * 
 * @author Benjamin_L
 */
@NoArgsConstructor
public class EquivalenceClassDecMapXtension<Sig extends Number>
	extends EquivalenceClass
	implements DecisionInfoExtendedClassSet<Instance, Map<Integer, Integer>>
{
	@Setter @Getter private Map<Integer, Integer> decisionInfo;
	@Setter @Getter private Sig singleSigMark;
	
	public EquivalenceClassDecMapXtension(Instance instance) {
		super(instance);
		decisionInfo = new HashMap<>();
		updateDecisionInfo4AddingClassItem(instance);
	}
		
	/**
	 * Add an {@link Instance} into this class set, and update {@link #decisionInfo}.
	 * 
	 * @see #addClassItem(Instance)
	 * @see #updateDecisionInfo4AddingClassItem(Instance)
	 * 
	 * @param ins
	 * 		The instance to be added.
	 */
	@Override
	public void addClassItem(Instance ins) {
		super.addClassItem(ins);
		updateDecisionInfo4AddingClassItem(ins);
	}

	/**
	 * Count the decision value of the given {@link Instance} in {@link #decisionInfo}.
	 *
	 * @param ins
	 *      An {@link Instance}.
	 */
	private void updateDecisionInfo4AddingClassItem(Instance ins) {
		Integer dValue = ins.getAttributeValue(0);
		Integer number = decisionInfo.get(dValue);
		if (number==null)	number=0;
		decisionInfo.put(dValue, number+1);
	}

	@Override
	public Collection<Integer> decisionValues() {
		return decisionInfo.keySet();
	}

	@Override
	public Collection<Integer> numberValues() {
		return decisionInfo.values();
	}

	/**
	 * Create an {@link EquivalenceClassDecMapXtension} instance and set values based on <code>this</code>.
	 *
	 * @return cloned {@link EquivalenceClassDecMapXtension} instance.
	 */
	@Override
	public EquivalenceClassDecMapXtension<Sig> clone() {
		EquivalenceClassDecMapXtension<Sig> clone = new EquivalenceClassDecMapXtension<>();
		
		clone.setAttrValue(getAttrValue());
		clone.setDecValue(getDecValue());
		clone.setInstanceCount(getInstanceCount());
		
		clone.decisionInfo = new HashMap<>(this.decisionInfo);
		clone.singleSigMark = this.singleSigMark;
		
		return clone;
	}
}