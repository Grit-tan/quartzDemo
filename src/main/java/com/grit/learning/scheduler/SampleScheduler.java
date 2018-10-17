package com.grit.learning.scheduler;

import com.grit.learning.job.SampleJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接下来构建 JobDetail，并且构建时传入 name 属性的值，
 * 构建 JobTrigger 和 scheduleBuilder，最后使用 Scheduler 启动定时任务。
 */
@Configuration
public class SampleScheduler {

    /**
     * JobBuilder 无构造函数，
     * 所以只能通过 JobBuilder 的静态方法 newJob(Class<? extends Job> jobClass) 生成 JobBuilder 实例。
     *
     * withIdentity 方法可以传入两个参数withIdentity(String name,String group)来定义 TriggerKey，
     * 也可以不设置，像本文会自动生成一个独一无二的 TriggerKey 用来区分不同的 trigger。
     */
    @Bean
    public JobDetail sampleJobDetail() {
        return JobBuilder.newJob(SampleJob.class).withIdentity("sampleJob")
                .usingJobData("name", "World").storeDurably().build();
    }

    @Bean
    public Trigger sampleJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(2).repeatForever();

        return TriggerBuilder.newTrigger().forJob(sampleJobDetail())
                .withIdentity("sampleTrigger").withSchedule(scheduleBuilder).build();
    }
}
