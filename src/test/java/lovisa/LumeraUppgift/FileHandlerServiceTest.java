package lovisa.LumeraUppgift;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FileHandlerServiceTest {

    @Autowired
    FileHandlerService fileHandlerService;

    @Test
    void inputFileBetalningsService() throws IOException, ParseException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file_betalningsservice.txt",
                "text/plain",
                ("O5555 5555555555       4711,17         420110315SEK\n" +
                "B          30001234567890                         \n" +
                "B          10002345678901                         \n" +
                "B        300,103456789012                         \n" +
                "B        400,074567890123                         \n").getBytes());

        assertEquals("file_betalningsservice.txt has been processed", fileHandlerService.inputFile(multipartFile));
    }

    @Test
    void inputFileBetalningsserviceWithLetters() throws IOException, ParseException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file_betalningsservice.txt",
                "text/plain",
                ("O5555 5555555555       4711,17         420110315SEK\n" +
                        "B          30001234567890                         \n" +
                        "B          10002345u78901                         \n" +
                        "B        300,103456789012                         \n" +
                        "B        400,074567890123                         \n").getBytes());

        Exception exception = assertThrows(FileInputException.class, () -> {
            fileHandlerService.inputFile(multipartFile);
        });

        String expectedMessage = "file_betalningsservice.txt, contains illegal characters";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void inputFileInbetalningstjansten() throws IOException, ParseException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file_inbetalningstjansten.txt",
                "text/plain",
                ("00000000001234123456789700000000000000000000000000000000000000000000000000000000\n" +
                        "30000000000000004000000000000000000000009876543210                              \n" +
                        "30000000000000001000000000000000000000009876543210                              \n" +
                        "30000000000000010300000000000000000000009876543210                              \n" +
                        "99000000000000015300000000000000000003000000000000000000000000000000000000000000\n").getBytes());

        assertEquals("file_inbetalningstjansten.txt has been processed", fileHandlerService.inputFile(multipartFile));
    }

    @Test
    void inputFileInbetalningstjanstenWithLetters() throws IOException, ParseException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file_inbetalningstjansten.txt",
                "text/plain",
                ("00000000001234123456789700000000000000000000000000000000000000000000000000000000\n" +
                        "30000000000000004000000000000000000000009876543210                              \n" +
                        "30000000000000001000000000000000000000009876543210                              \n" +
                        "3000000000000001L300000000000000000000009876543210                              \n" +
                        "99000000000000015300000000000000000003000000000000000000000000000000000000000000\n").getBytes());

        Exception exception = assertThrows(FileInputException.class, () -> {
            fileHandlerService.inputFile(multipartFile);
        });

        String expectedMessage = "file_inbetalningstjansten.txt, contains illegal characters";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void inputFileInbetalningstjanstenLastPostNotMatching() throws IOException, ParseException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file_inbetalningstjansten.txt",
                "text/plain",
                ("00000000001234123456789700000000000000000000000000000000000000000000000000000000\n" +
                        "30000000000000004000000000000000000000009876543210                              \n" +
                        "30000000000000001000000000000000000000009876543210                              \n" +
                        "30000000000000010300000000000000000000009876543210                              \n" +
                        "99000000000000016300000000000000000003000000000000000000000000000000000000000000\n").getBytes());

        Exception exception = assertThrows(FileInputException.class, () -> {
            fileHandlerService.inputFile(multipartFile);
        });

        String expectedMessage = "file_inbetalningstjansten.txt closing post data does not match actual posts";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void inputFileIncorrectFile() throws IOException, ParseException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                ("fileText").getBytes());

        Exception exception = assertThrows(FileInputException.class, () -> {
            fileHandlerService.inputFile(multipartFile);
        });

        String expectedMessage = "Unable to process file";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void inputFileUnknownFileType() throws IOException, ParseException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file_unknown.txt",
                "text/plain",
                ("fileText").getBytes());

        Exception exception = assertThrows(FileInputException.class, () -> {
            fileHandlerService.inputFile(multipartFile);
        });

        String expectedMessage = "file_unknown.txt is of unknown type";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
