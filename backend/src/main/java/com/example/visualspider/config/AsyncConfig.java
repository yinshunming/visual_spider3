package com.example.visualspider.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Value("${spider.crawler.thread-pool-size:10}")
    private int threadPoolSize;

    @Bean(name = "crawlTaskExecutor")
    public Executor crawlTaskExecutor() {
        log.info("Initializing crawl task executor with core={}, max={}, queue=100", threadPoolSize, threadPoolSize);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("crawl-");
        executor.initialize();
        return executor;
    }
}
