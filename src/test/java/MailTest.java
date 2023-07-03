import org.example.Mail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MailTest {

    @Test
    void testMailClass() { //пробую мокито на вкус
        Mail mail = Mockito.mock(Mail.class);
        assertEquals("weapons", mail.WEAPONS);
    }

    @Test
    void testPackage() { //content packaging test
        Mail.Package mailPackage = new Mail.Package("iPhone", 1000);
        assertEquals("iPhone", mailPackage.getContent());
    }
    @Test
    void testRealMailService() { //test delivery service
        Mail.Sendable mailMessage = new Mail.MailMessage("Berlin", "Moskow", "Iphone");
        Mail.RealMailService realMailService = new Mail.RealMailService();
        Assertions.assertEquals(mailMessage, realMailService.processMail(mailMessage));
    }


    /*
    Тест работы недоверенного почтальена.
    Если он перемешивает посылки, то возвращается null
    */
    @Test
    void testUntrustworthyMailWorker() {
        Mail.MailPackage mailPackage = Mockito.mock(Mail.MailPackage.class);
        Mail.RealMailService realMailService1 = Mockito.mock(Mail.RealMailService.class);
        Mail.RealMailService realMailService2 = Mockito.mock(Mail.RealMailService.class);
        Mail.MailService[] mailServices = {realMailService1,realMailService2};
        Mail.UntrustworthyMailWorker untrustworthyMailWorker = new Mail.UntrustworthyMailWorker(mailServices);
        assertEquals(null, untrustworthyMailWorker.processMail(mailPackage));
    }

    /*
    Тест работы шпиона. Проверка выполнения корректной записи с лог
    при соответствии отправителя.
     */
    @Test
    void testSpy() {
        Mail.MailMessage mailMessage = new Mail.MailMessage(Mail.AUSTIN_POWERS,"mailTo","mailContent");
        Logger log = Mockito.mock(Logger.class);
        Mail.Spy spy = new Mail.Spy(log);
        spy.processMail(mailMessage);
        verify(log).log(Level.WARNING,
                "Detected target mail correspondence: from {0} to {1} {2}",
                new Object[]{mailMessage.getFrom(), mailMessage.getTo(), mailMessage.getMessage()});
    }

    @Test
    void testTief() { // Проверяем что Tief ворует посылки (дороже 999 в данном случае)
        Mail.Package pack = new Mail.Package("iPhone", 1000);
        Mail.Sendable mailPackage = new Mail.MailPackage("Berlin", "Moskow", pack);
        Mail.Tief tief = new Mail.Tief(999);
        // Для вызова getContent требуется приведение из интерфейса Sendable к его реализации MailPackage
        Mail.MailPackage packBack = (Mail.MailPackage)tief.processMail(mailPackage);
        assertEquals("stones instead of iPhone", packBack.getContent().getContent());
    }


    /*
    Тестируем выброс исключения если в посылке оружие
    и проверяем сообщение этого исключения
     */
    @Test()
      void testInspector() {
        Mail.Package pack = new Mail.Package(Mail.WEAPONS, 1000);
        Mail.Sendable mailPackage = new Mail.MailPackage("Berlin", "Moskow", pack);
        Mail.Inspector inspector = new Mail.Inspector();
        Exception exception = assertThrows(Mail.IllegalPackageException.class, () -> {
            inspector.processMail(mailPackage);
        });
        String expectedMessage = "IllegalPackage";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}