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
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.ResultSet;

@Configuration
public class OutputJobConfig {

    // --- Reader: PROCESSED aus processing_deals ---
    @Bean
    public JdbcCursorItemReader<Deal> outputReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Deal>()
                .name("outputReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM processing_deals WHERE status = 'PROCESSED'")
                .rowMapper((ResultSet rs, int rowNum) -> {
                    Deal deal = new Deal();
                    deal.setDealNumber(rs.getString("deal_number"));
                    deal.setCustomerName(rs.getString("customer_name"));
                    deal.setAmount(rs.getBigDecimal("amount"));
                    deal.setCurrency(rs.getString("currency"));
                    deal.setDealDate(rs.getDate("deal_date").toLocalDate());
                    deal.setCategory(rs.getString("category"));
                    deal.setStatus(rs.getString("status"));
                    deal.setRiskLevel(rs.getString("risk_level"));
                    return deal;
                })
                .build();
    }

    // --- Susy 6: XML Writer ---    
    @Bean
    public Jaxb2Marshaller dealMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Deal.class);
        marshaller.setMarshallerProperties(java.util.Map.of(
            jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true
        ));
        return marshaller;
    }
    

    @Bean
    public StaxEventItemWriter<Deal> xmlWriter(Jaxb2Marshaller dealMarshaller) {
        return new StaxEventItemWriterBuilder<Deal>()
                .name("xmlWriter")
                .resource(new FileSystemResource("output/deals-export.xml"))
                .marshaller(dealMarshaller)
                .rootTagName("deals")
                .overwriteOutput(true)
                .build();
    }

    @Bean
    public Step xmlOutputStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                              JdbcCursorItemReader<Deal> outputReader, StaxEventItemWriter<Deal> xmlWriter) {
        return new StepBuilder("xmlOutputStep", jobRepository)
                .<Deal, Deal>chunk(5, transactionManager)
                .reader(outputReader)
                .writer(xmlWriter)
                .build();
    }

    // --- Job 3: OUTPUT ---
    @Bean
    public Job outputJob(JobRepository jobRepository, Step xmlOutputStep, Step xmlFormatStep, Step dbOutputStep) {
        return new JobBuilder("outputJob", jobRepository)
                .start(xmlOutputStep)
                .next(xmlFormatStep)
                .next(dbOutputStep)
                .build();
    }
    
    @Bean
    public Step xmlFormatStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("xmlFormatStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    java.io.File file = new java.io.File("output/deals-export.xml");
                    javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
                    javax.xml.transform.Transformer transformer = tf.newTransformer();
                    transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                    javax.xml.transform.stream.StreamSource source = new javax.xml.transform.stream.StreamSource(file);
                    java.io.StringWriter writer = new java.io.StringWriter();
                    transformer.transform(source, new javax.xml.transform.stream.StreamResult(writer));
                    java.nio.file.Files.writeString(file.toPath(), writer.toString());
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
 // --- Susy 7: DB Export Writer ---
    @Bean
    public JdbcBatchItemWriter<Deal> outputDbWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Deal>()
                .dataSource(dataSource)
                .sql("INSERT INTO output_deals (deal_number, customer_name, amount, currency, deal_date, category, status, risk_level, created_at) " +
                     "VALUES (:dealNumber, :customerName, :amount, :currency, :dealDate, :category, :status, :riskLevel, NOW())")
                .beanMapped()
                .build();
    }

    
    @Bean
    public Step dbOutputStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                             JdbcCursorItemReader<Deal> outputReader2, JdbcBatchItemWriter<Deal> outputDbWriter) {
        return new StepBuilder("dbOutputStep", jobRepository)
                .<Deal, Deal>chunk(5, transactionManager)
                .reader(outputReader2)
                .writer(outputDbWriter)
                .build();
    }
    
    
    @Bean
    public JdbcCursorItemReader<Deal> outputReader2(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Deal>()
                .name("outputReader2")
                .dataSource(dataSource)
                .sql("SELECT * FROM processing_deals WHERE status = 'PROCESSED'")
                .rowMapper((ResultSet rs, int rowNum) -> {
                    Deal deal = new Deal();
                    deal.setDealNumber(rs.getString("deal_number"));
                    deal.setCustomerName(rs.getString("customer_name"));
                    deal.setAmount(rs.getBigDecimal("amount"));
                    deal.setCurrency(rs.getString("currency"));
                    deal.setDealDate(rs.getDate("deal_date").toLocalDate());
                    deal.setCategory(rs.getString("category"));
                    deal.setStatus(rs.getString("status"));
                    deal.setRiskLevel(rs.getString("risk_level"));
                    return deal;
                })
                .build();
    }

 
    
}