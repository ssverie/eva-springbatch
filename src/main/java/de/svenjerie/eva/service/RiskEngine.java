package de.svenjerie.eva.service;

import de.svenjerie.eva.domain.Deal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskEngine {

    private final List<RiskRule> rules;

    public RiskEngine(List<RiskRule> rules) {
        this.rules = rules;
    }

    public Deal assess(Deal deal) {
        for (RiskRule rule : rules) {
            if (rule.applies(deal)) {
                deal.setRiskLevel(rule.getRiskLevel());
                deal.setStatus("PROCESSED");
                return deal;
            }
        }
        deal.setRiskLevel("LOW");
        deal.setStatus("PROCESSED");
        return deal;
    }
}