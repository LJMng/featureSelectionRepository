package featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.entity.BinaryHarmony;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;

public class HarmonyFactory {

	/**
	 * Create a new {@link Harmony} with the given class and given memory length(attribute length).
	 * 
	 * @param harmonyClass
	 * 		Class of {@link Harmony}.
	 * @param harmonyMemoryLength
	 * 		The length of the {@link Harmony} memory / attribute length.
	 * @return {@link Harmony}
	 */
	@SuppressWarnings("unchecked")
	public static <Hrmny extends Harmony<?>> Hrmny newHarmony(
			Class<Hrmny> harmonyClass, int harmonyMemoryLength
	) {
		if (harmonyClass.equals(BinaryHarmony.class)){
			return (Hrmny) newBinaryHarmony(harmonyMemoryLength);
		}else {
			return null;
		}
	}
	
	/**
	 * Create an {@link Hrmny} instance using the given coding. (attribute indexes)
	 * 
	 * @param harmonyClass
	 * 		{@link Class} of {@link Hrmny}.
	 * @param harmonyMemoryLength
	 * 		The length of {@link Hrmny} memory. / The length of the coding.
	 * @param attributeIndexes
	 * 		Indexes of {@link Instance} attributes.
	 * @return A {@link Hrmny} instance.
	 */
	@SuppressWarnings("unchecked")
	public static <Hrmny extends Harmony<?>> Hrmny newHarmony(
			Class<Hrmny> harmonyClass, int harmonyMemoryLength, int[] attributeIndexes
	) {
		if (harmonyClass.equals(BinaryHarmony.class)){
			byte[] code = new byte[harmonyMemoryLength];
			for (int index: attributeIndexes)	code[index] = (byte) 1;
			return (Hrmny) new BinaryHarmony(code);
		}else {
			return null;
		}
	}
	
	/**
	 * Copy the {@link Harmony} instance.
	 * 
	 * @param harmony
	 * 		The {@link Harmony} to be copied.
	 * @return {@link Harmony} copy that has the same attributes info as the original one.
	 */
	public static Harmony<?> copyHarmony(Harmony<?> harmony) {
		return harmony.clone();
	}
	
	/**
	 * Create a new {@link BinaryHarmony}
	 * 
	 * @param harmonyMemoryLength
	 * 		The length of the {@link Harmony} memory / attribute length.
	 * @return {@link BinaryHarmony}
	 */
	private static BinaryHarmony newBinaryHarmony(int harmonyMemoryLength) {
		return new BinaryHarmony(new byte[harmonyMemoryLength]);
	}
}