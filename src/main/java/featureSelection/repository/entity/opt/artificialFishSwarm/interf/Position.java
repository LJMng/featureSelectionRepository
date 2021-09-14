package featureSelection.repository.entity.opt.artificialFishSwarm.interf;

import featureSelection.basic.model.optimization.AttributeEncoding;

/**
 * Position model for Artificial Fish Swarm Algorithm.
 * 
 * @author Benjamin_L
 *
 * @param <Posi>
 * 		Class type of position value.
 */
public interface Position<Posi> extends AttributeEncoding<Posi>, Cloneable {
	Posi getPosition();
	void setPosition(Posi p);
	
	boolean containsAttribute(int attribute);
	
	Position<Posi> addAttributeInPosition(int attribute);
	Position<Posi> removeAttributeInPosition(int attribute);
	
	Position<Posi> clone();
}