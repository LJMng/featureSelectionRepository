package featureSelection.repository.entity.alg.liuRoughSet;

import java.util.Comparator;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import lombok.AllArgsConstructor;

/**
 * A implemented {@link Comparator} for {@link Instance}.
 * <p>
 * When comparing, only compare {@link #attributes} of 2 {@link Instance}s.
 * 
 * @see Instance#compareTo(Instance, IntegerIterator)
 *
 * @author Benjamin_L
 */
@AllArgsConstructor
public class InstanceComparator implements Comparator<Instance>{
	private IntegerIterator attributes;
	
	@Override public int compare(Instance o1, Instance o2) {
		return o1.compareTo(o2, attributes);
	}
}
