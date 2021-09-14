package featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.razaChromosome;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.DDCChromosome;

import java.util.Arrays;

public class RazaDDCChromosome extends RazaChromosome implements DDCChromosome<int[]> {

	public RazaDDCChromosome(int...razaGene) {
		super(razaGene);
	}
	
	public RazaDDCChromosome(int[] razaGene, int[] attributes) {
		super(razaGene, attributes);
	}
	
	@Override
	public RazaDDCChromosome clone() throws CloneNotSupportedException {
		return new RazaDDCChromosome(Arrays.copyOf(this.encodedValues(), this.encodedValues().length), 
				Arrays.copyOf(this.getAttributes(), this.getAttributes().length));
	}
}
