package featureSelection.repository.entity.opt.genetic.interf.chromosome.entity;

import featureSelection.basic.model.optimization.AttributeEncoding;

/**
 * Chromosome entity for Genetic Algorithm.
 * 
 * @author Benjamin_L
 *
 * @param <Gene>
 * 		The type of Gene in {@link Chromosome}.
 */
public interface Chromosome<Gene>
	extends AttributeEncoding<Gene>,
			Cloneable 
{
	void update(Gene gene);
	
	Chromosome<Gene> clone() throws CloneNotSupportedException;
}
