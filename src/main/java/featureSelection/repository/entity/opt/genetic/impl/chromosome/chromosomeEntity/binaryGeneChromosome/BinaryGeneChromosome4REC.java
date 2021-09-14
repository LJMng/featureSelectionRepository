package featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.binaryGeneChromosome;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.RECChromosome;

import java.util.Arrays;

public class BinaryGeneChromosome4REC
	extends BinaryGeneChromosome 
	implements RECChromosome<byte[]>
{

	public BinaryGeneChromosome4REC(byte... gene) {
		super(gene);
	}
	public BinaryGeneChromosome4REC(byte[] gene, int[] attributes) {
		super(gene, attributes);
	}

	@Override
	public BinaryGeneChromosome clone() throws CloneNotSupportedException {
		byte[] gene = Arrays.copyOf(this.encodedValues(), this.encodedValuesLength());
		return new BinaryGeneChromosome4REC(gene);
	}
}