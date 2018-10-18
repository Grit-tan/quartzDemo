## SpringBoot集成Quartz

### 什么是Quartz

    Quartz is a richly featured, open source job scheduling library that can be integrated within virtually any Java application - from the smallest stand-alone application to the largest e-commerce system. Quartz can be used to create simple or complex schedules for executing tens, hundreds, or even tens-of-thousands of jobs; jobs whose tasks are defined as standard Java components that may execute virtually anything you may program them to do.
    来源： http://www.quartz-scheduler.org/

### Quartz 的优点

- 丰富的 Job 操作 API；
- 支持多种配置
- Spring Boot 无缝集成；
- 支持持久化；
- 支持集群
- 开源

### Quartz的核心概念

1.Job 表示一个工作，要执行的具体内容。此接口中只有一个方法，如下：

    void execute(JobExecutionContext context)

2.JobDetail Quartz 每次调度 Job 时，都重新创建一个 Job 实例，所以它不直接接受一个 Job 的实例，相反它接收一个 Job 实现类（JobDetail，描述 Job 的实现类及其他相关的静态信息，如 Job 名字、描述、关联监听器等信息），以便运行时通过 newInstance() 的反射机制实例化 Job。

3.Trigger 代表一个调度参数的配置，什么时候去调。即触发的时机

4.Scheduler 代表一个调度容器，一个调度容器中可以注册多个 JobDetail 和 Trigger。当 Trigger 与 JobDetail 组合，就可以被 Scheduler 容器调度了。

### 使用

配置pom.xml

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-quartz</artifactId>
    </dependency>



简单示例

使用 Quartz 定时输出 Hello World

首先定义一个 Job 需要继承 QuartzJobBean，示例中 Job 定义一个变量 Name，用于在定时执行的时候传入。

    public class SampleJob extends QuartzJobBean {
    
    	private String name;
    
        public void setName(String name) {
    		this.name = name;
    	}
    
    	@Override
    	protected void executeInternal(JobExecutionContext context)
    			throws JobExecutionException {
    		System.out.println(String.format("Hello %s!", this.name));
    	}
    
    }

接下来构建 JobDetail，并且构建时传入 name 属性的值，构建 JobTrigger 和 scheduleBuilder，最后使用 Scheduler 启动定时任务。

    @Configuration
    public class SampleScheduler {
    
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

启动项目后每隔两秒输出：Hello World!

    Hello World!
    Hello World!
    Hello World!
    ...

CronSchedule 示例

CronSchedule 可以设置更灵活的使用方式，定时设置可以参考 cron 表达式。

首先定义两个 Job：

    public class ScheduledJob implements Job {
    
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            System.out.println("schedule job1 is running ...");
        }
    }

    public class ScheduledJob2 implements Job {
      
        @Override  
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("schedule job2 is running ...");
        }  
    }  

按照使用 Quartz 的逻辑，构建 jobDetail、CronTrigger，最后使用 scheduler 关联 jobDetail 和 CronTrigger。scheduleJob1 设置每间隔 6 秒执行一次。

    @Component
    public class CronSchedulerJob {
        @Autowired
        private SchedulerFactoryBean schedulerFactoryBean;
          
        public void scheduleJobs() throws SchedulerException {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduleJob1(scheduler);
            scheduleJob2(scheduler);   
        }  
          
        private void scheduleJob1(Scheduler scheduler) throws SchedulerException{
            JobDetail jobDetail = JobBuilder.newJob(ScheduledJob.class) .withIdentity("job1", "group1").build();
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0/6 * * * * ?");
            CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1") .withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail,cronTrigger);   
        }  
          
        private void scheduleJob2(Scheduler scheduler) throws SchedulerException{
            JobDetail jobDetail = JobBuilder.newJob(ScheduledJob2.class) .withIdentity("job2", "group2").build();
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0/12 * * * * ?");
            CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity("trigger2", "group2") .withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail,cronTrigger);  
        }  
    }  

CronScheduleBuilder.cronSchedule("0/6 * * * * ?")，按照 cron 表达式设置定时任务的执行周期。

scheduleJob 2 的内容和 scheduleJob 1 基本一致，时间设置为间隔 12 秒执行一次。

这里使用 scheduler 启动两个定时任务。

    public void scheduleJobs() throws SchedulerException {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduleJob1(scheduler);
            scheduleJob2(scheduler);   
    }  

何时触发定时任务

两种方案来触发 CronSchedule 定时任务，一种是启动时调用 scheduleJobs() 来启动定时任务，另外一种方案使用 Spring Boot 自带的 Scheduled 在特定时间触发启动。

第一种方案，启动时触发定时任务： （ 定时一个 Runner，继承 CommandLineRunner 并重新 run 方法，在 run 方法中调用 scheduleJobs() 来启动定时任务。）

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

第二种方案，特定时间启动定时任务：

    @Configuration
    @EnableScheduling
    @Component
    public class SchedulerListener {  
        @Autowired
        public CronSchedulerJob scheduleJobs;
        @Scheduled(cron="0 30 11 25 11 ?")
        public void schedule() throws SchedulerException {
            scheduleJobs.scheduleJobs();
         }      
    }

一般情况下，建议使用第一种方案来启动定时任务；第二种方案设置固定日期时，需要考虑重复启动定时任务的情况，重复启动定时任务会报错。

注意，两种启动方案，在项目中选择一种使用即可，否则会导致重复启动定时任务而报错。

结束

如果需要在项目中执行大量的批任务处理时，可以采用 Quartz 来解决，Spring Boot 2.0 中提供了对 Quartz 的支持，让我们在项目使用的过程中更加的灵活简洁。


