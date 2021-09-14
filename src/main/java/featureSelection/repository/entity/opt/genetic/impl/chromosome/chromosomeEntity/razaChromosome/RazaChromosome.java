package featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.razaChromosome;

import common.utils.ArrayCollectionUtils;
import common.utils.ArrayUtils;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Binary gene based {@link Chromosome}. Using <code>int[]</code> encoding for genes. 
 * (gene can be [1, |gene|])
 * 
 * @author Benjamin_L
 */
public abstract class RazaChromosome implements Chromosome<int[]> {
	private int[] razaGene;
	private int[] attributes;
	
	public RazaChromosome(int...razaGene) {
		this.razaGene = razaGene;
		this.attributes = null;
	}
	
	public RazaChromosome(int[] razaGene, int[] attributes) {
		this.razaGene = razaGene;
		this.attributes = attributes;
	}
	
	public static int[] getFilteredGene(int[] gene) {
		Set<Integer> genes = new HashSet<>(gene.length);
		for (int g: gene)	if (g>=0 && !genes.contains(g))	genes.add(g);
		return ArrayCollectionUtils.getIntArrayByCollection(genes);
	}
	
	@Override
	public int[] getAttributes() {
		if (attributes==null)	attributes = getFilteredGene(razaGene);
		return attributes;
	}

	@Override
	public int[] encodedValues() {
		return razaGene;
	}

	@Override
	public String encodedValuesToString() {
		return Arrays.toString(razaGene);
	}
	

	@Override
	public int encodedValuesLength() {
		return razaGene.length;
	}

	@Override
	public Class<int[]> encodedTypeClass() {
		return int[].class;
	}
	
	@Override
	public void update(int[] gene) {
		razaGene = gene;
		attributes = null;
	}
	
	
	@Override
	public boolean equals(Object k) {
		if (k instanceof int[])					return Arrays.equals((int[])k, razaGene);
		else if (k instanceof RazaChromosome)	return Arrays.equals(((RazaChromosome)k).razaGene, razaGene);
		else 									return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(razaGene);
	}
	
	@Override
	public String toString() {
		return "RazaChromosome [razaGene=" + Arrays.toString(this.encodedValues()) + ",	" + 
				ArrayUtils.intArrayToString(this.getAttributes(), 100) + "]";
	}
	
	public abstract RazaChromosome clone() throws CloneNotSupportedException;
}