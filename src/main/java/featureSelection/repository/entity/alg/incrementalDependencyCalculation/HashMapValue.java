package featureSelection.repository.entity.alg.incrementalDependencyCalculation;

/**
 * A structure entity for Incremental Dependency Calculation Algorithm, is used to calculate
 * consistent records.
 * 
 * @author Benjamin_L
 */
public class HashMapValue {
	private boolean cons;
	private int decision, count;

	public HashMapValue(boolean cons, int decision){
		this.cons = cons;
		this.decision = decision;
		count = 1;
	}
	
	public void add() {
		count++;
	}
	
	public void setCons(boolean cons) {
		this.cons = cons;
	}
	
	public boolean cons() {
		return cons;
	}
	
	public int decisionValue() {
		return decision;
	}
	
	public int count() {
		return count;
	}
}
