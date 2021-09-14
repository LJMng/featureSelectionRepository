package featureSelection.repository.support.calculation.asitKDasFitness.dependencyCalculation.incrementalDependencyCalculation;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.dynamics.instance.asitKDasIncremental.fitness.FitnessEvaluationParameters;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.incrementalDependencyCalculation.FeatureImportance4IncrementalDependencyCalculation;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

public class AsitKDasFitnessCalculation4IDC<FI extends FeatureImportance4IncrementalDependencyCalculation<Double>>
	extends DefaultAsitKDasFitnessCalculation<FI, Double>
	implements FeatureImportance<Double>,
				FeatureImportance4IncrementalDependencyCalculation<Double>
{
	public static final String CALCULATION_NAME = DefaultAsitKDasFitnessCalculation.CALCULATION_NAME+"(IDC)";

	@Getter private Double result;
	@Getter private Double featureImportanceValue;

	public AsitKDasFitnessCalculation4IDC(
			int attributeLength, Collection<Integer> reductB4DataArrived,
			FitnessEvaluationParameters fitnessEvalParams,
			FI featureImportance
	) {
		super(attributeLength, reductB4DataArrived, fitnessEvalParams, featureImportance);
	}

	public AsitKDasFitnessCalculation4IDC<FI> calculate(
			Collection<Instance> instances, IntegerIterator attributes, Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		featureImportanceValue = getFeatureImportance().calculate(instances, attributes, args)
													.getResult();
		result = evaluateFitness(
					getAttributeLength(), 
					getReductB4DataArrived(), 
					getFitnessEvalParams(), 
					instances.size(),
					featureImportanceValue, 
					attributes
				);
		return this;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}

	
	@Override
	public Double difference(Double v1, Double v2) {
		return FastMath.abs(v1.doubleValue()-v2.doubleValue());
	}
}