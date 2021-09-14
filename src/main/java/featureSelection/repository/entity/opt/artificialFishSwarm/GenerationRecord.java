package featureSelection.repository.entity.opt.artificialFishSwarm;

import java.util.Collection;
import java.util.LinkedList;

import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Generation record for Artificial Fish Swarm.
 * 
 * @author Benjamin_L
 *
 * @param <Posi>
 * 		Implemented {@link Position}.
 */
@ToString
public class GenerationRecord<Posi extends Position<?>, Sig> {
	/**
	 * Current generation/iteration.
	 */
	@Getter private int generation;
	
	/**
	 * Current best feature significance value.
	 */
	@Getter private Sig bestFeatureSignificance;
	/**
	 * Recorded positions with the correspondent best fitness value.
	 */
	@Getter private Collection<Posi> bestFitnessPosition;
	
	@Getter @Setter private double globalDependency;
	
	/**
	 * The least number of attributes.
	 */
	@Getter @Setter private Integer leastAttr;
	
	public GenerationRecord() {
		generation = 0;
		bestFitnessPosition = new LinkedList<>();
	}

	public void nextGeneration() {
		generation++;
	}
	
	public void updateBestFeatureSignificance(
			Posi position, Sig bestFeatureSignificance
	) {
		if (bestFitnessPosition.isEmpty()) {
			bestFitnessPosition.add(position);
			this.bestFeatureSignificance = bestFeatureSignificance;
			setLeastAttr(position.getAttributes().length);
		}else {
			int redLength = position.getAttributes().length;
			int cmp = Integer.compare(this.leastAttr, redLength);
			if (cmp<0) {
				return;
			}else if (cmp>0) {
				bestFitnessPosition.clear();
				this.bestFeatureSignificance = bestFeatureSignificance;
				setLeastAttr(redLength);
			}
			bestFitnessPosition.add(position);
		}
	}
}
