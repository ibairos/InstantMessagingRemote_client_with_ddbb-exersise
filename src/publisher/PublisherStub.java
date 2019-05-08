package publisher;

import apiREST.apiREST_Message;
import entity.Message;
import entity.Topic;

public class PublisherStub implements Publisher {

  Topic topic;

  public PublisherStub(Topic topic) {
    this.topic = topic;
  }

  @Override
  public void publish(Message message) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
  public Topic topic() {
    return topic;
  }

}
