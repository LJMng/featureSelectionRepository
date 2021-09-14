package featureSelection.tester.procedure.basic;

import common.utils.ArrayUtils;
import featureSelection.basic.model.universe.generator.UniverseGeneratorImp;
import featureSelection.basic.model.universe.instance.Instance;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BasicTester {

	public static List<Instance> instances;

	private static boolean stringSource = true;
	private static File fromFile = SimpleDataset4Test.FromFile.wine();

	@BeforeAll
	public static void initiateUniverse(){
		if (!stringSource) {
			UniverseGeneratorImp generator = new UniverseGeneratorImp();
			try {
				//for (String[] str : dataSet)	generator.addUniverse(str, -1);
				generator.setDataSetWithFileByLines(fromFile, ",", -1, null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			instances = generator.universe();
		}else {
			stringIntArray2Universes();
		}
	}

	private static void stringIntArray2Universes() {
		Instance.resetID();

		int[] values;
		List<Instance> instanceList = new LinkedList<>();
		for (String[] str : SimpleDataset4Test.sample1) {
			values = new int[str.length];
			values[0] = Integer.valueOf(str[str.length-1].replace(",", ""));
			for (int i=1; i<values.length; i++)	values[i] = Integer.valueOf(str[i-1].replace(",", ""));
			instanceList.add(new Instance(values));
		}
		instances = instanceList;
	}


	public static int getAttributeLength(){
		return instances.get(0).getAttributeValues().length-1;
	}
	public static int[] getAllConditionalAttributes(){
		return ArrayUtils.initIncrementalValueIntArray(getAttributeLength(), 1, 1);
	}

}
