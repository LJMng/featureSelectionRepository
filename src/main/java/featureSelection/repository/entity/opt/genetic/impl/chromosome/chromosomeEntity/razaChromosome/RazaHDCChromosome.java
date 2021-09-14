package featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.razaChromosome;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.HDCChromosome;

import java.util.Arrays;

public class RazaHDCChromosome extends RazaChromosome implements HDCChromosome<int[]> {

	public RazaHDCChromosome(int...razaGene) {
		super(razaGene);
	}
	
	public RazaHDCChromosome(int[] razaGene, int[] attributes) {
		super(razaGene, attributes);
	}
	
	@Override
	public RazaHDCChromosome clone() throws CloneNotSupportedException {
		return new RazaHDCChromosome(
					Arrays.copyOf(this.encodedValues(), this.encodedValues().length), 
					Arrays.copyOf(this.getAttributes(), this.getAttributes().length)
				);
	}
}
