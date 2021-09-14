package featureSelection.tester.report;

public class ReportConstants {

    public static class Procedure {

        public final static String REPORT_EXECUTION_TIME = "ExecutionTime(ms)";

        public final static String REPORT_CORE_CURRENT_ATTRIBUTE = "Core: CurrentAttribute";
        public final static String REPORT_CORE_INDIVIDUAL_RESULT = "Core: Result(is Core ?)";
        /* ----- ID-REC ----- */
        public final static String REPORT_CORE_4_IDREC_0_REC_DIRECT = "Core: '0-REC'Times";
        public final static String REPORT_CORE_4_IDREC_CURRENT_COMMON_CALCULATE_TIMES = "Core: CommonCalculateTimes";
        public final static String REPORT_CORE_4_IDREC_CURRENT_STATIC_CALCULATE_TIMES = "Core: StaticCalculateTimes";

        public final static String REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY = "AddingReductAttribute";
        public final static String REPORT_CURRENT_REDUCT_SIZE = "|CurrentReduct|";

        public final static String REPORT_CURRENT_UNIVERSE_SIZE = "|Current U|";
        public final static String REPORT_CURRENT_COMPACTED_UNIVERSE_SIZE = "|Current U/C|";

        public final static String REPORT_UNIVERSE_POS_REMOVE_HISTORY = "(U)PosRemoveHistory";
        public final static String REPORT_UNIVERSE_NEG_REMOVE_HISTORY = "(U)NegRemoveHistory";
        public final static String REPORT_UNIVERSE_SPECIAL_REMOVE_HISTORY = "(U)SpecialRemoveHistory";
        public final static String REPORT_COMPACTED_UNIVERSE_POS_REMOVE_HISTORY = "(U/C)PosRemoveHistory";
        public final static String REPORT_COMPACTED_UNIVERSE_NEG_REMOVE_HISTORY = "(U/C)NegRemoveHistory";

        public final static String REPORT_SIG_HISTORY = "SignificanceHistory";

        public final static String REPORT_INSPECT_4_IDREC_0_REC_DIRECT = "Inspect: '0-REC'Times";
        public final static String REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE = "Inspect: Attribute";
        public final static String REPORT_INSPECT_ATTRIBUTE_REDUNDANT = "Inspect: Attribute is Redundant";

        /* ----- Optimization ----- */
        public final static String STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP = "reductCodeMap";
        public final static String STATISTIC_OPTIMIZATION_EXIT_ITERATION = "Exit.iteration";
        public final static String STATISTIC_OPTIMIZATION_EXIT_FITNESS = "Exit.fitness";
        public final static String STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE = "Exit.convergence";
        public final static String STATISTIC_OPTIMIZATION_EXIT_REASON = "Exit.reason";

        public final static String REPORT_OPTIMIZATION_CURRENT_ITERATION = "Current.iteration";
        public final static String REPORT_OPTIMIZATION_CURRENT_CONVERGENCE = "Current.convergence";
        public final static String REPORT_OPTIMIZATION_CURRENT_MAX_FITNESS = "Current.max(fitness)";
        public final static String REPORT_OPTIMIZATION_CURRENT_BEST_FITNESS_GROUP_SIZE = "Current.max(fitness).size()";

        public final static String REPORT_OPTIMIZATION_EXIT_ITERATION = "Exit.iteration";
        public final static String REPORT_OPTIMIZATION_EXIT_FITNESS = "Exit.fitness";
        public final static String REPORT_OPTIMIZATION_EXIT_REDUCT_SIZE = "Exit.candidate.afterInspect.size()";

        public final static String REPORT_OPTIMIZATION_INSPECT_REDUNDANT_SIZE_4_DURING = "Inspect: Redundant size 4 during";
    }

    public final static String REPORT_FILE_BASE_FOLDER = "reports";
    public final static String REPORT_SINGLE_REPORT_FOLDER = "single reports";

    public final static String REPORT_FIRST_COLUMN_DESCRIPTION = "Description";

    public final static String REPORT_4_OPTIMIZATION_ALGORITHM_REDUCTS_LIST_FILE_NAME = "[Alg]reducts list 4 opts.csv";
    public final static String REPORT_4_OPTIMIZATION_ALGORITHM_REDUCTS_SUMMARY_FILE_NAME = "[Alg]reducts summary 4 opts.csv";
    public final static String REPORT_4_OPTIMIZATION_ALL_REDUCTS_SUMMARY_FILE_NAME = "[All]reducts summary 4 opts.csv";

}
