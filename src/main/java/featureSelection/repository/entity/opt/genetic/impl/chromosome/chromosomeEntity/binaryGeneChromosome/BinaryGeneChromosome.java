package featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.binaryGeneChromosome;

import common.utils.ArrayUtils;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Binary gene based {@link Chromosome}. Using <code>byte[]</code> encoding for genes. (gene
 * can be 0 or 1)
 * 
 * @author Benjamin_L
 */
public abstract class BinaryGeneChromosome 
	implements Chromosome<byte[]>
{
	private byte[] gene;
	private int[] attributes;
	
	public BinaryGeneChromosome(byte...gene) {
		this.gene = gene;
		this.attributes = null;
	}
	
	public BinaryGeneChromosome(byte[] gene, int[] attributes) {
		this.gene = gene;
		this.attributes = attributes;
	}
	
	public static int[] getFilteredGene(byte[] gene) {
		Collection<Integer> list = new LinkedList<>();	
		for (int b=0; b<gene.length; b++)	if (gene[b]==(byte) 1)	list.add(b+1);
		int[] attributes = new int[list.size()];
		int i=0;	for (int each: list)	attributes[i++] = each;
		return attributes;
	}
	
	@Override
	public int[] getAttributes() {
		if (attributes==null)	attributes = getFilteredGene(gene);
		return attributes;
	}

	@Override
	public byte[] encodedValues() {
		return gene;
	}

	@Override
	public String encodedValuesToString() {
		return Arrays.toString(gene);
	}
	

	@Override
	public int encodedValuesLength() {
		return gene.length;
	}

	@Override
	public Class<byte[]> encodedTypeClass() {
		return byte[].class;
	}
	
	@Override
	public void update(byte[] gene) {
		this.gene = gene;
		attributes = null;
	}
	
	
	@Override
	public boolean equals(Object k) {
		if (k instanceof byte[])					return Arrays.equals((byte[])k, gene);
		else if (k instanceof BinaryGeneChromosome)	return Arrays.equals(((BinaryGeneChromosome)k).gene, gene);
		else 										return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(gene);
	}
	
	@Override
	public String toString() {
		return "BinaryGeneChromosome [gene=" + Arrays.toString(encodedValues()) + ",	" + 
				ArrayUtils.intArrayToString(getAttributes(), 100) + "]";
	}
	
	public abstract BinaryGeneChromosome clone() throws CloneNotSupportedException;
}