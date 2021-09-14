package featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.particle;

import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position.HannahPosition;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import lombok.Getter;
import lombok.Setter;

/**
 * H. Hannah Inbarania's Particle model. Check out the original paper 
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0169260713003477">
 * "Supervised hybrid feature selection based on PSO and rough sets for medical diagnosis"
 * </a>
 * <p>
 * Velocity & position units are Integer and byte[], and position bits can only be 1 or 0.
 * Velocity represents the amount of changing position next, whose calculations involve the
 * number of  different bits between two particles relates to the differences of their position.
 * <p>
 * For example, if Position 1 = [1,0,1] and Position 2 = [1,1,1], the the difference between
 * them can be represented as the number of different values ( [1-1, 0-1, 1-1] where the value
 * is not 0). In this case, the difference is 1.
 */
@Getter @Setter 
public class HannahParticle<FValue extends FitnessValue<?>>
	implements Particle<Integer, Position<byte[]>, FValue>
{
	private Position<byte[]> position;
	private Integer velocity;
	private Fitness<Position<byte[]>, FValue> fitness;
	
	private HannahParticle() {	}
	public HannahParticle(Integer velocity, byte[] position) {
		this.velocity = velocity;
		this.position = new HannahPosition(position);
	}

	/**
	 * Check if particle contains the attributes index.
	 */
	@Override
	public boolean containsAttribute(int attributeIndex) {
		return position.encodedValues()[attributeIndex]==(byte) 1;
	}
	
	@Override
	public String toString() {
		return "HannahParticle [position=" + position + ", velocity=" + velocity + ", fitness=" + 
				(fitness==null?null:fitness.getFitnessValue()) + "]";
	}

	@Override
	protected HannahParticle<FValue> clone() throws CloneNotSupportedException {
		HannahParticle<FValue> clone = new HannahParticle<>();
		clone.setVelocity(velocity);
		Fitness<Position<byte[]>, FValue> fitnessClone = fitness.clone();
		clone.setFitness(fitnessClone);
		clone.setPosition(fitnessClone.getPosition());
		return clone;
	}
}