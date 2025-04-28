package com.shop.online.config;

import com.shop.online.util.FileUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Application startup listener
 * Initializes necessary resources when the application starts
 */
@Component
public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Initialize default payment QR codes
        initializeDefaultResources();
    }
    
    /**
     * Initialize default resources required by the application
     */
    private void initializeDefaultResources() {
        try {
            // Initialize default payment QR codes
            FileUtil.initializeDefaultPaymentQrCodes();
        } catch (Exception e) {
            System.err.println("Error initializing default resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 