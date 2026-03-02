package de.svenjerie.eva.domain;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@XmlRootElement(name = "deal")
@Entity
@Table(name = "staging_deals")
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dealNumber;
    private String customerName;
    private BigDecimal amount;
    private String currency;
    private LocalDate dealDate;
    private String category;
    private String status;
    private String riskLevel;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Deal() {}

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDealNumber() { return dealNumber; }
    public void setDealNumber(String dealNumber) { this.dealNumber = dealNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDate getDealDate() { return dealDate; }
    public void setDealDate(LocalDate dealDate) { this.dealDate = dealDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}