package featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition;

import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.RoughEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.interf.ClassSet;

/**
 * An entity implements {@link ClassSet} as a dummy version of {@link RoughEquivalenceClass}.
 * <p>
 * {@link #type} is required to be maintained manually when calling {@link #addClassItem(EquivalenceClass)}
 *
 * @see RoughEquivalenceClass
 *
 * @author Benjamin_L
 */
public class RoughEquivalenceClassDummy
		extends RoughEquivalenceClass<EquivalenceClass>
		implements ClassSet<EquivalenceClass>
{
	
	public RoughEquivalenceClassDummy(EquivalenceClass equClass) {
		super();
		decision = equClass.getDecisionValue();
		instanceSize = 0;
		type = equClass.getType();
		addClassItem(equClass);
	}
	
	@Override
	public void addClassItem(EquivalenceClass item) {
		equClasses.add(item);
		instanceSize += item.getInstanceSize();
	}

	public void setType(ClassSetType type){
		this.type = type;
	}

	public void setDecision(int decision){
		this.decision = decision;
	}

	@Override
	public String toString() {
		return String.format("(Ux%d) %s d=%2d %s", instanceSize, getType(), decision, equClasses);
	}
}