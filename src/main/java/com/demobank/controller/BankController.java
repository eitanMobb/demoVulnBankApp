package com.demobank.controller;

import com.demobank.entity.User;
import com.demobank.entity.Account;
import com.demobank.entity.CreditApplication;
import com.demobank.entity.Transaction;
import com.demobank.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class BankController {
    
    @Autowired
    private BankService bankService;
    
    @GetMapping("/")
    public String home() {
        return "login";
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        
        User user = bankService.authenticateUser(username, password);
        
        if (user != null) {
            session.setAttribute("user", user);
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Account> accounts = bankService.getUserAccounts(user.getId().toString());
        
        // Get recent transactions (last 5)
        List<Transaction> recentTransactions = bankService.getUserTransactions(user.getId().toString(), 
                                                                             null, null, null, null, null, null);
        List<Map<String, Object>> recentTransactionDetails = recentTransactions.stream()
            .limit(5)
            .map(bankService::getTransactionWithAccountInfo)
            .collect(Collectors.toList());
        
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("recentTransactions", recentTransactionDetails);
        return "dashboard";
    }
    
    @GetMapping("/accounts")
    public String accounts(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Account> accounts = bankService.getUserAccounts(user.getId().toString());
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        return "accounts";
    }
    
    @GetMapping("/transfer")
    public String transferPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Account> accounts = bankService.getUserAccounts(user.getId().toString());
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        return "transfer";
    }
    
    /**
     * Transfer endpoint
     */
    @PostMapping("/transfer")
    public String transfer(@RequestParam String fromAccount,
                          @RequestParam String toAccount,
                          @RequestParam String amount,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        boolean success = bankService.transferMoney(fromAccount, toAccount, amount);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Transfer completed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Oh boy, transfer failed!");
        }
        
        return "redirect:/accounts";
    }
    
    @GetMapping("/credit-application")
    public String creditApplicationPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        return "credit-application";
    }
    
    /**
     * Credit application endpoint
     */
    @PostMapping("/credit-application")
    public String submitCreditApplication(@RequestParam String requestedLimit,
                                        @RequestParam String annualIncome,
                                        @RequestParam String employmentStatus,
                                        @RequestParam String comments,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        boolean success = bankService.createCreditApplication(
            user.getId().toString(), requestedLimit, annualIncome, employmentStatus, comments
        );
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Credit application submitted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to submit credit application!");
        }
        
        return "redirect:/dashboard";
    }
    
    /**
     * Search endpoint
     */
    @GetMapping("/search")
    public String searchPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        return "search";
    }
    
    @PostMapping("/search")
    public String search(@RequestParam String searchTerm,
                        HttpSession session,
                        Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        List<CreditApplication> results = bankService.searchCreditApplications(searchTerm);
        
        model.addAttribute("user", user);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("results", results);
        return "search";
    }
    
    /**
     * Transaction history page
     */
    @GetMapping("/transactions")
    public String transactionsPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Get all transactions for the user (no filters initially)
        List<Transaction> transactions = bankService.getUserTransactions(user.getId().toString(), 
                                                                       null, null, null, null, null, null);
        
        // Get transaction details with account info
        List<Map<String, Object>> transactionDetails = transactions.stream()
            .map(bankService::getTransactionWithAccountInfo)
            .collect(Collectors.toList());
        
        List<Account> accounts = bankService.getUserAccounts(user.getId().toString());
        
        model.addAttribute("user", user);
        model.addAttribute("transactions", transactionDetails);
        model.addAttribute("accounts", accounts);
        return "transactions";
    }
    
    /**
     * Search transactions with filters
     */
    @PostMapping("/transactions/search")
    public String searchTransactions(@RequestParam(required = false) String transactionType,
                                   @RequestParam(required = false) String fromDate,
                                   @RequestParam(required = false) String toDate,
                                   @RequestParam(required = false) String minAmount,
                                   @RequestParam(required = false) String maxAmount,
                                   @RequestParam(required = false) String description,
                                   HttpSession session,
                                   Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Search transactions with filters
        List<Transaction> transactions = bankService.getUserTransactions(user.getId().toString(), 
                                                                       transactionType, fromDate, toDate, 
                                                                       minAmount, maxAmount, description);
        
        // Get transaction details with account info
        List<Map<String, Object>> transactionDetails = transactions.stream()
            .map(bankService::getTransactionWithAccountInfo)
            .collect(Collectors.toList());
        
        List<Account> accounts = bankService.getUserAccounts(user.getId().toString());
        
        model.addAttribute("user", user);
        model.addAttribute("transactions", transactionDetails);
        model.addAttribute("accounts", accounts);
        
        // Preserve search criteria in the form
        model.addAttribute("searchTransactionType", transactionType);
        model.addAttribute("searchFromDate", fromDate);
        model.addAttribute("searchToDate", toDate);
        model.addAttribute("searchMinAmount", minAmount);
        model.addAttribute("searchMaxAmount", maxAmount);
        model.addAttribute("searchDescription", description);
        
        return "transactions";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
