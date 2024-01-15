package ampLRadapter;

// I NOW JUST COPY-PASTED THE PLUGINADAPTER.API PACKAGE
// BUT IN A REAL SCENARION THIS SHOULD BE BUILT BY PROTO COMPILER??

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Adapter: starts the BrokerConnection and AdapterCore.
public class Adapter {
    private static Logger logger = LoggerFactory.getLogger(Adapter.class);

    public static void runTest(String name, String url, String token) {
        try {
            URI serverUri = new URI(url);
            BrokerConnection brokerConnection =
                new BrokerConnection(serverUri, token);
            Handler handler = new LRHandler();
            AdapterCore adapterCore =
                new AdapterCore(name, brokerConnection, handler);

            brokerConnection.registerAdapterCore(adapterCore);
            handler.registerAdapterCore(adapterCore);
            adapterCore.start();
        }
        catch (URISyntaxException ex) {
            logger.error("URISyntaxException: " + ex.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        String name  = "testAdapter";
        String url   = "wss://research01.axini.com:443/adapters";
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MTgzODEzODYsInN1YiI6ImphZGUubGV1cnNAc3R1ZGVudC51dmEubmwiLCJpc3MiOiJ2bXB1YmxpY3Byb2QwMSIsInNjb3BlIjoiYWRhcHRlciJ9.uiQj3XzViaVOP5YsA61IMuhPxbEIvPOL48dph4-Rpo0";
        
        logger.info("name:  " + name);
        logger.info("url:   " + url);
        logger.info("token: " + token);

        runTest(name, url, token);
    }
}
