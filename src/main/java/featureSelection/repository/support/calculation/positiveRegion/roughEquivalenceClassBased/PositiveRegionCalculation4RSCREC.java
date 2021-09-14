package featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Positive Region Calculation for <strong>Simple Counting REC (Real-time Counting version)</strong>.
 * <p>
 * RSC-REC: Real-time Simple Counting - REC.
 *
 * @author Benjamin_L
 */
public class PositiveRegionCalculation4RSCREC
	extends DefaultPositiveRegionCalculation
	implements RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Integer>
{
	private Integer positive;
	@Override
	public Integer getResult() {
		return positive;
	}

	@Override
	public RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Integer>
		calculate(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes,
			Object... args
	) {
		// Count the current calculation for experiment statistics
		countCalculate(attributes.size());
		// Calculate the size of positive region
		positive = positiveRegion(equClasses, attributes);
		return this;
	}
	
	private static int positiveRegion(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes
	) {
		IntArrayKey key;
		int[] keyArray, counting;

		Map<IntArrayKey, int[]> posCountings = new HashMap<>(equClasses.size());
		// Loop over elements(e) in equivalence classes:
		int pos = 0;
		for (EquivalenceClass e : equClasses) {
			// key = P(e)
			keyArray = new int[attributes.size()];
			attributes.reset();
			for (int i=0; i<attributes.size(); i++) {
				keyArray[i] = e.getAttributeValueAt(attributes.next() - 1);
			}
			key = new IntArrayKey(keyArray);
			
			// Search key. If doesn't contain key.
			if (!posCountings.containsKey(key)) {
				// Initiate
				// if e.cnst is true: 1-REC
				if (e.sortable()) {
					posCountings.put(key, new int[] {e.getDecisionValue(), e.getInstanceSize()});
					// pos = pos + e.count
					pos += e.getInstanceSize();
				// else: -1-REC
				}else {
					// e.cnst = false.
					posCountings.put(key, null);
				}
			// else contains key.
			}else {
				// If 1-REC
				counting = posCountings.get(key);
				if (counting!=null) {
					// check if 1-REC & decision attribute values are the same
					if (e.sortable() && counting[0]==e.getDecisionValue()) {
						// pos = pos + e.count
						pos += e.getInstanceSize();
						counting[1] += e.getInstanceSize();
					// else: 1-REC -> 0-REC/-1-REC
					}else {
						posCountings.put(key, null);
						// pos = pos - counting;
						pos -= counting[1];
					}
				}
			}
		}
		return pos;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
}