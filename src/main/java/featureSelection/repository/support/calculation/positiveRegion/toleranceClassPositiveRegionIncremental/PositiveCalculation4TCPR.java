package featureSelection.repository.support.calculation.positiveRegion.toleranceClassPositiveRegionIncremental;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.support.calculation.alg.FeatureImportance4ToleranceClassPositiveRegionIncremental;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;
import lombok.Getter;

/**
 * Positive region calculation implemented bases on the <strong>Algorithm 1 TCPR</strong>
 * in the paper <a href="https://www.sciencedirect.com/science/article/abs/pii/
 * S0031320314002234">"Incremental feature selection based on rough set in dynamic
 * incomplete data"</a> by Wenhao Shu, Hong Shen.
 * 
 * @see ToleranceClassObtainer#obtain(Collection, Collection, IntegerIterator, Object...)
 * 
 * @author Benjamin_L
 */
public class PositiveCalculation4TCPR
	extends DefaultPositiveRegionCalculation
	implements FeatureImportance4ToleranceClassPositiveRegionIncremental
{
	@Getter private Integer result;
	@Getter private Collection<Instance> positiveRegionInstances;

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}

	/**
	 * Calculate the positive region bases on the given Tolerance Classes.
	 */
	@Override
	public FeatureImportance4ToleranceClassPositiveRegionIncremental calculate(
			Collection<Entry<Instance, Collection<Instance>>> toleranceClasses
	) {
		Collection<Instance> pos = new LinkedList<>();
		// let y[0] be the first element in S<sub>B</sub>(x) for each x in U.
		// If d(y)=d(y[0]) for all y in S<sub>B</sub>(x) then
		//	POS<sub>B</sub>(D) = POS<sub>B</sub>(D) ∪ {x}.
		// ======================================================================================
		// | i.e.: To collect positive regions based on S<sub>B</sub>(x).			            |
		// ======================================================================================
		ToleranceClassLoop:
		for (Entry<Instance, Collection<Instance>> toleranceClass: toleranceClasses) {
			Iterator<Instance> insIterator = toleranceClass.getValue().iterator();
			int dec = insIterator.next().getAttributeValue(0);
			while (insIterator.hasNext()) {
				//  in-consistent
				if (insIterator.next().getAttributeValue(0)!=dec) {
					continue ToleranceClassLoop;
				}
			}
			// all consistent
			pos.add(toleranceClass.getKey());
		}
		positiveRegionInstances = pos;
		result = pos.size();
		return this;
	}
}
