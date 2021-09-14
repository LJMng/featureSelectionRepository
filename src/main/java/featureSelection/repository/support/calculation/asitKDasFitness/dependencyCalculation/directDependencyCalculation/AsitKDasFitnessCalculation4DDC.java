package featureSelection.repository.support.calculation.asitKDasFitness.dependencyCalculation.directDependencyCalculation;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.dynamics.instance.asitKDasIncremental.fitness.FitnessEvaluationParameters;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

/**
 * Implementation of {@link FeatureImportance4DirectDependencyCalculation} with Asit.K.Das
 * Algorithm <strong>PLUS</strong> DDC algorithm.
 * 
 * @see DefaultAsitKDasFitnessCalculation
 * @see FeatureImportance4DirectDependencyCalculation
 * 
 * @author Benjamin_L
 *
 * @param <FI>
 * 		Type of implemented {@link FeatureImportance4DirectDependencyCalculation} with Double 
 * 		{@link FeatureImportance}.
 */
public class AsitKDasFitnessCalculation4DDC<FI extends FeatureImportance4DirectDependencyCalculation<Double>>
	extends DefaultAsitKDasFitnessCalculation<FI, Double>
	implements FeatureImportance<Double>,
				FeatureImportance4DirectDependencyCalculation<Double>
{
	public static final String CALCULATION_NAME = DefaultAsitKDasFitnessCalculation.CALCULATION_NAME+"(DDC)";
	
	@Getter private Double result;
	@Getter private Double featureImportanceValue;

	public AsitKDasFitnessCalculation4DDC(
			int attributeLength, Collection<Integer> reductB4DataArrived,
			FitnessEvaluationParameters fitnessEvalParams,
			FI featureImportance
	) {
		super(attributeLength, reductB4DataArrived, fitnessEvalParams, featureImportance);
	}

	public AsitKDasFitnessCalculation4DDC<FI> calculate(
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