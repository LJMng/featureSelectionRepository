package featureSelection.repository.algorithm.opt.improvedHarmonySearch.dependencyCalculation;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.DirectDependencyCalculationAlgorithm;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.ReductionAlgorithm4IHS;
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.fitness.DefaultFitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.fitness.fValue.FitnessValue4Double4AsitKDas;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import featureSelection.repository.support.calculation.asitKDasFitness.dependencyCalculation.directDependencyCalculation.AsitKDasFitnessCalculation4DDC;

import java.util.Collection;

/**
 * Using {@link DirectDependencyCalculationAlgorithm} for fitness calculations and inspections in 
 * Improved Harmony Search.
 * 
 * @see DefaultAsitKDasFitnessCalculation
 * @see AsitKDasFitnessCalculation4DDC
 * @see DirectDependencyCalculationAlgorithm
 * @see FeatureImportance4DirectDependencyCalculation
 * 
 * @author Benjamin_L
 */
public class AsitKDasBasedDirectDependencyCalculationIHS<Cal extends AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>>>
	extends ReductionAlgorithm4IHS<Cal, Double, Instance, FitnessValue4Double4AsitKDas>
{
	@Override
	public String shortName() {
		return "IHS-AsitKDas-DDC";
	}

	@Override
	public Fitness<Double, FitnessValue4Double4AsitKDas> fitness(
			Cal calculation,
			Harmony<?> harmony,
			Collection<Instance> collection, int[] attributes
	) {
		if (attributeIndexDictionary==null)	initAttributeIndexDictionary(attributes);
		
		return new DefaultFitness<>(
				fitnessValue(
					calculation, 
					collection, 
					attributes, 
					harmony.getAttributes()
				)
			);
	}
	
	@Override
	public FitnessValue4Double4AsitKDas fitnessValue(
			Cal calculation, Collection<Instance> collection, int[] attributes
	) {
		return new FitnessValue4Double4AsitKDas(
				calculation.getFeatureImportanceValue(),
				calculation.calculate(collection, new IntegerArrayIterator(attributes))
							.getResult()
							.doubleValue()
			);
	}

	@Override
	public Collection<Integer> inspection(
			Cal calculation, Double sigDeviation, Collection<Instance> collection,
			int[] attributes
	) {
		return DirectDependencyCalculationAlgorithm
				.inspection(
					calculation.getFeatureImportance(), 
					sigDeviation, 
					collection, 
					attributes
				);
	}
	
	/**
	 * Compare by {@link FitnessValue4Double4AsitKDas#getFeatureImportance()}.
	 */
	@Override
	public int compareToMaxFitness(
			Fitness<Double, FitnessValue4Double4AsitKDas> bestFitness,
			ReductionParameters<Double, Harmony<?>, FitnessValue4Double4AsitKDas> params
	) {
		double bestFitnessV = bestFitness==null?0: bestFitness.getFitnessValue().getValue();
		return Double.compare(bestFitnessV, params.getMaxFitness().getValue().doubleValue());
	}

	/**
	 * Compare by {@link FitnessValue4Double4AsitKDas#getFeatureImportance()}.
	 */
	@Override
	public int compareToBestFitness(
			GenerationRecord<FitnessValue4Double4AsitKDas> geneRecord,
			Fitness<Double, FitnessValue4Double4AsitKDas> fitness
	) {
		double bestFitnessV = geneRecord.getBestFitness()==null? 0: geneRecord.getBestFitness().getValue().doubleValue();
		double fitnessV = fitness==null? 0: fitness.getFitnessValue().getValue().doubleValue();
		return Double.compare(bestFitnessV, fitnessV);
	}
}