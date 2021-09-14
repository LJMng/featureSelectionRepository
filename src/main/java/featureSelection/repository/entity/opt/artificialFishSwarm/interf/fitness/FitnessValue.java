package featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness;

public interface FitnessValue<FV extends Number> {
	void setFitnessValue(FV v);
	FV getFitnessValue();
	
	void setDependency(Number dep);
	Number getDependency();
	
	int compareTo(FitnessValue<? extends Number> fv);
}
