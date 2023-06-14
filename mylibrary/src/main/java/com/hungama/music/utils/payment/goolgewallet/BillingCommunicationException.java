package com.hungama.music.utils.payment.goolgewallet;

public class BillingCommunicationException extends Exception
{

    public BillingCommunicationException(Throwable cause)
    {
        super(cause);
    }

    public BillingCommunicationException(String message)
    {
        super(message);
    }
}
