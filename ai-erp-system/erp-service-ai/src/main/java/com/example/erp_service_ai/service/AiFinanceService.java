package com.example.erp_service_ai.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiFinanceService {

    private final ChatModel chatModel;

    public AiFinanceService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String detectAnomalies(List<Double> transactions) {
        // We instruct the AI to act as a strict financial auditor
        String prompt = "You are an expert financial auditor. " +
                "Review the following recent transaction amounts: " + transactions.toString() + ". " +
                "Identify any anomalies, outliers, or highly suspicious transactions. " +
                "Explain your reasoning concisely in 2 to 3 sentences.";

        System.out.println("🕵️ Sending financial data to Llama 3.2 for audit...");

        return chatModel.call(prompt);
    }
}