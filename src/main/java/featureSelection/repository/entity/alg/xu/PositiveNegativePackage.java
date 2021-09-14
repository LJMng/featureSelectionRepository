package featureSelection.repository.entity.alg.xu;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.model.universe.instance.Instance;

import java.util.HashSet;
import java.util.Set;

import java.util.Collection;

@ReturnWrapper
public class PositiveNegativePackage {
	/**
	 * Positive region
	 */
	private Collection<Instance> positive;
	/**
	 * Negative region
	 */
	private Collection<Instance> negative;
	
	public PositiveNegativePackage(Collection<Instance> positive, Collection<Instance> negative){
		this.positive = positive;
		this.negative = negative;
	}
	public PositiveNegativePackage(){
		positive = new HashSet<>();
		negative = new HashSet<>();
	}
	
	/**
	 * Add a positive item
	 * 
	 * @param universe
	 * 		The universe to be added with
	 * @return true if added successfully
	 */
	public boolean addPositive(Instance universe) {
		return positive.add(universe);
	}
	
	/**
	 * Add positive items
	 * 
	 * @param universes
	 * 		The universes to be added with
	 * @return true if added successfully
	 */
	public boolean addAllPositive(Collection<Instance> universes) {
		return positive.addAll(universes);
	}
	
	/**
	 * Get the positive region
	 * 
	 * @return A Collection of Universe
	 */
	public Collection<Instance> getPositiveSet(){
		return positive;
	}
	
	/**
	 * Get the size of the positive region
	 * 
	 * @return An int value
	 */
	public int getPositiveSize() {
		return positive.size();
	}
	
	/**
	 * Add a negative item
	 * 
	 * @param universe
	 * 		The universe to be added with
	 * @return true if added successfully
	 */
	public boolean addNegative(Instance universe) {
		return negative.add(universe);
	}
		
	/**
	 * Add negative items
	 * 
	 * @param universes
	 * 		The universes to be added with
	 * @return true if added successfully
	 */
	public boolean addAllNegative(Collection<Instance> universes) {
		return negative.addAll(universes);
	}
	
	/**
	 * Get the negative region
	 * 
	 * @return A Collection of Universe
	 */
	public Collection<Instance> getNegativeSet(){
		return negative;
	}
	
	/**
	 * Get the size of the negative region
	 * 
	 * @return An int value
	 */
	public int getNegativeSize() {
		return negative.size();
	}

	public Collection<Instance> getMix(){
		Set<Instance> mix = new HashSet<>(positive);
		mix.addAll(negative);
		return mix;
	}
	
	/**
	 * Remove a universe item from the positive region
	 * 
	 * @param universe
	 * 		The universe to be removed
	 * @return true if removed successfully
	 */
	public boolean removeFromPositive(Instance universe) {
		return positive.remove(universe);
	}
	
	/**
	 * Remove a universe item from the negative region
	 * 
	 * @param universe
	 * 		The universe to be removed
	 * @return true if removed successfully
	 */
	public boolean removeFromNegative(Instance universe) {
		return negative.remove(universe);
	}
	
	/**
	 * Remove all universes item from the positive region
	 * 
	 * @param universe
	 * 		The universes to be removed
	 * @return true if removed successfully
	 */
	public boolean removeAllFromPositive(Collection<Instance> universe) {
		if (universe.size()==0)	return true;
		else					return positive.removeAll(universe);
	}
	
	/**
	 * Remove all universes item from the positive region
	 * 
	 * @param universe
	 * 		The universes to be removed
	 * @return true if removed successfully
	 */
	public boolean removeAllFromNegative(Collection<Instance> universe) {
		if (universe.size()==0)	return true;
		else					return negative.removeAll(universe);
	}
}