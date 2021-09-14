package featureSelection.repository.entity.alg.liuQuickHash;

import featureSelection.basic.model.universe.instance.Instance;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;


/**
 * Equivalence Class entity for Liu Quick Hash Algorithm
 * 
 * @author Benjamin_L
 */
public class EquivalenceClass {
	private static int num;
	private final int ID = ++num;
	
	private boolean cons;
	private int decision;
	private int[] attributeValues;
	private Collection<Instance> instances;
	
	public EquivalenceClass() {
		cons = true;
		decision = -1;
		instances = new HashSet<>();
	}
	
	/**
	 * Add an {@link Instance} into the equivalence class
	 * 
	 * @param instance
	 * 		{@link Instance} to be added.
	 * @return false if fail to add into {@link Instance} collection.
	 */
	public boolean addInstance(Instance instance) {
		if (!instances.add(instance)){
			return false;
		}
		if (attributeValues==null){
			attributeValues = instance.getConditionAttributeValues();
		}
		if (decision == -1){
			decision = instance.getAttributeValue(0);
		}else if (decision!=instance.getAttributeValue(0) && cons==true){
			cons = false;
		}
		return true;
	}
	
	/**
	 * Set the consistency value of equivalence class.
	 * 
	 * @param cons 
	 * 		Consistency of the current equivalence class.
	 */
	public void setCons(boolean cons) {
		this.cons = cons;
	}
	
	/**
	 * Get if all {@link Instance}s share the same decision value.
	 * 
	 * @return <code>true</code> if equivalence class is consistent.
	 */
	public boolean cons() {
		return cons;
	}

	/**
	 * Get the normal attribute values.
	 * 
	 * @return An int array./null if |instances| = 0.
	 */
	public int[] normalAttributeValues() {
		return attributeValues;
	}
	
	/**
	 * Return the value of the attributeValues[index].
	 *
	 * @see #attributeValues
	 *
	 * @param index
	 * 		The index. (Starts from 0)
	 * @return the value at attributeValues[index].
	 */
	public int attributeValueAt(int index) {
		return attributeValues[index];
	}
	
	/**
	 * Get the decision value. (if {@link #cons()} returns false, return one of the values.)
	 * 
	 * @return A decision value in int.
	 */
	public int decision() {
		return decision;
	}

	/**
	 * Get the {@link #instances} of equivalence class.
	 * 
	 * @return A {@link Collection} of {@link Instance}s.
	 */
	public Collection<Instance> instances() {
		return instances;
	}

	/**
	 * Get the size of the {@link Instance}s
	 * 
	 * @return An int value.
	 */
	public int instanceSize() {
		return instances.size();
	}

	public String toString() {
		return "Equ-"+ID+" d="+decision+"  "+String.format("cons = %5b",cons)+"  "+
				Arrays.toString(instances.iterator().next().getAttributeValues())+" |U|="+
				instances.size();
	}
}
