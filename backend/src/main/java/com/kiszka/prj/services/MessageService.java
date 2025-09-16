package com.kiszka.prj.services;

import com.kiszka.prj.DTOs.PeopleInfoDTO;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Message;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.KidRepository;
import com.kiszka.prj.repositories.MessageRepository;
import com.kiszka.prj.repositories.ParentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ParentRepository parentRepository;
    private final KidRepository kidRepository;

    public MessageService(MessageRepository messageRepository, ParentRepository parentRepository, KidRepository kidRepository) {
        this.messageRepository = messageRepository;
        this.parentRepository = parentRepository;
        this.kidRepository = kidRepository;
    }
    public void saveMessage(Message msg){
        messageRepository.save(msg);
    }
    public List<Message> getAllRelatedMessagesForKid(int kidId) {
        List<Message> result = new ArrayList<>();
        Kid kid = kidRepository.findById(kidId).orElse(null);
        if (kid == null) return result;
        result.addAll(messageRepository.findBySenderIdAndSenderType(kid.getId(), "KID"));
        Set<Parent> parents = kid.getParents();
        for (Parent parent : parents) {
            result.addAll(messageRepository.findBySenderIdAndSenderType(parent.getId(), "PARENT"));
            for (Kid sibling : parent.getKids()) {
                if (sibling.getId() != kidId) {
                    result.addAll(messageRepository.findBySenderIdAndSenderType(sibling.getId(), "KID"));
                }
            }
        }
        return result;
    }
    public List<Message> getAllRelatedMessagesForParent(int parentId) {
        List<Message> result = new ArrayList<>();
        Parent parent = parentRepository.findById(parentId).orElse(null);
        if (parent == null) return result;
        result.addAll(messageRepository.findBySenderIdAndSenderType(parent.getId(), "PARENT"));
        for (Kid kid : parent.getKids()) {
            result.addAll(messageRepository.findBySenderIdAndSenderType(kid.getId(), "KID"));
            for (Parent otherParent : kid.getParents()) {
                if (otherParent.getId() != parentId) {
                    result.addAll(messageRepository.findBySenderIdAndSenderType(otherParent.getId(), "PARENT"));
                }
            }
        }
        return result;
    }
    public List<PeopleInfoDTO> getAllRelatedPeopleForKid(int kidId) {
        List<PeopleInfoDTO> result = new ArrayList<>();
        Kid kid = kidRepository.findById(kidId).orElse(null);
        if (kid == null) return result;
        result.add(new PeopleInfoDTO(kid.getId(), kid.getName(), "KID"));
        for (Parent parent : kid.getParents()) {
            result.add(new PeopleInfoDTO(parent.getId(), parent.getUsername(), "PARENT"));
            for (Kid sibling : parent.getKids()) {
                if (sibling.getId() != kid.getId()) {
                    result.add(new PeopleInfoDTO(sibling.getId(), sibling.getName(), "KID"));
                }
            }
        }
        return result;
    }

    public List<PeopleInfoDTO> getAllRelatedPeopleForParent(int parentId) {
        List<PeopleInfoDTO> result = new ArrayList<>();
        Parent parent = parentRepository.findById(parentId).orElse(null);
        if (parent == null) return result;
        result.add(new PeopleInfoDTO(parent.getId(), parent.getUsername(), "PARENT"));
        for (Kid kid : parent.getKids()) {
            result.add(new PeopleInfoDTO(kid.getId(), kid.getName(), "KID"));
            for (Parent otherParent : kid.getParents()) {
                if (otherParent.getId() != parent.getId()) {
                    result.add(new PeopleInfoDTO(otherParent.getId(), otherParent.getUsername(), "PARENT"));
                }
            }
        }
        return result;
    }

}
