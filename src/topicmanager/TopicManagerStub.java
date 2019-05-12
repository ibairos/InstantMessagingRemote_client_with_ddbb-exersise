package topicmanager;

import apiREST.apiREST_Message;
import apiREST.apiREST_Publisher;
import apiREST.apiREST_Subscriber;
import apiREST.apiREST_Topic;
import entity.Message;
import util.Subscription_check;
import entity.Topic;
import util.Topic_check;
import entity.User;
import java.util.List;
import publisher.Publisher;
import publisher.PublisherStub;
import subscriber.Subscriber;
import util.Subscription_close;
import webSocketService.WebSocketClient;

public class TopicManagerStub implements TopicManager {

    public User user;

    public TopicManagerStub(User user) {
        WebSocketClient.newInstance();
        this.user = user;
    }

    @Override
    public void close() {
        WebSocketClient.close();
    }

    @Override
    public Publisher addPublisherToTopic(Topic topic) {
        entity.Publisher publisher = new entity.Publisher();
        publisher.setTopic(topic);
        publisher.setUser(user);
        apiREST_Publisher.createPublisher(publisher);
        return new PublisherStub(topic);
    }

    @Override
    public void removePublisherFromTopic(Topic topic) {
        apiREST_Publisher.deletePublisher(apiREST_Publisher.PublisherOf(user));
    }

    @Override
    public Topic_check isTopic(Topic topic) {
        return apiREST_Topic.isTopic(topic);
    }

    @Override
    public List<Topic> topics() {
        return apiREST_Topic.allTopics();
    }

    @Override
    public Subscription_check subscribe(Topic topic, Subscriber subscriber) {
        Subscription_check sc;
        if (isTopic(topic).isOpen) {
            WebSocketClient.addSubscriber(topic, subscriber);
            sc = new Subscription_check(topic, Subscription_check.Result.OKAY);
        } else {
            sc = new Subscription_check(topic, Subscription_check.Result.NO_TOPIC);
        }
        return sc;
    }

    @Override
    public Subscription_check unsubscribe(Topic topic, Subscriber subscriber) {
        Subscription_check sc;
        if (isTopic(topic).isOpen) {
            WebSocketClient.removeSubscriber(topic);
            subscriber.onClose(new Subscription_close(topic, Subscription_close.Cause.SUBSCRIBER));
            sc = new Subscription_check(topic, Subscription_check.Result.OKAY);
        } else {
            sc = new Subscription_check(topic, Subscription_check.Result.NO_TOPIC);
        }
        return sc;
    }

    @Override
    public Publisher publisherOf() {
        entity.Publisher publisher = apiREST_Publisher.PublisherOf(user);
        if (publisher == null) {
            return null;
        } else {
            return new PublisherStub(publisher.getTopic());
        }
    }

    @Override
    public List<entity.Subscriber> mySubscriptions() {
        return apiREST_Subscriber.mySubscriptions(user);
    }

    @Override
    public List<Message> messagesFrom(Topic topic) {
        return apiREST_Message.messagesFromTopic(topic);
    }

}

