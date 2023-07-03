import org.example.Mail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.example.Mail.*;
import static org.example.Mail.Package;



public class MailTest {
    @Test
    void testPackage() { //content packaging test
        Package mailPackage = new Package("iPhone", 1000);
        assertEquals("iPhone", mailPackage.getContent());
    }
    @Test
    void testRealMailService() { //test delivery service
        Sendable mailMessage = new MailMessage("Berlin", "Moskow", "Iphone");
        RealMailService realMailService = new RealMailService();
        Assertions.assertEquals(mailMessage, realMailService.processMail(mailMessage));
    }


    /*
    Test untrustworthy mailworker.
    Untrustworthy mailworker sends mail to all mailservice
    and returns the result of the last delivery.
    */
    @Test
    void testUntrustworthyMailWorker() {
        Sendable mailMessage1 = new MailMessage("Berlin", "Moskow", "Iphone");
        RealMailService realMailService1 = new RealMailService();
        RealMailService realMailService2 = new RealMailService();
        RealMailService[] mailServices = {realMailService1,realMailService2};
        UntrustworthyMailWorker untrustworthyMailWorker = new UntrustworthyMailWorker(mailServices);
        assertEquals(realMailService2.processMail(mailMessage1), untrustworthyMailWorker.processMail(mailMessage1));
    }

    /*
    Тест работы шпиона. Проверка выполнения корректной записи с лог
    при соответствии отправителя.
     */
    @Test
    void testSpy() {
        String target = "Austin Powers";
        MailMessage mailMessage = new MailMessage("Austin Powers","mailTo","mailContent");
        Logger log = Mockito.mock(Logger.class);
        Spy spy = new Spy(log);
        spy.processMail(mailMessage, target);
        verify(log).log(Level.WARNING,
                "Detected target mail correspondence: from {0} to {1} {2}",
                new Object[]{target, mailMessage.getTo(), mailMessage.getMessage()});
    }

    @Test
    void testTief() { // Проверяем что Tief ворует посылки (дороже 999 в данном случае)
        Package pack = new Package("iPhone", 1000);
        Sendable mailPackage = new MailPackage("Berlin", "Moskow", pack);
        Tief tief = new Tief(999);
        // Для вызова getContent требуется приведение из интерфейса Sendable к его реализации MailPackage
        MailPackage packBack = (MailPackage)tief.processMail(mailPackage);
        assertEquals("stones instead of iPhone", packBack.getContent().getContent());
    }


    /*
    Тестируем выброс исключения если в посылке оружие
    и проверяем сообщение этого исключения
     */
    @Test()
      void testInspector() {
        Package pack = new Package(Mail.WEAPONS, 1000);
        Sendable mailPackage = new MailPackage("Berlin", "Moskow", pack);
        Inspector inspector = new Inspector();
        Exception exception = assertThrows(IllegalPackageException.class, () -> {
            inspector.processMail(mailPackage);
        });
        String expectedMessage = "IllegalPackage";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}