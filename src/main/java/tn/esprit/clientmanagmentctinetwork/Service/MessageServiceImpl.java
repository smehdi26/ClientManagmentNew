package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Model.MessageModel;
import tn.esprit.clientmanagmentctinetwork.Repository.MessageRepository;
import java.util.List;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void sendMessage(String from, String to, String subject, String content) {
        MessageModel msg = new MessageModel();
        msg.setSenderEmail(from);
        msg.setRecipientEmail(to);
        msg.setSubject(subject);
        msg.setContent(content);
        messageRepository.save(msg);
    }

    @Override
    public List<MessageModel> getInbox(String email) {
        return messageRepository.findByRecipientEmailOrderBySentAtDesc(email);
    }

    @Override
    public void markAsRead(Long id) {
        messageRepository.findById(id).ifPresent(msg -> {
            msg.setRead(true);
            messageRepository.save(msg);
        });
    }

    @Override
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    @Override
    public long getUnreadCount(String email) {
        return messageRepository.countByRecipientEmailAndIsReadFalse(email);
    }
}