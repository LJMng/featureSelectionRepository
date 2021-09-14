package featureSelection.tester.procedure.opt.genetic;

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
import featureSelection.basic.support.reductMiningStrategy.optimization.GeneticAlgorithmReductMiningStrategy;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.initlization.ChromosomeInitialization;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.genetic.procedure.common.chromosomeInitiate.ChromosomeInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.common.gaInitiate.GeneticAlgorithmInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.common.generationLoop.DefaultGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.common.generationLoop.GlobalBestPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.common.generationLoop.ReductPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.common.inspection.LocalInspectionProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.original.gaInitiate.GeneticAlgorithmInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.original.inspection.IPRECInspectionProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Genetic Algorithm</strong> <code>PLUS</code> <strong>Feature 
 * Selection</strong>.
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 5 
 * {@link ProcedureComponent}s, refer to steps: 
 * <ul>
 *  <li>
 *  	<strong>Initiate</strong>:
 *  	<p>Some initializations for Genetic Algorithm.
 *  	<p><code>GeneticAlgorithmInitiateProcedureContainer</code>,
 *  	<p><code>GeneticAlgorithmInitiateProcedureContainer4REC</code>,
 *  	<p><code>GeneticAlgorithmInitiate4AsitKDasAlgsProcedureContainer</code>,
 *  </li>
 *  <li>
 *  	<strong>Initiate chromosomes</strong>:
 *  	<p>Initiate chromosomes randomly using {@link ChromosomeInitialization}.
 *  	<p><code>ChromosomeInitiateProcedureContainer</code>
 *  </li>
 *  <li>
 *  	<strong>Generation loops</strong>:
 *  	<p>Loop generations, calculate chromosomes' fitness, cross-over, mutate. Until reaching exit criteria
 *  		(maximum iteration/maximum convergence/maximum fitness).
 *  	<p><code>DefaultGenerationLoopProcedureContainer</code>,
 *  	<p><code>GlobalBestPrioritizedGenerationLoopProcedureContainer</code>,
 *  	<p><code>ReductPrioritizedGenerationLoopProcedureContainer</code>
 *  </li>
 *  <li>
 *  	<strong>Chromosome to reducts</strong>:
 *  	<p>Transfer chromosomes to reducts.
 *  	<p><code>LocalInspectionProcedureContainer</code>,
 *  	<p><code>IPRECInspectionProcedureContainer</code>
 *  </li>
 *  <li>
 *  	<strong>Return reducts</strong>:
 *  	<p>Return reducts as result. (Nothing particular)
 * 	    <p><code>ProcedureComponent</code>
 *  </li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <p><i>NONE</i>
 * 
 * @author Benjamin_L
 * 
 * @see GeneticAlgorithmInitiateProcedureContainer
 * @see GeneticAlgorithmInitiateProcedureContainer4REC
 * @see ChromosomeInitiateProcedureContainer
 * @see DefaultGenerationLoopProcedureContainer
 * @see ReductPrioritizedGenerationLoopProcedureContainer
 * @see GlobalBestPrioritizedGenerationLoopProcedureContainer
 * @see LocalInspectionProcedureContainer
 * @see IPRECInspectionProcedureContainer
 *
 * @param <Chr>
 * 		Type of Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
@ThreadSafetyNotSecured
public class GeneticAlgorithmFeatureSelectionTester<Chr extends Chromosome<?>,
													FValue extends FitnessValue<?>>
	extends SelectiveComponentsProcedureContainer<Map<IntArrayKey, Collection<OptimizationReduct>>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				StatisticsCalculated,
				GeneticAlgorithmReductMiningStrategy
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	private String[] componentExecOrder;
	
	public GeneticAlgorithmFeatureSelectionTester(ProcedureParameters parameters, boolean logOn) {
		super(logOn, parameters);
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		return String.format("Opt(%s-%s)",
				ProcedureUtils.ShortName.optimizationAlgorithm(getParameters()),
				ProcedureUtils.ShortName.calculation(getParameters())
		);
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
						// Statistics
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
					"GeneticAlgorithmInitiateProcedureContainer",
					new GeneticAlgorithmInitiateProcedureContainer<>(getParameters(), logOn)
				),
			// 2. Initiate chromosomes.
			new ProcedureComponent<Chr[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (Chr[])
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, chromosome)->{
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(),
								0, 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
					}
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate chromosomes")
			.setSubProcedureContainer(
				"ChromosomeInitiateProcedureContainer", 
				new ChromosomeInitiateProcedureContainer<>(getParameters(), logOn)
			),
			// 3. Generation loops.
			new ProcedureComponent<GenerationRecord<Chr, FValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (GenerationRecord<Chr, FValue>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
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
					"DefaultGenerationLoopProcedureContainer", 
					new DefaultGenerationLoopProcedureContainer<>(getParameters(), logOn)
					//new ReductPrioritizedGenerationLoopProcedureContainer<>(getParameters(), logOn)
					//new GlobalBestPrioritizedGenerationLoopProcedureContainer<>(getParameters(), logOn)
				),
			// 4. Chromosome to reducts.
			new ProcedureComponent<Object[]>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
					},
					(component, parameters) -> {
						return (Object[])
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					null
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Chromosome to reducts")
				.setSubProcedureContainer(
					"InspectionProcedureContainer", 
					//new IPRECInspectionProcedureContainer<>(getParameters(), logOn)
					new LocalInspectionProcedureContainer<>(getParameters(), logOn)
				),
			// 5. Return reducts.
			new ProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("5. "+component.getDescription());
					},
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