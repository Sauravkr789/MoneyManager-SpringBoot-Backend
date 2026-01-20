package com.example.expanse_tracker.service;

import com.example.expanse_tracker.dto.ExpenseDTO;
import com.example.expanse_tracker.entity.ProfileEntity;
import com.example.expanse_tracker.repository.ExpenseRepository;
import com.example.expanse_tracker.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private final EmailService emailService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Kolkata") // Every day at 10 PM IST
    public void sendDailyIncomeExpenesRemainder()
    {
        log.info("Job started: sendDailyIncomeExpenesRemainder()");
        List<ProfileEntity> profiles=profileRepository.findAll();

        for(ProfileEntity profile: profiles)
        {
            String body="Hi " + profile.getFullName() +",<br><br>" +
                    "This is a gentle reminder to log your income and expenses for today.<br>" +
                    "You can log your income and expenses by clicking the link below:<br>" +
                    "<a href="+frontendUrl+" style='display:inline-block;padding:10px 20px;background-color:#4CAF50;color:#fff;text-decoration:none;border-radius:5px;font-weight:bold;'>Go to Money Manager</a><br><br><br>" +
                    "Best regards,<br>" +
                    "Money Manager Team";

            emailService.sendEmail(profile.getEmail(),"Daily remainder: Add your income and expenses",body);
        }
        log.info("Job completed: sendDailyIncomeExpenesRemainder()");
    }


    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kolkata") // Every day at 11 PM IST
    public void sendDailyExpenseSummary()
    {
        log.info("Job started: sendDailyExpenseSummary()");
        List<ProfileEntity> profiles=profileRepository.findAll();

        for(ProfileEntity profile: profiles)
        {
            List<ExpenseDTO> todayExpenses=expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now(ZoneId.of("Asia/Kolkata")));

            if(!todayExpenses.isEmpty())
            {
               StringBuilder table= new StringBuilder();
               table.append("<table style='width:100%;border-collapse:collapse;'>");
               table.append("<tr style='background-color:#f2f2f2;'><th style='border:1px solid #ddd;padding:8px;'>S.No</th><th style='border:1px solid #ddd;padding:8px;'>Name</th><th style='border:1px solid #ddd;padding:8px;'>Amount</th><th style='border:1px solid #ddd;padding:8px;'>Category</th><th style='border:1px solid #ddd;padding:8px;'>Date</th></tr>");
               int i=1;
                for(ExpenseDTO expense: todayExpenses)
                {
                    table.append("<tr>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>").append(i++).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getName()).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getAmount()).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getCategoryId()!=null?expense.getCategoryId():"N/A").append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getDate()).append("</td>")
                            .append("</tr>");
                }
                table.append("</table>");
                String body="Hi " + profile.getFullName() +",<br><br>" +
                        "Here is the summary of your expenses for today:<br><br>" +
                        table +
                        "<br>Best regards,<br>" +
                        "Money Manager Team";

                emailService.sendEmail(profile.getEmail(),"Daily Expense Summary",body);
            }
        }
        log.info("Job completed: sendDailyExpenseSummary()");
    }
}
