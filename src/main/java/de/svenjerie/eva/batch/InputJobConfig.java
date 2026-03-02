package de.svenjerie.eva.batch;

import de.svenjerie.eva.domain.Deal;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;

@Configuration
public class InputJobConfig {

    // --- Susy 2: DB Reader ---
    @Bean
    public JdbcCursorItemReader<Deal> sourceReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Deal>()
                .name("sourceReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM source_deals WHERE status = 'NEW'")
                .rowMapper((ResultSet rs, int rowNum) -> {
                    Deal deal = new Deal();
                    deal.setDealNumber(rs.getString("deal_number"));
                    deal.setCustomerName(rs.getString("customer_name"));
                    deal.setAmount(rs.getBigDecimal("amount"));
                    deal.setCurrency(rs.getString("currency"));
                    deal.setDealDate(rs.getDate("deal_date").toLocalDate());
                    deal.setCategory(rs.getString("category"));
                    deal.setStatus("NEW");
                    return deal;
                })
                .build();
    }

    // --- Susy 1: CSV Reader ---
    @Bean
    public FlatFileItemReader<Deal> csvReader() {
        return new FlatFileItemReaderBuilder<Deal>()
                .name("csvReader")
                .resource(new ClassPathResource("data/deals.csv"))
                .delimited()
                .names("dealNumber", "customerName", "amount", "currency", "dealDate", "category")
                .fieldSetMapper(fieldSet -> {
                    Deal deal = new Deal();
                    deal.setDealNumber(fieldSet.readString("dealNumber"));
                    deal.setCustomerName(fieldSet.readString("customerName"));
                    deal.setAmount(new BigDecimal(fieldSet.readString("amount")));
                    deal.setCurrency(fieldSet.readString("currency"));
                    deal.setDealDate(LocalDate.parse(fieldSet.readString("dealDate")));
                    deal.setCategory(fieldSet.readString("category"));
                    deal.setStatus("NEW");
                    return deal;
                })
                .linesToSkip(1)
                .build();
    }

    // --- Writer (für beide Steps gleich) ---
    @Bean
    public JdbcBatchItemWriter<Deal> stagingWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Deal>()
                .dataSource(dataSource)
                .sql("INSERT INTO staging_deals (deal_number, customer_name, amount, currency, deal_date, category, status, created_at) " +
                     "VALUES (:dealNumber, :customerName, :amount, :currency, :dealDate, :category, :status, NOW())")
                .beanMapped()
                .build();
    }

    // --- Steps ---
    @Bean
    public Step dbInputStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                            JdbcCursorItemReader<Deal> sourceReader, JdbcBatchItemWriter<Deal> stagingWriter) {
        return new StepBuilder("dbInputStep", jobRepository)
                .<Deal, Deal>chunk(5, transactionManager)
                .reader(sourceReader)
                .writer(stagingWriter)
                .build();
    }

    @Bean
    public Step csvInputStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                             FlatFileItemReader<Deal> csvReader, JdbcBatchItemWriter<Deal> stagingWriter) {
        return new StepBuilder("csvInputStep", jobRepository)
                .<Deal, Deal>chunk(5, transactionManager)
                .reader(csvReader)
                .writer(stagingWriter)
                .build();
    }

    // --- Job 1: INPUT (Susy 1 + Susy 2) ---
    @Bean
    public Job inputJob(JobRepository jobRepository, Step dbInputStep, Step csvInputStep) {
        return new JobBuilder("inputJob", jobRepository)
                .start(dbInputStep)
                .next(csvInputStep)
                .build();
    }
}