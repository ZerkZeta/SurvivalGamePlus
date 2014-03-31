package me.Zen3515.EssecoBank.Exception;

import java.math.BigDecimal;

public class EssBankException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7458813546971111480L;
	
	private BigDecimal amount = BigDecimal.ZERO;
	
	public EssBankException(String Message){
		super(Message);
		amount = BigDecimal.ZERO;
	}
	
	public EssBankException(String Message,BigDecimal ammount){
		super(Message);
		amount = ammount;
	}
	
	@Deprecated
	public EssBankException(String Message, Throwable throwable){
		super(Message, throwable);
		amount = BigDecimal.ZERO;
	}
	
	public EssBankException(String Message, Throwable throwable,BigDecimal ammount){
		super(Message, throwable);
		amount = ammount;
	}
	
	public String getMessage()
    {
        return super.getMessage();
    }
	
	public BigDecimal getammount(){
		return amount;
	}
	
}
