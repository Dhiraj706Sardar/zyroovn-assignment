package com.finance.backend.config;

import com.finance.backend.entity.FinancialRecord;
import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.enums.UserStatus;
import com.finance.backend.repository.FinancialRecordRepository;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final FinancialRecordRepository financialRecordRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) {
                // Check if users already exist
                if (userRepository.count() == 0) {
                        log.info("Initializing default users...");

                        // Create admin user
                        User admin = User.builder()
                                        .username("admin_user")
                                        .email("admin@example.com")
                                        .password(passwordEncoder.encode("password123"))
                                        .fullName("Amit - System Administrator")
                                        .role(Role.ADMIN)
                                        .status(UserStatus.ACTIVE)
                                        .build();
                        userRepository.save(admin);
                        log.info("Created admin user");

                        // Create viewer user
                        User viewer = User.builder()
                                        .username("viewer_user")
                                        .email("viewer@example.com")
                                        .password(passwordEncoder.encode("password123"))
                                        .fullName("Rahul - Normal User")
                                        .role(Role.VIEWER)
                                        .status(UserStatus.ACTIVE)
                                        .build();
                        userRepository.save(viewer);
                        log.info("Created viewer user");

                        // Create analyst user
                        User analyst = User.builder()
                                        .username("analyst_user")
                                        .email("analyst@example.com")
                                        .password(passwordEncoder.encode("password123"))
                                        .fullName("Priya - Financial Analyst")
                                        .role(Role.ANALYST)
                                        .status(UserStatus.ACTIVE)
                                        .build();
                        analyst = userRepository.save(analyst);
                        log.info("Created analyst user");

                        // Create financial records for analyst
                        createFinancialRecords(analyst);

                        log.info("Default users and financial records initialized successfully");
                } else {
                        log.info("Users already exist, skipping initialization");
                }
        }

        private void createFinancialRecords(User user) {
                log.info("Creating financial records for user: {}", user.getUsername());

                // January 2026
                createRecord(user, "5500.00", TransactionType.INCOME, "Salary", "2026-01-01", "Monthly salary",
                                "January salary payment");
                createRecord(user, "1600.00", TransactionType.EXPENSE, "Rent", "2026-01-05", "Monthly rent payment",
                                "Apartment rent");
                createRecord(user, "250.00", TransactionType.EXPENSE, "Groceries", "2026-01-07", "Weekly groceries",
                                "Supermarket shopping");
                createRecord(user, "180.00", TransactionType.EXPENSE, "Utilities", "2026-01-10", "Electricity and gas",
                                "Monthly utilities");
                createRecord(user, "95.00", TransactionType.EXPENSE, "Transportation", "2026-01-12", "Gas and parking",
                                "Weekly commute");
                createRecord(user, "450.00", TransactionType.INCOME, "Freelance", "2026-01-15", "Consulting work",
                                "Financial consulting project");
                createRecord(user, "320.00", TransactionType.EXPENSE, "Insurance", "2026-01-15", "Car insurance",
                                "Monthly premium");
                createRecord(user, "140.00", TransactionType.EXPENSE, "Entertainment", "2026-01-18", "Dinner and movie",
                                "Weekend entertainment");
                createRecord(user, "50.00", TransactionType.EXPENSE, "Subscriptions", "2026-01-20",
                                "Streaming services",
                                "Netflix and Spotify");
                createRecord(user, "220.00", TransactionType.EXPENSE, "Groceries", "2026-01-20", "Weekly groceries",
                                "Fresh produce and meat");
                createRecord(user, "75.00", TransactionType.EXPENSE, "Healthcare", "2026-01-22", "Pharmacy",
                                "Prescription medication");
                createRecord(user, "80.00", TransactionType.EXPENSE, "Pet Care", "2026-01-25", "Vet visit",
                                "Annual checkup for dog");
                createRecord(user, "300.00", TransactionType.EXPENSE, "Shopping", "2026-01-25", "Clothing",
                                "Winter clothes");

                // February 2026
                createRecord(user, "5500.00", TransactionType.INCOME, "Salary", "2026-02-01", "Monthly salary",
                                "February salary payment");
                createRecord(user, "1600.00", TransactionType.EXPENSE, "Rent", "2026-02-05", "Monthly rent payment",
                                "Apartment rent");
                createRecord(user, "200.00", TransactionType.EXPENSE, "Groceries", "2026-02-08", "Weekly groceries",
                                "Supermarket shopping");
                createRecord(user, "160.00", TransactionType.EXPENSE, "Utilities", "2026-02-10", "Water and internet",
                                "Monthly utilities");
                createRecord(user, "85.00", TransactionType.EXPENSE, "Transportation", "2026-02-12", "Public transport",
                                "Monthly pass");
                createRecord(user, "600.00", TransactionType.INCOME, "Bonus", "2026-02-14", "Performance bonus",
                                "Q4 2025 bonus");
                createRecord(user, "150.00", TransactionType.EXPENSE, "Gifts", "2026-02-14", "Valentine gifts",
                                "Flowers and dinner");
                createRecord(user, "320.00", TransactionType.EXPENSE, "Insurance", "2026-02-15", "Car insurance",
                                "Monthly premium");
                createRecord(user, "180.00", TransactionType.EXPENSE, "Entertainment", "2026-02-16", "Concert tickets",
                                "Live music event");
                createRecord(user, "240.00", TransactionType.EXPENSE, "Groceries", "2026-02-18", "Weekly groceries",
                                "Bulk shopping");
                createRecord(user, "120.00", TransactionType.EXPENSE, "Healthcare", "2026-02-20", "Dental checkup",
                                "Regular cleaning");
                createRecord(user, "50.00", TransactionType.EXPENSE, "Subscriptions", "2026-02-20",
                                "Streaming services",
                                "Netflix and Spotify");
                createRecord(user, "350.00", TransactionType.INCOME, "Investment", "2026-02-25", "Dividend payment",
                                "Stock dividends");
                createRecord(user, "80.00", TransactionType.EXPENSE, "Pet Care", "2026-02-25", "Pet supplies",
                                "Food and toys");
                createRecord(user, "1200.00", TransactionType.INCOME, "Tax Refund", "2026-02-28", "Annual tax refund",
                                "Federal tax return");

                // March 2026
                createRecord(user, "5500.00", TransactionType.INCOME, "Salary", "2026-03-01", "Monthly salary",
                                "March salary payment");
                createRecord(user, "1600.00", TransactionType.EXPENSE, "Rent", "2026-03-05", "Monthly rent payment",
                                "Apartment rent");
                createRecord(user, "210.00", TransactionType.EXPENSE, "Groceries", "2026-03-07", "Weekly groceries",
                                "Supermarket shopping");
                createRecord(user, "175.00", TransactionType.EXPENSE, "Utilities", "2026-03-10", "Electricity bill",
                                "Monthly electricity");
                createRecord(user, "90.00", TransactionType.EXPENSE, "Transportation", "2026-03-12",
                                "Gas and maintenance",
                                "Car expenses");
                createRecord(user, "500.00", TransactionType.EXPENSE, "Home Improvement", "2026-03-12", "Furniture",
                                "New desk and chair");
                createRecord(user, "500.00", TransactionType.INCOME, "Freelance", "2026-03-15", "Web development",
                                "Client project completion");
                createRecord(user, "320.00", TransactionType.EXPENSE, "Insurance", "2026-03-15", "Car insurance",
                                "Monthly premium");
                createRecord(user, "160.00", TransactionType.EXPENSE, "Entertainment", "2026-03-18",
                                "Restaurant and bar",
                                "Weekend outing");
                createRecord(user, "230.00", TransactionType.EXPENSE, "Groceries", "2026-03-20", "Weekly groceries",
                                "Household items");
                createRecord(user, "50.00", TransactionType.EXPENSE, "Subscriptions", "2026-03-20",
                                "Streaming services",
                                "Netflix and Spotify");
                createRecord(user, "200.00", TransactionType.EXPENSE, "Healthcare", "2026-03-22", "Doctor visit",
                                "Annual checkup");
                createRecord(user, "150.00", TransactionType.EXPENSE, "Education", "2026-03-25", "Online course",
                                "Professional development");
                createRecord(user, "80.00", TransactionType.EXPENSE, "Pet Care", "2026-03-25", "Pet grooming",
                                "Professional grooming");
                createRecord(user, "280.00", TransactionType.EXPENSE, "Shopping", "2026-03-28", "Electronics",
                                "New headphones");
                createRecord(user, "200.00", TransactionType.INCOME, "Cashback", "2026-03-31", "Credit card rewards",
                                "Quarterly cashback");

                // April 2026 (only past dates - today is April 3, 2026)
                createRecord(user, "5500.00", TransactionType.INCOME, "Salary", "2026-04-01", "Monthly salary",
                                "April salary payment");
                createRecord(user, "1600.00", TransactionType.EXPENSE, "Rent", "2026-04-02", "Monthly rent payment",
                                "Apartment rent");
                createRecord(user, "190.00", TransactionType.EXPENSE, "Groceries", "2026-04-03", "Weekly groceries",
                                "Fresh produce");

                log.info("Created {} financial records", financialRecordRepository.count());
        }

        private void createRecord(User user, String amount, TransactionType type, String category,
                        String date, String description, String notes) {
                FinancialRecord record = FinancialRecord.builder()
                                .amount(new BigDecimal(amount))
                                .type(type)
                                .category(category)
                                .transactionDate(LocalDate.parse(date))
                                .description(description)
                                .notes(notes)
                                .user(user)
                                .build();
                financialRecordRepository.save(record);
        }
}
