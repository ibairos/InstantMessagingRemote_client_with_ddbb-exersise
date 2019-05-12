package main;

import entity.Message;
import util.Subscription_check;
import entity.Topic;
import subscriber.SubscriberImpl;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import publisher.Publisher;
import publisher.PublisherStub;
import subscriber.Subscriber;
import topicmanager.TopicManager;
import topicmanager.TopicManagerStub;
import webSocketService.WebSocketClient;

public class SwingClient {

    TopicManager topicManager;
    public Map<Topic, Subscriber> my_subscriptions;
    Publisher publisher;
    Topic publisherTopic;

    JFrame frame;
    JTextArea topic_list_TextArea;
    public JTextArea messages_TextArea;
    public JTextArea my_subscriptions_TextArea;
    JTextArea publisher_TextArea;
    JTextField argument_TextField;

    public SwingClient(TopicManager topicManager) {
        this.topicManager = topicManager;
        my_subscriptions = new HashMap<Topic, Subscriber>();
        publisher = null;
        publisherTopic = null;
    }

    public void createAndShowGUI() {
        String login = ((TopicManagerStub) topicManager).user.getLogin();
        frame = new JFrame("Publisher/Subscriber demo, user : " + login);
        frame.setSize(300, 300);
        frame.addWindowListener(new CloseWindowHandler());

        topic_list_TextArea = new JTextArea(5, 10);
        messages_TextArea = new JTextArea(10, 20);
        my_subscriptions_TextArea = new JTextArea(5, 10);
        publisher_TextArea = new JTextArea(1, 10);
        argument_TextField = new JTextField(20);

        JButton show_topics_button = new JButton("show Topics");
        JButton new_publisher_button = new JButton("new Publisher");
        JButton new_subscriber_button = new JButton("new Subscriber");
        JButton to_unsubscribe_button = new JButton("to unsubscribe");
        JButton to_post_an_event_button = new JButton("post an event");
        JButton to_close_the_app = new JButton("close app.");

        show_topics_button.addActionListener(new showTopicsHandler());
        new_publisher_button.addActionListener(new newPublisherHandler());
        new_subscriber_button.addActionListener(new newSubscriberHandler());
        to_unsubscribe_button.addActionListener(new UnsubscribeHandler());
        to_post_an_event_button.addActionListener(new postEventHandler());
        to_close_the_app.addActionListener(new CloseAppHandler());

        JPanel buttonsPannel = new JPanel(new FlowLayout());
        buttonsPannel.add(show_topics_button);
        buttonsPannel.add(new_publisher_button);
        buttonsPannel.add(new_subscriber_button);
        buttonsPannel.add(to_unsubscribe_button);
        buttonsPannel.add(to_post_an_event_button);
        buttonsPannel.add(to_close_the_app);

        JPanel argumentP = new JPanel(new FlowLayout());
        argumentP.add(new JLabel("Write content to set a new_publisher / new_subscriber / unsubscribe / post_event:"));
        argumentP.add(argument_TextField);

        JPanel topicsP = new JPanel();
        topicsP.setLayout(new BoxLayout(topicsP, BoxLayout.PAGE_AXIS));
        topicsP.add(new JLabel("Topics:"));
        topicsP.add(topic_list_TextArea);
        topicsP.add(new JScrollPane(topic_list_TextArea));
        topicsP.add(new JLabel("My Subscriptions:"));
        topicsP.add(my_subscriptions_TextArea);
        topicsP.add(new JScrollPane(my_subscriptions_TextArea));
        topicsP.add(new JLabel("I'm Publisher of topic:"));
        topicsP.add(publisher_TextArea);
        topicsP.add(new JScrollPane(publisher_TextArea));

        JPanel messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.PAGE_AXIS));
        messagesPanel.add(new JLabel("Messages:"));
        messagesPanel.add(messages_TextArea);
        messagesPanel.add(new JScrollPane(messages_TextArea));

        Container mainPanel = frame.getContentPane();
        mainPanel.add(buttonsPannel, BorderLayout.PAGE_START);
        mainPanel.add(messagesPanel, BorderLayout.CENTER);
        mainPanel.add(argumentP, BorderLayout.PAGE_END);
        mainPanel.add(topicsP, BorderLayout.LINE_START);

        //this is where you restore the user profile:
        clientSetup();

        frame.pack();
        frame.setVisible(true);
    }

    private void clientSetup() {
        System.out.println("Setting up the client...");
        publisher = topicManager.publisherOf();
        if (publisher != null) {
            publisherTopic = publisher.topic();
        }
        List<entity.Subscriber> subs = topicManager.mySubscriptions();
        for (entity.Subscriber sub : subs) {
            my_subscriptions.put(sub.getTopic(), new SubscriberImpl(this));
        }
        
        refreshTopics();
        refreshMySubscriptions();
        refreshPublisher();
        refreshMessages();
    }

   class showTopicsHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            refreshTopics();
        }
    }

    class newPublisherHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String topic_str = argument_TextField.getText();
            argument_TextField.setText("");
            boolean topicFound = false;
            for (Topic topic : topicManager.topics()) {
                if (topic.name.equals(topic_str)) {
                    publisherTopic = topic;
                    topicFound = true;
                    break;
                }
            }
            if (!topicFound) {
                publisherTopic = new Topic(topic_str);
            }
            publisher = topicManager.addPublisherToTopic(publisherTopic);
            publisher_TextArea.setText(publisherTopic.name);
        }
    }

    class newSubscriberHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String topic_str = argument_TextField.getText();
            argument_TextField.setText("");
            Topic topic = null;
            for (Topic t : topicManager.topics()) {
                if (t.name.equals(topic_str)) {
                    topic = t;
                    break;
                }
            }
            if (topic != null && !my_subscriptions.containsKey(topic)) {
                Subscriber s = new SubscriberImpl(SwingClient.this);
                Subscription_check sc = topicManager.subscribe(topic, s);
                if (sc.result == Subscription_check.Result.OKAY) {
                    my_subscriptions.put(topic, s);
                    refreshMySubscriptions();
                }
            }
        }
    }

    class UnsubscribeHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String topic_str = argument_TextField.getText();
            argument_TextField.setText("");
            Topic topic = null;
            for (Topic t : my_subscriptions.keySet()) {
                if (t.name.equals(topic_str)) {
                    topic = t;
                    break;
                }
            }
            if (topic != null) {
                Subscriber subs = my_subscriptions.get(topic);
                Subscription_check sc = topicManager.unsubscribe(topic, subs);
                if (sc.result == Subscription_check.Result.OKAY) {
                    my_subscriptions.remove(topic);
                    refreshMySubscriptions();
                }
            }
        }
    }

    class postEventHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = argument_TextField.getText();
            argument_TextField.setText("");
            if (publisher != null) {
                publisher.publish(new Message(publisherTopic, message));            
            }
        }
    }

    class CloseAppHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
          System.out.println("all users closed");
          System.exit(0);
        }
    }

    class CloseWindowHandler implements WindowListener {

        @Override
        public void windowDeactivated(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowClosed(WindowEvent e) {
            
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
            
            // Seems like we don't have to do that in this implementation
            /*
            for (Map.Entry<Topic, Subscriber> entry : my_subscriptions.entrySet()) {
                entry.getValue().onClose(new Subscription_close(entry.getKey(), Subscription_close.Cause.SUBSCRIBER));
            }
            if (publisher != null) {
                topicManager.removePublisherFromTopic(publisherTopic);
            }
            */
            // TODO Ending the execution if no more clients are launched
            /*if (The_system.numberOfClients > 1) {
                The_system.numberOfClients--;
                System.out.println("one user closed");
            } else {
                System.out.println("all users closed");
                System.exit(0);
            }*/
        }
    }
    
    private void refreshMySubscriptions() {
        my_subscriptions_TextArea.setText("");
        for (Topic p : my_subscriptions.keySet()) {
            my_subscriptions_TextArea.append(p.name + "\n");
        }
    }
    
    private void refreshTopics() {
        topic_list_TextArea.setText("");
        java.util.List<Topic> topics = topicManager.topics();
        for (Topic topic : topics) {
            topic_list_TextArea.append(topic.name + "\n");            
        }
        
    }
    
    private void refreshPublisher() {
        if (publisherTopic != null) {
            publisher_TextArea.setText(publisherTopic.name);
        }
    }
    
    private void refreshMessages() {
        messages_TextArea.setText("");
        List<Message> all_messages = new ArrayList<Message>();
        for (Topic t : my_subscriptions.keySet()) {
            List<Message> messages = topicManager.messagesFrom(t);
            // Ordering the messages
            for (Message m : messages) {
                boolean placed = false;
                int pos = 0;
                for (Message m2 : all_messages) {
                    if (m.getId() >= m2.getId()) {
                        all_messages.add(pos, m);
                        placed = true;
                        break;
                    }
                    pos++;
                }
                if (!placed) {
                    all_messages.add(m);
                }
            }
        }
        for (Message m : all_messages) {
            messages_TextArea.append(m.topic.name + ": " + m.content + "\n");
        }
    }
    
}


















