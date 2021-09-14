package featureSelection.repository.algorithm.opt.genetic.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.IncrementalDependencyCalculationAlgorithm;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.IDCChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.incrementalDependencyCalculation.FeatureImportance4IncrementalDependencyCalculation;

import java.util.Collection;

/**
 * Implementation of {@link DependencyBasedGA} Attribute Reduction with IDC algorithm. 
 * Based on the paper
 * <a href="https://www.sciencedirect.com/science/article/pii/S0020025516000785">
 * "An incremental dependency calculation technique for feature selection using rough sets"</a> 
 * by Muhammad Summair Raza, Usman Qamar.
 * 
 * @see FeatureImportance4IncrementalDependencyCalculation
 * @see IncrementalDependencyCalculationAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class IncrementalDependencyCalculationGA<Chr extends IDCChromosome<?>, Sig extends Number>
	extends DependencyBasedGA<Sig, FeatureImportance4IncrementalDependencyCalculation<Sig>, Chr>
{
	public IncrementalDependencyCalculationGA(Class<Chr> chromosomeClass) {
		super(chromosomeClass);
	}

	@Override
	public String shortName() {
		return "GA-IDC";
	}
	
	@Override
	public FitnessValue<Double> calculateFitness(
			FeatureImportance4IncrementalDependencyCalculation<Sig> calculation,
			Collection<Instance> collection, int... attributes
	) {
		Sig sig = calculation.calculate(collection, new IntegerArrayIterator(attributes))
							.getResult();
		return new FitnessValue4Double(sig.doubleValue());
	}

	@Override
	public Collection<Integer> inspection(
			FeatureImportance4IncrementalDependencyCalculation<Sig> calculation,
			Sig sigDeviation, Collection<Instance> collection, int...attributes
	) {
		return IncrementalDependencyCalculationAlgorithm
				.inspection(
						calculation, 
						sigDeviation, 
						collection, 
						attributes
				);
	}

	@Override
	public Collection<Integer> inspection(
			FeatureImportance4IncrementalDependencyCalculation<Sig> calculation,
			Sig sigDeviation, Collection<Instance> collection, Collection<Integer> attributes
	) {
		IncrementalDependencyCalculationAlgorithm
			.inspection(
					calculation, 
					sigDeviation, 
					collection, 
					attributes
		);
		return attributes;
	}
}