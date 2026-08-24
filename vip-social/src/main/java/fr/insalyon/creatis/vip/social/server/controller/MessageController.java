package fr.insalyon.creatis.vip.social.server.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insalyon.creatis.vip.core.client.DefaultError;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.social.models.GroupMessage;
import fr.insalyon.creatis.vip.social.models.Message;
import fr.insalyon.creatis.vip.social.server.business.MessageBusiness;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageBusiness messageBusiness;

    @Autowired
    public MessageController(MessageBusiness messageBusiness) {
        this.messageBusiness = messageBusiness;
    }

    @GetMapping
    public List<Message> getReceivedMessages(@RequestParam(required = false) Long startDate) throws VipException {
        return messageBusiness.getMessagesByUser(toDate(startDate));
    }

    @GetMapping("/send")
    public List<Message> getSentMessages(@RequestParam(required = false) Long startDate) throws VipException {
        return messageBusiness.getSentMessagesByUser(toDate(startDate));
    }

    @GetMapping("/groups")
    public List<GroupMessage> getGroupMessages(@RequestParam String groupName,
            @RequestParam(required = false) Long startDate) throws VipException {
        return messageBusiness.getGroupMessages(groupName, toDate(startDate));
    }

    @PostMapping
    public void postMessage(@RequestBody @Valid Message message) throws VipException {

        if (message == null || message.getReceivers() == null || message.getReceivers().length == 0) {
            throw new VipException(DefaultError.BAD_INPUT_FIELD, "receivers", "Receivers are required!");
        }

        // extract receiver IDs
        String[] recipientIds = java.util.Arrays.stream(message.getReceivers())
                .filter(r -> r != null && r.getId() != null && !r.getId().isBlank())
                .map(r -> r.getId())
                .toArray(String[]::new);

        if (recipientIds.length == 0) {
            throw new VipException(DefaultError.BAD_INPUT_FIELD, "receivers", "Receivers must contain at least one valid user ID");
        }

        messageBusiness.sendMessage(recipientIds, message.getTitle(), message.getMessage());
    }

    @PostMapping("/groups")
    public void postGroupMessage(@RequestBody @Valid GroupMessage groupMessage) throws VipException {
        if (groupMessage == null || groupMessage.getGroupName() == null || groupMessage.getGroupName().isBlank()) {
            throw new VipException(DefaultError.BAD_INPUT_FIELD, "groupName", "Group name is required!");
        }

        messageBusiness.sendGroupMessage(groupMessage.getGroupName(), groupMessage.getTitle(), groupMessage.getMessage());
    }

    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable long id) throws VipException {
        messageBusiness.removeByReceiver(id);
    }

    @DeleteMapping("/send/{id}")
    public void deleteSentMessage(@PathVariable long id) throws VipException {
        messageBusiness.remove(id);
    }

    @DeleteMapping("/groups/{id}")
    public void deleteGroupMessage(@PathVariable long id) throws VipException {
        messageBusiness.removeGroupMessage(id);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable long id) throws VipException {
        messageBusiness.markAsRead(id);
    }

    private Date toDate(Long startDate) {
        return startDate == null ? new Date() : new Date(startDate);
    }

}
