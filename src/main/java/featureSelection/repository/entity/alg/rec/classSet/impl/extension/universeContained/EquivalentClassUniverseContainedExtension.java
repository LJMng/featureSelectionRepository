package featureSelection.repository.entity.alg.rec.classSet.impl.extension.universeContained;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;

/**
 * An extension model of {@link EquivalenceClass}. This class contains {@link Instance} members.
 * 
 * @see RoughEquivalentClassBasedExtensionAlgorithm.IncrementalDecision
 * 
 * @author Benjamin_L
 */
public class EquivalentClassUniverseContainedExtension
	extends EquivalenceClass
{
	@Getter private Collection<Instance> universes;
	
	public EquivalentClassUniverseContainedExtension(Instance ins) {
		super(ins);
		universes = new HashSet<>();
		universes.add(ins);
	}
		
	/**
	 * Add a universe, and update dValueMap.
	 * 
	 * @param ins
	 * 		The universe instance to be added. / false if couldn't add the universe.
	 * @return true if added successfully
	 */
	@Override
	public void addClassItem(Instance ins) {
		super.addClassItem(ins);
		universes.add(ins);
	}
}