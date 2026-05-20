public class LoggingThreadPoolTaskScheduler extends ThreadPoolTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(LoggingThreadPoolTaskScheduler.class);

    @Override
    protected ScheduledExecutorService createExecutor(int poolSize,
                                                      ThreadFactory threadFactory,
                                                      RejectedExecutionHandler rejectedExecutionHandler) {
        return new ScheduledThreadPoolExecutor(poolSize, threadFactory, rejectedExecutionHandler) {

            @Override
            protected void onShutdown() {
                log.info(
                        "[{}] ScheduledThreadPoolExecutor.onShutdown() — " +
                                "activeCount={}, queueSize={}, isShutdown={}, thread={}",
                        getBeanName(),
                        getActiveCount(),
                        getQueue().size(),
                        isShutdown(),
                        Thread.currentThread().getName()
                );
                super.onShutdown(); // removes/cancels delayed tasks per Spring's continueExistingPeriodicTasksAfterShutdownPolicy
            }

            @Override
            protected void terminated() {
                log.info(
                        "[{}] ScheduledThreadPoolExecutor.terminated() — " +
                                "completedTasks={}, thread={}",
                        getBeanName(),
                        getCompletedTaskCount(),
                        Thread.currentThread().getName()
                );
                super.terminated();
            }
        };
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
