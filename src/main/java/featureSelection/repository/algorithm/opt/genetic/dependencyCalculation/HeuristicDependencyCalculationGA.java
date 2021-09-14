package featureSelection.repository.algorithm.opt.genetic.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.HeuristicDependencyCalculationAlgorithm;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.HDCChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.heuristicDependencyCalculation.FeatureImportance4HeuristicDependencyCalculation;

import java.util.Collection;

/**
 * Implementation of {@link DependencyBasedGA} Attribute Reduction with HDC algorithm. 
 * Based on the paper
 * <a href="https://www.sciencedirect.com/science/article/abs/pii/S0031320318301432">
 * "A heuristic based dependency calculation technique for rough set theory"</a> by Muhammad
 * Summair Raza, Usman Qamar.
 * 
 * @see FeatureImportance4HeuristicDependencyCalculation
 * @see HeuristicDependencyCalculationAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class HeuristicDependencyCalculationGA<Chr extends HDCChromosome<?>, Sig extends Number>
	extends DependencyBasedGA<Sig, FeatureImportance4HeuristicDependencyCalculation<Sig>, Chr>
{
	public HeuristicDependencyCalculationGA(Class<Chr> chromosomeClass) {
		super(chromosomeClass);
	}

	private Collection<Integer> decisionValues;
	
	@Override
	public String shortName() {
		return "GA-HDC";
	}

	@Override
	public FitnessValue<Double> calculateFitness(
			FeatureImportance4HeuristicDependencyCalculation<Sig> calculation,
			Collection<Instance> collection, int... attributes
	) {
		Sig sig = calculation.calculate(collection, getDecisionValues(collection), new IntegerArrayIterator(attributes))
							.getResult();
		return new FitnessValue4Double(sig.doubleValue());
	}

	@Override
	public int compareBestFitness(
			Fitness<Chr, FitnessValue<Double>> fitness,
			GenerationRecord<Chr, FitnessValue<Double>> geneRecord
	) {
		Number v1 = fitness==null || fitness.getFitnessValue()==null?
						0.0: fitness.getFitnessValue().getValue();
		Number v2 = geneRecord==null || geneRecord.getBestFitness()==null?
						0.0: geneRecord.getBestFitness().getValue();
		return Double.compare(v1.doubleValue(), v2.doubleValue());
	}

	@Override
	public Collection<Integer> inspection(
			FeatureImportance4HeuristicDependencyCalculation<Sig> calculation,
			Sig sigDeviation, Collection<Instance> collection, int...attributes
	) {
		return HeuristicDependencyCalculationAlgorithm
					.inspection(
							calculation, 
							sigDeviation, 
							collection, 
							getDecisionValues(collection), 
							attributes
				);
	}

	@Override
	public Collection<Integer> inspection(
			FeatureImportance4HeuristicDependencyCalculation<Sig> calculation,
			Sig sigDeviation, Collection<Instance> collection, Collection<Integer> attributes
	) {
		return HeuristicDependencyCalculationAlgorithm
					.inspection(
							calculation, 
							sigDeviation, 
							collection,
							getDecisionValues(collection), 
							attributes
				);
	}

	private Collection<Integer> getDecisionValues(Collection<Instance> universes) {
		if (decisionValues==null) {
			decisionValues = HeuristicDependencyCalculationAlgorithm
								.Basic
								.decisionValues(universes);
		}
		return decisionValues;
	}
}