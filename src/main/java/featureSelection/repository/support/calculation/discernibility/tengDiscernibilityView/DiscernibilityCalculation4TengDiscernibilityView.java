package featureSelection.repository.support.calculation.discernibility.tengDiscernibilityView;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.alg.TengDiscernibilityViewStrategy;
import featureSelection.repository.algorithm.alg.discernibilityView.TengDiscernibilityViewAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4TengDiscernibilityView;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

public class DiscernibilityCalculation4TengDiscernibilityView 
	implements FeatureImportance4TengDiscernibilityView<Integer>,
				TengDiscernibilityViewStrategy
{
	@Getter private long calculationAttributeLength;
	@Getter private long calculationTimes;
	
	@Getter private Integer result;

	@Override
	public DiscernibilityCalculation4TengDiscernibilityView calculate(
			int insSize, Collection<Collection<Instance>> equClasses,
			Object... args
	) {
		// |DIS(P)| = |U|^2 - &Sigma;<sub>t=1:m</sub>|P<sub>t</sub>|^2
		int sum = 0;
		for (Collection<Instance> equClass: equClasses)	sum += FastMath.pow(equClass.size(), 2);
		result = (int) (FastMath.pow(insSize, 2) - sum);
		return this;
	}
	
	@Override
	public DiscernibilityCalculation4TengDiscernibilityView calculate(
			Collection<Collection<Instance>> condEquClasses,
			Collection<Collection<Instance>> gainedEquClasses, Object... args
			
	) {
		// initiate DIS(Q/P): sum
		int dis = 0;
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
	public DiscernibilityCalculation4TengDiscernibilityView calculateOuterSignificance(
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
		int disB4Gain = calculate(condEquClasses, gainedEquClasses).getResult().intValue();
		calculateOuterSignificance(condEquClasses, disB4Gain, outerAttributes, gainedAttributes, args);
		return this;
	}
	
	@Override
	public DiscernibilityCalculation4TengDiscernibilityView calculateOuterSignificance(
			Collection<Collection<Instance>> condEquClasses, Integer disB4Gain,
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
		int dis4Gained = calculate(condEquClasses4Outer, gainedEquClasses4Outer).getResult().intValue();
		//	SIG<sub>dis</sub><sup>outer</sup>(a, P, Q) = |DIS(Q/P)| - |DIS(Q/P∪{a})|
		result = disB4Gain - dis4Gained;
		return this;
	}

	
	@Override
	public boolean value1IsBetter(Integer v1, Integer v2, Integer deviation) {
		throw new UnsupportedOperationException("Unimplemented method!");
	}

	@Override
	public Integer plus(Integer v1, Integer v2) {
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
