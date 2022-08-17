package com.transactionmanagement.datatranx.service;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.transactionmanagement.datatranx.dao.IAccountDAO;
import com.transactionmanagement.datatranx.model.Account;

@Service
@Qualifier("declarativeTrxManagementBean")
public class DeclarativeTrxAccountService implements IAccountService {

	private final NumberFormat fmt = NumberFormat.getCurrencyInstance();

	@Autowired
	private IAccountDAO accountDAO;

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
	@Transactional
	public void transferMoney(Account from, Account to, double amount, double fee){
		withdraw(from, amount, fee);
		deposit(to, amount);
	}


	public void deposit(Account to, double amount) {
		Account accountToCredit = getAccount(to.getId().intValue()).get();

		if (amount < 0) // deposit value is negative
		{
			throw new RuntimeException("Error: Deposit amount is invalid." + accountToCredit.getAcctNumber() + " " + fmt.format(amount));

		} else {
			accountToCredit.setBalance(accountToCredit.getBalance() + amount);
			accountToCredit.setLast_operation("Credited");
		}
	}

	/**
	 * Withdraws the specified amount from the account and returns the new balance.
	 */
	@Transactional(propagation = Propagation.NEVER)
	public void withdraw(Account from, double amount, double fee) {
		Account accountToDebit = getAccount(from.getId().intValue()).get();

		amount += fee;
		if (amount < 0) // withdraw value is negative
		{
			throw new RuntimeException("Error: Withdraw amount is invalid. for the Account: " + accountToDebit.getAcctNumber() + " Name: " + accountToDebit.getName());
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
