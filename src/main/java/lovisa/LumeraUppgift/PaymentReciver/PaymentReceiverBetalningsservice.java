package lovisa.LumeraUppgift.PaymentReciver;

import java.math.BigDecimal;
import java.util.Date;

public class PaymentReceiverBetalningsservice implements PaymentReceiver{
    @Override
    public void startPaymentBundle(String accountNumber, Date paymentDate, String currency) {
        //TODO code to handle the start of a Betalningsservice file.
    }

    @Override
    public void payment(BigDecimal amount, String reference) {
        //TODO code to hande payment for a Betalningsservice file
    }

    @Override
    public void endPaymentBundle() {
        //TODO code to end bundle for a Betalningsservice file
    }
}
