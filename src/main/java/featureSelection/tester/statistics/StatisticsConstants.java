package featureSelection.tester.statistics;

import featureSelection.tester.utils.DBUtils;

public class StatisticsConstants {

	public static class Procedure {

		public final static String STATISTIC_BASIC_UNIVERSE_INFO_BUILDER = "BasicUniverseInfoBuilder";

		public final static String STATISTIC_POS_INSTANCE_REMOVED = "PosInstanceRemoved";
		public final static String STATISTIC_POS_COMPACTED_INSTANCE_REMOVED = "PosCompactedInstanceRemoved";
		public final static String STATISTIC_NEG_INSTANCE_REMOVED = "NegInstanceRemoved";
		public final static String STATISTIC_NEG_COMPACTED_UNIVERSE_REMOVED = "NegCompactedInstanceRemoved";

		public final static String STATISTIC_CORE_LIST = "CoreList";
		public final static String STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH = "CoreAttributeExamedLength";

		public final static String STATISTIC_SIG_LOOP_TIMES = "SigLoopTimes";

		public final static String STATISTIC_SIG_HISTORY = "SignificanceIncrementHistory";
		public final static String STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH = "HeuristicReductSigCalculateAttributesLength";

		public final static String STATISTIC_RED_BEFORE_INSPECT = "ReductBeforeIndpect";
		public final static String STATISTIC_RED_AFTER_INSPECT = "ReductAfterIndpect";

		public final static String STATISTIC_REDUNDANT_ATTRIBUTE_DISCOVERED = "RedundantAttributeDiscoverdDirectly";

		public final static String STATISTIC_ITERATION_INFOS = "IterationInfos";
	}

	public final static class Database {
		public static final String TABLE_DATASET = "DATASET";
		public static final String TABLE_ALGORITHM = "ALGORITHM";
		public static final String TABLE_PARAMETER_EXT_QR = "PARAMETER_EXT_QR";
		public static final String TABLE_PARAMETER_GENERAL = "PARAMETER_GENERAL";
		public static final String TABLE_RUN_TIME_EXT_QR = "RUN_TIME_EXT_QR";
		public static final String TABLE_RUN_TIME_GENERAL = "RUN_TIME_GENERAL";
		public static final String TABLE_RUN_TIME_EXT_SWARM = "RUN_TIME_EXT_SWARM";

//		public static final String EXPERIMENT_MARK = DBUtils.EXP_MARK_ID_REC;
//		public static final String EXPERIMENT_MARK = DBUtils.EXP_MARK_C_NEC;
		public static final String EXPERIMENT_MARK = DBUtils.EXP_MARK_IP_NEC;
	}

	public final static class PlainRecordInfo {
		public final static String DATASET_ID = "dataset.id";
		public final static String DATASET_NAME = "dataset";

		public final static String CONTAINER_ID = "id";
		public final static String DATETIME = "datetime";
		public final static String DATABASE_UNIQUE_ID= "database.uniqueID";

		public final static String TOTAL_TIME = "time(运行总时间)";
		public final static String PURE_TIME = "time(除去检查总时间)";
		public final static String RUN_TIMES = "times(次数)";

		public final static String ALG_ID = "Alg.id";
		public final static String ALG = "Alg";
		public final static String PARAMETER_ID = "Parameter.id";
		public final static String ORIGINAL_UNIVERSE_SIZE = "|U|";
		public final static String PURE_UNIVERSE_SIZE = "|AU|";
		public final static String UNIVERSE_PURE_RATE = "Ratio_U";
		public final static String EXEC_INSTANCE_SIZE = "|Exec_U|";
		public final static String EXEC_INSTANCE_UNIT = "Exec_U.unit";
		public final static String ATTRIBUTE_SIZE = "|Attr|";
		public final static String ATTRIBUTE_LIST = "Attr";
		public final static String DECISION_VALUE_NUMBER = "|D|";

