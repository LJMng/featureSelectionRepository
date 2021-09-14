package featureSelection.tester.procedure.opt.improvedHarmonySearch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.utils.CollectionUtils;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.support.reductMiningStrategy.optimization.ImprovedHarmonySearchReductMiningStrategy;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.generationLoop.DefaultGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.generationLoop.GlobalBestPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.generationLoop.ReductPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.harmonyInitiate.HarmonyInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.ihsInitiate.ImprovedHarmonySearchInitiate4AsitKDasAlgsProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.ihsInitiate.ImprovedHarmonySearchInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.inspection.LocalInspectionProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.original.ihsInitiate.ImprovedHarmonySearchInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.original.inspection.IPRECInspectionProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Improved Harmony Search Algorithm</strong> <code>PLUS</code> 
 * <strong>Feature Selection</strong>. Check out the original article 
 * <a href="https://link.springer.com/article/10.1007/s00521-015-1840-0">
 * "A novel hybrid feature selection method based on rough set and improved harmony search"</a> 
 * by H.Hannah.
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 5 {@link ProcedureComponent}s, 
 * refer to steps: 
 * <ul>
 * 	<li>
 * 		<strong>Initiate</strong>: 
 * 		<p>Some initializations for Improved Harmony Search Algorithm.
 * 		<p><code>ImprovedHarmonySearchInitiateProcedureContainer</code>,  
 * 		<p><code>ImprovedHarmonySearchInitiate4AsitKDasAlgsProcedureContainer</code>,  
 * 		<p><code>ImprovedHarmonySearchInitiateProcedureContainer4REC</code>
 * 	</li>
 * 	<li>
 * 		<strong>Initiate harmonies</strong>: 
 * 		<p>Initiate harmonies randomly.
 * 		<p><code>HarmonyInitiateProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Generation loops</strong>: 
 * 		<p>Loop generations, calculate harmonies' fitness, "study". Until reaching maximum 
 * 			iteration/maximum convergence/maximum fitness.
 * 		<p><code>DefaultGenerationLoopProcedureContainer</code>
 * 		<p><code>GlobalBestPrioritizedGenerationLoopProcedureContainer</code>
 * 		<p><code>ReductPrioritizedGenerationLoopProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Harmony to reducts</strong>:
 * 		<p>Transfer harmonies into reducts.
 * 		<p><code>LocalInspectionProcedureContainer</code>,
 * 		<p><code>IPRECInspectionProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Return reducts</strong>:
 * 		<p>Return reducts as result. (Nothing particular)
 * 		<p><code>ProcedureComponent</code>
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_REDUCT_LIST_AFTER_INSPECTATION}</li>
 * </ul>
 * 
 * @author Benjamin_L
 * 
 * @see ImprovedHarmonySearchInitiateProcedureContainer
 * @see ImprovedHarmonySearchInitiate4AsitKDasAlgsProcedureContainer
 * @see ImprovedHarmonySearchInitiateProcedureContainer4REC
 * @see HarmonyInitiateProcedureContainer
 * @see DefaultGenerationLoopProcedureContainer
 * @see GlobalBestPrioritizedGenerationLoopProcedureContainer
 * @see ReductPrioritizedGenerationLoopProcedureContainer
 * @see LocalInspectionProcedureContainer
 * @see IPRECInspectionProcedureContainer
 *
 * @param <Hrmny>
 * 		Type of Implemented {@link Harmony}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
@ThreadSafetyNotSecured
public class ImprovedHarmonySearchFeatureSelectionTester<Hrmny extends Harmony<?>,
														FValue extends FitnessValue<?>>
	extends SelectiveComponentsProcedureContainer<Map<IntArrayKey, Collection<OptimizationReduct>>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				StatisticsCalculated,
				ImprovedHarmonySearchReductMiningStrategy
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	private String[] componentExecOrder;
	
	public ImprovedHarmonySearchFeatureSelectionTester(ProcedureParameters parameters, boolean logOn) {
		super(logOn, parameters);
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Opt("+
					ProcedureUtils.ShortName.optimizationAlgorithm(getParameters())+"-"+
					ProcedureUtils.ShortName.calculation(getParameters())+
				")";
	}

	@Override
	public String staticsName() {
		return shortName();
	}
	
	@Override
	public String reportName() {
		return shortName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initDefaultComponents(boolean logOn) {
		ProcedureComponent<?>[] componentArray = new ProcedureComponent<?>[] {
			// 1. Initiate.
			new ProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("1. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (Object[])
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					},
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate")
			.setSubProcedureContainer(
				"ImprovedHarmonySearchInitiateProcedureContainer", 
				new ImprovedHarmonySearchInitiateProcedureContainer<>(getParameters(), logOn)
//				new ImprovedHarmonySearchInitiateProcedureContainer4REC<>(getParameters(), logOn)
			),
			// 2. Initiate harmonies.
			new ProcedureComponent<Hrmny[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (Hrmny[]) component.getSubProcedureContainers().values().iterator().next().exec();
					}, 
					(component, chromosome)->{
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate chromosomes")
			.setSubProcedureContainer(
				"HarmonyInitiateProcedureContainer", 
				new HarmonyInitiateProcedureContainer<>(getParameters(), logOn)
			),
			// 3. Generation loops.
			new ProcedureComponent<GenerationRecord<FValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (GenerationRecord<FValue>) component.getSubProcedureContainers().values().iterator().next().exec();
					}, 
					(component, geneRecord)->{
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
		
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Generation loops")
				.setSubProcedureContainer(
					"GenerationLoopProcedureContainer", 
					new DefaultGenerationLoopProcedureContainer<>(getParameters(), logOn)
//					new GlobalBestPrioritizedGenerationLoopProcedureContainer<>(getParameters(), logOn)
//					new ReductPrioritizedGenerationLoopProcedureContainer<>(getParameters(), logOn)
				),
			// 4. Harmony to reducts.
			new ProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
					},
					(component, parameters) -> {
						return (Map<IntArrayKey, Collection<OptimizationReduct>>) 
								component.getSubProcedureContainers()
										.values()
										.iterator()
										.next()
										.exec();
					}, 
					null
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Harmony to reducts")
				.setSubProcedureContainer(
					"InspectionProcedureContainer", 
					new LocalInspectionProcedureContainer<>(getParameters(), logOn)
//					new IPRECInspectionProcedureContainer<>(getParameters(), logOn)
				),
			// 5. Return reducts.
			new ProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					null, 
					(component, parameters) -> {
						return getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION);
					}, 
					null
				) {
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Return reducts"),
		};
		//	Component order.
		componentExecOrder = new String[componentArray.length];
		for (int i=0; i<componentArray.length; i++) {
			this.setComponent(componentArray[i].getDescription(), componentArray[i]);
			componentExecOrder[i] = componentArray[i].getDescription();
		}
	}
	
	@Override
	public String[] componentsExecOrder() {
		return componentExecOrder;
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		String[] keys = new String[this.getComponents().size()];
		int i=0;
		for (ProcedureComponent<?> comp: this.getComponents())	keys[i++] = comp.getDescription();
		return keys;
	}

	@Override
	public long getTime() {
		long total = 0;
		for (ProcedureComponent<?> component : this.getComponents())
			total += ProcedureUtils.Time.sumProcedureComponentTimes(component);
		return total;
	}

	@Override
	public Map<String, Long> getTimeDetailByTags() {
		return ProcedureUtils.Time.sumProcedureComponentsTimesByTags(this);
	}
}