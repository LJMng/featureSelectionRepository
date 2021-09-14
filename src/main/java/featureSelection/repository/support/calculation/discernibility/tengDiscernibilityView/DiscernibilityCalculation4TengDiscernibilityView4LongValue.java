package featureSelection.repository.support.calculation.discernibility.tengDiscernibilityView;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.alg.TengDiscernibilityViewStrategy;
import featureSelection.repository.algorithm.alg.discernibilityView.TengDiscernibilityViewAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4TengDiscernibilityView;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

public class DiscernibilityCalculation4TengDiscernibilityView4LongValue 
	implements FeatureImportance4TengDiscernibilityView<Long>,
				TengDiscernibilityViewStrategy
{
	@Getter private long calculationAttributeLength;
	@Getter private long calculationTimes;
	
	@Getter private Long result;

	@Override
	public DiscernibilityCalculation4TengDiscernibilityView4LongValue calculate(
			int universeSize, Collection<Collection<Instance>> equClasses, Object... args
	) {
		// |DIS(P)| = |U|^2 - &Sigma;<sub>t=1:m</sub>|P<sub>t</sub>|^2
		long sum = 0;
		for (Collection<Instance> equClass: equClasses)	sum += FastMath.pow(equClass.size(), 2);
		result = (long) (FastMath.pow(universeSize, 2) - sum);
		return this;
	}
	
	@Override
	public DiscernibilityCalculation4TengDiscernibilityView4LongValue calculate(
			Collection<Collection<Instance>> condEquClasses,
			Collection<Collection<Instance>> gainedEquClasses, Object... args
	) {
		// initiate DIS(Q/P): sum
		long dis = 0;
		// Go through p in U/P
		for (Collection<Instance> universes: condEquClasses) {
			// sum += |p|*|p|
			dis += FastMath.pow(universes.size(), 2);
		}
		// Go through m in U/(P&cup;Q)
		for (Collection<Instance> universes: gainedEquClasses) {
			// sum -= |m|*|m|
			dis -= FastMath.pow(universes.size(), 2);
		}
		result = dis;
		return this;
	}
	
	
	@Override
	public DiscernibilityCalculation4TengDiscernibilityView4LongValue
		calculateOuterSignificance(
			Collection<Instance> universes,
			IntegerIterator conditionalAttributes, IntegerIterator outerAttributes,
			IntegerIterator gainedAttributes,
			Object... args
	) {
		// SIG<sub>dis</sub><sup>outer</sup>(a, P, Q) = |DIS(Q/P)| - |DIS(Q/P∪{a})|
		//	U/P
		Collection<Collection<Instance>> condEquClasses =
				TengDiscernibilityViewAlgorithm
					.Basic
					.equivalenceClass(universes, conditionalAttributes)
					.values();
		//	(U/P)/D
		Collection<Collection<Instance>> gainedEquClasses =
				TengDiscernibilityViewAlgorithm
					.Basic
					.gainEquivalenceClass(condEquClasses, gainedAttributes);
		//	DIS(D/P)
		long disB4Gain = calculate(condEquClasses, gainedEquClasses).getResult().intValue();
		calculateOuterSignificance(condEquClasses, disB4Gain, outerAttributes, gainedAttributes, args);
		return this;
	}
	
	@Override
	public DiscernibilityCalculation4TengDiscernibilityView4LongValue calculateOuterSignificance(
			Collection<Collection<Instance>> condEquClasses, Long disB4Gain,
			IntegerIterator outerAttributes, IntegerIterator gainedAttributes, 
			Object... args
	) {
		//	U/(P∪{a}), based on U/P
		Collection<Collection<Instance>> condEquClasses4Outer =
				TengDiscernibilityViewAlgorithm
					.Basic
					.gainEquivalenceClass(condEquClasses, outerAttributes);
		//	(U/(P∪{a}))/Q
		Collection<Collection<Instance>> gainedEquClasses4Outer =
				TengDiscernibilityViewAlgorithm
					.Basic
					.gainEquivalenceClass(condEquClasses4Outer, gainedAttributes);
		//	DIS(Q/P∪{a})
		long dis4Gained = calculate(condEquClasses4Outer, gainedEquClasses4Outer).getResult();
		//	SIG<sub>dis</sub><sup>outer</sup>(a, P, Q) = |DIS(Q/P)| - |DIS(Q/P∪{a})|
		result = disB4Gain - dis4Gained;
		return this;
	}

	
	@Override
	public boolean value1IsBetter(Long v1, Long v2, Long deviation) {
		throw new UnsupportedOperationException("Unimplemented method!");
	}

	@Override
	public Long plus(Long v1, Long v2) throws Exception {
		throw new UnsupportedOperationException("Unimplemented method!");
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		if (item instanceof Collection) {
			Collection<?> collection = (Collection<?>) item;
			return collection.isEmpty() || collection.iterator().next() instanceof Instance;
		}else {
			return false;
		}
	}
}