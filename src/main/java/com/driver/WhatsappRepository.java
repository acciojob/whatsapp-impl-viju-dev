package com.driver;

import java.util.*;

import org.springframework.beans.factory.support.ManagedList;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
            try {
                if (userMobile.contains(mobile)){
                    throw new RuntimeException("User already exists");
                }
            }
            catch (RuntimeException e){
                System.out.println(e.getMessage());
            }
            User user = new User(name,mobile);
            userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
        Group group = null;
         if (users.size() == 2){
             group = new Group(users.get(1).getName(),users.size());
            // group.setAdmin(users.get(0).getName()); // not admin one

         } else if (users.size() > 2) {
             customGroupCount++; // incremented first as 1
             group = new Group("Group"+customGroupCount+"",users.size());
            // group.setAdmin(users.get(0).getName()); // not admin one

         }
//         for (User user : users){
//             if (user.isInGrp()){
//
//             }
//         }
         groupUserMap.put(group,users);
         adminMap.put(group,users.get(0));
        return group;
    }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        messageId++; //incremented first to avoid fluctuation of returid and main id
        Message message = new Message(messageId,content);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        List<User> userList ;
        List<Message> messageList;
        boolean flag = false;
        try {
            if (!groupUserMap.containsKey(group)){
                throw new RuntimeException("Group does not exist");
            }

            userList = groupUserMap.get(group);
            for(User user:userList){
                if (Objects.equals(user,sender))
                    flag = true;
            }
            if(!flag){
                throw new RuntimeException("You are not allowed to send message");
            }
        }
        catch (RuntimeException e){
            System.out.println(e.getMessage());
        }

        if (groupMessageMap.containsKey(group)){
            messageList = groupMessageMap.get(group);
        }
        else {
            messageList = new ArrayList<>();
        }
        messageList.add(message);
        senderMap.put(message,sender);
        return messageList.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.
        List<User> userList ;
        boolean flag = false;
        try {
            if (!groupUserMap.containsKey(group)){
                throw new RuntimeException("Group does not exist");
            }
            userList = groupUserMap.get(group);
            if (!Objects.equals(userList.get(0),approver)){
                throw new RuntimeException("Approver does not have rights");
            }
            for(User user1:userList){
                if (Objects.equals(user1,user))
                    flag = true;
            }
            if (!flag){
                throw new RuntimeException("User is not a participant");
            }
        }
        catch (RuntimeException e){
            System.out.println(e.getMessage());
        }
        adminMap.put(group,user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)

        HashSet<User> userHashSet ;
        List<Message> messageList;
        List<User> userList;
        int userCount=0;
        int grpMessageCount=0;
        int totalMessageCount=0;
        boolean flag = false;
        try {
            for (Group group: groupUserMap.keySet()){
                userList = groupUserMap.get(group);
                for (User user1:userList){
                    if (Objects.equals(user1,user)){//user found
                        flag = true;
                        if(Objects.equals(adminMap.get(group),user)){
                            throw new RuntimeException("Cannot remove admin");
                        }
                        userList.remove(user);
                        userCount = userList.size();
                        //remove its messages
//                        groupMessageMap.get(group).
//                        senderMap.remove(user);
                        messageList = groupMessageMap.get(group); //took that specific group
                      //  messageList = new ArrayList<>();
                        for (Message message: senderMap.keySet()){ // iterating through all map to delte messages with same user
                            if (Objects.equals(senderMap.get(message),user)){
                              for (Message message1 : messageList){
                                  if (Objects.equals(message1,message)){
                                      messageList.remove(message);
//                                      groupMessageMap.get(group).remove(message); // deleted by direct refernce
                                  }
                              }
                                senderMap.remove(message);
                              break; // now if we reach here that means we have completed all of our operation so lets break;
                            }
                        }//deletd all messages from sendermap  and also from groupMessageMap
                        totalMessageCount = senderMap.size();
                        grpMessageCount = messageList.size();
//                        for(Message message:groupMessageMap.get(group)){
//                            if (Objects.equals(message,))
//                        }
                    }
                }
            }
        }
        catch (RuntimeException e){
            System.out.println(e.getMessage());
        }
        return userCount+grpMessageCount+totalMessageCount;
    }

    public String findMessage(Date start, Date end, int K) throws Exception{
        //This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        int counter = 0;
        try {
            for(Message message: senderMap.keySet()){
                if ((message.getTimestamp()).after(start) && (message.getTimestamp()).before(end)){
                    counter++;
                    if (K==counter){
                        return message.getContent();
                    }
                }
            }
            if (counter<K){
                throw new RuntimeException("K is greater than the number of messages");
            }
        }catch (RuntimeException e){
            System.out.println(e.getMessage());
        }

        return "";
    }
}
