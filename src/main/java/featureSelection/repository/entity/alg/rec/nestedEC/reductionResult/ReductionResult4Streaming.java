package featureSelection.repository.entity.alg.rec.nestedEC.reductionResult;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import common.utils.DateTimeUtils;
import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.model.universe.generator.UniverseGeneratingGuidance;
import featureSelection.basic.model.universe.generator.UniverseGeneratingGuidanceTemplate;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.FastMath;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * An entity to store the return value of IP-NEC streaming execution. With the following fields:
 * <ul>
 * 	<li><strong>reductUpdated</strong> <code>boolean</code>:
 * 		<p>Whether the reduct has been updated.
 * 	</li>
 * 	<li><strong>newEquClasses</strong> <code>{@link EquivalenceClass} {@link Collection}</code>:
 * 		<p>A {@link Collection} of updated {@link EquivalenceClass}.
 * 	</li>
 * 	<li><strong>reduct</strong> <code>int[]</code>:
 * 		<p>Updated reduct.
 * 	</li>
 * 	<li><strong>reductSig</strong> <code>{@link Sig}</code>:
 * 		<p>Significance of the updated reduct.
 * 	</li>
 * 	<li><strong>universeGeneratingGuidance</strong> <code>{@link UniverseGeneratingGuidance}</code>:
 * 		<p>{@link UniverseGeneratingGuidance} instance for saving <code>this</code> into file.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <Sig>
 * 		Type of feature significance
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@ReturnWrapper
public class ReductionResult4Streaming<Sig extends Number>{
	private boolean reductUpdated;
	private Collection<EquivalenceClass> newEquClasses;
	private int[] reduct;
	private Sig reductSig;
	
	private UniverseGeneratingGuidance universeGeneratingGuidance;
	
