package com.kirchnersolutions.picenter.backup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**

 * Created by gkatzioura on 4/26/17.

 */

@Configuration
public class ThreadConfig {

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {


        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(9);

        executor.setMaxPoolSize(30);

        executor.setWaitForTasksToCompleteOnShutdown(true);

        executor.setKeepAliveSeconds(10);

        executor.setThreadNamePrefix("picenter_backupservice_task_executor_thread");

        executor.initialize();

        return executor;

    }

}