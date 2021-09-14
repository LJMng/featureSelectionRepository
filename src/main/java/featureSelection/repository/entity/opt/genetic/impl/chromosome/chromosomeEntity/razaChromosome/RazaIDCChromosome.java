package featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.razaChromosome;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.IDCChromosome;

import java.util.Arrays;

public class RazaIDCChromosome 
	extends RazaChromosome 
	implements IDCChromosome<int[]>
{

	public RazaIDCChromosome(int...razaGene) {
		super(razaGene);
	}
	
	public RazaIDCChromosome(int[] razaGene, int[] attributes) {
		super(razaGene, attributes);
	}
	
	@Override
	public RazaIDCChromosome clone() throws CloneNotSupportedException {
		return new RazaIDCChromosome(
					Arrays.copyOf(this.encodedValues(), this.encodedValues().length), 
					Arrays.copyOf(this.getAttributes(), this.getAttributes().length)
				);
	}
}
