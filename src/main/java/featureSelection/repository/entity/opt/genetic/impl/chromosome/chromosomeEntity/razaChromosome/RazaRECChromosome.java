package featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.razaChromosome;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.RECChromosome;

import java.util.Arrays;

public class RazaRECChromosome
	extends RazaChromosome 
	implements RECChromosome<int[]>
{
	public RazaRECChromosome(int...razaGene) {
		super(razaGene);
	}
	
	public RazaRECChromosome(int[] razaGene, int[] attributes) {
		super(razaGene, attributes);
	}
	
	@Override
	public RazaRECChromosome clone() throws CloneNotSupportedException {
		return new RazaRECChromosome(
					Arrays.copyOf(this.encodedValues(), this.encodedValues().length), 
					Arrays.copyOf(this.getAttributes(), this.getAttributes().length)
				);
	}
}