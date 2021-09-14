package featureSelection.tester.procedure.opt.genetic.procedure.common.chromosomeInitiate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import common.utils.LoggerUtil;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.genetic.ReductionParameters;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.initlization.ChromosomeInitialization;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.gaInitiate.GeneticAlgorithmInitiateProcedureContainer4IPNECStreaming;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.original.gaInitiate.GeneticAlgorithmInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Chromosomes initialization of <strong>Genetic Algorithm</strong>. This procedure contains 1 step: 
 * <ul>
 * 	<li>
 * 		<strong>Chromesome initiation</strong>
 * 		<p>Using the given {@link ChromosomeInitialization} to initiate chromosomes.
 * 	</li>
 * </ul>
 * <p>
 * In this {@link DefaultProcedureContainer}(only!), the following parameters are used in 
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_RANDOM_INSTANCE}</li>
 * </ul>
 * 
 * @see ChromosomeInitialization
 * @see GeneticAlgorithmInitiateProcedureContainer4REC
 * @see GeneticAlgorithmInitiateProcedureContainer4IPNECStreaming
 * 
 * @author Benjamin_L
 * 
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <Chr>
 * 		Type of Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Type of {@link Comparable} extended fitness value.
 */
@Slf4j
public class ChromosomeInitiateProcedureContainer<Cal extends FeatureImportance<Sig>,
													Sig extends Number, 
													CollectionItem,
													Chr extends Chromosome<?>,
													FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<Chr[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	public ChromosomeInitiateProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new ArrayList<>(1);
	}

	@Override
	public String shortName() {
		return "Chromosome Initiation";
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
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Chromosome initiation.
			new TimeCountedProcedureComponent<Chr[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_RANDOM_INSTANCE),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue> params =
								(ReductionParameters<Cal, Sig, CollectionItem, Chr, FValue>) 
								parameters[p++];
						Random random = (Random) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return params.getChromosomeInitAlgorithm().initChromosomes(random);
					}, 
					(component, chromosome) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("chromosome", chromosome);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "Initiated {} chromosome(s)"), chromosome.length);
						}
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
			}.setDescription("Chromosome initiation"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Chr[] exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (Chr[]) componentArray[0].exec();
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}
