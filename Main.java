public class LoggingThreadPoolTaskScheduler extends ThreadPoolTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(LoggingThreadPoolTaskScheduler.class);

    @Override
    public void shutdown() {
        ScheduledThreadPoolExecutor executor = getScheduledThreadPoolExecutor();
        log.info(
                "ThreadPoolTaskScheduler '{}' shutdown initiated — " +
                        "activeCount={}, queueSize={}, completedTasks={}",
                getBeanName(),
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount()
        );
        super.shutdown(); // delegates to ExecutorConfigurationSupport → executor.shutdown()
    }
}

public Main {
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        LoggingThreadPoolTaskScheduler scheduler = new LoggingThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("my-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }

}
