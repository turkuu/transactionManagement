package com.transactionmanagement.datatranx.service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.transactionmanagement.datatranx.dao.IAccountDAO;
import com.transactionmanagement.datatranx.model.Account;

/**
 * @author Hamza AFFANI
 * @version 1.0
 * 
 * This class represents the first implementation of the programmatic transaction management using directly the transactionManager
 * 
 */

@Service
@Qualifier("progTrxManagerBean")
public class ProgTrxManagerAccountService implements IAccountService {

	private final NumberFormat fmt = NumberFormat.getCurrencyInstance();

	@Autowired
	private IAccountDAO accountDAO;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Override
	public List<Account> getAllAccounts() {
		return accountDAO.findAll();
	}

	@Override
	public void addAccount(Account account) {
		accountDAO.save(account);
	}

	@Override
	public Optional<Account> getAccount(int accountId) {
		return accountDAO.findById((long) accountId);
	}

	@Override
	public void transferMoney(Account from, Account to, double amount, double fee) {
		TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
		try {
			withdraw(from, amount, fee);
			deposit(to, amount);
			transactionManager.commit(transactionStatus);
		} catch (RuntimeException e) {
			transactionManager.rollback(transactionStatus);
			throw e;
		}
	}

	private void deposit(Account to, double amount) {
		Account accountToCredit = getAccount(to.getId().intValue()).get();

		if (amount < 0) // deposit value is negative
		{
			throw new RuntimeException(
					"Error: Deposit amount is invalid." + accountToCredit.getAcctNumber() + " " + fmt.format(amount));

		} else {
			accountToCredit.setBalance(accountToCredit.getBalance() + amount);
			accountToCredit.setLast_operation("Credited");
		}
	}

	private void withdraw(Account from, double amount, double fee) {
		Account accountToDebit = getAccount(from.getId().intValue()).get();

		amount += fee;
		if (amount < 0) // withdraw value is negative
		{
			throw new RuntimeException("Error: Withdraw amount is invalid. for the Account: "
					+ accountToDebit.getAcctNumber() + " Name: " + accountToDebit.getName());
		} else if (amount > accountToDebit.getBalance()) // withdraw value exceeds balance
		{
			throw new RuntimeException(
					"Error: Insufficient funds.\n Account: " + accountToDebit.getAcctNumber() + "\n" + "Requested:"
							+ fmt.format(amount) + "Available: " + "\n" + fmt.format(accountToDebit.getBalance()));
		} else {
			accountToDebit.setBalance(accountToDebit.getBalance() - amount);
			accountToDebit.setLast_operation("Debited");
		}
	}

}