		public final static String CALCULATION_TIMES = "Cal. Times";
		public final static String CALCULATION_ATTR_LEN = "sum(Cal. AttrLen)";
		public final static String CALCULATION_HEURISTIC_LENGTH_IN_SEARCH_LIST = "Search.cal_attr.list";
		public final static String CALCULATION_HEURISTIC_LENGTH_IN_SEARCH_LENGTH = "sum(Search.cal_attr.len)";

		public final static String SIGNIFICANCE_HISTORY = "SIGs";
		public final static String SIGNIFICANCE_SUM = "sum(SIGs)";
		public final static String SIGNIFICANCE_AVERAGE = "ave(SIGs)";

		public final static String TOTAL_UNIVERSE_POS_REMOVE_NUMBER_HISTORY = "remove POS(U)";
		public final static String TOTAL_UNIVERSE_POS_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE = "Sum(remove POS(U))";
		public final static String TOTAL_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION = "Eva(remove POS(U))";

		public final static String TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_HISTORY = "remove NEG(U)";
		public final static String TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE = "Sum(remove NEG(U))";
		public final static String TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_EVALUATION = "Eva(remove NEG(U))";

		public final static String TOTAL_UNIVERSE_REMOVE_NUMBER_HISTORY = "remove U";
		public final static String TOTAL_UNIVERSE_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE = "Sum(remove U)";

		public final static String TOTAL_UNIVERSE_REMOVE_INDIVIDUAL_EVALUATION = "Eva(remove U)";
		public final static String TOTAL_UNIVERSE_REMOVE_TOTAL_EVALUATION = "Eva(Sum(remove U))";

		public final static String COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_HISTORY = "remove POS(U/C)";
		public final static String COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE = "Sum(remove POS(U/C))";
		public final static String COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION = "Eva(remove POS(U/C))";

		public final static String COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_HISTORY = "remove NEG(U/C)";
		public final static String COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE = "Sum(remove NEG(U/C))";
		public final static String COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_EVALUATION = "Eva(remove NEG(U/C))";

		public final static String COMPACTED_UNIVERSE_REMOVE_NUMBER_HISTORY = "remove U/C";
		public final static String COMPACTED_UNIVERSE_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE = "Sum(remove U/C)";

		public final static String COMPACTED_UNIVERSE_REMOVE_INDIVIDUAL_EVALUATION = "Eva(remove U/C)";
		public final static String COMPACTED_UNIVERSE_REMOVE_TOTAL_EVALUATION = "Eva(Sum(remove U/C))";

		public final static String TIME_INIT = "init_T";
		public final static String TIME_COMPRESS = "comp_T";
		public final static String TIME_CORE = "core_T";
		public final static String TIME_RED = "red_T";
		public final static String TIME_INSPECT = "red_inspect_T";

		public final static String CORE_INCLUDE = "Core ?";
		public final static String CORE_SIZE = "|Core|";
		public final static String CORE_LIST = "Core";
		public final static String CORE_ATTRIBUTE_EXAM_SIZE = "|CoreAttrUsed|";

		public final static String REDUCT_LIST = "Reduct";
		public final static String REDUCT_SIZE = "|Reduct|";
		public final static String REDUNDANT_LIST_BEFORE_INSPECT = "Redundant";
		public final static String REDUNDANT_SIZE_BEFORE_INSPECT = "|Redundant|";

		public final static String PROCEDURE_CLASSES_INFO = "Procedures";
		public final static String PROCEDURE_INFO = "Procedure Tester Info";
	}

	public final static class OptimizationInfo {

		public final static class Common {
			public final static String TIME_INIT = "init_T";
			public final static String TIME_COMPRESS = "comp_T";
			public final static String TIME_RED = "red_T";
			public final static String TIME_INSPECT_DURING = "red_inspect_T(during)";
			public final static String TIME_INSPECT_LAST = "red_inspect_T(last)";
			public final static String TIME_SOLUTION_SELECTION = "solution_selection_T";

