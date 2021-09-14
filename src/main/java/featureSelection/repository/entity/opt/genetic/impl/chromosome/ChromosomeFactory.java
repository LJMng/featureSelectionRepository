package featureSelection.repository.entity.opt.genetic.impl.chromosome;

import java.lang.reflect.InvocationTargetException;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChromosomeFactory {
	
	/**
	 * Generate Chromosome based on the given <code>gene</code> and {@link Chromosome}
	 * {@link Class}
	 * 
	 * @param <Chr>
	 * 		Implemented {@link Chromosome}.
	 * @param gene
	 * 		The gene values in int array.
	 * @param clazz
	 * 		The {@link Class} of generated {@link Chromosome}.
	 * @return A generated {@link Chromosome}.
	 */
	public static <Chr extends Chromosome<?>> Chr getChromosome(int[] gene, Class<Chr> clazz) {
		try {
			return clazz.getConstructor(int[].class)
						.newInstance(gene);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | 
				InvocationTargetException | NoSuchMethodException | SecurityException e
		) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * Generate Chromosome based on the given <code>gene</code> and {@link Chromosome} {@link Class}
	 * 
	 * @param <Chr>
	 * 		Implemented {@link Chromosome}.
	 * @param gene
	 * 		The gene values in byte array.
	 * @param clazz
	 * 		The {@link Class} of generated {@link Chromosome}.
	 * @return A generated {@link Chromosome}.
	 */
	public static <Chr extends Chromosome<?>> Chr getChromosome(byte[] gene, Class<Chr> clazz) {
		try {
			return clazz.getConstructor(byte[].class)
						.newInstance(gene);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | 
				InvocationTargetException | NoSuchMethodException | SecurityException e
		) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
}