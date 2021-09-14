package featureSelection.repository.entity.alg.liangIncrementalAlgorithm;

import java.util.Collection;
import java.util.LinkedList;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.classSetType.ClassSetType;
import lombok.Data;

/**
 * An entity for mixed Equivalence Class which is an Equivalence Class that contains both previous
 * {@link Instance}s and new ones who share the same equivalence relation({@link ClassSetType#MIXED}).
 *
 * @see EquivalenceClassInterf
 * @see ClassSetType#MIXED
 * 
 * @author Benjamin_L
 */
@Data
public class MixedEquivalenceClass implements EquivalenceClassInterf {
	private Collection<Instance> previousInstances;
	private Collection<Instance> newInstances;
	private ClassSetType classSetType;
	
	public MixedEquivalenceClass() {
		this(null, null);
		classSetType = ClassSetType.MIXED;
	}
	private MixedEquivalenceClass(
		Collection<Instance> previousInstances, Collection<Instance> newInstances
	) {
		this.previousInstances = previousInstances;
		this.newInstances = newInstances;
	}

	@Override
	public Collection<Instance> getInstances(ClassSetType classSetType) {
		switch(classSetType) {
			case PREVIOUS:
				return previousInstances;
			case NEW:
				return newInstances;
			default:
				Collection<Instance> both = new LinkedList<>();
				if (previousInstances!=null)	both.addAll(previousInstances);
				if (newInstances!=null)			both.addAll(newInstances);
				return both;
		}
	}
}
