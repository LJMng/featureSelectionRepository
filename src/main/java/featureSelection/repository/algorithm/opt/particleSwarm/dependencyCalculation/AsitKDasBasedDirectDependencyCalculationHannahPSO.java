package featureSelection.repository.algorithm.opt.particleSwarm.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.DirectDependencyCalculationAlgorithm;
import featureSelection.repository.algorithm.opt.particleSwarm.AbstractHannahPSO;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.HannahFitness;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Double4AsitKDas;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.particle.HannahParticle;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position.HannahPosition;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue4AsitKDas;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import featureSelection.repository.support.calculation.asitKDasFitness.dependencyCalculation.directDependencyCalculation.AsitKDasFitnessCalculation4DDC;

import java.util.Collection;

/**
 * Using {@link DirectDependencyCalculationAlgorithm} and Asit.K.Das fitness calculation for
 * fitness calculations and inspections in Particle Swarm Optimization.
 * 
 * @see DefaultAsitKDasFitnessCalculation
 * @see AsitKDasFitnessCalculation4DDC
 * @see AbstractHannahPSO
 * @see DirectDependencyCalculationAlgorithm
 * @see FeatureImportance4DirectDependencyCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class AsitKDasBasedDirectDependencyCalculationHannahPSO
	implements ReductionAlgorithm<Instance, Integer, HannahPosition, FitnessValue4AsitKDas<Double, Double>, AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>>, Double>
{
	@Override
	public String shortName() {
		return "PSO-AsitKDas-DDC";
	}

	@Override
	public Collection<Integer> inspection(
			AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>> calculation,
			Double sigDeviation, Collection<Instance> collection, int[] positionAttr
	) {
		return DirectDependencyCalculationAlgorithm
					.inspection(
						calculation.getFeatureImportance(), 
						sigDeviation, 
						collection, 
						positionAttr
				);
	}

	@Override
	public Collection<Integer> inspection(
			AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>> calculation, 
			Double sigDeviation, Collection<Instance> collection,
			Collection<Integer> positionAttr
	) {
		return DirectDependencyCalculationAlgorithm
				.inspection(
					calculation.getFeatureImportance(),
					sigDeviation,
					collection,
					positionAttr
				);
	}

	@Override
	public FitnessValue4AsitKDas<Double, Double> fitnessValue(
			AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>> calculation,
			Collection<Instance> collection, int[] attributes
	) {
		calculation.calculate(collection, new IntegerArrayIterator(attributes));
		
		double fitnessValue = calculation.getResult();
		double featureImportance = calculation.getFeatureImportanceValue();
		
		return new FitnessValue4Double4AsitKDas(fitnessValue, featureImportance);
	}

	public HannahFitness<FitnessValue4AsitKDas<Double, Double>> fitness(
			AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>> calculation, 
			Collection<Instance> collection, int[] attributesSrc, int[] attributeIndexes
	) {
		int[] attrValues;
		if (attributesSrc.length==attributeIndexes.length) {
			attrValues = attributesSrc;		// => {1, 2, ..., C}
		}else {
			attrValues = new int[attributeIndexes.length];
			for (int i=0; i<attrValues.length; i++)	attrValues[i] = attributesSrc[attributeIndexes[i]];
		}
		return new HannahFitness<>(
				fitnessValue(calculation, collection, attrValues),
				toPosition(attributeIndexes, attributesSrc.length)
			);
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public Fitness<HannahPosition, FitnessValue4AsitKDas<Double, Double>>[] fitness(
			AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>> calculation, 
			Collection<Instance> collection, int[] attributesSrc,
			Particle<Integer, HannahPosition, FitnessValue4AsitKDas<Double, Double>>...particle
	) {
		@SuppressWarnings("rawtypes")
		HannahFitness[] fitness = new HannahFitness[particle.length];
		for (int i=0; i<fitness.length; i++) {
			fitness[i] = fitness(calculation, 
								collection, 
								attributesSrc,
								particle[i].getPosition().getAttributes()
						);
		}
		return fitness;
	}
	
	@Override
	public int compareMaxFitness(
			FitnessValue4AsitKDas<Double, Double> maxFitnessValue,
			FitnessValue4AsitKDas<Double, Double> fitnessValue
	) {
		return Double.compare(
				maxFitnessValue==null? 0: maxFitnessValue.getValue().doubleValue(), 
				fitnessValue==null? 0: fitnessValue.getValue().doubleValue()
			);
	}
	@Override
	public int compareMaxFitness(FitnessValue4AsitKDas<Double, Double> maxFitnessValue, 
								GenerationRecord<Integer, HannahPosition, FitnessValue4AsitKDas<Double, Double>> generRecord
	) {
		return Double.compare(
				maxFitnessValue==null? 0: maxFitnessValue.getValue().doubleValue(), 
				generRecord.globalBestFitnessValue().doubleValue()
			);
	}
	@Override
	public int compareFitness(Fitness<HannahPosition, FitnessValue4AsitKDas<Double, Double>> fitness1, Fitness<HannahPosition, FitnessValue4AsitKDas<Double, Double>> fitness2) {
		return Double.compare(
				fitness1==null?0:fitness1.getFitnessValue().getValue().doubleValue(), 
				fitness2==null?0:fitness2.getFitnessValue().getValue().doubleValue()
			);
	}
	
	@Override
	public HannahPosition toPosition(int[] attributeIndexes, int positionSize) {
		return new HannahPosition(attributeIndexes, positionSize);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Particle> getParticleClass() {
		return HannahParticle.class;
	}

}