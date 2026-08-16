package tn.esprit.clientmanagmentctinetwork.Service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Model.MessageModel;
import tn.esprit.clientmanagmentctinetwork.Model.UserModel;
import tn.esprit.clientmanagmentctinetwork.Repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // This ensures test data doesn't clutter your real database
public class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldSendAndRetrieveMessagesCorrect() {
        // 1. Arrange: Create test users
        UserModel sender = new UserModel("Sender", "Test", "sender@cti.tn", "password", "ROLE_ADMIN");
        UserModel receiver = new UserModel("Receiver", "Test", "receiver@cti.tn", "password", "ROLE_TECHNICIAN");
        userRepository.save(sender);
        userRepository.save(receiver);

        // 2. Act: Send a message via the service
        messageService.sendMessage(
                sender.getEmail(),
                receiver.getEmail(),
                "Technical Alert",
                "Please check the server rack in Megrine."
        );

        // 3. Assert: Check if the message arrived in the receiver's inbox
        List<MessageModel> inbox = messageService.getInbox(receiver.getEmail());

        assertFalse(inbox.isEmpty(), "Inbox should not be empty");
        assertEquals("Technical Alert", inbox.get(0).getSubject());
        assertEquals(sender.getEmail(), inbox.get(0).getSenderEmail());
        assertFalse(inbox.get(0).isRead(), "New messages should be marked as unread");
    }
}