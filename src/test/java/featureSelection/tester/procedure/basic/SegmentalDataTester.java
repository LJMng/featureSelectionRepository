package featureSelection.tester.procedure.basic;

import featureSelection.basic.model.universe.generator.UniverseGeneratingGuidance;
import featureSelection.basic.model.universe.generator.UniverseGeneratorImp;
import featureSelection.basic.model.universe.instance.Instance;
import org.junit.jupiter.api.BeforeAll;

import java.util.*;

public class SegmentalDataTester {

	public static List<Instance> instanceSamples, datasetInstances;
	public static List<Instance>[] sampleParts, datasetUniverseParts;
	public static UniverseGeneratingGuidance[] generatingInfo;

	@SuppressWarnings("unchecked")
	@BeforeAll
	public static void initiateUniverseByInput(){
		UniverseGeneratorImp generator = new UniverseGeneratorImp();
		try {
			for (String[] str : SimpleDataset4Test.sample1){
				generator.addInstance(str, -1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		instanceSamples = generator.universe();
		Collections.shuffle(instanceSamples);

		Iterator<Instance> iterator = instanceSamples.iterator();
		int part = 5, totalSize = instanceSamples.size(),
			partSize = totalSize / part, extra = totalSize % part;
		sampleParts = new List[part];
		for (int p=0; p<part; p++) {
			sampleParts[p] = new LinkedList<>();
			while (iterator.hasNext()) {
				sampleParts[p].add(iterator.next());
				if (sampleParts[p].size()>=partSize) {
					if (p<extra) {
						if (sampleParts[p].size()>partSize) {
							break;
						}
					}else {
						break;
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@BeforeAll
	public static void initiateUniverseByFiles() {
		UniverseGeneratorImp generator = new UniverseGeneratorImp();
		try {
			generator.setDataSetWithFileByLines(SimpleDataset4Test.FromFile.wine(), ",", -1, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		datasetInstances = generator.universe();
		Collections.shuffle(datasetInstances, new Random(0));

		datasetUniverseParts = new List[5];
		for (int i=0; i<datasetUniverseParts.length; i++) {
			datasetUniverseParts[i] = new LinkedList<>();
		}
		for (int i = 0, j = 0; i< datasetInstances.size(); i++) {
			datasetUniverseParts[j].add(datasetInstances.get(i));
			if (j==datasetUniverseParts.length-1)	j=0;
			else									j++;
		}
	}//*/

}
