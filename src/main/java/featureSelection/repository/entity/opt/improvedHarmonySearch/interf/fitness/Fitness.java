package featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness;

import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;

/**
 * Fitness of the Harmony in <code>Improved Harmony Search Algorithm</code>.
 * 
 * @author Benjamin_L
 */
public interface Fitness<Sig extends Number, FValue extends FitnessValue<Sig>> {
	FValue getFitnessValue();
	void setFitnessValue(FValue value);
	
	int compareToFitness(Fitness<Sig, FValue> fitness);
}
