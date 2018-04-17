package raft;

import ledger.Ledger;
import ledger.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;


/**
 * This class needs to be used by the Leader to listen to incoming client messages
 *
 * @author thomasnguyen
 */
@Controller
public class ClientController {

    private Ledger ledger; // Stateful ledger to be shared by all RAFT node states

    public ClientController() {
        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        this.ledger = (Ledger) context.getBean("ledger");
    }

    /**
     * Handler for default path
     *
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public String home() {
        return "This is the RAFT Consensus Algorithm";
    }

    /**
     * This is a request handler to enable a client to update Data in the
     * key-value store
     *
     * @param key
     * @param value
     * @return
     */
    @RequestMapping(value = "/update/{key}/{value}",
            method = RequestMethod.GET)
    @ResponseBody
    public String updateKV(@PathVariable("key") String key,
                                 @PathVariable("value") String value) {
        Log update = new Log(0,0,key,value);
        this.ledger.addToQueue(update);
        System.out.println("Hit");
        System.out.println(Arrays.toString(this.ledger.getUpdates().toArray()));
        return "Update Queued for Replication";
    }

    /**
     * This is a request handler method to return commited data from the key-value
     * store from a third party client that requests the data
     *
     * @param key
     * @return
     */
    @RequestMapping(value = "/retrieve/{key}",
            method = RequestMethod.GET)
    @ResponseBody
    public String getValue(@PathVariable("key") String key) {
        try {
            String result = this.ledger.getData(key);
            return result;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}