			public final static String RUN_TIME_ID = "RunTime.ID";
			public final static String REDUCT_ID = "Reds.ID";
			public final static String REDUCT_DISTINCT_NUMBER = "Reds.distinct.size()";
			public final static String REDUCT_NUMBER = "Reds.size()";
			public final static String REDUCT_CODE_NUMBER = "Reds.code.size()";
			public final static String REDUCT_NO = "Reds.No.";
			public final static String SUM_REDUCT_CANDIDATE_NUMBER = "sum(Red.Candidate)";
			public final static String SUM_REDUCT_CANDIDATE_HAS_REDUNDANT_NUMBER = "sum(Red.Candidate.redundant)";

			public final static String MIN_CANDIDATE_ATTRIBUTE_LENGTH = "min(Reds.Candidate.attr)";
			public final static String MAX_CANDIDATE_ATTRIBUTE_LENGTH = "max(Reds.Candidate.attr)";
			public final static String AVG_CANDIDATE_ATTRIBUTE_LENGTH = "avg(Reds.Candidate.attr)";
			public final static String SUM_CANDIDATE_ATTRIBUTE_LENGTH = "sum(Reds.Candidate.attr)";
			public final static String MIN_REDUCT_LENGTH = "min(Reds.attr)";
			public final static String MAX_REDUCT_LENGTH = "max(Reds.attr)";
			public final static String AVG_REDUCT_LENGTH = "avg(Reds.attr)";
			public final static String SUM_REDUCT_LENGTH = "sum(Reds.attr)";
			public final static String REDUCT_CANDIDATE_REDUNDANT_ATTRIBUTE_PROPORTION = "sum(Red.Candidate.redundant.attr)/sum(Red.Candidate.attr)";

			public final static String LIST_REDUCT_CANDIDATE_CODES_WITH_REDUNDANCY = "Red.Candidate.code(√ redundancies)";
			public final static String REDUCT_CANDIDATE_CODE_LENGTH_WITHOUT_REDUNDANCY = "Red.Candidate.code.length(× redundancies)";
			public final static String REDUCT_CANDIDATE_CODES_WITHOUT_REDUNDANCY = "Red.Candidate.code(× redundancies)";
		}

		public final static class IterationInfos {
			public final static String GENERATION = "Generation";
			public final static String CONVERGENCE = "Convergence";

			public final static String RECORD_NUM = "|record|";
			public final static String RECORD_CLASS = "record.class";

			public final static String ENTITY_NO = "Entity_No.";
			public final static String ENTITY_CODING_ATTR_LEN = "Entity_coding.attr.len";
			public final static String ENTITY_FINAL_ATTR_LEN = "Entity_final.attr.len";
			public final static String ENTITY_FINAL_ATTR = "Entity_final.attr";
			public final static String ENTITY_INSPECTED_ATTR_LEN = "Entity_inspected.attr.len";
			public final static String ENTITY_INSPECTED_ATTR = "Entity_inspected.attr";

			public final static String ENTITY_IS_SOLUTION = "Entity_solution";
			public final static String ENTITY_FITNESS_VALUE = "Entity_fitness_value";
			public final static String ENTITY_SUPREME_MARK = "Entity_supreme";
			public final static String ENTITY_SUPREME_MARK_NO = "Entity_supreme_No.";
		}

		public final static class Exit {
			public final static String STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP = "reductCodeMap";
			public final static String STATISTIC_OPTIMIZATION_EXIT_ITERATION = "Exit.iteration";
			public final static String STATISTIC_OPTIMIZATION_EXIT_FITNESS = "Exit.fitness";
			public final static String STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE = "Exit.convergence";
			public final static String STATISTIC_OPTIMIZATION_EXIT_REASON = "Exit.reason";
		}