	@SuppressWarnings("unchecked")
	public static <Sig extends Number> ReductionResult4Streaming<Sig> load(File file, Class<Sig> sigClass) 
			throws IOException 
	{
		// --------------------------------------------------------------------------------------------
		long time = System.nanoTime();
		log.info("Loading {} ...", file.getName());
		// --------------------------------------------------------------------------------------------
		String str = FileUtils.readFileToString(file, "UTF-8");
		ReductionResult4Streaming<Sig> result =  JSONObject.parseObject(str, ResultSavingTemplate.class)
															.toReductionResult4Streaming();
		// --------------------------------------------------------------------------------------------
		log.info("Finished loading, used {} sec.", 
				String.format("%.2f", (System.nanoTime() - time) / FastMath.pow(1000, 3))
		);
		// --------------------------------------------------------------------------------------------
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <Sig extends Number> ReductionResult4Streaming<Sig> loadBulk(File file, Class<Sig> sigClass) 
			throws IOException 
	{
		// --------------------------------------------------------------------------------------------
		long time = System.nanoTime();
		log.info("Loading {} ...", file.getName());
		// --------------------------------------------------------------------------------------------
		String str = FileUtils.readFileToString(file, "UTF-8");
		ResultSavingTemplate<Sig> template = JSONObject.parseObject(str, ResultSavingTemplate.class);
		// --------------------------------------------------------------------------------------------
		log.info("Finished loading, used {} sec.", 
				String.format("%.2f", (System.nanoTime() - time) / FastMath.pow(1000, 3))
		);
		// --------------------------------------------------------------------------------------------
		return template.toReductionResult4Streaming();
	}
	
	/**
	 * For all results in <code>results</code>, save all reducts and the detail of the one with least 
	 * reduct length. Details include all fields in {@link ReductionResult4Streaming} and can be fully
	 * restored by calling {@link ReductionResult4Streaming#loadBulk(File, Class)}.
	 * 
	 * @param file
	 * @param results
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <Sig extends Number> void saveLeastDetailAndRestReduct(File file, Collection<ReductionResult4Streaming<Sig>> results) 
			throws IOException 
	{
		// --------------------------------------------------------------------------------------------
		long time = System.nanoTime();
		log.info("Saving {} reduction result(s) (reducts and least reduct detail is saved) into {} ...", 
				results.size(), file.getName());
		// --------------------------------------------------------------------------------------------
		ResultSavingTemplate<Sig> saveTemplate = new ResultSavingTemplate<>();
		results = results.parallelStream()
						// reverse order
						.sorted((r1,r2)->-(r1.reduct.length-r2.reduct.length))
						.collect(Collectors.toList());
		Iterator<ReductionResult4Streaming<Sig>> iterator = results.iterator();
		Collection<int[]> otherReducts = new LinkedList<>();
		for (int i=0; i<results.size()-1; i++)	otherReducts.add(iterator.next().getReduct());
		//saveTemplate.setOtherReducts(otherReducts);
		
		ReductionResult4Streaming<Sig> result = iterator.next();
		saveTemplate.setMainReduct(result.getReduct());
		saveTemplate.setNewEquClasses(result.getNewEquClasses());
		saveTemplate.setReductSig(result.getReductSig());
		saveTemplate.setReductUpdated(result.isReductUpdated());
		saveTemplate.setSigClass((Class<Sig>) result.getReductSig().getClass());
		saveTemplate.setUniverseGeneratingGuidanceTemplate(
				new UniverseGeneratingGuidanceTemplate(result.getUniverseGeneratingGuidance())
		);
		
		if (!file.exists())	file.createNewFile();
		FileUtils.writeStringToFile(file, JSONObject.toJSONString(saveTemplate), "UTF-8", false);
		// --------------------------------------------------------------------------------------------
		log.info("Saved, used {} sec.", 
			String.format("%.2f", (System.nanoTime() - time) / FastMath.pow(1000, 3))
		);
		// --------------------------------------------------------------------------------------------
	}
	
	public void save(File file) throws IOException {
		// --------------------------------------------------------------------------------------------
		long time = System.nanoTime();
		log.info("Saving reduction result into {} ...", file.getName());
		// --------------------------------------------------------------------------------------------
		if (!file.exists())	file.createNewFile();
		FileUtils.writeStringToFile(file, JSONObject.toJSONString(new ResultSavingTemplate<>(this)), "UTF-8", false);
		// --------------------------------------------------------------------------------------------
		log.info("Saved, used {} sec.", 
				String.format("%.2f", (System.nanoTime() - time) / FastMath.pow(1000, 3))
		);
		// --------------------------------------------------------------------------------------------
	}

	public static String defaultResultFileName(String datasetName, String algName) {
		StringBuilder reductMemoryFileName = new StringBuilder();
		reductMemoryFileName.append("(");
		reductMemoryFileName.append(DateTimeUtils.currentDateTimeString("yyyy-MM-dd_HH-mm-ss"));
		reductMemoryFileName.append(") [");
		reductMemoryFileName.append(datasetName);
		reductMemoryFileName.append("] ");
		reductMemoryFileName.append(algName);
		reductMemoryFileName.append(".json");
		return reductMemoryFileName.toString();
	}

	@Data
	@NoArgsConstructor
	private static class ResultSavingTemplate<Sig extends Number> {
		private boolean reductUpdated;
		private Collection<EquivalenceClass> newEquClasses;
//		private Collection<NestedEquivalentClass> newNestedEquClasses;
		private int[] mainReduct;
		//private Collection<int[]> otherReducts;
		private Sig reductSig;
		private Class<Sig> sigClass;

		private UniverseGeneratingGuidanceTemplate universeGeneratingGuidanceTemplate;
		
		@SuppressWarnings("unchecked")
		public ResultSavingTemplate(ReductionResult4Streaming<Sig> result) {
			this.reductUpdated = result.reductUpdated;
			this.mainReduct = result.reduct;
			this.newEquClasses = result.newEquClasses;
			this.reductSig = result.reductSig;
			this.sigClass = (Class<Sig>) result.reductSig.getClass();
			this.universeGeneratingGuidanceTemplate = new UniverseGeneratingGuidanceTemplate(result.universeGeneratingGuidance);
		}
		
		public ReductionResult4Streaming<Sig> toReductionResult4Streaming(){
			return new ReductionResult4Streaming<Sig>(
						reductUpdated,
						newEquClasses,
						mainReduct,
						reductSig,
						universeGeneratingGuidanceTemplate.toUniverseGeneratingGuidance()
					);
		}
	}
}