package lovisa.LumeraUppgift;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class FileHandlerControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void postFileCorrect() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file_betalningsservice.txt",
                "text/plain",
                ("O5555 5555555555       4711,17         420110315SEK\n" +
                        "B          30001234567890                         \n" +
                        "B          10002345678901                         \n" +
                        "B        300,103456789012                         \n" +
                        "B        400,074567890123                         \n").getBytes());

        MockMvc mockMvc
                = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(multipart("/handler/inputFile").file(multipartFile))
                .andExpect(status().isOk());
    }

    @Test
    void postFileIncorrect() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                ("fileText").getBytes());

        MockMvc mockMvc
                = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(multipart("/handler/inputFile").file(multipartFile))
                .andExpect(status().isBadRequest());
    }

}
