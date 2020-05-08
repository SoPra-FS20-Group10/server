package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.testclasses.GreetingWS;
import ch.uzh.ifi.seal.soprafs20.testclasses.HelloMessageWS;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class GreetingControllerWS {


    @MessageMapping("/game/{gameId}/chat")
    @SendTo("/topic/greetings")
    public GreetingWS greeting(HelloMessageWS message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new GreetingWS("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }


}
