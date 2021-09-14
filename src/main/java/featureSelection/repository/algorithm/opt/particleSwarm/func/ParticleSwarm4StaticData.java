package featureSelection.repository.algorithm.opt.particleSwarm.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import org.apache.commons.math3.util.FastMath;

/**
 * Procedure algorithm for Optimization Algorithm of <strong>Particle Swarm Optimization</strong> 
 * <code>PLUS</code> Attribute Reduction.
 * <p>
 * <strong>PS</strong>: This class is only for analysis in constructing such execution, please
 * use the correspondent {@link ProcedureComponent} tester instead for formal usage.
 * 
 * @author Benjamin_L
 */
public class ParticleSwarm4StaticData {
	/**
	 * Update each particle's individual best fitness.
	 * <p>
	 * Loop over each {@link Fitness} in <code>particleFitness</code>, check if its fitness is 
	 * better than the one in {@link Particle#getFitness()}. 
	 * If it is better, replace:
	 * <pre>
	 * particle[i].setPosition((Posi) particleFitness[i].getPosition().clone());
	 * particle[i].setFitness(particleFitness[i]);
	 * </pre>
	 * <p>
	 * <strong>Before</strong> reaching {@link ReductionParameters#getMaxFitness()}, {@link Position} in
	 * {@link Particle} will be replaced by the one in {@link Fitness}. 
	 * <p>
	 * <strong>After</strong> reaching, {@link Position} in {@link Particle} will not be replaced by the 
	 * one in {@link Fitness}.
	 * 
	 * @param <Cal>
	 * 		Type of implemented {@link FeatureImportance}
	 * @param <Sig>
	 * 		Type of feature significance that implements {@link Number}.
	 * @param <CollectionItem> 
	 * 		{@link Instance} or <code>EquivaenceClass</code>.
	 * @param <Velocity> 
	 * 		Class extends {@link Number} as values of velocity.
	 * @param <Posi> 
	 * 		Class extends {@link Position}.
	 * @param <FValue>
	 * 		Type of implemented {@link FitnessValue}.
	 * @param particle
	 * 		An array of {@link Particle}.
	 * @param redAlg
	 * 		{@link ReductionAlgorithm}.
	 * @param particleFitness
	 * 		An array of {@link Fitness}.
	 * @return return {@link boolean[]} to indicate which particles' individual best fitenss have been 
	 * 		updated.
	 */
	@SuppressWarnings("unchecked")
	public static <Cal extends FeatureImportance<Sig>, Sig extends Number, CollectionItem, Velocity,
					Posi extends Position<?>, FValue extends FitnessValue<?>> boolean[]
		updateParticleIndividualBestFitness(
				Particle<Velocity, Posi, FValue>[] particle,
				ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig> redAlg,
				Fitness<Posi, FValue>[] particleFitness
	) {
		boolean[] updated = new boolean[particle.length];
		for (int i=0; i<particleFitness.length; i++) {
			if (particle[i].getFitness()==null ||
				redAlg.compareFitness(particle[i].getFitness(), particleFitness[i])<0
			) {
				particle[i].setPosition((Posi) particleFitness[i].getPosition().clone());
				particle[i].setFitness(particleFitness[i]);
				updated[i] = true;
			}
		}
		return updated;
	}
	
