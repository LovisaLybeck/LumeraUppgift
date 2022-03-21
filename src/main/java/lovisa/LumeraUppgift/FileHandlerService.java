package lovisa.LumeraUppgift;


import lovisa.LumeraUppgift.PaymentReciver.PaymentReceiver;
import lovisa.LumeraUppgift.PaymentReciver.PaymentReceiverBetalningsservice;
import lovisa.LumeraUppgift.PaymentReciver.PaymentReceiverInbetalningstjansten;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class FileHandlerService {

    FileStatus status;
    PaymentReceiver paymentReceiver;

    public String inputFile(MultipartFile file) throws FileInputException, IOException, ParseException {
        if (file.getOriginalFilename() != null && file.getOriginalFilename().contains("_")) {
            String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("_"));
            switch (fileType){
                case "_inbetalningstjansten.txt":
                    typeInbetalningstjansten(file);
                    break;

                case "_betalningsservice.txt":
                    typeBetalningsservice(file);
                    break;

                default:
                    throw new FileInputException(file.getOriginalFilename() + " is of unknown type");
            }
            return file.getOriginalFilename() + " has been processed";
        } else {
            throw new FileInputException("Unable to process file");
        }
    }

    private void typeBetalningsservice(MultipartFile file) throws IOException, ParseException {
        paymentReceiver = new PaymentReceiverBetalningsservice();
        status = FileStatus.STARTED;
        String tempLine;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1))) {
            while ((tempLine = reader.readLine()) != null){
                String post = new String(tempLine.getBytes("ISO-8859-1"), "UTF-8");

                switch (post.charAt(0)){
                    case 'O':
                        if (status != FileStatus.IN_PROGRESS){
                            status = FileStatus.IN_PROGRESS;
                            String accountNumber = post.substring(1,16).replace(" ", "");
                            if (!accountNumber.matches("[0-9]+"))
                                throw new FileInputException(file.getOriginalFilename() + ", account number contains illegal characters");

                            Date paymentDate = new SimpleDateFormat("yyyyMMdd").parse(post.substring(40,48));
                            String currency = post.substring(48);

                            paymentReceiver.startPaymentBundle(accountNumber, paymentDate, currency);
                        } else {
                            throw new FileInputException(file.getOriginalFilename() + " contains multiple starting posts");
                        }
                        break;
                    case 'B':
                        if (status == FileStatus.IN_PROGRESS){
                            BigDecimal amount = new BigDecimal(post.substring(1,15).trim().replace(',', '.'));
                            String reference = post.substring(15).trim();

                            if (!reference.matches("[0-9A-ZÅÄÖ]+"))
                                throw new FileInputException(file.getOriginalFilename() + ", contains illegal characters");

                            paymentReceiver.payment(amount, reference);

                        } else {
                            throw new FileInputException(file.getOriginalFilename() + " is missing a starting post");
                        }
                        break;

                    default:
                        throw new FileInputException(file.getOriginalFilename() + ", contains unknown type of post");
                }
            }
            paymentReceiver.endPaymentBundle();
            status = FileStatus.CLOSED;
        }
    }

    private void typeInbetalningstjansten(MultipartFile file) throws IOException, ParseException {
        paymentReceiver = new PaymentReceiverInbetalningstjansten();
        String tempLine;
        status = FileStatus.STARTED;
        BigDecimal totalAmount = BigDecimal.valueOf(0);
        int numberOfPosts = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1));) {
            while ((tempLine = reader.readLine()) != null){
                String post = new String(tempLine.getBytes("ISO-8859-1"), "UTF-8");

                switch (post.substring(0,2)){
                    case "00":
                        if (status == FileStatus.STARTED){
                            status = FileStatus.IN_PROGRESS;

                            if (!(post.substring(2,10) + post.substring(24,80)).matches("[0]+")) {
                                throw new FileInputException(file.getOriginalFilename() + " start post contains illegal characters");
                            }

                            String accountNumber = post.substring(10,14) + post.substring(14,24);
                            if (!accountNumber.matches("[0-9]+")) {
                                throw new FileInputException(file.getOriginalFilename() + ", account number contains illegal characters");
                            }

                            paymentReceiver.startPaymentBundle(accountNumber, new Date(), "SEK");
                        } else {
                            throw new FileInputException(file.getOriginalFilename() + " contains multiple starting posts");
                        }
                        break;

                    case "30":
                        if (status == FileStatus.IN_PROGRESS) {
                            if (!post.substring(22,40).matches("[0]+")) {
                                throw new FileInputException(file.getOriginalFilename() + " a post contains illegal characters");
                            }
                            String amountString = post.substring(2,20) + "." + post.substring(20,22);

                            if (!amountString.matches("[0-9.]+")) {
                                throw new FileInputException(file.getOriginalFilename() + ", contains illegal characters");
                            }
                            BigDecimal amount = new BigDecimal(amountString);
                            String reference = post.substring(40).trim();

                            if (!reference.matches("[0-9A-ZÅÄÖ]+")) {
                                throw new FileInputException(file.getOriginalFilename() + ", reference contains illegal characters");
                            }

                            paymentReceiver.payment(amount, reference);
                            totalAmount = totalAmount.add(amount);
                            numberOfPosts ++;

                        } else {
                            throw new FileInputException(file.getOriginalFilename() + " is missing a starting post");
                        }
                        break;

                    case "99":
                        if (reader.readLine() != null)
                            throw new FileInputException(file.getOriginalFilename() + ", contains posts after closing post");

                        if (!(post.substring(22,30) + post.substring(38,80)).matches("[0]+")) {
                            throw new FileInputException(file.getOriginalFilename() + " contains illegal characters");
                        }

                        if (!totalAmount.equals(new BigDecimal(post.substring(2,20) + "." + post.substring(20,22))) ||
                                numberOfPosts != Integer.decode(post.substring(30,38))) {
                            throw new FileInputException(file.getOriginalFilename() + " closing post data does not match actual posts");
                        }

                        paymentReceiver.endPaymentBundle();
                        status = FileStatus.CLOSED;
                        break;

                    default:
                        throw new FileInputException(file.getOriginalFilename() + ", contains unknown type of post");
                }
            }
        }
        if (status != FileStatus.CLOSED) {
            throw new FileInputException(file.getOriginalFilename() + ", is missing a closing post");
        }
    }
}

