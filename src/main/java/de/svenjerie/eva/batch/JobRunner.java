package de.svenjerie.eva.batch;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class JobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job inputJob;
    private final Job processingJob;
    private final Job outputJob;

    public JobRunner(JobLauncher jobLauncher, Job inputJob, Job processingJob, Job outputJob) {
        this.jobLauncher = jobLauncher;
        this.inputJob = inputJob;
        this.processingJob = processingJob;
        this.outputJob = outputJob;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(inputJob, params);
        jobLauncher.run(processingJob, params);
        jobLauncher.run(outputJob, params);
    }
}