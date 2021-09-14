package featureSelection.repository.support.calculation.positiveRegion.liuQuickHash;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.algorithm.alg.quickHash.LiuQuickHashAlgorithm;
import featureSelection.repository.entity.alg.liuQuickHash.EquivalenceClass;
import featureSelection.repository.entity.alg.liuQuickHash.RoughEquivalenceClass;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiuQuickHash;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;

import java.util.Collection;

/**
 * Positive Region Calculation for Liu-RoughSet.
 * 
 * @see LiuRoughSetAlgorithm
 * 
 * @author Benjamin_L
 */
public class PositiveRegionCalculation4LiuQuickHash
	extends DefaultPositiveRegionCalculation
	implements FeatureImportance4LiuQuickHash<Integer>
{
	private Integer positive;
	@Override
	public Integer getResult() {
		return positive;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
	
	@Override
	public Integer difference(Integer v1, Integer v2) {
		return v1.intValue() - v2.intValue();
	}

	@Override
	public FeatureImportance4LiuQuickHash<Integer> calculate(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes,
			Object... args
	) {
		positive = 0;
		Collection<RoughEquivalenceClass> roughClasses =
				LiuQuickHashAlgorithm.roughEquivalenceClass(equClasses, attributes);
		for (RoughEquivalenceClass rough: roughClasses)
			if (rough.cons())	positive+= rough.instanceSize();
		return this;
	}

	@Override
	public FeatureImportance4LiuQuickHash<Integer> calculate4Incremental(
			Collection<RoughEquivalenceClass> roughClasses, IntegerIterator attributes,
			Object... args
	) {
		positive = 0;
		Collection<RoughEquivalenceClass> increment =
				LiuQuickHashAlgorithm.incrementalRoughEquivalenceClass(roughClasses, attributes);
		for (RoughEquivalenceClass rough: increment)
			if (rough.cons())	positive+=rough.instanceSize();
		return this;
	}
}