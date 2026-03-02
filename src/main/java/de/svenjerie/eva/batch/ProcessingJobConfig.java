package de.svenjerie.eva.batch;

import de.svenjerie.eva.domain.Deal;
import de.svenjerie.eva.service.DealValidator;
import de.svenjerie.eva.service.RiskEngine;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.ResultSet;

@Configuration
public class ProcessingJobConfig {

    // ==================== SUSY 3: VALIDATION ====================

    @Bean
    public JdbcCursorItemReader<Deal> stagingReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Deal>()
                .name("stagingReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM staging_deals WHERE status = 'NEW'")
                .rowMapper((ResultSet rs, int rowNum) -> {
                    Deal deal = new Deal();
                    deal.setDealNumber(rs.getString("deal_number"));
                    deal.setCustomerName(rs.getString("customer_name"));
                    deal.setAmount(rs.getBigDecimal("amount"));
                    deal.setCurrency(rs.getString("currency"));
                    deal.setDealDate(rs.getDate("deal_date").toLocalDate());
                    deal.setCategory(rs.getString("category"));
                    deal.setStatus(rs.getString("status"));
                    return deal;
                })
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Deal> processingWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Deal>()
                .dataSource(dataSource)
                .sql("INSERT INTO processing_deals (deal_number, customer_name, amount, currency, deal_date, category, status, error_message, created_at) " +
                     "VALUES (:dealNumber, :customerName, :amount, :currency, :dealDate, :category, :status, :errorMessage, NOW())")
                .beanMapped()
                .build();
    }

    @Bean
    public Step validationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                               JdbcCursorItemReader<Deal> stagingReader, JdbcBatchItemWriter<Deal> processingWriter,
                               DealValidator dealValidator) {
        return new StepBuilder("validationStep", jobRepository)
                .<Deal, Deal>chunk(5, transactionManager)
                .reader(stagingReader)
                .processor(dealValidator::validate)
                .writer(processingWriter)
                .build();
    }

    // ==================== SUSY 4: RISK ENGINE ====================

    @Bean
    public JdbcCursorItemReader<Deal> validReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Deal>()
                .name("validReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM processing_deals WHERE status = 'VALID'")
                .rowMapper((ResultSet rs, int rowNum) -> {
                    Deal deal = new Deal();
                    deal.setId(rs.getLong("id"));
                    deal.setDealNumber(rs.getString("deal_number"));
                    deal.setCustomerName(rs.getString("customer_name"));
                    deal.setAmount(rs.getBigDecimal("amount"));
                    deal.setCurrency(rs.getString("currency"));
                    deal.setDealDate(rs.getDate("deal_date").toLocalDate());
                    deal.setCategory(rs.getString("category"));
                    deal.setStatus(rs.getString("status"));
                    return deal;
                })
                .build();
    }

    
    
    @Bean
    public JdbcBatchItemWriter<Deal> riskWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Deal>()
                .dataSource(dataSource)
                .sql("UPDATE processing_deals SET risk_level = :riskLevel, status = :status, updated_at = NOW() WHERE deal_number = :dealNumber")
                .beanMapped()
                .build();
    }
    

    @Bean
    public Step riskStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                         JdbcCursorItemReader<Deal> validReader, JdbcBatchItemWriter<Deal> riskWriter,
                         RiskEngine riskEngine) {
        return new StepBuilder("riskStep", jobRepository)
                .<Deal, Deal>chunk(5, transactionManager)
                .reader(validReader)
                .processor(riskEngine::assess)
                .writer(riskWriter)
                .build();
    }

    // ==================== JOB 2: PROCESSING ====================

    @Bean
    public Job processingJob(JobRepository jobRepository, Step validationStep, Step riskStep, Step aggregationStep) {
        return new JobBuilder("processingJob", jobRepository)
                .start(validationStep)
                .next(riskStep)
                .next(aggregationStep)
                .build();
    }
    
    
 // ==================== SUSY 5: AGGREGATION ====================

    @Bean
    public Step aggregationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                DataSource dataSource) {
        return new StepBuilder("aggregationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    new org.springframework.jdbc.core.JdbcTemplate(dataSource).execute(
                        "INSERT INTO summary_deals (category, risk_level, deal_count, total_amount, avg_amount, calculated_at) " +
                        "SELECT category, risk_level, COUNT(*), SUM(amount), AVG(amount), NOW() " +
                        "FROM processing_deals WHERE status = 'PROCESSED' " +
                        "GROUP BY category, risk_level"
                    );
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}