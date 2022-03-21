package lovisa.LumeraUppgift.PaymentReciver;

import java.math.BigDecimal;
import java.util.Date;

public class PaymentReceiverInbetalningstjansten implements PaymentReceiver{
    @Override
    public void startPaymentBundle(String accountNumber, Date paymentDate, String currency) {
        //TODO code to handle the start of a Inbetalningstjänsten file.
    }

    @Override
    public void payment(BigDecimal amount, String reference) {
        //TODO code to hande payment for a Inbetalningstjänsten file
    }

    @Override
    public void endPaymentBundle() {
        //TODO code to end bundle for a Inbetalningstjänsten file
    }
}
