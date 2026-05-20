public Main {
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler() {

            @Override
            protected ExecutorService initializeExecutor(
                    ThreadFactory threadFactory,
                    RejectedExecutionHandler rejectedExecutionHandler) {

                ScheduledThreadPoolExecutor executor =
                        new ScheduledThreadPoolExecutor(getPoolSize(), threadFactory, rejectedExecutionHandler) {

                            @Override
                            public void shutdown() {
                                log.warn("==> ScheduledThreadPoolExecutor.shutdown() called directly\n{}",
                                        stackTrace());
                                super.shutdown();
                            }

                            @Override
                            public List<Runnable> shutdownNow() {
                                log.warn("==> ScheduledThreadPoolExecutor.shutdownNow() called directly\n{}",
                                        stackTrace());
                                return super.shutdownNow();
                            }

                            private String stackTrace() {
                                StringWriter sw = new StringWriter();
                                new Exception("executor shutdown trace").printStackTrace(new PrintWriter(sw));
                                return sw.toString();
                            }
                        };

                return executor;
            }
        };
    }

}
