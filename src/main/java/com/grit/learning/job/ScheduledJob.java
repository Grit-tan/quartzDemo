package com.grit.learning.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * CronSchedule 示例
 * CronSchedule 可以设置更灵活的使用方式, 定时设置参考 cron 表达式。
 */
public class ScheduledJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("schedule job1 is running ...");
    }
}
