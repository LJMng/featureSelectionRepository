package featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value;

import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;

/**
 * An interface for Particle Fitness Value in Asit.K.Das <code>Particle Swarm Optimization</code>.
 * <p>
 * Fitness contains the <strong>calculated fitness value</strong> defined in the paper 
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S1568494618300462">"A group incremental
 * feature selection for classification using rough set theory based genetic algorithm"</a> by
 * Asit.K.Das, Shampa Sengupta, Siddhartha Bhattacharyya and the <strong>feature subset
 * significance(importance)</strong> value.
 * 
 * @see FitnessValue
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		The type of fitness value. Implemented {@link Number}.
 * @param <Sig>
 * 		The type of feature importance. Implemented {@link Number}.
 */
public interface FitnessValue4AsitKDas<V extends Number, Sig extends Number> extends FitnessValue<V>{
	V getValue();
	/**
	 * Get the feature importance of attributes that the {@link Particle} represents.
	 * 
	 * @return {@link Sig} as feature importance.
	 */
	Sig getFeatureImportance();
}
