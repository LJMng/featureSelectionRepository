package featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value;

/**
 * An interface for Particle Fitness Value in Particle Swarm Optimization.
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		The type of fitness value. Implemented {@link Number}.
 */
public interface FitnessValue<V extends Number> {
	V getValue();
}
