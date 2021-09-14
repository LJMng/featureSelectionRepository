package featureSelection.repository.algorithm.opt.genetic.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.DirectDependencyCalculationAlgorithm;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.DDCChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;

import java.util.Collection;

/**
 * Implementation of {@link DependencyBasedGA} Attribute Reduction with DDC algorithm. 
 * Based on the paper
 * <a href="https://www.sciencedirect.com/science/article/abs/pii/S0888613X17300178">
 * "Feature selection using rough set-based direct dependency calculation by avoiding the
 * positive region"</a> by Muhammad Summair Raza, Usman Qamar.
 * 
 * @see FeatureImportance4DirectDependencyCalculation
 * @see DirectDependencyCalculationAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class DirectDependencyCalculationGA<Chr extends DDCChromosome<?>, Sig extends Number>
	extends DependencyBasedGA<Sig, FeatureImportance4DirectDependencyCalculation<Sig>, Chr>
{
	public DirectDependencyCalculationGA(Class<Chr> chromosomeClass) {
		super(chromosomeClass);
	}

	@Override
	public String shortName() {
		return "GA-DDC";
	}

	@Override
	public FitnessValue<Double> calculateFitness(
			FeatureImportance4DirectDependencyCalculation<Sig> calculation,
			Collection<Instance> collection, int... attributes
	) {
		Sig sig = calculation.calculate(collection, new IntegerArrayIterator(attributes))
							.getResult();
		return new FitnessValue4Double(sig.doubleValue());
	}

	@Override
	public Collection<Integer> inspection(
			FeatureImportance4DirectDependencyCalculation<Sig> calculation,
			Sig sigDeviation, Collection<Instance> collection, int... attributes
	) {
		return DirectDependencyCalculationAlgorithm
					.inspection(
							calculation, 
							sigDeviation, 
							collection, 
							attributes
				);
	}

	@Override
	public Collection<Integer> inspection(
			FeatureImportance4DirectDependencyCalculation<Sig> calculation,
			Sig sigDeviation, Collection<Instance> collection,
			Collection<Integer> attributes
	) {
		DirectDependencyCalculationAlgorithm
			.inspection(
					calculation, 
					sigDeviation, 
					collection,
					attributes
		);
		return attributes;
	}
}