	/**
	 * Inspect particle position/attributes redundancy with {@link Fitness}.
	 * 
	 * @param <Cal>
	 * 		Implemented {@link FeatureImportance}.
	 * @param <Sig>
	 * 		Type of feature significance.
	 * @param <CollectionItem> 
	 * 		{@link Instance} or <code>EquivaenceClass</code>.
	 * @param <Velocity> 
	 * 		{@link Number} as values of velocity.
	 * @param <Posi> 
	 * 		Implemented {@link Position}.
	 * @param <FValue>
	 * 		Type of {@link FitnessValue}.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance}.
	 * @param collectionList
	 * 		A collection of universe/equivalent classes.
	 * @param particle
	 * 		An array of {@link Particle}.
	 * @param params
	 * 		{@link ReductionParameters}.
	 */
	public static <Cal extends FeatureImportance<Sig>, Sig extends Number, CollectionItem, Velocity, 
					Posi extends Position<?>, FValue extends FitnessValue<?>> void
			particleLocalSearch(
				Cal calculation, Sig sigDeviation,
				Collection<CollectionItem> collectionList, Particle<Velocity, Posi, FValue> particle, 
				ReductionParameters<Velocity, Posi, FValue> params
	) {
		int[] attributesSrc = params.getAttributes();
		int attrLength = attributesSrc.length;
		
		FValue examSig, maxSig;
		int maxAttrIndex = -1;
		Fitness<Posi, FValue> maxFitness = particle.getFitness();
		@SuppressWarnings("unchecked")
		ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig> redAlg = params.getReductionAlgorithm();
		int[] examAttr = new int[particle.getPosition().getAttributes().length+1];
		for (int i=0; i<examAttr.length-1; i++)
			examAttr[i] = attributesSrc[particle.getPosition().getAttributes()[i]];
		// Go through a in C-R
		maxSig = maxFitness.getFitnessValue();
		for (int i=0; i<attributesSrc.length; i++) {
			if (!particle.containsAttribute(attributesSrc[i])) {
				// Calculate the fitness of R U {a}
				examAttr[examAttr.length-1] = attributesSrc[i];
				examSig = redAlg.fitnessValue(calculation, collectionList, examAttr);
				//Fit > p.bestFitness
				if (Double.compare(examSig.getValue().doubleValue(), maxSig.getValue().doubleValue())>0) {
					maxAttrIndex = i;
					maxSig = examSig;
				}
			}
		}
		if (maxAttrIndex!=-1) {
			int[] newIndexes = Arrays.copyOf(particle.getPosition().getAttributes(), examAttr.length);
			newIndexes[newIndexes.length-1] = maxAttrIndex;
			// 更新p.position；
			// p.best = p.position;	p.bestFiness = Fit;
			particle.setFitness(maxFitness);
			particle.setPosition(redAlg.toPosition(newIndexes, attrLength));
		}
	}
	
	/**
	 * Transfer particle positions to Attributes in {@link Integer}.
	 * 
	 * @param <Sig>
	 * 		Type of Feature(subset) Significance.
	 * @param <Velocity> 
	 * 		Class extends {@link Number} as values of velocity
	 * @param <Posi> 
	 * 		Class extends {@link Position}.
	 * @param <FValue>
	 * 		Type of {@link FitnessValue}.
	 * @param attributesSrc
	 * 		Attributes source. (starts from 1)
	 * @param redAlg
	 * 		{@link ReductionAlgorithm} instance.
	 * @param geneRecord
	 * 		{@link GenerationRecord} instance.
	 * @param particles
	 * 		The {@link Particle}s to be transfered.
	 * @param collectionItem
	 * 		Collection items in {@link Instance}s or equivalence classes.
	 * @param calculation
	 * 		{@link Cal} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Consider equal when the 
	 * 		difference between two sig is less than the given deviation value.
	 * @return Attributes in an {@link Integer} {@link List}.
	 */
	@SuppressWarnings("unchecked")
	public static <Cal extends FeatureImportance<Sig>, Sig extends Number, CollectionItem, Velocity, 
					Posi extends Position<?>, FValue extends FitnessValue<?>> Collection<Integer>[] 
		getReds(
			int[] attributesSrc,
			ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig> redAlg,
			GenerationRecord<Velocity, Posi, FValue> geneRecord,
			Particle<Velocity, Posi, FValue>[] particles,
			Collection<CollectionItem> collectionItem, Cal calculation, Sig sigDeviation
	){
		Collection<Collection<Integer>> reds = new LinkedList<>();
		Collection<Integer> red = new LinkedList<>();
		for (int attr: geneRecord.getGlobalBestFitness().getPosition().getAttributes())	red.add(attr);
		for (Particle<Velocity, Posi, FValue> particle: particles) {
			if (Double.compare(
					FastMath.abs(
						geneRecord.getGlobalBestFitness().getFitnessValue().getValue().doubleValue()
						- 
						particle.getFitness().getFitnessValue().getValue().doubleValue()
					),
					sigDeviation.doubleValue()
				)<=0
			) {
				red = new LinkedList<>();
				for (int attrIndex: particle.getPosition().getAttributes())
					red.add(attributesSrc[attrIndex]);
				reds.add(red);
			}
		}
		int i=0;
		Collection<Integer>[] redsArray = new Collection[reds.size()];
		for (Collection<Integer> collection: reds)
			redsArray[i++] = redAlg.inspection(calculation, sigDeviation, collectionItem, collection);
		return redsArray;
	}
}