		public final static class Genetic {
			public final static String PARAM_POPULATION = "[Param] population.size()";
			public final static String PARAM_GENE_LENGTH = "[Param] gene.length";
			public final static String PARAM_CHROMOSOME_GENE_SWITCH_NUMBER = "[Param] gene.switch.length";
			public final static String PARAM_CHROMOSOME_RESERVE_NUMBER = "[Param] chromosome.reserve.size()";
			public final static String PARAM_ITERATION = "[Param] iteration";
			public final static String PARAM_CONVERGENCE = "[Param] convergence";
			public final static String PARAM_MAX_FITNESS = "[Param] limit.max(fitness)";
			public final static String PARAM_GENE_MUTATE_NUMBER = "[Param] gene.mutate.length";
			public final static String PARAM_GENE_MUTATE_PROBABILITY = "[Param] gene.mutate.probability";
			public final static String PARAM_RANDOM_SEED = "[Param] random.seed";

			public final static String PARAM_4_ALG_CHROMOSOME_INITIATION = "[Param.Alg] chromosome.init";
			public final static String PARAM_4_ALG_CHROMOSOME_CROSS = "[Param.Alg] chromosome.cross";
			public final static String PARAM_4_ALG_CHROMOSOME_CLASS = "[Param.Alg] chromosome.class";

			public final static String RECORD_EXIT_REASON = "exit.reason";
			public final static String RECORD_EXIT_ITERATION = "exit.iteration";
			public final static String RECORD_EXIT_CONVERGENCE = "exit.convergence";
			public final static String RECORD_EXIT_FITNESS = "exit.fitness";
			public final static String RECORD_AVG_EXIT_ITERATION = "avg(exit.iteration)";
			public final static String RECORD_AVG_EXIT_CONVERGENCE = "avg(exit.convergence)";
			public final static String RECORD_AVG_EXIT_FITNESS = "avg(exit.fitness)";
			public final static String RECORD_MEDIAN_ORDER_BY_ITERATION_EXIT_FITNESS = "median(exit.fitness)-order by exit.iteration";
		}

		public final static class ParticleSwarm {
			public final static String PARAM_POPULATION = "[Param] population.size()";
			public final static String PARAM_PARTICLE_LENGTH = "[Param] particle.length";
			public final static String PARAM_C1 = "[Param] c1";
			public final static String PARAM_C2 = "[Param] c2";
			public final static String PARAM_R1 = "[Param] r1";
			public final static String PARAM_R2 = "[Param] r2";
			public final static String PARAM_MIN_VELOCITY = "[Param] min(Velocity)";
			public final static String PARAM_MAX_VELOCITY = "[Param] max(Velocity)";
			public final static String PARAM_MAX_FITNESS = "[Param] limit.max(fitness)";
			public final static String PARAM_ITERATION = "[Param] iteration";
			public final static String PARAM_CONVERGENCE = "[Param] convergence";
			public final static String PARAM_RANDOM_SEED = "[Param] random.seed";

			public final static String PARAM_4_ALG_PARTICLE_INITIATION = "[Param.Alg] particle.init";
			public final static String PARAM_4_ALG_PARTICLE_UPDATE = "[Param.Alg] particle.update";
			public final static String PARAM_4_ALG_INERTIA_WEIGHT = "[Param.Alg] particle.inertiaWeight";

			public final static String RECORD_EXIT_REASONS = "exit.reasons";
			public final static String RECORD_EXIT_ITERATION = "exit.iteration";
			public final static String RECORD_EXIT_CONVERGENCE = "exit.convergence";
			public final static String RECORD_EXIT_FITNESS = "exit.fitness";
			public final static String RECORD_AVG_EXIT_ITERATION = "avg(exit.iteration)";
			public final static String RECORD_AVG_EXIT_CONVERGENCE = "avg(exit.convergence)";
			public final static String RECORD_AVG_EXIT_FITNESS = "avg(exit.fitness)";
			public final static String RECORD_MEDIAN_ORDER_BY_ITERATION_EXIT_FITNESS = "median(exit.fitness)-order by exit.iteration";
		}

