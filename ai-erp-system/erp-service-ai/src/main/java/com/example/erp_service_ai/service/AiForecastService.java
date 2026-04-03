package com.example.erp_service_ai.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class AiForecastService {

    private final ChatModel chatModel;

    // Spring AI automatically injects the ChatModel connected to Ollama!
    public AiForecastService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String predictDemand(String productName, String salesHistory) {
        // 1. We construct the prompt for the LLM
        String prompt = "You are an expert supply chain ERP analyst. " +
                "Here is the past 6 months of sales data for a product called '" + productName + "': " + salesHistory + ". " +
                "Based on this data, predict the sales for the next month. " +
                "Provide a brief, professional explanation of your reasoning. Keep it under 3 sentences.";

        System.out.println("🤖 Sending prompt to Llama 3.2...");

        // 2. We call the local AI and return its response!
        return chatModel.call(prompt);
    }
}