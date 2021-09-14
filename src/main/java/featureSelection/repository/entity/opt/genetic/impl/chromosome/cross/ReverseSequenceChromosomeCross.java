package featureSelection.repository.entity.opt.genetic.impl.chromosome.cross;

import common.utils.RandomUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.genetic.ReductionParameters;
import featureSelection.repository.entity.opt.genetic.ReverseFitnessComparator;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.ChromosomeFactory;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.ChromosomeCrossAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Cross-overs for <code>Genetic Algorithm</code>. 
 * Following steps:
 * <ul>
 * 	<li>Sort chromosomes in <strong>reverse</strong> order based on their {@link Fitness} values
 * 			using <code>ReverseFitnessComparator</code></li>
 * 	<li>Determine cross-over numbers</li>
 * 	<li>For every 2 {@link Chromosome}s, use
 * 			{@link RandomUtils#randomUniqueInts(int, int, int, Random)} to get indexes for cross-over
 * 			genes. Call {@link #crossGenes(Chromosome, Chromosome, Collection)} to perform cross-over.
 * 	</li>
 * 	<li>Return results.</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 * 		Type of implemented {@link Chromosome}.
 * @param <FValue>
 * 		Type of {@link Fitness}'s value.
 */
public class ReverseSequenceChromosomeCross<Chr extends Chromosome<?>,
											FValue extends FitnessValue<?>>
	implements ChromosomeCrossAlgorithm<Chr, FValue>
{
	@SuppressWarnings("unchecked")
	@Override
	public Chr[] crossChromosomes(
			Fitness<Chr, FValue>[] fitness,
			ReductionParameters<? extends FeatureImportance<?>, ? extends Number, ?, Chr, FValue> params,
			Random random
	) {
		if (random==null)	random = new Random();
		// 倒排
		List<Fitness<Chr, FValue>> chromosomeList = Arrays.asList(fitness);
		Collections.sort(chromosomeList, new ReverseFitnessComparator<Chr, FValue>());
		
		Collection<Integer> crossIndex;
		int N = Math.min(params.getReserveNum(), fitness.length);
		Chr[] crossFunc, crossedChr = (Chr[]) Array.newInstance(params.getReductionAlgorithm()
																		.getChromosomeClass(), 
																fitness.length);
		for (int i=0; i<N; i++)		crossedChr[i] = chromosomeList.get(i).getChromosome();	
		for (int i=N; i<params.getPopulation(); i+=2) {			
			// 染色体交叉
			if (fitness[i-N].getChromosome().encodedValuesLength()>0) {
				crossIndex = RandomUtils.randomUniqueInts(0,
								fitness[i-N].getChromosome()
											.encodedValuesLength(), 
								params.getChromosomeSwitchNum(), 
								random
							);
				
				crossFunc = crossGenes(fitness[i-N].getChromosome(), 
										fitness[i-N+1].getChromosome(), 
										crossIndex
							);
				crossedChr[i] = crossFunc[0];
				if (i+1<params.getPopulation())	crossedChr[i+1] = crossFunc[1];
			}
		}
		return (Chr[]) crossedChr;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public Chr[] crossGenes(Chr c1, Chr c2, Collection<Integer> crossIndex) {
		if (c1==null && c2==null) {
			throw new IllegalStateException("null Chromosome & null Chromosome can not perform gene cross action!");
		}else if (int[].class.equals(c1.encodedTypeClass()) || int[].class.equals(c2.encodedTypeClass())) {
			int[] g1, g2;
			
			if (c1!=null)	g1 = Arrays.copyOf((int[]) c1.encodedValues(), c1.encodedValuesLength());
			else			g1 = emptyIntArrayGeneChromosome(c2.encodedValuesLength());
			
			if (c2!=null)	g2 = Arrays.copyOf((int[]) c2.encodedValues(), c2.encodedValuesLength());
			else			g2 = emptyIntArrayGeneChromosome(c1.encodedValuesLength());
						
			int temp;
			for (int i : crossIndex) {
				if (g1[i]==g2[i])	continue;
				temp = g1[i];
				g1[i] = g2[i];
				g2[i] = temp;
			}
			
			return (Chr[]) new Chromosome[] {
						(Chr) ChromosomeFactory.getChromosome(g1, c1.getClass()),
						(Chr) ChromosomeFactory.getChromosome(g2, c2.getClass())
					};
		}else if (byte[].class.equals(c1.encodedTypeClass()) || byte[].class.equals(c2.encodedTypeClass())) {
			byte[] g1, g2;
			
			if (c1!=null)	g1 = Arrays.copyOf((byte[]) c1.encodedValues(), c1.encodedValuesLength());
			else			g1 = emptyByteArrayGeneChromosome(c2.encodedValuesLength());
			
			if (c2!=null)	g2 = Arrays.copyOf((byte[]) c2.encodedValues(), c2.encodedValuesLength());
			else			g2 = emptyByteArrayGeneChromosome(c1.encodedValuesLength());
			
			byte temp;
			for (int i : crossIndex) {
				if (g1[i]==g2[i])	continue;
				temp = g1[i];
				g1[i] = g2[i];
				g2[i] = temp;
			}
			
			return (Chr[]) new Chromosome[] {
						(Chr) ChromosomeFactory.getChromosome(g1, c1.getClass()),
						(Chr) ChromosomeFactory.getChromosome(g2, c2.getClass())
					};
		}else {
			throw new UnsupportedOperationException("unimplemented Chromosome Cross for Gene Class : "+c1.encodedTypeClass());
		}
	}

	/**
	 * Get an empty int array Gene Chromosome. (filled with -1)
	 * 
	 * @param length
	 * 		The length of gene.
	 * @return {@link int[]} [-1, -1, ...].
	 */
	private static int[] emptyIntArrayGeneChromosome(int length) {
		int[] gene = new int[length];
		Arrays.fill(gene, -1);
		return gene;
	}
	/**
	 * Get an empty byte array Gene Chromosome. (filled with 0)
	 * 
	 * @param length
	 * 		The length of gene.
	 * @return {@link byte[]} [0, , ...].
	 */
	private static byte[] emptyByteArrayGeneChromosome(int length) {
		return new byte[length];
	}

}