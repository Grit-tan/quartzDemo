package com.grit.learning;

import com.grit.learning.scheduler.CronSchedulerJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 何时触发定时任务
 * 两种方案来触发 CronSchedule 定时任务，
 * 一种是启动时调用 scheduleJobs() 来启动定时任务
 * 另外一种方案使用 Spring Boot 自带的 Scheduled 在特定时间触发启动。 见SchedulerListener
 * 注意，两种启动方案，在项目中选择一种使用即可，否则会导致重复启动定时任务而报错。
 */
@Component
public class MyStartupRunner implements CommandLineRunner {

    @Autowired
    public CronSchedulerJob scheduleJobs;

    @Override
    public void run(String... args) throws Exception {
        scheduleJobs.scheduleJobs();
        System.out.println(">>>>>>>>>>>>>>>定时任务开始执行<<<<<<<<<<<<<");
    }
}