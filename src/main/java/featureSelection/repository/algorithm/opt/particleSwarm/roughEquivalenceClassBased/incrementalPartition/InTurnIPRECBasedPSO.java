package featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.incrementalPartition;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.RoughEquivalenceClassBasedPSO;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;

import java.util.Collection;

/**
 * An implementation of {@link RoughEquivalenceClassBasedPSO}, using <code>Incremental Partition</code>
 * Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong> by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * </ul>
 * 
 * @see AbstractIPRECBasedPSO
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class InTurnIPRECBasedPSO<Sig extends Number>
	extends AbstractIPRECBasedPSO<Sig>
{
	public InTurnIPRECBasedPSO(int universeSize) {
		super(universeSize);
	}
	
	@Override
	public String shortName() {
		return "PSO-IP-REC (In-turn attribute process)";
	}

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Sig> fitnessValue(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig> calculation,
			Collection<EquivalenceClass> collection, int[] attributes
	) {
		Sig sig = calculation.calculate(collection, new IntegerArrayIterator(attributes), getUniverseSize())
							.getResult();
		return (FitnessValue<Sig>) 
				(sig instanceof Integer? 
						new FitnessValue4Integer(sig.intValue()):
						new FitnessValue4Double(sig.doubleValue())
				);
	}
}