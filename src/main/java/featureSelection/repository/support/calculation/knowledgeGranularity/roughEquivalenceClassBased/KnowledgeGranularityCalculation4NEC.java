package featureSelection.repository.support.calculation.knowledgeGranularity.roughEquivalenceClassBased;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadUnsafe;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.support.calculation.knowledgeGranularity.DefaultKnowledgeGranularityCalculation;
import lombok.Getter;

/**
 * Knowledge Granularity Calculation for NEC.
 * 
 * @see DefaultKnowledgeGranularityCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadUnsafe
public class KnowledgeGranularityCalculation4NEC
	extends DefaultKnowledgeGranularityCalculation
{
	@Getter private long calculationTimes = 0;	
	
	/**
	 * Calculate the <strong>knowledge granularity</strong> of A based on the given
	 * equivalence classes (<i>U</i>/<i>C</i>)
	 * <p>
	 * where <i>A</i> is a subset of conditional attributes: <i>A</i> ⊂ <i>C</i>.
	 * 
	 * @param equClasses
	 * 		{@link EquivalenceClass} {@link Collection}: <strong>U/A</strong>
	 * @param insSize
	 * 		The size of {@link Instance}: <strong>|U|</strong>
	 * @return this {@link KnowledgeGranularityCalculation4NEC} instance.
	 */
	public KnowledgeGranularityCalculation4NEC calculate4ConditionalAttributes(
			Collection<EquivalenceClass> equClasses, int insSize
	) {
		// count calculate
		calculationTimes++;
		// calculate
		calculate(
			equClasses.stream().mapToInt(EquivalenceClass::getInstanceSize).toArray(),
			insSize
		);
		return this;
	}
	
	/**
	 * Calculate the <strong>knowledge granularity</strong> of A based on the given
	 * equivalence classes(U/A).
	 * 
	 * @param decEquClasses
	 * 		{@link EquivalenceClass} {@link Collection}: <strong>U/D</strong>
	 * @param insSize
	 * 		The size of {@link Instance}: <strong>|U|</strong>
	 * @return this {@link KnowledgeGranularityCalculation4NEC} instance.
	 */
	public KnowledgeGranularityCalculation4NEC calculate4DecisionAttribute(
			Collection<Collection<Instance>> decEquClasses, int insSize
	) {
		// count calculate
		calculationTimes++;
		// calculate
		calculate(decEquClasses.stream().mapToInt(Collection::size).toArray(), insSize);
		return this;
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		throw new UnsupportedOperationException("Unimplemented method!");
	}

	@Override
	public long getCalculationAttributeLength() {
		throw new UnsupportedOperationException("Unimplemented method!");
	}
}