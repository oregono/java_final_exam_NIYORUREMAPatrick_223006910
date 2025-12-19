package com.main;

import com.ui.LoginForm;
import com.utils.DB;

public class Main {
	public static void main(String[] args) {
        System.out.println("🚀 Starting Utilities Platform System...");
        
        // Test database connection
        if (!DB.testConnection()) {
            System.err.println("💥 Cannot start application without database connection!");
            return;
        }
        
        // Start the login form
        System.out.println("✅ Starting login interface...");
        new LoginForm().setVisible(true);
    }
}


