package featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.ReductionAlgorithm4IHS;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.fitness.DefaultFitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.fitness.fValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.fitness.fValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@RoughSet
@ThreadSafetyNotSecured
public abstract class RoughEquivalenceClassBasedIHS<FI extends FeatureImportance<Sig>, Sig extends Number, CollectionItem>
	extends ReductionAlgorithm4IHS<FI, Sig, CollectionItem, FitnessValue<Sig>>
{
	private int insSize;
	
	@Override
	public Fitness<Sig, FitnessValue<Sig>> fitness(
			FI calculation,
			Harmony<?> harmony,
			Collection<CollectionItem> collection, int[] attributes
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
	
	/**
	 * Using compatible {@link FitnessValue} to load Feature Importance value.
	 * 
	 * @param featureImportance
	 * 		The feature importance calculated by {@link FeatureImportance}.
	 * @return loaded {@link FitnessValue4Integer}/{@link FitnessValue4Double} instance.
	 */
	@SuppressWarnings("unchecked")
	protected FitnessValue<Sig> newFitnessValue(Number featureImportance){
		return (FitnessValue<Sig>) 
				(featureImportance instanceof Integer?
					new FitnessValue4Integer(featureImportance.intValue()):
					new FitnessValue4Double(featureImportance.doubleValue())
				);
	}
}