package featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.razaChromosome;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.ClassicChromosome;

import java.util.Arrays;

public class RazaClassicChromosome
	extends RazaChromosome 
	implements ClassicChromosome<int[]>
{
	public RazaClassicChromosome(int...razaGene) {
		super(razaGene);
	}
	
	public RazaClassicChromosome(int[] razaGene, int[] attributes) {
		super(razaGene, attributes);
	}
	
	@Override
	public RazaClassicChromosome clone() throws CloneNotSupportedException {
		return new RazaClassicChromosome(
					Arrays.copyOf(this.encodedValues(), this.encodedValues().length), 
					Arrays.copyOf(this.getAttributes(), this.getAttributes().length)
				);
	}
}