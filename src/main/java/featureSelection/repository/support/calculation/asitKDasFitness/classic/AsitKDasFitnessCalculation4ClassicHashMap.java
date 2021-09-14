package featureSelection.repository.support.calculation.asitKDasFitness.classic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.dynamics.instance.asitKDasIncremental.fitness.FitnessEvaluationParameters;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import lombok.Getter;

/**
 * An implementation of {@link DefaultAsitKDasFitnessCalculation} using Classic {@link HashMap} 
 * Feature Importance calculation.
 * 
 * @see DefaultAsitKDasFitnessCalculation
 * @see ClassicHashMapCalculation
 * 
 * @author Benjamin_L
 *
 * @param <FI>
 * 		Implemented {@link ClassicHashMapCalculation}.
 */
public class AsitKDasFitnessCalculation4ClassicHashMap<FI extends ClassicHashMapCalculation<Double>>
	extends DefaultAsitKDasFitnessCalculation<FI, Double>
	implements ClassicHashMapCalculation<Double>
{
	public static final String CALCULATION_NAME = DefaultAsitKDasFitnessCalculation.CALCULATION_NAME;
	
	@Getter private Double result;
	@Getter private Double featureImportanceValue;

	public AsitKDasFitnessCalculation4ClassicHashMap(
			int attributeLength, Collection<Integer> reductB4DataArrived,
			FitnessEvaluationParameters fitnessEvalParams,
			FI featureImportance
	) {
		super(attributeLength, reductB4DataArrived, fitnessEvalParams, featureImportance);
	}
	
	public AsitKDasFitnessCalculation4ClassicHashMap<FI> calculate(
			Collection<Instance> instances,
			IntegerIterator attributes, Map<Integer, Collection<Instance>> decEClasses,
			Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		featureImportanceValue = getFeatureImportance().calculate(instances, attributes, decEClasses, args)
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
}