		public final static class ArtificialFishSwarm {
			public final static String PARAM_GROUP_SIZE = "[Param] fishGroup.size()";
			public final static String PARAM_VISUAL = "[Param] visual";
			public final static String PARAM_CROWD_FACTOR = "[Param] crowd fator";
			public final static String PARAM_SEARCH_TRY_NUMBER = "[Param] search try number";
			public final static String PARAM_ITERATION = "[Param] iteration";
			public final static String PARAM_CONVERGENCE = "[Param] convergence";
			public final static String PARAM_MAX_FISH_EXIT = "[Param] max(|fish.exit|)";
			public final static String PARAM_RANDOM_SEED = "[Param] random.seed";

			public final static String PARAM_4_ALG_DISTANCE_MEASURE = "[Param.Alg] distance measure";
			public final static String PARAM_4_ALG_FITNESS_CALCULATE = "[Param.Alg] fitness calculate";
			public final static String PARAM_4_ALG_FISH_GROUP_UPDATE = "[Param.Alg] fishGroup.update";
			public final static String PARAM_4_ALG_FISH_GROUP_SWARM = "[Param.Alg] fishGroup.swarm";
			public final static String PARAM_4_ALG_FISH_GROUP_FOLLOW = "[Param.Alg] fishGroup.follow";
			public final static String PARAM_4_ALG_FISH_GROUP_CENTER = "[Param.Alg] fishGroup.center";

			public final static String PARAM_4_CLASS_POSITION = "[Param.Class] position";
			public final static String PARAM_4_CLASS_FISH = "[Param.Class] fish";

			public final static String RECORD_EXIT_ITERATION = "exit.iteration";
			public final static String RECORD_EXIT_CONVERGENCE = "exit.convergence";
			public final static String RECORD_AVG_EXIT_ITERATION = "avg(exit.iteration)";
			public final static String RECORD_AVG_EXIT_CONVERGENCE = "avg(exit.convergence)";
		}

		public final static class ImprovedHarmonySearch {
			public final static String PARAM_GROUP_SIZE = "[Param] group.size()";
			public final static String PARAM_HARMONY_MEMORY_SIZE = "[Param] harmonyMemory.length";
			public final static String PARAM_HARMONY_MEMORY_CONSIDERATION_RATE = "[Param] harmonyMemory.considerationRate";
			public final static String PARAM_ITERATION = "[Param] iteration";
			public final static String PARAM_CONVERGENCE = "[Param] convergence";
			public final static String PARAM_RANDOM_SEED = "[Param] random.seed";

			public final static String PARAM_4_ALG_PITCH_ADJUSTMENT_RATE = "[Param.Alg] pitch adjustment rate";
			public final static String PARAM_4_ALG_BAND_WIDTH = "[Param.Alg] band width";
			public final static String PARAM_4_ALG_HARMONY_INITIALIZTION = "[Param.Alg] harmony.initialization";

//			public final static String PARAM_4_CLASS_FITNESS = "[Param.Class] fitness";

			public final static String RECORD_EXIT_REASON = "exit.reason";
			public final static String RECORD_EXIT_ITERATION = "exit.iteration";
			public final static String RECORD_EXIT_CONVERGENCE = "exit.convergence";
			public final static String RECORD_EXIT_FITNESS = "exit.fitness";
			public final static String RECORD_AVG_EXIT_ITERATION = "avg(exit.iteration)";
			public final static String RECORD_AVG_EXIT_CONVERGENCE = "avg(exit.convergence)";
			public final static String RECORD_AVG_EXIT_FITNESS = "avg(exit.fitness)";
			public final static String RECORD_MEDIAN_ORDER_BY_ITERATION_EXIT_FITNESS = "median(exit.fitness)-order by exit.iteration";
		}
	}
}