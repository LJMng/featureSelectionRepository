package featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity;

import featureSelection.basic.model.optimization.AttributeEncoding;

/**
 * Position entity for {@link Particle}.
 * 
 * @author Benjamin_L
 *
 * @param <P>
 * 		Class type of Position.
 */
public interface Position<P> extends AttributeEncoding<P>, Cloneable {
	Position<P> setEncodedValues(P position);
	Position<P> setAttributes(int[] attributes);
	
	Position<P> clone();
}
