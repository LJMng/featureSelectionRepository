package featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity;

import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;

/**
 * Particle model with local best fitness stored.
 * <p>
 * Notice that the <strong>local(history) best fitness</strong> should be stored(using setter/
 * getter for modifying and acquiring:{@link #setFitness(Fitness)}, {@link #getFitness()}) apart
 * from the <strong>current particle position</strong>({@link #setPosition(Position)},
 * {@link #getPosition()}) and <strong>current velocity</strong>({@link #setVelocity(Object)},
 * {@link #getVelocity()}).
 * 
 * @param <Velocity>
 * 		Velocity
 * @param <Posi>
 * 		Implemented {@link Position}.
 * @param <FValue>
 * 		Implemented {@link FitnessValue}.
 */
public interface Particle<Velocity, Posi extends Position<?>, FValue extends FitnessValue<?>> {
	boolean containsAttribute(int attribute);
	
	/**
	 * Particle's <strong>local best fitness</strong>.
	 * <p>
	 * So it should contains info. of the correspondent local best {@link Position} and 
	 * {@link FValue}.
	 * 
	 * @return {@link Fitness} instance.
	 */
	Fitness<Posi, FValue> getFitness();
	/**
	 * Set particle's <strong>local best fitness</strong>.
	 * 
	 * @see #getFitness()
	 * 
	 * @param fitness
	 * 		A {@link Fitness} instance.
	 */
	void setFitness(Fitness<Posi, FValue> fitness);
	
	/**
	 * Particle's current position.
	 * 
	 * @return {@link Posi}
	 */
	Posi getPosition();
	/**
	 * Set particle's current position.
	 * @param p
	 * 		{@link Posi} instance.
	 */
	void setPosition(Posi p);
	
	Velocity getVelocity();
	void setVelocity(Velocity velocity);	
}
